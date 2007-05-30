#!/usr/bin/python
import os
import shutil
import urllib
import re
from sgmllib import SGMLParser

javacCmd = 'javac'
jarCmd = 'jar'
javaCmd = 'java'
javadocCmd = 'javadoc'

buildRoot = '.'

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
  return findFilesWithExtension("dependencies", "jar")

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

def runValidator():
  ensureDirExists(os.path.join(buildRoot, "logs"))
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2",
                                                "html5-datatypes",
                                                "validator"]))
  runCmd(javaCmd
    + ' -cp ' + classPath
    + ' -Dfi.iki.hsivonen.verifierservlet.log4j-properties=validator/log4j.properties'
    + ' -Dfi.iki.hsivonen.verifierservlet.presetconfpath=validator/presets.txt'
    + ' -Dfi.iki.hsivonen.verifierservlet.cachepathprefix=local-entities/'
    + ' -Dfi.iki.hsivonen.verifierservlet.cacheconfpath=validator/entity-map.txt'
    + ' -Dfi.iki.hsivonen.verifierservlet.version="VerifierServlet-RELAX-NG-Validator/2.0b10 (http://hsivonen.iki.fi/validator/)"'
    + ' fi.iki.hsivonen.verifierservlet.Main')

def fetchUrlTo(url, path):
  # I bet there's a way to do this with more efficient IO and less memory
  # print url
  f = urllib.urlopen(url)
  data = f.read()
  f.close()
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
 
#downloadOperaSuite()
#downloadLocalEntities()
#buildUtil()
#buildDatatypeLibrary()
#buildNonSchema()
#buildXmlParser()
#buildHtmlParser()
#buildValidator()

#runValidator()