#!/usr/bin/python

# Copyright (c) 2007 Henri Sivonen
# Copyright (c) 2008-2009 Mozilla Foundation
#
# Permission is hereby granted, free of charge, to any person obtaining a 
# copy of this software and associated documentation files (the "Software"), 
# to deal in the Software without restriction, including without limitation 
# the rights to use, copy, modify, merge, publish, distribute, sublicense, 
# and/or sell copies of the Software, and to permit persons to whom the 
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in 
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
# DEALINGS IN THE SOFTWARE.

import os
import shutil
import urllib
import re
try:
  from hashlib import md5
except ImportError:
  from md5 import new as md5
import zipfile
import sys
from sgmllib import SGMLParser

javacCmd = 'javac'
jarCmd = 'jar'
javaCmd = 'java'
javadocCmd = 'javadoc'
svnCmd = 'svn'
tarCmd = 'tar'
scpCmd = 'scp'

buildRoot = '.'
svnRoot = 'http://svn.versiondude.net/whattf/'
portNumber = '8888'
controlPort = None
useAjp = 0
log4jProps = 'validator/log4j.properties'
heapSize = '64'
html5specLink = 'http://www.whatwg.org/specs/web-apps/current-work/'
html5specLoad = 'file:validator/spec/html5.html'
ianaLang = 'http://www.iana.org/assignments/language-subtag-registry'
aboutPage = 'http://about.validator.nu/'
microsyntax = 'http://wiki.whatwg.org/wiki/MicrosyntaxDescriptions'
altAdvice = 'http://wiki.whatwg.org/wiki/Validator.nu_alt_advice'
icon = None
stylesheet = None
script = None
serviceName = 'Validator.nu'
maxFileSize = 4096
usePromiscuousSsl = 0
genericHost = ''
html5Host = ''
parsetreeHost = ''
genericPath = '/'
html5Path = '/html5/'
parsetreePath = '/parsetree/'
deploymentTarget = None
noSelfUpdate = 0
useLocalCopies = 0

downloadedDeps = 0

dependencyPackages = [
  ("http://archive.apache.org/dist/commons/codec/binaries/commons-codec-1.3.zip", "c30c769e07339390862907504ff4b300"),
  ("http://archive.apache.org/dist/httpcomponents/commons-httpclient/binary/commons-httpclient-3.1.zip", "1752a2dc65e2fb03d4e762a8e7a1db49"),
  ("http://archive.apache.org/dist/commons/logging/binaries/commons-logging-1.1.1-bin.zip", "f88520ed791673aed6cc4591bc058b55"),
  ("http://download.icu-project.org/files/icu4j/4.0/icu4j-4_0.jar", "08397653119558593204474fd5a9a7e3"),
  ("http://download.icu-project.org/files/icu4j/4.0/icu4j-charsets-4_0.jar", "5dd1d6aaffa6762e09541b3bb412d8ee"),
  ("http://switch.dl.sourceforge.net/sourceforge/jena/iri-0.5.zip", "87b0069e689c22ba2a2b50f4d200caca"),
  ("http://dist.codehaus.org/jetty/jetty-6.1.11/jetty-6.1.11.zip", "64d88fdca5de560e84139c0b53218933"),
  ("http://archive.apache.org/dist/logging/log4j/1.2.15/apache-log4j-1.2.15.zip", "5b0d27be24d6ac384215b6e269d3e352"),
  ("http://archive.apache.org/dist/xerces/j/Xerces-J-bin.2.9.1.zip", "a0e07ede1c3bd5231fe15eae24032b2e"),
  ("http://ftp.mozilla.org/pub/mozilla.org/js/rhino1_7R1.zip", "613eed8201d37be201805e5048ebb0c3"),
  ("http://download.berlios.de/jsontools/jsontools-core-1.5.jar", "1f242910350f28d1ac4014928075becd"),
  ("http://hsivonen.iki.fi/code/antlr.jar", "9d2e9848c52204275c72d9d6e79f307c"),
  ("http://www.cafeconleche.org/XOM/xom-1.1.jar", "6b5e76db86d7ae32a451ffdb6fce0764"),
  ("http://www.slf4j.org/dist/slf4j-1.5.2.zip", "00ff08232a9959af3c7101b88ec456a7"),
  ("http://archive.apache.org/dist/commons/fileupload/binaries/commons-fileupload-1.2.1-bin.zip", "975100c3f74604c0c22f68629874f868"),
  ("http://archive.apache.org/dist/ant/binaries/apache-ant-1.7.0-bin.zip" , "ac30ce5b07b0018d65203fbc680968f5"),
  ("http://surfnet.dl.sourceforge.net/sourceforge/iso-relax/isorelax.20041111.zip" , "10381903828d30e36252910679fcbab6"),
  ("http://ovh.dl.sourceforge.net/sourceforge/junit/junit-4.4.jar", "f852bbb2bbe0471cef8e5b833cb36078"),
  ("http://kent.dl.sourceforge.net/sourceforge/jchardet/chardet.zip", "4091d24451ee9a840933bce34b9e3a55"),
  ("http://kent.dl.sourceforge.net/sourceforge/saxon/saxonb9-1-0-2j.zip", "9e649eec59103593fb75befaa28e1f3d"),
]

# Unfortunately, the packages contain old versions of certain libs, so 
# can't just autodiscover all jars. Hence, an explicit list.

runDependencyJars = [
  "commons-codec-1.3/commons-codec-1.3.jar",
  "commons-httpclient-3.1/commons-httpclient-3.1.jar",
  "commons-logging-1.1.1/commons-logging-1.1.1.jar",
  "commons-logging-1.1.1/commons-logging-adapters-1.1.1.jar",
  "commons-logging-1.1.1/commons-logging-api-1.1.1.jar",
  "icu4j-charsets-4_0.jar",
  "icu4j-4_0.jar",
  "iri-0.5/lib/iri.jar",
  "jetty-6.1.11/lib/servlet-api-2.5-6.1.11.jar",
  "jetty-6.1.11/lib/jetty-6.1.11.jar",
  "jetty-6.1.11/lib/jetty-util-6.1.11.jar",
  "jetty-6.1.11/lib/ext/jetty-ajp-6.1.11.jar",
  "apache-log4j-1.2.15/log4j-1.2.15.jar",
  "rhino1_7R1/js.jar",
  "xerces-2_9_1/xercesImpl.jar",
  "xerces-2_9_1/xml-apis.jar",
  "slf4j-1.5.2/slf4j-log4j12-1.5.2.jar",
  "commons-fileupload-1.2.1/lib/commons-fileupload-1.2.1.jar",
  "isorelax.jar",
  "mozilla/intl/chardet/java/dist/lib/chardet.jar",
  "saxon9.jar",
]

buildOnlyDependencyJars = [
  "jsontools-core-1.5.jar",
  "antlr.jar",
  "xom-1.1.jar",
  "junit-4.4.jar",
  "apache-ant-1.7.0/lib/ant.jar",
  "apache-ant-1.7.0/lib/ant-launcher.jar",
]

dependencyJars = runDependencyJars + buildOnlyDependencyJars

moduleNames = [
  "syntax",
  "util",
  "htmlparser",
  "xmlparser",
  "validator",
]

javaSafeNamePat = re.compile(r'[^a-zA-Z0-9]')
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
  if os.name == 'nt':
    cmd = cmd.replace('"', '')
    print cmd
    os.system(cmd)    
  else:
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

def removeIfDirExists(dirPath):
  if os.path.exists(dirPath):
    shutil.rmtree(dirPath)

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

def findFiles(directory):
  rv = []
  for root, dirs, files in os.walk(directory):
    for file in files:
      candidate = os.path.join(root, file)
      if candidate.find("/.svn") == -1:
        rv.append(candidate)
  return rv

def jarNamesToPaths(names):
  return map(lambda x: os.path.join(buildRoot, "jars", x + ".jar"), names)

def jingJarPath():
  return [os.path.join("jing-trang", "build", "jing.jar"),]

def runJavac(sourceDir, classDir, classPath):
  ensureDirExists(classDir)
  sourceFiles = findFilesWithExtension(sourceDir, "java")
  f = open("temp-javac-list", "w")
  if os.name == 'nt':
    f.write("\r\n".join(sourceFiles))
  else:
    f.write("\n".join(sourceFiles))
  f.close()
  runCmd('"%s" -nowarn -classpath "%s" -sourcepath "%s" -d "%s" %s'\
		% (javacCmd, classPath, sourceDir, classDir, "@temp-javac-list"))
  removeIfExists("temp-javac-list")

def copyFiles(sourceDir, classDir):
  files = findFiles(sourceDir)
  for f in files:
    destFile = os.path.join(classDir, f[len(sourceDir)+1:])
    head, tail = os.path.split(destFile)
    if not os.path.exists(head):
      os.makedirs(head)
    shutil.copyfile(f, destFile)

def runJar(classDir, jarFile, sourceDir):
  classFiles = findFiles(classDir)
  classList = map(lambda x: 
                    "-C " + classDir + " " + x[len(classDir)+1:] + "", 
                  classFiles)
  f = open("temp-jar-list", "w")
  if os.name == 'nt':
    f.write("\r\n".join(classList))
  else:
    f.write("\n".join(classList))
  f.close()
  runCmd('"%s" cf "%s" %s' 
    % (jarCmd, jarFile, "@temp-jar-list"))
  removeIfExists("temp-jar-list")

def buildModule(rootDir, jarName, classPath):
  sourceDir = os.path.join(rootDir, "src")
  classDir = os.path.join(rootDir, "classes")
  distDir = os.path.join(rootDir, "dist")
  jarFile = os.path.join(distDir, jarName + ".jar")
  removeIfExists(jarFile)
  removeIfDirExists(classDir)
  ensureDirExists(classDir)
  ensureDirExists(distDir)
  runJavac(sourceDir, classDir, classPath)
  copyFiles(sourceDir, classDir)
  runJar(classDir, jarFile, sourceDir)
  ensureDirExists(os.path.join(buildRoot, "jars"))
  shutil.copyfile(jarFile, os.path.join(buildRoot, "jars", jarName + ".jar"))

def dependencyJarPaths(depList=dependencyJars):
  dependencyDir = os.path.join(buildRoot, "dependencies")
  extrasDir = os.path.join(buildRoot, "extras")
  # XXX may need work for Windows portability
  pathList = map(lambda x: os.path.join(dependencyDir, x), depList)
  ensureDirExists(extrasDir)
  pathList += findFilesWithExtension(extrasDir, "jar")
  return pathList

def buildUtil():
  classPath = os.pathsep.join(dependencyJarPaths()
                              + jarNamesToPaths(["html5-datatypes", "htmlparser"])
                              + jingJarPath())
  buildModule(
    os.path.join(buildRoot, "util"), 
    "io-xml-util", 
    classPath)

def buildDatatypeLibrary():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jingJarPath())
  buildModule(
    os.path.join(buildRoot, "syntax", "relaxng", "datatype", "java"), 
    "html5-datatypes", 
    classPath)

def buildNonSchema():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["html5-datatypes",])
                              + jingJarPath())
  buildModule(
    os.path.join(buildRoot, "syntax", "non-schema", "java"), 
    "non-schema", 
    classPath)

def buildSchemaDrivers():
  schemaDir = os.path.join(buildRoot, "syntax", "relaxng")
  legacyRnc = os.path.join(os.path.join(buildRoot, "validator", "schema", "legacy", "legacy.rnc"))
  buildSchemaDriverHtmlCore(schemaDir)
  buildSchemaDriverHtml5(schemaDir)
  buildSchemaDriverHtml5Aria(schemaDir)
  buildSchemaDriverHtml5AriaRdfa(schemaDir)
  buildSchemaDriverXhtmlCore(schemaDir)
  buildSchemaDriverXhtmlCorePlusWf2(schemaDir)
  buildSchemaDriverXhtml5html(schemaDir)
  buildSchemaDriverXhtml5xhtml(schemaDir)
  buildSchemaDriverXhtml5Aria(schemaDir)
  buildSchemaDriverXhtml5AriaRdfa(schemaDir)
  removeIfExists(os.path.join(schemaDir, "legacy.rnc"))
  shutil.copy(legacyRnc, schemaDir)

#################################################################
# start of data and functions for building schema drivers
#################################################################

schemaDriverBase = '''\
start = html.elem
include "meta.rnc"
include "phrase.rnc"
include "block.rnc"
include "sectional.rnc"
include "revision.rnc"
include "embed.rnc"
include "core-scripting.rnc"
'''
schemaDriverHtml5 = '''\
include "structural.rnc"
include "ruby.rnc"
include "media.rnc"
include "tables.rnc"
include "form-datatypes.rnc"
include "web-forms.rnc"
include "web-forms2.rnc"
include "applications.rnc"
include "data.rnc"
include "legacy.rnc"
'''
schemaDriverPlusWebForms2 = '''\
include "tables.rnc"
include "form-datatypes.rnc"
include "web-forms.rnc"
include "web-forms2.rnc"
'''
schemaDriverNamespace = '''\
default namespace = "http://www.w3.org/1999/xhtml"
'''
schemaDriverToggle_HtmlCore = '''\
include "common.rnc" {
		XMLonly = notAllowed
		HTMLonly = empty
		v5only = notAllowed
}
'''
schemaDriverToggle_XhtmlCore = '''\
include "common.rnc" {
		XMLonly = empty
		HTMLonly = notAllowed
		v5only = notAllowed
}
'''
schemaDriverToggle_Html5 = '''\
include "common.rnc" {
		XMLonly = notAllowed
		HTMLonly = empty
		v5only = empty
		nonHTMLizable = notAllowed
		nonRoundtrippable = notAllowed
}
'''
schemaDriverToggle_Xhtml5xhtml = '''\
include "common.rnc" {
		XMLonly = empty
		HTMLonly = notAllowed
		v5only = empty
}
'''
schemaDriverToggle_Xhtml5html = '''\
include "common.rnc" {
		XMLonly = empty
		HTMLonly = notAllowed
		v5only = empty
		nonHTMLizable = notAllowed
		nonRoundtrippable = notAllowed
}
'''
schemaDriverHtml5Aria = '''\
include "aria.rnc"
'''
schemaDriverHtml5Rdfa = '''\
include "rdfa.rnc"
'''

def openDriver(schemaDir, driverName, sourceName=""):
  removeIfExists(os.path.join(schemaDir, driverName))
  if sourceName != "":
    # if we have a file sourceName, copy it so that we can later
    # just append additions to the copy
    shutil.copyfile(os.path.join(schemaDir, sourceName), os.path.join(schemaDir, driverName))
  f = open(os.path.join(schemaDir, driverName),"a")
  return f

################################
# HTML schema drivers
################################
def buildSchemaDriverHtmlCore(schemaDir):
  f = openDriver(schemaDir, "html5core.rnc")
  f.write(schemaDriverToggle_HtmlCore)
  f.write(schemaDriverBase)
  f.close()

def buildSchemaDriverHtml5(schemaDir):
  f = openDriver(schemaDir, "html5full.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_Html5)
  f.write(schemaDriverBase)
  f.write(schemaDriverHtml5)
  f.close()

def buildSchemaDriverHtml5Aria(schemaDir):
  f = openDriver(schemaDir, "html5full-aria.rnc", "html5full.rnc")
  f.write(schemaDriverHtml5Aria)
  f.close()

def buildSchemaDriverHtml5AriaRdfa(schemaDir):
  f = openDriver(schemaDir, "html5full-aria-rdfa.rnc", "html5full-aria.rnc")
  f.write(schemaDriverHtml5Rdfa)
  f.close()

################################
# XHTML schema drivers
################################
def buildSchemaDriverXhtmlCore(schemaDir):
  f = openDriver(schemaDir, "xhtml5core.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_XhtmlCore)
  f.write(schemaDriverBase)
  f.close()

def buildSchemaDriverXhtmlCorePlusWf2(schemaDir):
  f = openDriver(schemaDir, "xhtml5core-plus-web-forms2.rnc", "xhtml5core.rnc")
  f.write(schemaDriverPlusWebForms2)
  f.close()

def buildSchemaDriverXhtml5html(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-html.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_Xhtml5html)
  f.write(schemaDriverBase)
  f.write(schemaDriverHtml5)
  f.close()

def buildSchemaDriverXhtml5xhtml(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-xhtml.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_Xhtml5xhtml)
  f.write(schemaDriverBase)
  f.write(schemaDriverHtml5)
  f.close()

def buildSchemaDriverXhtml5Aria(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-xhtml-aria.rnc", "xhtml5full-xhtml.rnc")
  f.write(schemaDriverHtml5Aria)
  f.close()

def buildSchemaDriverXhtml5AriaRdfa(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-xhtml-aria-rdfa.rnc", "xhtml5full-xhtml-aria.rnc")
  f.write(schemaDriverHtml5Rdfa)
  f.close()

#################################################################
# end of data and functions for building schema drivers
#################################################################

def buildXmlParser():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["htmlparser", "io-xml-util"]))
  buildModule(
    os.path.join(buildRoot, "xmlparser"), 
    "hs-aelfred2", 
    classPath)

def buildHtmlParser():
  classPath = os.pathsep.join(dependencyJarPaths())
  buildModule(
    os.path.join(buildRoot, "htmlparser"), 
    "htmlparser", 
    classPath)

def buildJing():
  os.chdir("jing-trang")
  runCmd(os.path.join(".", "ant"))
  os.chdir("..")

def buildValidator():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2",
                                                "html5-datatypes"])
                              + jingJarPath())
  buildModule(
    os.path.join(buildRoot, "validator"), 
    "validator", 
    classPath)

def buildTestHarness():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2"])
                              + jingJarPath())
  buildModule(
    os.path.join(buildRoot, "syntax", "relaxng", "tests", "jdriver"), 
    "test-harness", 
    classPath)

def ownJarList():
  return jarNamesToPaths(["non-schema", 
                          "io-xml-util",
                          "htmlparser",
                          "hs-aelfred2",
                          "html5-datatypes",
                          "validator"]) + jingJarPath()

def buildRunJarPathList():
  return dependencyJarPaths(runDependencyJars)  + ownJarList()

def getRunArgs(heap="$((HEAP))"):
  classPath = os.pathsep.join(buildRunJarPathList())
  args = [
    '-Xms%sk' % heap,
    '-Xmx%sk' % heap,
    '-XX:ThreadStackSize=2048',
    '-cp',
    classPath,
    '-Dnu.validator.servlet.log4j-properties=' + log4jProps,
    '-Dnu.validator.servlet.version=3',
    '-Dnu.validator.servlet.service-name=' + serviceName,
    '-Dorg.whattf.datatype.lang-registry=' + ianaLang,
    '-Dnu.validator.servlet.about-page=' + aboutPage,
    '-Dnu.validator.servlet.style-sheet=' + stylesheet,
    '-Dnu.validator.servlet.icon=' + icon,
    '-Dnu.validator.servlet.script=' + script,
    '-Dnu.validator.spec.microsyntax-descriptions=' + microsyntax,
    '-Dnu.validator.spec.alt-advice=' + altAdvice,
    '-Dnu.validator.spec.html5-load=' + html5specLoad,
    '-Dnu.validator.spec.html5-link=' + html5specLink,
    '-Dnu.validator.servlet.max-file-size=%d' % (maxFileSize * 1024),
    '-Dorg.mortbay.http.HttpRequest.maxFormContentSize=%d' % (maxFileSize * 1024),
    '-Dnu.validator.servlet.host.generic=' + genericHost,
    '-Dnu.validator.servlet.host.html5=' + html5Host,
    '-Dnu.validator.servlet.host.parsetree=' + parsetreeHost,
    '-Dnu.validator.servlet.path.generic=' + genericPath,
    '-Dnu.validator.servlet.path.html5=' + html5Path,
    '-Dnu.validator.servlet.path.parsetree=' + parsetreePath,
  ]

  if usePromiscuousSsl:
    args.append('-Dnu.validator.xml.promiscuous-ssl=true')  

  args.append('nu.validator.servlet.Main')
  
  if useAjp:
    args.append('ajp')
  args.append(portNumber)
  if controlPort:
    args.append(controlPort)
  return args
  
def generateRunScript():
  args = getRunArgs()
  f = open(os.path.join(buildRoot, "run-validator.sh"), 'wb')
  f.write("#!/bin/sh\n")
  if heapSize.startswith('-'):
    f.write("HEAP=`grep MemTotal /proc/meminfo | awk '{print $2}'`\n")
    f.write("HEAP=$((HEAP-%d))\n" % (int(heapSize[1:]) * 1024))
  else:
    f.write("HEAP=%d\n" % (int(heapSize) * 1024))
  f.write(" ".join([javaCmd,] + args))
  if controlPort:
    f.write(" <&- 1>/dev/null 2>&1 &")
  f.write("\n")
  f.close()  

def runValidator():
  ensureDirExists(os.path.join(buildRoot, "logs"))
  args = getRunArgs(str(int(heapSize) * 1024))
  execCmd(javaCmd, args)

def createTarball():
  args = [
    "zcf",
    os.path.join(buildRoot, "jars.tar.gz"),
    os.path.join(buildRoot, "run-validator.sh"),
  ] + ownJarList()
  runCmd('"%s" %s' %(tarCmd, " ".join(args)))

def createDepTarball():
  args = [
    "zcf",
    os.path.join(buildRoot, "deps.tar.gz"),
  ] + dependencyJarPaths(runDependencyJars)
  runCmd('"%s" %s' %(tarCmd, " ".join(args)))

def deployOverScp():
  if not deploymentTarget:
    print "No target"
    return
  if downloadedDeps:
    runCmd('"%s" "%s" %s/deps.tar.gz' % (scpCmd, os.path.join(buildRoot, "deps.tar.gz"), deploymentTarget))  
  runCmd('"%s" "%s" %s/jars.tar.gz' % (scpCmd, os.path.join(buildRoot, "jars.tar.gz"), deploymentTarget))
  emptyPath = os.path.join(buildRoot, "EMPTY")
  f = open(emptyPath, 'wb')
  f.close()
  runCmd('"%s" "%s" %s/DEPLOY' % (scpCmd, emptyPath, deploymentTarget))  
  os.remove(emptyPath)

def fetchUrlTo(url, path, md5sum=None):
  # I bet there's a way to do this with more efficient IO and less memory
  print url
  f = urllib.urlopen(url)
  data = f.read()
  f.close()
  if md5sum:
    m = md5(data)
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
  f = open(os.path.join(buildRoot, "validator", "entity-map.txt"))
  try:
    for line in f:
      url, path = line.strip().split("\t")
      if not path.startswith("schema/"):
        if not os.path.exists(os.path.join(buildRoot, "local-entities", path)):
          fetchUrlTo(url, os.path.join(buildRoot, "local-entities", path))
  finally:
    f.close()

def localPathToJarCompatName(path):
  return javaSafeNamePat.sub('_', path)
  
def prepareLocalEntityJar():
  filesDir = os.path.join(buildRoot, "validator", "src", "nu", "validator", "localentities", "files")
  if os.path.exists(filesDir):
    shutil.rmtree(filesDir)
  os.makedirs(filesDir)
  shutil.copyfile(os.path.join(buildRoot, "validator", "presets.txt"), os.path.join(filesDir, "presets"))
  shutil.copyfile(os.path.join(buildRoot, "validator", "spec", "html5.html"), os.path.join(filesDir, "html5spec"))
  f = open(os.path.join(buildRoot, "validator", "entity-map.txt"))
  o = open(os.path.join(filesDir, "entitymap"), 'wb')
  try:
    for line in f:
      url, path = line.strip().split("\t")
      entPath = None
      if path.startswith("schema/html5/"):
        entPath = os.path.join(buildRoot, "syntax", "relaxng", path[13:])
      elif path.startswith("schema/"):
        entPath = os.path.join(buildRoot, "validator", path)
      else:
        entPath = os.path.join(buildRoot, "local-entities", path)
      safeName = localPathToJarCompatName(path)
      safePath = os.path.join(filesDir, safeName)
      if os.path.exists(entPath):
        o.write("%s\t%s\n" % (url, safeName))
        shutil.copyfile(entPath, safePath)
        
  finally:
    f.close()
    o.close()

def downloadOperaSuite():
  return
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
  downloadedDeps = 1
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
  buildJing()
  buildDatatypeLibrary()
  buildNonSchema()
  buildSchemaDrivers()
  buildHtmlParser()
  buildUtil()
  buildXmlParser()
  buildTestHarness()
  buildValidator()

def checkout():
  # XXX root dir
  for mod in moduleNames:
    runCmd('"%s" co "%s" "%s"' % (svnCmd, svnRoot + mod + '/trunk/', mod))
  runCmd('"%s" co http://jing-trang.googlecode.com/svn/branches/validator-nu jing-trang' % (svnCmd))

def selfUpdate():
  runCmd('"%s" co "%s" "%s"' % (svnCmd, svnRoot + 'build' + '/trunk/', 'build'))
  newArgv = [sys.executable, buildScript, '--no-self-update']
  newArgv.extend(argv)
  os.execv(sys.executable, newArgv)  


def runTests():
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2",
                                                "html5-datatypes",
                                                "test-harness"])
                              + jingJarPath())
  runCmd('"%s" -cp %s org.whattf.syntax.Driver' % (javaCmd, classPath))

def splitHostSpec(spec):
  index = spec.find('/')
  return (spec[0:index], spec[index:])

def printHelp():
  print "Usage: python build/build.py [options] [tasks]"
  print ""
  print "Options:"
  print "  --svn=/usr/bin/svn         -- Sets the path to the svn binary"
  print "  --java=/usr/bin/java       -- Sets the path to the java binary"
  print "  --jar=/usr/bin/jar         -- Sets the path to the jar binary"
  print "  --javac=/usr/bin/javac     -- Sets the path to the javac binary"
  print "  --javadoc=/usr/bin/javadoc -- Sets the path to the javadoc binary"
  print "  --jdk-bin=/j2se/bin        -- Sets the paths for all JDK tools"
  print "  --log4j=log4j.properties   -- Sets the path to log4 configuration"
  print "  --port=8888                -- Sets the server port number"
  print "  --ajp=on                   -- Use AJP13 instead of HTTP"
  print "  --promiscuous-ssl=on       -- Don't check SSL/TLS certificate trust chain"
  print "  --heap=64                  -- Sets the heap size in MB"
  print "  --name=Validator.nu        -- Sets the service name"
  print "  --html5link=http://www.whatwg.org/specs/web-apps/current-work/"
  print "                                Sets the link URL of the HTML5 spec"
  print "  --html5load=file:validator/spec/html5.html"
  print "                                Sets the load URL of the HTML5 spec"
  print "  --iana-lang=http://www.iana.org/assignments/language-subtag-registry"
  print "                                Sets the URL for language tag registry"
  print "  --about=http://about.validator.nu/"
  print "                                Sets the URL for the about page"
  print "  --stylesheet=http://about.validator.nu/style.css"
  print "                                Sets the URL for the style sheet"
  print "                                Defaults to --about= plus style.css"
  print "  --script=http://about.validator.nu/script.js"
  print "                                Sets the URL for the style sheet"
  print "                                Defaults to --about= plus script.js"
  print "  --microsyntax=http://wiki.whatwg.org/wiki/MicrosyntaxDescriptions"
  print "                                Sets the URL for microformat"
  print "                                descriptions"
  print "  --alt-advice=http://wiki.whatwg.org/wiki/Validator.nu_alt_advice"
  print "                                Sets the URL for alt attribute"
  print "                                advice"
  print ""
  print "Tasks:"
  print "  checkout -- Checks out the source from SVN"
  print "  dldeps   -- Downloads missing dependency libraries and entities"
  print "  dltests  -- Downloads the external test suite if missing"
  print "  build    -- Build the source"
  print "  test     -- Run tests"
  print "  run      -- Run the system"
  print "  all      -- checkout dldeps dltests build test run"

buildScript = sys.argv[0]
argv = sys.argv[1:]
if len(argv) == 0:
  printHelp()
else:
  for arg in argv:
    if arg.startswith("--svn="):
      svnCmd = arg[6:]
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
    elif arg.startswith("--svnRoot="):
      svnRoot = arg[10:]
    elif arg.startswith("--port="):
      portNumber = arg[7:]
    elif arg.startswith("--control-port="):
      controlPort = arg[15:]
    elif arg.startswith("--log4j="):
      log4jProps = arg[8:]
    elif arg.startswith("--heap="):
      heapSize = arg[7:]
    elif arg.startswith("--html5link="):
      html5specLink = arg[12:]
    elif arg.startswith("--html5load="):
      html5specLoad = arg[12:]
    elif arg.startswith("--iana-lang="):
      ianaLang = arg[12:]
    elif arg.startswith("--about="):
      aboutPage = arg[8:]
    elif arg.startswith("--microsyntax="):
      microsyntax = arg[14:]
    elif arg.startswith("--alt-advice="):
      altAdvice = arg[13:]
    elif arg.startswith("--stylesheet="):
      stylesheet = arg[13:]
    elif arg.startswith("--icon="):
      icon = arg[7:]
    elif arg.startswith("--scp-target="):
      deploymentTarget = arg[13:]
    elif arg.startswith("--script="):
      script = arg[9:]
    elif arg.startswith("--name="):
      script = arg[7:]
    elif arg.startswith("--genericpath="):
      (genericHost, genericPath) = splitHostSpec(arg[14:])
    elif arg.startswith("--html5path="):
      (html5Host, html5Path) = splitHostSpec(arg[12:])
    elif arg.startswith("--parsetreepath="):
      (parsetreeHost, parsetreePath) = splitHostSpec(arg[16:])
    elif arg == '--ajp=on':
      useAjp = 1
    elif arg == '--ajp=off':
      useAjp = 0
    elif arg == '--promiscuous-ssl=on':
      usePromiscuousSsl = 1
    elif arg == '--promiscuous-ssl=off':
      usePromiscuousSsl = 0
    elif arg == '--no-self-update':
      noSelfUpdate = 1
    elif arg == '--local':
      useLocalCopies = 1
      noSelfUpdate = 1
    elif arg == '--help':
      printHelp()
    elif arg == 'dldeps':
      if noSelfUpdate:
        downloadDependencies()
        downloadLocalEntities()
      else:
        selfUpdate()
#    elif arg == 'dltests':
#      downloadOperaSuite()
    elif arg == 'checkout':
      if noSelfUpdate:
        checkout()
      else:
        selfUpdate()
    elif arg == 'build':
      if noSelfUpdate:
        buildAll()
      else:
        selfUpdate()
    elif arg == 'localent':
      if noSelfUpdate:
        prepareLocalEntityJar()
      else:
        selfUpdate()
    elif arg == 'deploy':
      if noSelfUpdate:
        deployOverScp()
      else:
        selfUpdate()
    elif arg == 'tar':
      if noSelfUpdate:
        createTarball()
        createDepTarball()
      else:
        selfUpdate()
    elif arg == 'script':
      if noSelfUpdate:
        if not stylesheet:
          stylesheet = aboutPage + 'style.css'
        if not script:
          script = aboutPage + 'script.js'
        if not icon:
          icon = aboutPage + 'icon.png'
        generateRunScript()
      else:
        selfUpdate()
    elif arg == 'test':
      if noSelfUpdate:
        runTests()
      else:
        selfUpdate()
    elif arg == 'run':
      if noSelfUpdate:
        if not stylesheet:
          stylesheet = aboutPage + 'style.css'
        if not script:
          script = aboutPage + 'script.js'
        if not icon:
          icon = aboutPage + 'icon.png'
        if useLocalCopies:
          ianaLang = 'file:local-entities/www.iana.org/assignments/language-subtag-registry'
          microsyntax = 'file:local-entities/wiki.whatwg.org/wiki/MicrosyntaxDescriptions'
          altAdvice = 'file:local-entities/wiki.whatwg.org/wiki/Validator.nu_alt_advice'
        runValidator()
      else:
        selfUpdate()
    elif arg == 'all':
      if noSelfUpdate:
        checkout()
        downloadDependencies()
        downloadLocalEntities()
        downloadOperaSuite()
        prepareLocalEntityJar()
        buildAll()
        runTests()
        if not stylesheet:
          stylesheet = aboutPage + 'style.css'
        if not script:
          script = aboutPage + 'script.js'
        if not icon:
          icon = aboutPage + 'icon.png'
        runValidator()
      else:
        selfUpdate()
    else:
      print "Unknown option %s." % arg
