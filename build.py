#!/usr/bin/python
import os
import shutil
import urllib
import re
import md5
import zipfile
import sys
from sgmllib import SGMLParser

javacCmd = 'javac'
jarCmd = 'jar'
javaCmd = 'java'
javadocCmd = 'javadoc'
cvsCmd = 'cvs'

buildRoot = '.'
cvsRoot = ':pserver:html5-readonly@cvsdude.org:/cvs/stdx'
portNumber = '8888'
useAjp = 0
log4jProps = 'validator/log4j.properties'

dependencyPackages = [
  ("http://mirror.eunet.fi/apache/jakarta/commons/codec/binaries/commons-codec-1.3.zip", "c30c769e07339390862907504ff4b300"),
  ("http://mirror.eunet.fi/apache/jakarta/commons/httpclient/binary/commons-httpclient-3.0.1.zip", "a0077efae9c1e0aaa6615f23e5cd3a78"),
  ("http://mirror.eunet.fi/apache/jakarta/commons/logging/binaries/commons-logging-1.1.zip", "cc4d307492a48e27fbfeeb04d59c6578"),
  ("http://download.icu-project.org/files/icu4j/3.6.1/icu4j_3_6_1.jar", "f5ffe0784a9e4c414f42d88e7f6ecefd"),
  ("http://download.icu-project.org/files/icu4j/3.6.1/icu4j-charsets_3_6_1.jar", "0c8485bc3846fb8f243ed393f3f5b7f9"),
  ("http://belnet.dl.sourceforge.net/sourceforge/jena/Jena-2.5.2.zip", "cd9c74f58b7175e56e3512443c84fcf8"),
  ("http://dist.codehaus.org/jetty/jetty-5.1.12.zip", "a61adc832be6baf2678935506743cfc3"),
  ("http://hsivonen.iki.fi/code/xmlidfilter-0.9.zip", "689acccb60c964bce3eee3b04da45d5d"), # The official location is https and .tar.gz
  ("http://mirror.eunet.fi/apache/logging/log4j/1.2.14/logging-log4j-1.2.14.zip", "6c4f8da1fed407798ea0ad7984fe60db"),
  ("http://mirror.eunet.fi/apache/xml/xerces-j/Xerces-J-bin.2.9.0.zip", "a3aece3feb68be6d319072b85ad06023"),
  ("http://belnet.dl.sourceforge.net/sourceforge/saxon/saxon6-5-5.zip", "e913002af9c6bbb4c4361ff41baac3af"),
  ("http://ftp.mozilla.org/pub/mozilla.org/js/rhino1_6R5.zip", "c93b6d0bb8ba83c3760efeb30525728a"),
  ("http://hsivonen.iki.fi/code/onvdl-hsivonen.zip", "b5cda2ed1488c7d702339a92b1bf480f"),
]

# Unfortunately, the packages contain old versions of certain libs, so 
# can't just autodiscover all jars. Hence, an explicit list.
dependencyJars = [
  "commons-codec-1.3/commons-codec-1.3.jar",
  "commons-httpclient-3.0.1/commons-httpclient-3.0.1.jar",
  "commons-logging-1.1/commons-logging-1.1.jar",
  "commons-logging-1.1/commons-logging-adapters-1.1.jar",
  "commons-logging-1.1/commons-logging-api-1.1.jar",
  "icu4j-charsets_3_6_1.jar",
  "icu4j_3_6_1.jar",
  "Jena-2.5.2/lib/iri.jar",
  "jetty-5.1.12/lib/javax.servlet.jar",
  "jetty-5.1.12/lib/org.mortbay.jetty.jar",
  "logging-log4j-1.2.14/dist/lib/log4j-1.2.14.jar",
  "onvdl-hsivonen/bin/isorelax.jar",
  "onvdl-hsivonen/onvdl.jar",
  "rhino1_6R5/js.jar",
  "saxon.jar",
  "xerces-2_9_0/xercesImpl.jar",
  "xerces-2_9_0/xml-apis.jar",
  "xmlidfilter-0.9/lib/xmlidfilter.jar",
]

moduleNames = [
  "build",
  "syntax",
  "util",
  "htmlparser",
  "xmlparser",
  "validator",
]

directoryPat = re.compile(r'^[a-zA-Z0-9_-]+/$')
leafPat = re.compile(r'^[a-zA-Z0-9_-]+\.[a-z]+$')

class UrlExtractor(SGMLParser):
  def __init__(self, baseUrl):
    SGMLParser.__init__(self)
    self.baseUrl = baseUrl
    self.leaves = []
    self.directories = []
    
  def start_a(self, attrs):
    for name, value in attrs:
      if name == "href":
        if directoryPat.match(value):
          self.directories.append(self.baseUrl + value)
        if leafPat.match(value):
          self.leaves.append(self.baseUrl + value)    
    
def runCmd(cmd):
  print cmd
  os.system(cmd)

def execCmd(cmd, args):
  print "%s %s" % (cmd, " ".join(args))
  if os.execvp(cmd, [cmd,] + args):
    print "Command failed."
    sys.exit(2)

def removeIfExists(filePath):
  if os.path.exists(filePath):
    os.unlink(filePath)

def ensureDirExists(dirPath):
  if not os.path.exists(dirPath):
    os.makedirs(dirPath)

def findFilesWithExtension(directory, extension):
  rv = []
  ext = '.' + extension 
  for root, dirs, files in os.walk(directory):
    for file in files:
      if file.endswith(ext):
        rv.append(os.path.join(root, file))
  return rv

def jarNamesToPaths(names):
  return map(lambda x: os.path.join(buildRoot, "jars", x + ".jar"), names)

def runJavac(sourceDir, classDir, classPath):
  sourceFiles = findFilesWithExtension(sourceDir, "java")
  runCmd("%s -classpath '%s' -sourcepath '%s' -d '%s' %s"\
		% (javacCmd, classPath, sourceDir, classDir, " ".join(sourceFiles)))

def runJar(classDir, jarFile, sourceDir):
  classFiles = findFilesWithExtension(classDir, "class")
  metaDir = os.path.join(sourceDir, "META-INF")
  classList = map(lambda x: 
                    "-C '" + classDir + "' '" + x[len(classDir)+1:] + "'", 
                  classFiles)
  if os.path.exists(metaDir):
    # XXX get rid of CVS directories here
    runCmd("%s cf '%s' -C '%s' META-INF %s" 
      % (jarCmd, jarFile, sourceDir, " ".join(classList)))
  else:  
    runCmd("%s cf '%s' %s" 
      % (jarCmd, jarFile, " ".join(classList)))

def buildModule(rootDir, jarName, classPath):
  sourceDir = os.path.join(rootDir, "src")
  classDir = os.path.join(rootDir, "classes")
  distDir = os.path.join(rootDir, "dist")
  jarFile = os.path.join(distDir, jarName + ".jar")
  removeIfExists(jarFile)
  ensureDirExists(classDir)
  ensureDirExists(distDir)
  runJavac(sourceDir, classDir, classPath)
  runJar(classDir, jarFile, sourceDir)
  ensureDirExists(os.path.join(buildRoot, "jars"))
  shutil.copyfile(jarFile, os.path.join(buildRoot, "jars", jarName + ".jar"))

def dependencyJarPaths():
  dependencyDir = os.path.join(buildRoot, "dependencies")
  extrasDir = os.path.join(buildRoot, "extras")
  # XXX may need work for Windows portability
  pathList = map(lambda x: os.path.join(dependencyDir, x), dependencyJars)
  ensureDirExists(extrasDir)
  pathList += findFilesWithExtension(extrasDir, "jar")
  return pathList

def buildUtil():
  classPath = os.pathsep.join(dependencyJarPaths())
  buildModule(
    os.path.join(buildRoot, "util"), 
    "io-xml-util", 
    classPath)

def buildDatatypeLibrary():
  classPath = os.pathsep.join(dependencyJarPaths())
  buildModule(
    os.path.join(buildRoot, "syntax", "relaxng", "datatype", "java"), 
    "html5-datatypes", 
    classPath)

def buildNonSchema():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["html5-datatypes"]))
  buildModule(
    os.path.join(buildRoot, "syntax", "non-schema", "java"), 
    "non-schema", 
    classPath)

def buildXmlParser():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", "io-xml-util"]))
  buildModule(
    os.path.join(buildRoot, "xmlparser"), 
    "hs-aelfred2", 
    classPath)

def buildHtmlParser():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", "io-xml-util"]))
  buildModule(
    os.path.join(buildRoot, "htmlparser"), 
    "htmlparser", 
    classPath)

def buildValidator():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2"]))
  buildModule(
    os.path.join(buildRoot, "validator"), 
    "validator", 
    classPath)

def buildTestHarness():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2"]))
  buildModule(
    os.path.join(buildRoot, "syntax", "relaxng", "tests", "jdriver"), 
    "test-harness", 
    classPath)

def runValidator():
  ensureDirExists(os.path.join(buildRoot, "logs"))
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2",
                                                "html5-datatypes",
                                                "validator"]))
  args = [
    '-cp',
    classPath,
    '-Dfi.iki.hsivonen.verifierservlet.log4j-properties=' + log4jProps,
    '-Dfi.iki.hsivonen.verifierservlet.presetconfpath=validator/presets.txt',
    '-Dfi.iki.hsivonen.verifierservlet.cachepathprefix=local-entities/',
    '-Dfi.iki.hsivonen.verifierservlet.cacheconfpath=validator/entity-map.txt',
    '-Dfi.iki.hsivonen.verifierservlet.version="VerifierServlet-RELAX-NG-Validator/2.0b10 (http://hsivonen.iki.fi/validator/)"',
    'fi.iki.hsivonen.verifierservlet.Main',
  ]
  if useAjp:
    args.append('ajp')
  args.append(portNumber)
  execCmd(javaCmd, args)

def fetchUrlTo(url, path, md5sum=None):
  # I bet there's a way to do this with more efficient IO and less memory
  print url
  f = urllib.urlopen(url)
  data = f.read()
  f.close()
  if md5sum:
    m = md5.new(data)
    if md5sum != m.hexdigest():
      print "Bad MD5 hash for %s." % url
      sys.exit(1)
  head, tail = os.path.split(path)
  if not os.path.exists(head):
    os.makedirs(head)
  f = open(path, "wb")
  f.write(data)
  f.close()

def spiderApacheDirectories(baseUrl, baseDir):
  f = urllib.urlopen(baseUrl)
  parser = UrlExtractor(baseUrl)
  parser.feed(f.read())
  f.close()
  parser.close()
  for leaf in parser.leaves:
    fetchUrlTo(leaf, os.path.join(baseDir, leaf[7:]))
  for directory in parser.directories:
    spiderApacheDirectories(directory, baseDir)

def downloadLocalEntities():
  ensureDirExists(os.path.join(buildRoot, "local-entities"))
  if not os.path.exists(os.path.join(buildRoot, "local-entities", "syntax")):
    # XXX not portable to Windows
    os.symlink(os.path.join("..", "syntax"), 
               os.path.join(buildRoot, "local-entities", "syntax"))
  f = open(os.path.join(buildRoot, "validator", "entity-map.txt"))
  try:
    for line in f:
      url, path = line.strip().split("\t")
      if not path.startswith("syntax/"):
        # XXX may not work on Windows
        if not os.path.exists(os.path.join(buildRoot, "local-entities", path)):
          fetchUrlTo(url, os.path.join(buildRoot, "local-entities", path))
  finally:
    f.close()

def downloadOperaSuite():
  operaSuiteDir = os.path.join(buildRoot, "opera-tests")
  validDir = os.path.join(operaSuiteDir, "valid")
  invalidDir = os.path.join(operaSuiteDir, "invalid")
  if not os.path.exists(operaSuiteDir):
    os.makedirs(operaSuiteDir)
    os.makedirs(validDir)
    os.makedirs(invalidDir)
    spiderApacheDirectories("http://tc.labs.opera.com/html/", validDir)

def zipExtract(zipArch, targetDir):
  z = zipfile.ZipFile(zipArch)
  for name in z.namelist():
    file = os.path.join(targetDir, name)
    # is this portable to Windows?
    if not name.endswith('/'):
      head, tail = os.path.split(file)
      ensureDirExists(head)
      o = open(file, 'wb')
      o.write(z.read(name))
      o.flush()
      o.close()

def downloadDependency(url, md5sum):
  dependencyDir = os.path.join(buildRoot, "dependencies")
  ensureDirExists(dependencyDir)
  path = os.path.join(dependencyDir, url[url.rfind("/") + 1:])
  if not os.path.exists(path):
    fetchUrlTo(url, path, md5sum)
    if path.endswith(".zip"):
      zipExtract(path, dependencyDir)

def downloadDependencies():
  for url, md5sum in dependencyPackages:
    downloadDependency(url, md5sum)

def buildAll():
  buildUtil()
  buildDatatypeLibrary()
  buildNonSchema()
  buildXmlParser()
  buildHtmlParser()
  buildTestHarness()
  buildValidator()

def checkout():
  # XXX root dir
  for mod in moduleNames:
    if os.path.exists(mod):
      runCmd("%s -d%s up %s" % (cvsCmd, cvsRoot, mod))
    else:
      runCmd("%s -d%s co %s" % (cvsCmd, cvsRoot, mod))

def runTests():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2",
                                                "html5-datatypes",
                                                "test-harness"]))
  runCmd("%s -cp %s org.whattf.syntax.Driver" % (javaCmd, classPath))

def printHelp():
  print "Usage: python build/build.py [options] [tasks]"
  print ""
  print "Options:"
  print "  --cvs=/usr/bin/cvs         -- Sets the path to the cvs binary"
  print "  --java=/usr/bin/java       -- Sets the path to the java binary"
  print "  --jar=/usr/bin/jar         -- Sets the path to the jar binary"
  print "  --javac=/usr/bin/javac     -- Sets the path to the javac binary"
  print "  --javadoc=/usr/bin/javadoc -- Sets the path to the javadoc binary"
  print "  --jdk-bin=/j2se/bin        -- Sets the paths for all JDK tools"
  print "  --log4j=log4j.properties   -- Sets the path to log4 configuration"
  print "  --port=8888                -- Sets the server port number"
  print "  --ajp=on                   -- Use AJP13 instead of HTTP"
  print ""
  print "Tasks:"
  print "  checkout -- Checks out the source from CVS"
  print "  dldeps   -- Downloads missing dependency libraries and entities"
  print "  dltests  -- Downloads the external test suite if missing"
  print "  build    -- Build the source"
  print "  test     -- Run tests"
  print "  run      -- Run the system"
  print "  all      -- checkout dldeps dltests build test run"

if __name__ == "__main__":
  argv = sys.argv[1:]
  if len(argv) == 0:
    printHelp()
  else:
    for arg in argv:
      if arg.startswith("--cvs="):
        cvsCmd = arg[6:]
      elif arg.startswith("--java="):
        javaCmd = arg[7:]
      elif arg.startswith("--jar="):
        jarCmd = arg[6:]
      elif arg.startswith("--javac="):
        javacCmd = arg[8:]
      elif arg.startswith("--javadoc="):
        javadocCmd = arg[10:]
      elif arg.startswith("--jdk-bin="):
        jdkBinDir = arg[10:]
        javaCmd = os.path.join(jdkBinDir, "java")
        jarCmd = os.path.join(jdkBinDir, "jar")
        javacCmd = os.path.join(jdkBinDir, "javac")
        javadocCmd = os.path.join(jdkBinDir, "javadoc")
      elif arg.startswith("--cvsroot="):
        cvsRoot = arg[10:]
      elif arg.startswith("--port="):
        portNumber = arg[7:]
      elif arg.startswith("--log4j="):
        log4jProps = arg[8:]
      elif arg == '--ajp=on':
        useAjp = 1
      elif arg == '--ajp=off':
        useAjp = 0
      elif arg == '--help':
        printHelp()
      elif arg == 'dldeps':
        downloadDependencies()
        downloadLocalEntities()
      elif arg == 'dltests':
        downloadOperaSuite()
      elif arg == 'checkout':
        checkout()
      elif arg == 'build':
        buildAll()
      elif arg == 'test':
        runTests()
      elif arg == 'run':
        runValidator()
      elif arg == 'all':
        checkout()
        downloadDependencies()
        downloadLocalEntities()
        downloadOperaSuite()
        buildAll()
        runTests()
        runValidator()
      else:
        print "Unknown option %s." % arg
