#!/usr/bin/env python2.7

# Copyright (c) 2007 Henri Sivonen
# Copyright (c) 2008-2015 Mozilla Foundation
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
import time

javaVersion = '1.6'
javacCmd = 'javac'
jarCmd = 'jar'
javaCmd = 'java'
javadocCmd = 'javadoc'
tarCmd = 'tar'
scpCmd = 'scp'
gitCmd = 'git'

buildRoot = '.'
vnuDir = (os.path.join(buildRoot, "build", "vnu"))
portNumber = '8888'
controlPort = None
useAjp = 0
log4jProps = 'resources/log4j.properties'
heapSize = '128'
stackSize = ''
html5specLink = 'https://www.whatwg.org/specs/web-apps/current-work/'
html5specLoad = 'https://www.whatwg.org/specs/web-apps/current-work/'
aboutPage = 'https://about.validator.nu/'
userAgent = 'Validator.nu/LV'
icon = None
stylesheet = None
script = None
serviceName = 'Validator.nu'
resultsTitle = 'Validation results'
maxFileSize = 8192
usePromiscuousSsl = 0
genericHost = ''
html5Host = ''
parsetreeHost = ''
genericPath = '/'
html5Path = '/html5/'
parsetreePath = '/parsetree/'
deploymentTarget = None
subtagRegistry = os.path.join("local-entities", "subtag-registry")
syntaxDescriptions = os.path.join("local-entities", "syntax-descriptions")
vnuAltAdvice = os.path.join("local-entities", "vnu-alt-advice")
metaNameExtensions = os.path.join("local-entities", "meta-name-extensions")
linkRelExtensions = os.path.join("local-entities", "link-rel-extensions")
aRelExtensions = os.path.join("local-entities", "a-rel-extensions")
pageTemplate = os.path.join("site", "PageEmitter.xml")
formTemplate = os.path.join("site", "FormEmitter.xml")
presetsFile = os.path.join("resources", "presets.txt")
aboutFile = os.path.join("site", "about.html")
stylesheetFile = os.path.join("site", "style.css")
scriptFile = os.path.join("site", "script.js")
httpTimeoutSeconds = 120
connectionTimeoutSeconds = 5
socketTimeoutSeconds = 5
followW3Cspec = 0
statistics = 0
miniDoc = '<!doctype html><meta charset=utf-8><title>test</title>'

dependencyPackages = [
  ("http://archive.apache.org/dist/commons/codec/binaries/commons-codec-1.4-bin.zip", "749bcf44779f95eb02d6cd7b9234bdaf"),
  ("http://archive.apache.org/dist/httpcomponents/commons-httpclient/binary/commons-httpclient-3.1.zip", "1752a2dc65e2fb03d4e762a8e7a1db49"),
  ("http://archive.apache.org/dist/commons/logging/binaries/commons-logging-1.1.1-bin.zip", "f88520ed791673aed6cc4591bc058b55"),
  ("http://download.icu-project.org/files/icu4j/53.1/icu4j-53_1.jar", "4d481ce010c1a786af2b79552778a3fc"),
  ("http://download.icu-project.org/files/icu4j/53.1/icu4j-charset-53_1.jar", "00d47513129425040dcfbfcf40da5fea"),
  ("https://github.com/validator/validator/releases/download/dependencies/iri-0.5.zip", "87b0069e689c22ba2a2b50f4d200caca"),
  ("http://dist.codehaus.org/jetty/jetty-6.1.26/jetty-6.1.26.zip", "0d9b2ae3feb2b207057358142658a11f"),
  ("http://archive.apache.org/dist/logging/log4j/1.2.15/apache-log4j-1.2.15.zip", "5b0d27be24d6ac384215b6e269d3e352"),
  ("http://archive.apache.org/dist/xerces/j/Xerces-J-bin.2.9.1.zip", "a0e07ede1c3bd5231fe15eae24032b2e"),
  ("https://github.com/mozilla/rhino/releases/download/Rhino1_7R5_RELEASE/rhino1_7R5.zip", "e6a5d95f6949dbaa4e97a94bdfb7e1eb"),
  ("http://central.maven.org/maven2/com/sdicons/jsontools/jsontools-core/1.5/jsontools-core-1.5.jar", "1f242910350f28d1ac4014928075becd"),
  ("http://hsivonen.iki.fi/code/antlr.jar", "9d2e9848c52204275c72d9d6e79f307c"),
  ("http://www.cafeconleche.org/XOM/xom-1.1.jar", "6b5e76db86d7ae32a451ffdb6fce0764"),
  ("http://www.slf4j.org/dist/slf4j-1.5.2.zip", "00ff08232a9959af3c7101b88ec456a7"),
  ("http://archive.apache.org/dist/commons/fileupload/binaries/commons-fileupload-1.2.1-bin.zip", "975100c3f74604c0c22f68629874f868"),
  ("http://archive.apache.org/dist/ant/binaries/apache-ant-1.7.0-bin.zip", "ac30ce5b07b0018d65203fbc680968f5"),
  ("http://central.maven.org/maven2/junit/junit/4.4/junit-4.4.jar", "f852bbb2bbe0471cef8e5b833cb36078"),
  ("https://github.com/validator/validator/releases/download/dependencies/chardet.zip", "4091d24451ee9a840933bce34b9e3a55"),
  ("http://central.maven.org/maven2/io/mola/galimatias/galimatias/0.1.0/galimatias-0.1.0.jar", "55f2b9a4648d7593db3d8f307f84bb01"),
  ("https://raw.githubusercontent.com/tabatkins/parse-css/91f2450b4b009d79569125674898b9aea0cb6a3b/parse-css.js", "278e875a4d4fa2d95480f28a6d5808be"),
  ("https://raw.githubusercontent.com/douglascrockford/JSON-js/3d7767b6b1f3da363c625ff54e63bbf20e9e83ac/json.js", "f508cbf66725dc438c780334f6849e6f"),
  ("https://github.com/validator/validator/releases/download/dependencies/www.w3.org.zip", "167efbb410689e028129142aa3cf77ba"),
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
  "rhino1_7R5/js.jar",
  "xerces-2_9_1/xercesImpl.jar",
  "xerces-2_9_1/xml-apis.jar",
  "slf4j-1.5.2/slf4j-log4j12-1.5.2.jar",
  "commons-fileupload-1.2.1/lib/commons-fileupload-1.2.1.jar",
  "jing-trang/lib/isorelax.jar",
  "mozilla/intl/chardet/java/dist/lib/chardet.jar",
  "jing-trang/lib/saxon9.jar",
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
    return os.system(cmd)

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
    print "Removing %s" % filePath
    os.unlink(filePath)

def removeIfDirExists(dirPath):
  if os.path.exists(dirPath):
    print "Removing %s" % dirPath
    shutil.rmtree(dirPath)

def ensureDirExists(dirPath):
  if not os.path.exists(dirPath):
    os.makedirs(dirPath)

def findFilesWithExtension(directory, extension, subtrees=None):
  subtrees = subtrees if subtrees else [directory]
  rv = []
  ext = '.' + extension
  for root, dirs, files in os.walk(directory):
    for filename in files:
      for subtree in subtrees:
        if subtree in root and filename.endswith(ext):
          rv.append(os.path.join(root, filename))
  return rv

def findFiles(directory, subtrees=None):
  subtrees = subtrees if subtrees else [directory]
  rv = []
  for root, dirs, files in os.walk(directory):
    for filename in files:
      for subtree in subtrees:
        candidate = os.path.join(root, filename)
        if subtree in root and candidate.find("/.svn") == -1:
          rv.append(candidate)
  return rv

def jarNamesToPaths(names):
  return [os.path.join(buildRoot, "jars", name + ".jar") for name in names]

def jingJarPath():
  return [os.path.join("jing-trang", "build", "jing.jar"),]

def runJavac(sourceDir, classDir, classPath, subtrees):
  ensureDirExists(classDir)
  sourceFiles = findFilesWithExtension(sourceDir, "java", subtrees)
  f = open("temp-javac-list", "w")
  if os.name == 'nt':
    f.write("\r\n".join(sourceFiles))
  else:
    f.write("\n".join(sourceFiles))
  f.close()
  args = [
    '-g',
    '-nowarn',
    '-classpath "%s"' % classPath,
    '-sourcepath "%s"' % sourceDir,
    '-d "%s"' % classDir,
    '-encoding UTF-8',
  ]
  if javaVersion != "":
    args.append('-target ' + javaVersion)
    args.append('-source ' + javaVersion)
  if runCmd('"%s" %s %s' % (javacCmd, " ".join(args), '@temp-javac-list')):
    sys.exit(1)
  removeIfExists("temp-javac-list")

def copyFiles(sourceDir, classDir, subtrees):
  files = findFiles(sourceDir, subtrees)
  for f in files:
    destFile = os.path.join(classDir, f[len(sourceDir)+1:])
    head, tail = os.path.split(destFile)
    if not os.path.exists(head):
      os.makedirs(head)
    shutil.copyfile(f, destFile)

def runJar(classDir, jarFile, sourceDir, subtrees):
  classFiles = []
  if "html5-datatypes" in jarFile:
    shutil.copytree(os.path.join(buildRoot, "src", "META-INF"), os.path.join(classDir, "META-INF"))
    classFiles.append(os.path.join(classDir, "META-INF", "services", "org.relaxng.datatype.DatatypeLibraryFactory"))
  for file in findFiles(classDir, subtrees):
    classFiles.append(file)
  classList = ["-C " + classDir + " " + x[len(classDir)+1:] + ""
               for x in
               classFiles]
  f = open("temp-jar-list", "w")
  if os.name == 'nt':
    f.write("\r\n".join(classList))
  else:
    f.write("\n".join(classList))
  f.close()
  runCmd('"%s" cf "%s" %s'
    % (jarCmd, jarFile, "@temp-jar-list"))
  removeIfExists("temp-jar-list")

def buildModule(rootDir, jarName, classPath, subtrees=None):
  subtrees = subtrees if subtrees else [rootDir]
  sourceDir = os.path.join(rootDir, "src")
  classDir = os.path.join(rootDir, "classes")
  distDir = os.path.join(rootDir, "dist")
  jarFile = os.path.join(distDir, jarName + ".jar")
  removeIfExists(jarFile)
  removeIfDirExists(classDir)
  ensureDirExists(classDir)
  ensureDirExists(distDir)
  runJavac(sourceDir, classDir, classPath, subtrees)
  copyFiles(sourceDir, classDir, subtrees)
  runJar(classDir, jarFile, sourceDir, subtrees)
  ensureDirExists(os.path.join(buildRoot, "jars"))
  shutil.copyfile(jarFile, os.path.join(buildRoot, "jars", jarName + ".jar"))

def dependencyJarPaths(depList=dependencyJars):
  dependencyDir = os.path.join(buildRoot, "dependencies")
  extrasDir = os.path.join(buildRoot, "extras")
  # XXX may need work for Windows portability
  pathList = [os.path.join(dependencyDir, dep) for dep in depList]
  ensureDirExists(extrasDir)
  pathList += findFilesWithExtension(extrasDir, "jar")
  return pathList

def buildDatatypeLibrary():
  classPath = os.pathsep.join(dependencyJarPaths()
                              + jingJarPath())
  buildModule(
    buildRoot,
    "html5-datatypes",
    classPath,
    [os.path.join("org", "whattf", "datatype"), os.path.join("org", "whattf", "io")])

def buildNonSchema():
  classPath = os.pathsep.join(dependencyJarPaths()
                              + jarNamesToPaths(["html5-datatypes",])
                              + jingJarPath())
  buildModule(
    buildRoot,
    "non-schema",
    classPath,
    [os.path.join("org", "whattf", "checker")])

def buildSchemaDrivers():
  baseDir = os.path.join(buildRoot, "schema")
  html5Dir = os.path.join(baseDir, "html5")
  driversDir = os.path.join(baseDir, ".drivers")
  legacyRnc = os.path.join(driversDir, "legacy.rnc")
  itsRnc = os.path.join(os.path.join(baseDir, "its2/its20-html5.rnc"))
  itsTypesRnc = os.path.join(os.path.join(baseDir, "its2/its20-html5-types.rnc"))
  buildSchemaDriverHtmlCore(html5Dir)
  buildSchemaDriverHtml5NoMicrodata(html5Dir)
  buildSchemaDriverHtml5(html5Dir)
  buildSchemaDriverHtml5RDFa(html5Dir)
  buildSchemaDriverHtml5RDFaLite(html5Dir)
  buildSchemaDriverXhtmlCore(html5Dir)
  buildSchemaDriverXhtmlCorePlusWf2(html5Dir)
  buildSchemaDriverXhtml5xhtmlNoMicrodata(html5Dir)
  buildSchemaDriverXhtml5htmlNoMicrodata(html5Dir)
  buildSchemaDriverXhtml5html(html5Dir)
  buildSchemaDriverXhtml5xhtml(html5Dir)
  buildSchemaDriverXhtml5xhtmlRDFa(html5Dir)
  buildSchemaDriverXhtml5xhtmlRDFaLite(html5Dir)
  buildSchemaDriverXhtml5htmlRDFaLite(html5Dir)
  for file in coreSchemaDriverFiles:
    print "Copying %s to %s" % (os.path.join(driversDir, file), os.path.join(baseDir, file))
    shutil.copy(os.path.join(driversDir, file), baseDir)
  xhtmlSourceDir = os.path.join(driversDir, "xhtml10")
  xhtmlTargetDir = os.path.join(baseDir, "xhtml10")
  removeIfDirExists(xhtmlTargetDir)
  shutil.copytree(xhtmlSourceDir, xhtmlTargetDir)
  print "Copying %s to %s" % (xhtmlSourceDir, xhtmlTargetDir)
  rdfDir = os.path.join(baseDir, "rdf")
  removeIfDirExists(rdfDir)
  os.mkdir(rdfDir)
  print "Copying %s to %s/rdf.rnc" % (os.path.join(driversDir, "rdf.rnc"), rdfDir)
  shutil.copy(os.path.join(driversDir, "rdf.rnc"), rdfDir)
  removeIfExists(os.path.join(html5Dir, "legacy.rnc"))
  removeIfExists(os.path.join(html5Dir, "its20-html5.rnc"))
  removeIfExists(os.path.join(html5Dir, "its20-html5-types.rnc"))
  shutil.copy(legacyRnc, html5Dir)
  shutil.copy(itsRnc, html5Dir)
  shutil.copy(itsTypesRnc, html5Dir)

#################################################################
# data and functions for building schema drivers
#################################################################

coreSchemaDriverFiles = [
  "html5-all.rnc",
  "html5-its.rnc",
  "html5-no-microdata.rnc",
  "html5-rdfalite-w3c.rnc",
  "html5-rdfalite.rnc",
  "html5-svg-mathml.rnc",
  "html5.rnc",
  "svg-xhtml5-rdf-mathml.rnc",
  "xhtml1-ruby-rdf-svg-mathml.rnc",
  "xhtml5-all.rnc",
  "xhtml5-no-microdata.rnc",
  "xhtml5-rdfalite-w3c.rnc",
  "xhtml5-rdfalite.rnc",
  "xhtml5-svg-mathml.rnc",
  "xhtml5.rnc"
]

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
  f = open(os.path.join(schemaDir, driverName), "a")
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

def buildEmitters():
  compilerFile = os.path.join(buildRoot, "src", "nu", "validator", "xml", "SaxCompiler.java")
  compilerClass = "nu.validator.xml.SaxCompiler"
  classDir = os.path.join(buildRoot, "classes")
  ensureDirExists(classDir)
  args = [
    '-g',
    '-nowarn',
    '-d "%s"' % classDir,
    '-encoding UTF-8',
  ]
  if javaVersion != "":
    args.append('-target ' + javaVersion)
    args.append('-source ' + javaVersion)
  if runCmd('"%s" %s %s' % (javacCmd, " ".join(args), compilerFile)):
    sys.exit(1)
  pageEmitter = os.path.join("src", "nu", "validator", "servlet", "PageEmitter.java")
  formEmitter = os.path.join("src", "nu", "validator", "servlet", "FormEmitter.java")
  if runCmd('"%s" -cp %s %s %s %s' % (javaCmd, classDir, compilerClass, pageTemplate, pageEmitter)):
    sys.exit(1)
  if runCmd('"%s" -cp %s %s %s %s' % (javaCmd, classDir, compilerClass, formTemplate, formEmitter)):
    sys.exit(1)

def buildValidator():
  classPath = os.pathsep.join(dependencyJarPaths()
                              + jarNamesToPaths(["non-schema",
                                                "htmlparser",
                                                "html5-datatypes"])
                              + jingJarPath())
  buildEmitters();
  buildModule(
    buildRoot,
    "validator",
    classPath,
    [os.path.join("nu", "validator")])

def ownJarList():
  return jarNamesToPaths(["non-schema",
                          "htmlparser",
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

  if stackSize != "":
    args.append('-Xss' + stackSize + 'k')
    args.append('-XX:ThreadStackSize=' + stackSize + 'k')

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
  f.write(" ".join([javaCmd,] + [("'{0}'".format(x) if " " in x else x) for x in args]))
  if controlPort:
    f.write(" <&- 1>/dev/null 2>&1 &")
  f.write("\n")
  f.close()

def runValidator():
  ensureDirExists(os.path.join(buildRoot, "logs"))
  args = getRunArgs(str(int(heapSize) * 1024))
  execCmd(javaCmd, args)

def checkService():
  doc= miniDoc.replace(" ", "%20")
  query = "?out=gnu&doc=data:text/html;charset=utf-8,%s" % doc
  url = "http://localhost:%s/%s" % (portNumber, query)
  args = getRunArgs(str(int(heapSize) * 1024))
  daemon = subprocess.Popen([javaCmd,] + args)
  time.sleep(15)
  print "Checking %s" % url
  try:
    print urllib2.urlopen(url).read()
  except urllib2.HTTPError as e:
    print e.reason
    sys.exit(1)
  except urllib2.URLError as e:
    print e.reason
    sys.exit(1)
  time.sleep(5)
  daemon.terminate()

def createDistZip(distType):
  removeIfDirExists(vnuDir)
  os.mkdir(vnuDir)
  print "Waiting for version number on stdin..."
  version = sys.stdin.read().rstrip()
  print "Building vnu-%s.%s" % (version, distType)
  f = open(os.path.join(vnuDir, "VERSION"), "w")
  f.write(version)
  f.close()
  if distType == "war":
    os.mkdir(os.path.join(vnuDir, "war"))
  antRoot = os.path.join(buildRoot, "jing-trang", "lib")
  antJar = os.path.join(antRoot, "ant.jar")
  antLauncherJar = os.path.join(antRoot, "ant-launcher.jar")
  classPath = os.pathsep.join([antJar, antLauncherJar])
  runCmd('"%s" -cp %s org.apache.tools.ant.Main -f %s %s'
    % (javaCmd, classPath, os.path.join(buildRoot, "build", "build.xml"), distType))
  shutil.copy(os.path.join(buildRoot, "index.html"), vnuDir)
  shutil.copy(os.path.join(buildRoot, "README.md"), vnuDir)
  shutil.copy(os.path.join(buildRoot, "CHANGELOG.md"), vnuDir)
  shutil.copy(os.path.join(buildRoot, "LICENSE"), vnuDir)
  os.chdir("build")
  distroFile = os.path.join("vnu-%s.%s.zip" % (version, distType))
  removeIfExists(distroFile)
  zf = zipfile.ZipFile(distroFile, "w")
  for dirname, subdirs, files in os.walk("vnu"):
    zf.write(dirname)
    for filename in files:
      zf.write(os.path.join(dirname, filename))
  zf.close()
  os.chdir("..")
  if distType == "jar":
    checkJar()

def checkJar():
  vnu = os.path.join(vnuDir, "vnu.jar")
  formats = ["gnu", "xml", "json", "text"]
  for _format in formats:
    if runCmd('echo \'%s\' | "%s" -jar %s --format %s -' % (miniDoc, javaCmd, vnu, _format)):
      sys.exit(1)
  # to also make sure it works even w/o --format value given; returns gnu output
  if runCmd('echo \'%s\' | "%s" -jar %s -' % (miniDoc, javaCmd, vnu)):
    sys.exit(1)
  if runCmd('"%s" -jar %s --version' % (javaCmd, vnu)):
    sys.exit(1)

def createTarball():
  args = [
    "zcf",
    os.path.join(buildRoot, "jars.tar.gz"),
    os.path.join(buildRoot, "run-validator.sh"),
    os.path.join(buildRoot, "site", "style.css"),
    os.path.join(buildRoot, "site", "script.js"),
    os.path.join(buildRoot, "site", "icon.png"),
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
  antJar = os.path.join(antRoot, "ant.jar")
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
  removeIfDirExists(os.path.join(buildRoot, "local-entities", "www.iana.org"))
  removeIfDirExists(os.path.join(buildRoot, "local-entities", "wiki.whatwg.org"))
  fetchUrlTo("https://www.iana.org/assignments/language-subtag-registry/language-subtag-registry", os.path.join(buildRoot, subtagRegistry))
  fetchUrlTo("https://wiki.whatwg.org/wiki/MicrosyntaxDescriptions", os.path.join(buildRoot, syntaxDescriptions))
  fetchUrlTo("https://wiki.whatwg.org/wiki/Validator.nu_alt_advice", os.path.join(buildRoot, vnuAltAdvice))
  fetchUrlTo("https://help.whatwg.org/extensions/meta-name/", os.path.join(buildRoot, metaNameExtensions))
  fetchUrlTo("https://help.whatwg.org/extensions/link-rel/", os.path.join(buildRoot, linkRelExtensions))
  fetchUrlTo("https://help.whatwg.org/extensions/a-rel/", os.path.join(buildRoot, aRelExtensions))
  dtds = os.path.join(buildRoot, "local-entities", "www.w3.org")
  removeIfDirExists(dtds)
  shutil.copytree(os.path.join(buildRoot, "dependencies", "www.w3.org"), dtds)

def localPathToJarCompatName(path):
  return javaSafeNamePat.sub('_', path)

def preparePropertiesFile():
  filesDir = os.path.join(buildRoot, "src", "nu", "validator", "localentities", "files")
  f = open(os.path.join(filesDir, "misc.properties"), 'w')
  f.write("nu.validator.servlet.service-name=%s\n" % serviceName)
  f.write("nu.validator.servlet.results-title=%s\n" % resultsTitle)
  f.close()

def prepareLocalEntityJar():
  filesDir = os.path.join(buildRoot, "src", "nu", "validator", "localentities", "files")
  if os.path.exists(filesDir):
    shutil.rmtree(filesDir)
  os.makedirs(filesDir)
  preparePropertiesFile()
  createCssParserJS(filesDir)
  shutil.copyfile(os.path.join(buildRoot, subtagRegistry), os.path.join(filesDir, "subtag-registry"))
  shutil.copyfile(os.path.join(buildRoot, syntaxDescriptions), os.path.join(filesDir, "syntax-descriptions"))
  shutil.copyfile(os.path.join(buildRoot, vnuAltAdvice), os.path.join(filesDir, "vnu-alt-advice"))
  shutil.copyfile(os.path.join(buildRoot, metaNameExtensions), os.path.join(filesDir, "meta-name-extensions"))
  shutil.copyfile(os.path.join(buildRoot, linkRelExtensions), os.path.join(filesDir, "link-rel-extensions"))
  shutil.copyfile(os.path.join(buildRoot, aRelExtensions), os.path.join(filesDir, "a-rel-extensions"))
  shutil.copyfile(os.path.join(buildRoot, presetsFile), os.path.join(filesDir, "presets"))
  shutil.copyfile(os.path.join(buildRoot, aboutFile), os.path.join(filesDir, "about.html"))
  shutil.copyfile(os.path.join(buildRoot, stylesheetFile), os.path.join(filesDir, "style.css"))
  shutil.copyfile(os.path.join(buildRoot, scriptFile), os.path.join(filesDir, "script.js"))
  shutil.copyfile(os.path.join(buildRoot, "site", "icon.png"), os.path.join(filesDir, "icon.png"))
  shutil.copyfile(os.path.join(buildRoot, "resources", "spec", "html5.html"), os.path.join(filesDir, "html5spec"))
  if followW3Cspec:
    shutil.copyfile(os.path.join(buildRoot, "resources", "spec", "w3c-html5.html"), os.path.join(filesDir, "html5spec"))
  shutil.copyfile(os.path.join(buildRoot, "resources", "log4j.properties"), os.path.join(filesDir, "log4j.properties"))
  shutil.copyfile(os.path.join(buildRoot, "README.md"), os.path.join(filesDir, "cli-help"))
  f = open(os.path.join(buildRoot, "resources", "entity-map.txt"))
  o = open(os.path.join(filesDir, "entitymap"), 'wb')
  try:
    for line in f:
      url, path = line.strip().split("\t")
      entPath = None
      if path.startswith("schema/html5/"):
        entPath = os.path.join(buildRoot, "schema", "html5", path[13:])
      elif path.startswith("schema/"):
        entPath = os.path.join(buildRoot, path)
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
  for file in coreSchemaDriverFiles:
    removeIfExists(os.path.join(buildRoot, "schema", file))
  removeIfDirExists(os.path.join(buildRoot, "schema", "xhtml10"))
  removeIfDirExists(os.path.join(buildRoot, "schema", "rdf"))

def createCssParserJS(filesDir):
  p = open(os.path.join(buildRoot, "dependencies", "parse-css.js"), 'r')
  j = open(os.path.join(buildRoot, "dependencies", "json.js"), 'r')
  o = open(os.path.join(filesDir, "parse-css-js"), 'wb')
  shutil.copyfileobj(p, o)
  shutil.copyfileobj(j, o)
  p.close()
  j.close()
  consoleLogForRhino = '''\
  var console = {
    log: function (msg) {
      throw msg;
      return true;
    }
  }
  '''
  o.write(consoleLogForRhino)
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
    filepath = os.path.join(targetDir, name)
    # is this portable to Windows?
    if not name.endswith('/'):
      head, tail = os.path.split(filepath)
      ensureDirExists(head)
      o = open(filepath, 'wb')
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

def updateSubmodules():
  runCmd('"%s" submodule update --init' % gitCmd)

def downloadDependencies():
  for url, md5sum in dependencyPackages:
    downloadDependency(url, md5sum)

def buildAll():
  if 'JAVA_HOME' not in os.environ:
    print "Error: The JAVA_HOME environment variable is not set."
    print "Set the JAVA_HOME environment variable to the pathname of the directory where your JDK is installed."
    sys.exit(1)
  buildJing()
  buildDatatypeLibrary()
  buildNonSchema()
  buildSchemaDrivers()
  prepareLocalEntityJar()
  buildHtmlParser()
  buildValidator()

def runTests():
  if followW3Cspec:
    args = "--ignore=hgroup tests/messages.json"
  else:
    args = "--ignore=html-its tests/messages.json"
  className = "nu.validator.client.TestRunner"
  classPath = os.pathsep.join(dependencyJarPaths()
                              + jarNamesToPaths(["non-schema",
                                                "htmlparser",
                                                "html5-datatypes",
                                                "validator"])
                              + jingJarPath())
  if runCmd('"%s" -classpath %s %s %s' % (javaCmd, classPath, className, args)):
    sys.exit(1)

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
  print "  --heap=64                  -- Sets the Java heap size in MB"
  print "  --stacksize=NN             -- Sets the Java thread stack size in KB"
  print "  --javaversion=N.N          -- Sets the Java VM version to build for"
  print "  --name=Validator.nu        -- Sets the service name"
  print "  --html5link=http://www.whatwg.org/specs/web-apps/current-work/"
  print "                                Sets the link URL of the HTML5 spec"
  print "  --html5load=http://www.whatwg.org/specs/web-apps/current-work/"
  print "                                Sets the load URL of the HTML5 spec"
  print "  --about=https://about.validator.nu/"
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
  print "  update   -- Update git submodules"
  print "  dldeps   -- Download missing dependency libraries and entities"
  print "  build    -- Build the source"
  print "  test     -- Run regression tests"
  print "  check    -- Perform self-test of the system"
  print "  run      -- Run the system"
  print "  all      -- update dldeps build test run"
  print "  jar      -- Create a JAR file containing a release distribution"
  print "  war      -- Create a WAR file containing a release distribution"
  print "  checkjar -- Run tests with the build jar file"
#  print "  script   -- Make run-validator.sh script for starting the system"
  print "  script   -- Make run-validator.sh script for running the system"

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
    elif arg.startswith("--port="):
      portNumber = arg[7:]
    elif arg.startswith("--control-port="):
      controlPort = arg[15:]
    elif arg.startswith("--log4j="):
      log4jProps = arg[8:]
    elif arg.startswith("--heap="):
      heapSize = arg[7:]
    elif arg.startswith("--stacksize="):
      stackSize = arg[12:]
    elif arg.startswith("--javaversion="):
      javaVersion = arg[14:]
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
      pageTemplate = arg[16:]
    elif arg.startswith("--form-template="):
      formTemplate = arg[16:]
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
      pass
    elif arg == '--local':
      pass
    elif arg.startswith("--connection-timeout="):
      connectionTimeoutSeconds = int(arg[21:])
    elif arg.startswith("--socket-timeout="):
      socketTimeoutSeconds = int(arg[17:])
    elif arg == '--follow-w3c-spec':
      followW3Cspec = 1
    elif arg == '--statistics':
      statistics = 1
    elif arg == '--help':
      printHelp()
    elif arg == 'update':
      updateSubmodules()
    elif arg == 'dldeps':
      downloadDependencies()
      downloadLocalEntities()
    elif arg == 'checkout':
      pass
    elif arg == 'build':
      buildAll()
    elif arg == 'jar':
      createDistZip('jar')
    elif arg == 'checkjar':
      checkJar();
    elif arg == 'war':
      createDistZip('war')
    elif arg == 'localent':
      prepareLocalEntityJar()
    elif arg == 'deploy':
      deployOverScp()
    elif arg == 'tar':
      createTarball()
      createDepTarball()
    elif arg == 'script':
      if not stylesheet:
        stylesheet = 'style.css'
      if not script:
        script = 'script.js'
      if not icon:
        icon = 'icon.png'
      generateRunScript()
    elif arg == 'test':
      runTests()
    elif arg == 'check':
      if not stylesheet:
        stylesheet = 'style.css'
      if not script:
        script = 'script.js'
      if not icon:
        icon = 'icon.png'
      checkService()
    elif arg == 'run':
      if not stylesheet:
        stylesheet = 'style.css'
      if not script:
        script = 'script.js'
      if not icon:
        icon = 'icon.png'
      runValidator()
    elif arg == 'all':
      updateSubmodules()
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
      print "Unknown option %s." % arg
