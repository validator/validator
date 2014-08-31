#!/usr/bin/python

# Copyright (c) 2007 Henri Sivonen
# Copyright (c) 2008-2014 Mozilla Foundation
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
import httplib
import urllib2
import socket
import re
try:
  from hashlib import md5
except ImportError:
  from md5 import new as md5
import zipfile
import sys
from sgmllib import SGMLParser
import subprocess

javacCmd = 'javac'
jarCmd = 'jar'
javaCmd = 'java'
javadocCmd = 'javadoc'
tarCmd = 'tar'
scpCmd = 'scp'
gitCmd = 'git'

buildRoot = '.'
gitRoot = 'https://github.com/validator/'
portNumber = '8888'
controlPort = None
useAjp = 0
log4jProps = 'validator/log4j.properties'
heapSize = '128'
html5specLink = 'http://www.whatwg.org/specs/web-apps/current-work/'
html5specLoad = 'http://www.whatwg.org/specs/web-apps/current-work/'
aboutPage = 'http://about.validator.nu/'
userAgent = 'Validator.nu/LV'
icon = None
stylesheet = None
script = None
serviceName = 'Validator.nu'
resultsTitle = 'Validation results'
maxFileSize = 7168
usePromiscuousSsl = 0
genericHost = ''
html5Host = ''
parsetreeHost = ''
genericPath = '/'
html5Path = '/html5/'
parsetreePath = '/parsetree/'
deploymentTarget = None
noSelfUpdate = 0
metaNameExtensions = os.path.join("local-entities", "meta-name-extensions")
linkRelExtensions = os.path.join("local-entities", "link-rel-extensions")
aRelExtensions = os.path.join("local-entities", "a-rel-extensions")
pageTemplateFile = os.path.join("validator", "xml-src", "PageEmitter.xml")
formTemplateFile = os.path.join("validator", "xml-src", "FormEmitter.xml")
presetsFile = os.path.join("validator", "presets.txt")
aboutFile = os.path.join("validator", "site", "about.html")
stylesheetFile = os.path.join("validator", "site", "style.css")
scriptFile = os.path.join("validator", "site", "script.js")
httpTimeoutSeconds = 120
connectionTimeoutSeconds = 5
socketTimeoutSeconds = 5
followW3Cspec = 0
statistics = 0

dependencyPackages = [
  ("http://archive.apache.org/dist/commons/codec/binaries/commons-codec-1.4-bin.zip", "749bcf44779f95eb02d6cd7b9234bdaf"),
  ("http://archive.apache.org/dist/httpcomponents/commons-httpclient/binary/commons-httpclient-3.1.zip", "1752a2dc65e2fb03d4e762a8e7a1db49"),
  ("http://archive.apache.org/dist/commons/logging/binaries/commons-logging-1.1.1-bin.zip", "f88520ed791673aed6cc4591bc058b55"),
  ("http://download.icu-project.org/files/icu4j/53.1/icu4j-53_1.jar", "4d481ce010c1a786af2b79552778a3fc"),
  ("http://download.icu-project.org/files/icu4j/53.1/icu4j-charset-53_1.jar", "00d47513129425040dcfbfcf40da5fea"),
  ("http://switch.dl.sourceforge.net/sourceforge/jena/iri-0.5.zip", "87b0069e689c22ba2a2b50f4d200caca"),
  ("http://dist.codehaus.org/jetty/jetty-6.1.26/jetty-6.1.26.zip", "0d9b2ae3feb2b207057358142658a11f"),
  ("http://archive.apache.org/dist/logging/log4j/1.2.15/apache-log4j-1.2.15.zip", "5b0d27be24d6ac384215b6e269d3e352"),
  ("http://archive.apache.org/dist/xerces/j/Xerces-J-bin.2.9.1.zip", "a0e07ede1c3bd5231fe15eae24032b2e"),
  ("http://ftp.mozilla.org/pub/mozilla.org/js/rhino1_7R1.zip", "613eed8201d37be201805e5048ebb0c3"),
  ("http://sunet.dl.sourceforge.net/project/jsontools.berlios/jsontools-core-1.5.jar", "1f242910350f28d1ac4014928075becd"),
  ("http://hsivonen.iki.fi/code/antlr.jar", "9d2e9848c52204275c72d9d6e79f307c"),
  ("http://www.cafeconleche.org/XOM/xom-1.1.jar", "6b5e76db86d7ae32a451ffdb6fce0764"),
  ("http://www.slf4j.org/dist/slf4j-1.5.2.zip", "00ff08232a9959af3c7101b88ec456a7"),
  ("http://archive.apache.org/dist/commons/fileupload/binaries/commons-fileupload-1.2.1-bin.zip", "975100c3f74604c0c22f68629874f868"),
  ("http://archive.apache.org/dist/ant/binaries/apache-ant-1.7.0-bin.zip" , "ac30ce5b07b0018d65203fbc680968f5"),
  ("http://switch.dl.sourceforge.net/sourceforge/iso-relax/isorelax.20041111.zip" , "10381903828d30e36252910679fcbab6"),
  ("http://switch.dl.sourceforge.net/sourceforge/junit/junit-4.4.jar", "f852bbb2bbe0471cef8e5b833cb36078"),
  ("http://switch.dl.sourceforge.net/sourceforge/jchardet/chardet.zip", "4091d24451ee9a840933bce34b9e3a55"),
  ("http://switch.dl.sourceforge.net/sourceforge/saxon/saxonb9-1-0-2j.zip", "9e649eec59103593fb75befaa28e1f3d"),
  ("http://central.maven.org/maven2/io/mola/galimatias/galimatias/0.1.0/galimatias-0.1.0.jar", "55f2b9a4648d7593db3d8f307f84bb01"),
  ("https://raw.githubusercontent.com/tabatkins/css-parser/9c82592afb4cbbde79048ae61ba6da2566489b73/tokenizer.js", "731705f1bb5cf2aaf74b4e590759f026"),
  ("https://raw.githubusercontent.com/tabatkins/css-parser/9c82592afb4cbbde79048ae61ba6da2566489b73/parser.js", "a6cab081aa2320ea0c3d49fa99a44a33"),
  ("https://raw.githubusercontent.com/douglascrockford/JSON-js/3d7767b6b1f3da363c625ff54e63bbf20e9e83ac/json.js", "f508cbf66725dc438c780334f6849e6f"),
]

# Unfortunately, the packages contain old versions of certain libs, so 
# can't just autodiscover all jars. Hence, an explicit list.

runDependencyJars = [
  "commons-codec-1.4/commons-codec-1.4.jar",
  "commons-httpclient-3.1/commons-httpclient-3.1.jar",
  "commons-logging-1.1.1/commons-logging-1.1.1.jar",
  "commons-logging-1.1.1/commons-logging-adapters-1.1.1.jar",
  "commons-logging-1.1.1/commons-logging-api-1.1.1.jar",
  "icu4j-charset-53_1.jar",
  "icu4j-53_1.jar",
  "iri-0.5/lib/iri.jar",
  "jetty-6.1.26/lib/servlet-api-2.5-20081211.jar",
  "jetty-6.1.26/lib/jetty-6.1.26.jar",
  "jetty-6.1.26/lib/jetty-util-6.1.26.jar",
  "jetty-6.1.26/lib/ext/jetty-ajp-6.1.26.jar",
  "apache-log4j-1.2.15/log4j-1.2.15.jar",
  "rhino1_7R1/js.jar",
  "xerces-2_9_1/xercesImpl.jar",
  "xerces-2_9_1/xml-apis.jar",
  "slf4j-1.5.2/slf4j-log4j12-1.5.2.jar",
  "commons-fileupload-1.2.1/lib/commons-fileupload-1.2.1.jar",
  "isorelax.jar",
  "mozilla/intl/chardet/java/dist/lib/chardet.jar",
  "saxon9.jar",
  "galimatias-0.1.0.jar",
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
  "xmlparser",
  "validator",
  "jing-trang",
  "htmlparser",
  "nu-validator-site",
  "tests",
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
  print cmd
  if os.name == 'nt' and cmd[:1] == '"':
    subprocess.call(cmd)
  else:
    os.system(cmd)

def execCmd(cmd, args):
  print "%s %s" % (cmd, " ".join(args))
  if os.name == 'nt':
    if subprocess.call([cmd,] + args):
      print "Command failed."
      sys.exit(2)
  else:
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
  runCmd('"%s" -encoding UTF-8 -g -nowarn -classpath "%s" -sourcepath "%s" -d "%s" %s'\
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
  itsRnc = os.path.join(os.path.join(buildRoot, "validator", "schema", "its20-html5.rnc"))
  itsTypesRnc = os.path.join(os.path.join(buildRoot, "validator", "schema", "its20-html5-types.rnc"))
  buildSchemaDriverHtmlCore(schemaDir)
  buildSchemaDriverHtml5NoMicrodata(schemaDir)
  buildSchemaDriverHtml5(schemaDir)
  buildSchemaDriverHtml5RDFa(schemaDir)
  buildSchemaDriverHtml5RDFaLite(schemaDir)
  buildSchemaDriverXhtmlCore(schemaDir)
  buildSchemaDriverXhtmlCorePlusWf2(schemaDir)
  buildSchemaDriverXhtml5xhtmlNoMicrodata(schemaDir)
  buildSchemaDriverXhtml5htmlNoMicrodata(schemaDir)
  buildSchemaDriverXhtml5html(schemaDir)
  buildSchemaDriverXhtml5xhtml(schemaDir)
  buildSchemaDriverXhtml5xhtmlRDFa(schemaDir)
  buildSchemaDriverXhtml5xhtmlRDFaLite(schemaDir)
  buildSchemaDriverXhtml5htmlRDFaLite(schemaDir)
  removeIfExists(os.path.join(schemaDir, "legacy.rnc"))
  removeIfExists(os.path.join(schemaDir, "its20-html5.rnc"))
  removeIfExists(os.path.join(schemaDir, "its20-html5-types.rnc"))
  shutil.copy(legacyRnc, schemaDir)
  shutil.copy(itsRnc, schemaDir)
  shutil.copy(itsTypesRnc, schemaDir)

#################################################################
# data and functions for building schema drivers
#################################################################

schemaDriverBase = '''\
start = html.elem
include "phrase.rnc"
include "block.rnc"
include "sectional.rnc"
include "revision.rnc"
include "embed.rnc"
include "core-scripting.rnc"
'''
schemaDriverHtml5NoMicrodata = '''\
include "structural.rnc"
include "ruby.rnc"
include "media.rnc"
include "tables.rnc"
include "form-datatypes.rnc"
include "web-forms.rnc"
include "web-forms2.rnc"
include "web-components.rnc"
include "applications.rnc"
include "data.rnc"
include "legacy.rnc"
include "aria.rnc"
include "meta.rnc" {
		html.inner =
			(	head.elem
			,	(	body.elem
				|	frameset.elem
				)
			)
}
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
schemaDriverMeta = '''\
include "meta.rnc"
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
'''
schemaDriverToggle_Xhtml5xhtml = '''\
include "common.rnc" {
		XMLonly = empty
		HTMLonly = notAllowed
		v5only = empty
'''
schemaDriverToggle_Xhtml5html = '''\
include "common.rnc" {
		XMLonly = empty
		HTMLonly = notAllowed
		v5only = empty
		nonHTMLizable = notAllowed
		nonRoundtrippable = notAllowed
'''
schemaDriverHtml5Microdata = '''\
include "microdata.rnc"
'''
schemaDriverHtml5RDFa = '''\
include "rdfa.rnc"
'''
schemaDriverHtml5RDFaLite = '''\
include "rdfa.rnc" {
		nonRDFaLite = notAllowed
}
'''

def openDriver(schemaDir, driverName, sourceName=""):
  removeIfExists(os.path.join(schemaDir, driverName))
  if sourceName != "":
    # if we have a file sourceName, copy it so that we can later
    # just append additions to the copy
    shutil.copyfile(os.path.join(schemaDir, sourceName), os.path.join(schemaDir, driverName))
  f = open(os.path.join(schemaDir, driverName),"a")
  return f

def writeW3CToggle(f):
  if followW3Cspec:
    f.write("\t\tnonW3C = notAllowed\n")
  f.write("}\n")

################################
# HTML schema drivers
################################
def buildSchemaDriverHtmlCore(schemaDir):
  f = openDriver(schemaDir, "html5core.rnc")
  f.write(schemaDriverToggle_HtmlCore)
  f.write(schemaDriverBase)
  f.write(schemaDriverMeta)
  f.close()

def buildSchemaDriverHtml5NoMicrodata(schemaDir):
  f = openDriver(schemaDir, "html5full-no-microdata.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_Html5)
  writeW3CToggle(f)
  f.write(schemaDriverBase)
  f.write(schemaDriverHtml5NoMicrodata)
  # For W3C case, make HTML5 checking always include ITS2 support
  if followW3Cspec:
    f.write('include "its20-html5.rnc"\n')
    f.write('common.attrs &= its-html-attributes\n')
  f.close()

def buildSchemaDriverHtml5(schemaDir):
  f = openDriver(schemaDir, "html5full.rnc", "html5full-no-microdata.rnc")
  f.write(schemaDriverHtml5Microdata)
  f.close()

def buildSchemaDriverHtml5RDFa(schemaDir):
  f = openDriver(schemaDir, "html5full-rdfa.rnc", "html5full.rnc")
  f.write(schemaDriverHtml5RDFa)
  f.close()
def buildSchemaDriverHtml5RDFaLite(schemaDir):
  f = openDriver(schemaDir, "html5full-rdfalite.rnc", "html5full.rnc")
  f.write(schemaDriverHtml5RDFaLite)
  f.close()

################################
# XHTML schema drivers
################################
def buildSchemaDriverXhtmlCore(schemaDir):
  f = openDriver(schemaDir, "xhtml5core.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_XhtmlCore)
  f.write(schemaDriverBase)
  f.write(schemaDriverMeta)
  f.close()

def buildSchemaDriverXhtmlCorePlusWf2(schemaDir):
  f = openDriver(schemaDir, "xhtml5core-plus-web-forms2.rnc", "xhtml5core.rnc")
  f.write(schemaDriverPlusWebForms2)
  f.close()

def buildSchemaDriverXhtml5htmlNoMicrodata(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-html-no-microdata.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_Xhtml5html)
  writeW3CToggle(f)
  f.write(schemaDriverBase)
  f.write(schemaDriverHtml5NoMicrodata)
  f.close()

def buildSchemaDriverXhtml5xhtmlNoMicrodata(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-xhtml-no-microdata.rnc")
  f.write(schemaDriverNamespace)
  f.write(schemaDriverToggle_Xhtml5xhtml)
  writeW3CToggle(f)
  f.write(schemaDriverBase)
  f.write(schemaDriverHtml5NoMicrodata)
  f.close()

def buildSchemaDriverXhtml5xhtml(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-xhtml.rnc", "xhtml5full-xhtml-no-microdata.rnc")
  f.write(schemaDriverHtml5Microdata)
  f.close()

def buildSchemaDriverXhtml5xhtmlRDFa(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-xhtml-rdfa.rnc", "xhtml5full-xhtml.rnc")
  f.write(schemaDriverHtml5RDFa)
  f.close()
def buildSchemaDriverXhtml5xhtmlRDFaLite(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-xhtml-rdfalite.rnc", "xhtml5full-xhtml.rnc")
  f.write(schemaDriverHtml5RDFaLite)
  f.close()

def buildSchemaDriverXhtml5html(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-html.rnc", "xhtml5full-html-no-microdata.rnc")
  f.write(schemaDriverHtml5Microdata)
  f.close()

def buildSchemaDriverXhtml5htmlRDFa(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-html-rdfa.rnc", "xhtml5full-html.rnc")
  f.write(schemaDriverHtml5RDFa)
  f.close()
def buildSchemaDriverXhtml5htmlRDFaLite(schemaDir):
  f = openDriver(schemaDir, "xhtml5full-html-rdfalite.rnc", "xhtml5full-html.rnc")
  f.write(schemaDriverHtml5RDFaLite)
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
  ioJar  = os.path.join("util", "dist", "io-xml-util.jar")
  pageEmitter = os.path.join("validator", "src", "nu", "validator", "servlet", "PageEmitter.java")
  formEmitter = os.path.join("validator", "src", "nu", "validator", "servlet", "FormEmitter.java")
  runCmd('"%s" -classpath %s nu.validator.tools.SaxCompiler %s %s' % (javaCmd, ioJar, pageTemplateFile, pageEmitter))
  runCmd('"%s" -classpath %s nu.validator.tools.SaxCompiler %s %s' % (javaCmd, ioJar, formTemplateFile, formEmitter))
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
    '-XX:-DontCompileHugeMethods',
    '-Xms%sk' % heap,
    '-Xmx%sk' % heap,
    '-Xss512k',
    '-XX:ThreadStackSize=2048',
    '-classpath',
    classPath,
    '-Dnu.validator.servlet.read-local-log4j-properties=1',
    '-Dnu.validator.servlet.log4j-properties=' + log4jProps,
    '-Dnu.validator.servlet.version=3',
    '-Dnu.validator.servlet.service-name=' + serviceName,
    '-Dnu.validator.servlet.results-title=' + resultsTitle,
    '-Dorg.whattf.datatype.warn=true',
    '-Dnu.validator.servlet.about-page=' + aboutPage,
    '-Dnu.validator.servlet.user-agent=' + userAgent,
    '-Dnu.validator.servlet.style-sheet=' + stylesheet,
    '-Dnu.validator.servlet.icon=' + icon,
    '-Dnu.validator.servlet.script=' + script,
    '-Dnu.validator.spec.html5-load=' + html5specLoad,
    '-Dnu.validator.spec.html5-link=' + html5specLink,
    '-Dnu.validator.servlet.max-file-size=%d' % (maxFileSize * 1024),
    '-Dnu.validator.servlet.connection-timeout=%d' % (connectionTimeoutSeconds * 1000),
    '-Dnu.validator.servlet.socket-timeout=%d' % (socketTimeoutSeconds * 1000),
    '-Dnu.validator.servlet.follow-w3c-spec=%d' % followW3Cspec,
    '-Dnu.validator.servlet.statistics=%d' % statistics,
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
  # Quick hack to strike a balance between shell variable expansions
  # having to be unquoted and command line switches that take
  # human-readable string potentially containing spaces.
  f.write(" ".join([javaCmd,] + map(lambda x: "'{0}'".format(x) if " " in x else x, args)))
  if controlPort:
    f.write(" <&- 1>/dev/null 2>&1 &")
  f.write("\n")
  f.close()  

def runValidator():
  ensureDirExists(os.path.join(buildRoot, "logs"))
  args = getRunArgs(str(int(heapSize) * 1024))
  execCmd(javaCmd, args)

def createDistZip(distType):
  distDir = (os.path.join(buildRoot, "build", "vnu"))
  removeIfDirExists(distDir)
  os.mkdir(distDir)
  if (distType == "war"):
    os.mkdir(os.path.join(distDir, "war"))
  antRoot = os.path.join(buildRoot, "jing-trang", "lib")
  antJar= os.path.join(antRoot, "ant.jar")
  antLauncherJar = os.path.join(antRoot, "ant-launcher.jar")
  classPath = os.pathsep.join([antJar, antLauncherJar])
  runCmd('"%s" -cp %s org.apache.tools.ant.Main -f %s %s'
    % (javaCmd, classPath, os.path.join(buildRoot, "build", "build.xml"), distType))
  f = open(os.path.join(distDir, "VERSION"), "r")
  version = f.read()
  readmeHtml = "https://raw.github.com/validator/validator.github.io/master/index.html"
  readmeMarkdown = "https://raw.github.com/validator/validator.github.io/master/README.md"
  changelog = "https://raw.github.com/validator/validator.github.io/master/CHANGELOG.md"
  fetchUrlTo(readmeHtml, os.path.join(distDir, "index.html"))
  fetchUrlTo(readmeMarkdown, os.path.join(distDir, "README.md"))
  fetchUrlTo(changelog, os.path.join(distDir, "CHANGELOG.md"))
  os.chdir("build")
  distroFile = os.path.join("vnu-%s.%s.zip" % (version, distType))
  removeIfExists(distroFile)
  zf = zipfile.ZipFile(distroFile, "w")
  for dirname, subdirs, files in os.walk("vnu"):
    zf.write(dirname)
    for filename in files:
      zf.write(os.path.join(dirname, filename))
  zf.close()
  if (distType == "jar"):
    testJar(distDir);
  os.chdir("..")

def testJar(distDir):
  testUrl = "https://raw.githubusercontent.com/validator/tests/master/html/elements/a/href-isvalid.html"
  testFile = os.path.join(distDir, "test.html")
  fetchUrlTo(testUrl, testFile)
  runCmd('"%s" -jar %s %s' % (javaCmd, os.path.join("vnu", "vnu.jar"), testFile))
  formats = ["xml","json","text"]
  for _format in formats:
    runCmd('"%s" -jar %s --format %s %s' % (javaCmd, os.path.join("vnu", "vnu.jar"), _format, testFile))
  removeIfExists(testFile)

def createTarball():
  args = [
    "zcf",
    os.path.join(buildRoot, "jars.tar.gz"),
    os.path.join(buildRoot, "run-validator.sh"),
    os.path.join(buildRoot, "validator", "site", "style.css"),
    os.path.join(buildRoot, "validator", "site", "script.js"),
    os.path.join(buildRoot, "validator", "site", "icon.png"),
  ] + ownJarList()
  runCmd('"%s" %s' %(tarCmd, " ".join(args)))

def createDepTarball():
  args = [
    "zcf",
    os.path.join(buildRoot, "deps.tar.gz"),
  ] + dependencyJarPaths(runDependencyJars)
  runCmd('"%s" %s' %(tarCmd, " ".join(args)))


def createWar():
  warDir = (os.path.join(buildRoot, "build", "vnu", "war"))
  removeIfDirExists(warDir)
  os.mkdir(warDir)
  antRoot = os.path.join(buildRoot, "jing-trang", "lib")
  antJar= os.path.join(antRoot, "ant.jar")
  antLauncherJar = os.path.join(antRoot, "ant-launcher.jar")
  classPath = os.pathsep.join([antJar, antLauncherJar])
  runCmd('"%s" -cp %s org.apache.tools.ant.Main -f %s war'
    % (javaCmd, classPath, os.path.join(buildRoot, "build", "build.xml")))

def deployOverScp():
  if not deploymentTarget:
    print "No target"
    return
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
  completed = False
  defaultTimeout = socket.getdefaulttimeout()
  while not completed:
   try:
    socket.setdefaulttimeout(httpTimeoutSeconds)
    f = urllib2.urlopen(url)
    data = f.read()
    f.close()
    completed = True
   except httplib.BadStatusLine, e:
    print "received error, retrying"
   finally:
    socket.setdefaulttimeout(defaultTimeout)
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
  f = urllib2.urlopen(baseUrl, timeout=httpTimeoutSeconds)
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
  if os.path.isfile(os.path.join(buildRoot, "local-entities", "www.iana.org/assignments/language-subtag-registry")):
    removeIfExists(os.path.join(buildRoot, "local-entities", "www.iana.org/assignments/language-subtag-registry"))
  if os.path.isfile(os.path.join(buildRoot, "local-entities", "wiki.whatwg.org/wiki/MicrosyntaxDescriptions")):
    removeIfExists(os.path.join(buildRoot, "local-entities", "wiki.whatwg.org/wiki/MicrosyntaxDescriptions"))
  fetchUrlTo("http://help.whatwg.org/extensions/meta-name/", os.path.join(buildRoot, metaNameExtensions))
  fetchUrlTo("http://help.whatwg.org/extensions/link-rel/", os.path.join(buildRoot, linkRelExtensions))
  fetchUrlTo("http://help.whatwg.org/extensions/a-rel/", os.path.join(buildRoot, aRelExtensions))
  f = open(os.path.join(buildRoot, "validator", "entity-map.txt"))
  try:
    for line in f:
      url, path = line.strip().split("\t")
      if not path.startswith("schema/"):
        if not os.path.exists(os.path.join(buildRoot, "local-entities", path)):
          if url.startswith("http://www.w3.org/"):
            removeIfDirExists(os.path.join(buildRoot, "local-entities", "www.w3.org"))
            zipExtract(os.path.join(buildRoot, "nu-validator-site", "www.w3.org.zip"),
                os.path.join(buildRoot, "local-entities"))
          else:
            fetchUrlTo(url, os.path.join(buildRoot, "local-entities", path))
  finally:
    f.close()

def localPathToJarCompatName(path):
  return javaSafeNamePat.sub('_', path)

def preparePropertiesFile():
  filesDir = os.path.join(buildRoot, "validator", "src", "nu", "validator", "localentities", "files")
  f = open(os.path.join(filesDir, "misc.properties"), 'w')
  f.write("nu.validator.servlet.service-name=%s\n" % serviceName)
  f.write("nu.validator.servlet.results-title=%s\n" % resultsTitle)
  f.close

def prepareLocalEntityJar():
  filesDir = os.path.join(buildRoot, "validator", "src", "nu", "validator", "localentities", "files")
  if os.path.exists(filesDir):
    shutil.rmtree(filesDir)
  os.makedirs(filesDir)
  preparePropertiesFile()
  createCssParserJS(filesDir)
  shutil.copyfile(os.path.join(buildRoot, metaNameExtensions), os.path.join(filesDir, "meta-name-extensions"))
  shutil.copyfile(os.path.join(buildRoot, linkRelExtensions), os.path.join(filesDir, "link-rel-extensions"))
  shutil.copyfile(os.path.join(buildRoot, aRelExtensions), os.path.join(filesDir, "a-rel-extensions"))
  shutil.copyfile(os.path.join(buildRoot, presetsFile), os.path.join(filesDir, "presets"))
  shutil.copyfile(os.path.join(buildRoot, aboutFile), os.path.join(filesDir, "about.html"))
  shutil.copyfile(os.path.join(buildRoot, stylesheetFile), os.path.join(filesDir, "style.css"))
  shutil.copyfile(os.path.join(buildRoot, scriptFile), os.path.join(filesDir, "script.js"))
  shutil.copyfile(os.path.join(buildRoot, "validator", "site", "icon.png"), os.path.join(filesDir, "icon.png"))
  shutil.copyfile(os.path.join(buildRoot, "validator", "spec", "html5.html"), os.path.join(filesDir, "html5spec"))
  shutil.copyfile(os.path.join(buildRoot, "validator", "spec", "w3c-html5.html"), os.path.join(filesDir, "html5spec"))
  shutil.copyfile(os.path.join(buildRoot, "validator", "log4j.properties"), os.path.join(filesDir, "log4j.properties"))
  shutil.copyfile(os.path.join(buildRoot, "validator", "site", "language-subtag-registry"), os.path.join(filesDir, "language-subtag-registry"))
  shutil.copyfile(os.path.join(buildRoot, "validator", "src", "nu", "validator", "client", "cli-help"), os.path.join(filesDir, "cli-help"))
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

def createCssParserJS(filesDir):
  t = open(os.path.join(buildRoot, "dependencies", "tokenizer.js"), 'r')
  p = open(os.path.join(buildRoot, "dependencies", "parser.js"), 'r')
  j = open(os.path.join(buildRoot, "dependencies", "json.js"), 'r')
  o = open(os.path.join(filesDir, "css-parser-js"),'wb')
  shutil.copyfileobj(t, o)
  shutil.copyfileobj(p, o)
  shutil.copyfileobj(j, o)
  t.close
  p.close
  consoleLogForRhino = '''\
  var console = {
    log: function (msg) {
      throw msg;
      return true;
    }
  }
  '''
  o.write(consoleLogForRhino)
  o.close

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
  if 'JAVA_HOME' not in os.environ:
    print "Error: The JAVA_HOME environment variable is not set."
    print "Set the JAVA_HOME environment variable to the pathname of the directory where your JDK is installed."
    sys.exit(1)
  prepareLocalEntityJar()
  buildJing()
  buildDatatypeLibrary()
  buildNonSchema()
  buildSchemaDrivers()
  buildHtmlParser()
  buildUtil()
  buildXmlParser()
  buildValidator()

def gitCloneOrUpdate(mod, baseUrl):
  if os.path.exists(mod):
    if os.path.exists(mod + "/.git"):
      os.chdir(mod)
      runCmd('"%s" pull -q -r %s%s.git master' % (gitCmd, baseUrl, mod))
      os.chdir("..")
    else:
      if os.path.exists(mod + "-old"):
        print "The %s module has moved to github. Can't proceed automatically, because %s-old exists. Please remove it." % (mod, mod)
        sys.exit(3)
      else:
        print "The %s module has moved to github. Renaming the old directory to %s-old and pulling from github." % (mod, mod)
        os.rename(mod, mod + "-old")
        runCmd('"%s" clone %s%s.git' % (gitCmd, baseUrl, mod))
  else:
    runCmd('"%s" clone %s%s.git' % (gitCmd, baseUrl, mod))

def checkout():
  # XXX root dir
  for mod in moduleNames:
    gitCloneOrUpdate(mod, gitRoot)

def selfUpdate():
  gitCloneOrUpdate("build", gitRoot)
  newArgv = [sys.executable, buildScript, '--no-self-update']
  newArgv.extend(argv)
  if os.name == 'nt':
    sys.exit(subprocess.call(newArgv))
  else:
    os.execv(sys.executable, newArgv)  

def runTests():
  if followW3Cspec:
    args = "tests/messages.json"
  else:
    args = "--ignore=html-its tests/messages.json"
  className = "nu.validator.client.TestRunner"
  classPath = os.pathsep.join(dependencyJarPaths() 
                              + jarNamesToPaths(["non-schema", 
                                                "io-xml-util",
                                                "htmlparser",
                                                "hs-aelfred2",
                                                "html5-datatypes",
                                                "validator"])
                              + jingJarPath())
  runCmd('"%s" -classpath %s %s %s' % (javaCmd, classPath, className, args))

def splitHostSpec(spec):
  index = spec.find('/')
  return (spec[0:index], spec[index:])

def printHelp():
  print "Usage: python build/build.py [options] [tasks]"
  print ""
  print "Options:"
  print "  --git=/usr/bin/git         -- Sets the path to the git binary"
  print "  --java=/usr/bin/java       -- Sets the path to the java binary"
  print "  --jar=/usr/bin/jar         -- Sets the path to the jar binary"
  print "  --javac=/usr/bin/javac     -- Sets the path to the javac binary"
  print "  --javadoc=/usr/bin/javadoc -- Sets the path to the javadoc binary"
  print "  --jdk-bin=/j2se/bin        -- Sets the paths for all JDK tools"
  print "  --log4j=log4j.properties   -- Sets the path to log4 configuration"
  print "  --port=8888                -- Sets the server port number"
  print "  --control-port=-1"
  print "                                Sets the server control port number"
  print "                                (necessary for daemonizing)"
  print "  --ajp=on                   -- Use AJP13 instead of HTTP"
  print "  --promiscuous-ssl=on       -- Don't check SSL/TLS certificate trust chain"
  print "  --heap=64                  -- Sets the heap size in MB"
  print "  --name=Validator.nu        -- Sets the service name"
  print "  --html5link=http://www.whatwg.org/specs/web-apps/current-work/"
  print "                                Sets the link URL of the HTML5 spec"
  print "  --html5load=http://www.whatwg.org/specs/web-apps/current-work/"
  print "                                Sets the load URL of the HTML5 spec"
  print "  --about=http://about.validator.nu/"
  print "                                Sets the URL for the about page"
  print "  --stylesheet=style.css"
  print "                                Sets the URL for the style sheet"
  print "                                Defaults to just style.css relative to"
  print "                                the validator URL"
  print "  --script=script.js"
  print "                                Sets the URL for the script"
  print "                                Defaults to just script.js relative to"
  print "                                the validator URL"
  print ""
  print "Tasks:"
  print "  checkout -- Checks out the sources"
  print "  dldeps   -- Downloads missing dependency libraries and entities"
  print "  dltests  -- Downloads the external test suite if missing"
  print "  build    -- Build the source"
  print "  test     -- Run tests"
  print "  run      -- Run the system"
  print "  all      -- checkout dldeps dltests build test run"
  print "  dist     -- Create a release distribution"
  print "  war      -- Create a WAR file containing a release distribution"

buildScript = sys.argv[0]
argv = sys.argv[1:]
if len(argv) == 0:
  printHelp()
else:
  for arg in argv:
    if arg.startswith("--git="):
      gitCmd = arg[6:]
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
    elif arg.startswith("--gitRoot="):
      gitRoot = arg[10:]
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
      ianaCharset = arg[15:]
    elif arg.startswith("--about="):
      aboutPage = arg[8:]
    elif arg.startswith("--stylesheet="):
      stylesheet = arg[13:]
    elif arg.startswith("--icon="):
      icon = arg[7:]
    elif arg.startswith("--user-agent="):
      userAgent = arg[13:]
    elif arg.startswith("--scp-target="):
      deploymentTarget = arg[13:]
    elif arg.startswith("--script="):
      script = arg[9:]
    elif arg.startswith("--name="):
      serviceName = arg[7:]
    elif arg.startswith("--results-title="):
      resultsTitle = arg[16:]
    elif arg.startswith("--genericpath="):
      (genericHost, genericPath) = splitHostSpec(arg[14:])
    elif arg.startswith("--html5path="):
      (html5Host, html5Path) = splitHostSpec(arg[12:])
    elif arg.startswith("--parsetreepath="):
      (parsetreeHost, parsetreePath) = splitHostSpec(arg[16:])
    elif arg.startswith("--page-template="):
      pageTemplateFile = arg[16:]
    elif arg.startswith("--form-template="):
      formTemplateFile = arg[16:]
    elif arg.startswith("--presets-file="):
      presetsFile = arg[15:]
    elif arg.startswith("--about-file="):
      aboutFile = arg[13:]
    elif arg.startswith("--stylesheet-file="):
      stylesheetFile = arg[18:]
    elif arg.startswith("--script-file="):
      scriptFile = arg[14:]
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
      noSelfUpdate = 1
    elif arg.startswith("--connection-timeout="):
      connectionTimeoutSeconds = int(arg[21:]);
    elif arg.startswith("--socket-timeout="):
      socketTimeoutSeconds = int(arg[17:]);
    elif arg == '--follow-w3c-spec':
      followW3Cspec = 1
    elif arg == '--statistics':
      statistics = 1
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
    elif arg == 'jar':
      if noSelfUpdate:
        createDistZip('jar')
      else:
        selfUpdate()
    elif arg == 'war':
      if noSelfUpdate:
        createDistZip('war')
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
          stylesheet = 'style.css'
        if not script:
          script = 'script.js'
        if not icon:
          icon = 'icon.png'
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
          stylesheet = 'style.css'
        if not script:
          script = 'script.js'
        if not icon:
          icon = 'icon.png'
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
          stylesheet = 'style.css'
        if not script:
          script = 'script.js'
        if not icon:
          icon = 'icon.png'
        runValidator()
      else:
        selfUpdate()
    else:
      print "Unknown option %s." % arg
