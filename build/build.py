#!/usr/bin/env python
# -*- coding: utf-8 -*- vim: set fileencoding=utf-8 :

# Copyright (c) 2007 Henri Sivonen
# Copyright (c) 2008-2018 Mozilla Foundation
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
try:
    from urllib.request import urlopen
    from urllib.error import URLError, HTTPError
    from http.client import BadStatusLine
except ImportError:
    from urllib2 import urlopen, URLError, HTTPError
    from httplib import BadStatusLine
import socket
import re
try:
    from hashlib import md5
except ImportError:
    from md5 import new as md5
try:
    from hashlib import sha1
except ImportError:
    from sha1 import new as sha1
import zipfile
import sys
import platform
try:
    from html.parser import HTMLParser
except ImportError:
    from HTMLParser import HTMLParser
import subprocess
from ssl import SSLError
import time
# Use newer https certifications from certifi package if available
try:
    import certifi
    CAFILE = certifi.where()
except ImportError:
    CAFILE = None

javaTargetVersion = '1.8'
JAVA_HOME = os.getenv('JAVA_HOME')
javacCmd = os.path.join(JAVA_HOME, 'bin', 'javac')
jarCmd = os.path.join(JAVA_HOME, 'bin', 'jar')
javaCmd = os.path.join(JAVA_HOME, 'bin', 'java')
jdepsCmd = os.path.join(JAVA_HOME, 'bin', 'jdeps')
jlinkCmd = os.path.join(JAVA_HOME, 'bin', 'jlink')
javadocCmd = os.path.join(JAVA_HOME, 'bin', 'javadoc')
herokuCmd = 'heroku'
ghRelCmd = 'github-release'  # https://github.com/sideshowbarker/github-release
tarCmd = 'tar'
scpCmd = 'scp'
gitCmd = 'git'
mvnCmd = 'mvn'
gpgCmd = 'gpg'
npmCmd = 'npm'

try:
    javaRawVersion = subprocess.check_output([javaCmd, '-version'],
                                             universal_newlines=True,
                                             stderr=subprocess.STDOUT)
except TypeError:
    javaRawVersion = subprocess.check_output([javaCmd, '-version'],
                                             stderr=subprocess.STDOUT)
javaEnvVersion = int(javaRawVersion
                     .splitlines()[0].split()[2].strip('"').split('.')[0])

snapshotsRepoUrl = 'https://oss.sonatype.org/content/repositories/snapshots/'
stagingRepoUrl = 'https://oss.sonatype.org/service/local/staging/deploy/maven2/'
# in your ~/.ssh/config, you'll need to define a host named "releasesHost"
releasesHost = "releasesHost"
nightliesPath = "/var/www/nightlies"
releasesPath = "/var/www/releases"

releaseDate = time.strftime('%d %B %Y')
year = time.strftime('%y')
month = time.strftime('%m').lstrip('0')
day = time.strftime('%d').lstrip('0')
validatorVersion = "%s.%s.%s" % (year, month, day)
# validatorVersion = "18.11.5"
jingVersion = "20180722VNU"
htmlparserVersion = "1.4.12"
cssvalidatorVersion = "1.0.4"
galimatiasVersion = "0.1.3"
langdetectVersion = "1.2"

buildRoot = '.'
distDir = os.path.join(buildRoot, "build", "dist")
distWarDir = os.path.join(buildRoot, "build", "dist-war")
dependencyDir = os.path.join(buildRoot, "dependencies")
jarsDir = os.path.join(buildRoot, "jars")
jingTrangDir = os.path.join(buildRoot, "jing-trang")
cssValidatorDir = os.path.join(buildRoot, "css-validator")
vnuSrc = os.path.join(buildRoot, "src", "nu", "validator")
filesDir = os.path.join(vnuSrc, "localentities", "files")
antRoot = os.path.join(jingTrangDir, "lib")
antJar = os.path.join(antRoot, "ant.jar")
antLauncherJar = os.path.join(antRoot, "ant-launcher.jar")

pageTemplate = os.path.join("site", "PageEmitter.xml")
formTemplate = os.path.join("site", "FormEmitter.xml")
presetsFile = os.path.join("resources", "presets.txt")
aboutFile = os.path.join("site", "about.html")
stylesheetFile = os.path.join("site", "style.css")
scriptFile = os.path.join("site", "script.js")
filterFile = os.path.join("resources", "message-filters.txt")

portNumber = '8888'
controlPort = None
log4jProps = 'resources/log4j.properties'
heapSize = '512'
stackSize = ''
html5specLink = 'https://html.spec.whatwg.org/multipage/'
aboutPage = 'https://about.validator.nu/'
denyList = ''
userAgent = 'Validator.nu/LV'
icon = None
stylesheet = None
script = None
scriptAdditional = ''
serviceName = 'Validator.nu'
resultsTitle = 'Validation results'
messagesLimit = 1000
maxFileSize = 11264
disablePromiscuousSsl = 0
genericHost = ''
html5Host = ''
parsetreeHost = ''
genericPath = '/'
html5Path = '/html5/'
parsetreePath = '/parsetree/'
deploymentTarget = None
httpTimeoutSeconds = 120
connectionTimeoutSeconds = 5
socketTimeoutSeconds = 5
maxConnPerRoute = 100
maxTotalConnections = 200
maxRedirects = 20  # Gecko default
statistics = 0
miniDoc = '<!doctype html><html lang=""><meta charset=utf-8><title>test</title>'

dependencyPackages = [
    ("https://repo1.maven.org/maven2/com/ibm/icu/icu4j/58.2/icu4j-58.2.jar", "605d8a0276a280ff6332c3bd26071180"),  # nopep8
    ("https://repo1.maven.org/maven2/com/shapesecurity/salvation/2.6.0/salvation-2.6.0.jar", "ee5bddf2f9b8ad5b0d5caba86308c178"),  # nopep8
    ("https://repo1.maven.org/maven2/commons-codec/commons-codec/1.10/commons-codec-1.10.jar", "353cf6a2bdba09595ccfa073b78c7fcb"),  # nopep8
    ("https://repo1.maven.org/maven2/commons-fileupload/commons-fileupload/1.3.1/commons-fileupload-1.3.1.jar", "ed8eec445e21ec7e49b86bf3cbcffcbc"),  # nopep8
    ("https://repo1.maven.org/maven2/commons-io/commons-io/2.4/commons-io-2.4.jar", "7f97854dc04c119d461fed14f5d8bb96"),  # nopep8
    ("https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar", "040b4b4d8eac886f6b4a2a3bd2f31b00"),  # nopep8
    ("https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2-adapters.jar", "5c82e86cc5b769f72abd2af1f92255fa"),  # nopep8
    ("https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2-api.jar", "289dcb376743ab24ecaeb194a0d287d9"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/3.1.0/javax.servlet-api-3.1.0.jar", "79de69e9f5ed8c7fcb8342585732bbf7"),  # nopep8
    ("https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar", "04a41f0a068986f0f73485cf507c0f40"),  # nopep8
    ("https://repo1.maven.org/maven2/log4j/apache-log4j-extras/1.2.17/apache-log4j-extras-1.2.17.jar", "f32ed7ae770c83a4ac6fe6714f98f1bd"),  # nopep8
    ("https://repo1.maven.org/maven2/net/sourceforge/jchardet/jchardet/1.0/jchardet-1.0.jar", "90c63f0e53e6f714dbc7641e066620e4"),  # nopep8
    ("https://repo1.maven.org/maven2/org/apache/httpcomponents/httpclient/4.4/httpclient-4.4.jar", "ccf9833ec0cbd38831ceeb8fc246e2dd"),  # nopep8
    ("https://repo1.maven.org/maven2/org/apache/httpcomponents/httpcore/4.4/httpcore-4.4.jar", "e016cf1346ba3f65302c3d71c5b91f44"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-http/9.2.25.v20180606/jetty-http-9.2.25.v20180606.jar", "d65032c93330cc33f92ab3e9a74690bb"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-io/9.2.25.v20180606/jetty-io-9.2.25.v20180606.jar", "c0596d22696d73ee5919e82253596797"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-security/9.2.25.v20180606/jetty-security-9.2.25.v20180606.jar", "04d4d6f08166bffee9fba032b4976cef"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-server/9.2.25.v20180606/jetty-server-9.2.25.v20180606.jar", "fe8be4dcd7402177601bc5cb904f9014"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlet/9.2.25.v20180606/jetty-servlet-9.2.25.v20180606.jar", "14f656b4359dd7a945c41dcd1c80b281"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-servlets/9.2.25.v20180606/jetty-servlets-9.2.25.v20180606.jar", "e1885683a65213deb62eff2dddf5f2dd"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util/9.2.25.v20180606/jetty-util-9.2.25.v20180606.jar", "40e16d5b86902bd7c3a440e2c3eac041"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-util-ajax/9.2.25.v20180606/jetty-util-ajax-9.2.25.v20180606.jar", "84eca54fd6b260f3ca2d87bbb0cdd4f9"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/mail/mail/1.4.7/mail-1.4.7.jar", "77f53ff0c78ba43c4812ecc9f53e20f8"),  # nopep8
]

moduleDependencyPackages = [
    ("https://repo1.maven.org/maven2/com/sun/activation/javax.activation/1.2.0/javax.activation-1.2.0.jar", "be7c430df50b330cffc4848a3abedbfb"),  # nopep8
    ("https://repo1.maven.org/maven2/org/apache/avalon/framework/avalon-framework-api/4.3.1/avalon-framework-api-4.3.1.jar", "7c543869a7eb2bad323a54e873973acf"),  # nopep8
    ("https://repo1.maven.org/maven2/org/apache/avalon/logkit/avalon-logkit/2.2.1/avalon-logkit-2.2.1.jar", "1cff819c8516bbe070530f3a8d801f2e"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/enterprise/cdi-api/2.0/cdi-api-2.0.jar", "a7768f3b33cd35f63d9cdcedfd537500"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/el/el-api/2.2/el-api-2.2.jar", "900b2de76d7c98f8dcbb43684c823113"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/activation/javax.activation-api/1.2.0/javax.activation-api-1.2.0.jar", "5e50e56bcf4a3ef3bc758f69f7643c3b"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/inject/javax.inject/1/javax.inject-1.jar", "289075e48b909e9e74e6c915b3631d2e"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/interceptor/javax.interceptor-api/1.2.2/javax.interceptor-api-1.2.2.jar", "d46f2dac1607941fbfb81eb7bc83157a"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/jms/javax.jms-api/2.0.1/javax.jms-api-2.0.1.jar", "d69d2e02910e97b2478c0105e9b2caab"),  # nopep8
    ("https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-jmx/9.2.25.v20180606/jetty-jmx-9.2.25.v20180606.jar", "ee698f4b87da059db0d71ca3737eca17"),  # nopep8
    ("https://repo1.maven.org/maven2/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar", "dd83accb899363c32b07d7a1b2e4ce40"),  # nopep8
    ("https://repo1.maven.org/maven2/javax/portlet/portlet-api/3.0.1/portlet-api-3.0.1.jar", "ff22c9434e12a87b6023e301600b6c44"),  # nopep8
    ("https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.26/slf4j-api-1.7.26.jar", "60ec8751be37d54a2aa1b6178f87b968"),  # nopep8
    ("https://repo1.maven.org/maven2/org/slf4j/slf4j-log4j12/1.7.9/slf4j-log4j12-1.7.9.jar", "54528e0b3ea7656072a9b591248c6457"),  # nopep8
]

runDependencyJars = [
    "apache-log4j-extras-1.2.17.jar",
    "commons-codec-1.10.jar",
    "commons-fileupload-1.3.1.jar",
    "commons-io-2.4.jar",
    "commons-logging-1.2.jar",
    "commons-logging-1.2-adapters.jar",
    "commons-logging-1.2-api.jar",
    "httpcore-4.4.jar",
    "httpclient-4.4.jar",
    "icu4j-58.2.jar",
    "salvation-2.6.0.jar",
    "javax.servlet-api-3.1.0.jar",
    "jchardet-1.0.jar",
    "jetty-http-9.2.25.v20180606.jar",
    "jetty-io-9.2.25.v20180606.jar",
    "jetty-security-9.2.25.v20180606.jar",
    "jetty-server-9.2.25.v20180606.jar",
    "jetty-servlet-9.2.25.v20180606.jar",
    "jetty-servlets-9.2.25.v20180606.jar",
    "jetty-util-9.2.25.v20180606.jar",
    "jetty-util-ajax-9.2.25.v20180606.jar",
    "log4j-1.2.17.jar",
    "mail-1.4.7.jar",
]

dependencyJars = runDependencyJars

javaSafeNamePat = re.compile(r'[^a-zA-Z0-9]')
directoryPat = re.compile(r'^[a-zA-Z0-9_-]+/$')
leafPat = re.compile(r'^[a-zA-Z0-9_-]+\.[a-z]+$')


class UrlExtractor(HTMLParser):

    def __init__(self, baseUrl):
        HTMLParser.__init__(self)
        self.baseUrl = baseUrl
        self.leaves = []
        self.directories = []

    def handle_starttag(self, tag, attrs):
        if tag == "a":
            print(attrs)
            for name, value in attrs:
                if name == "href":
                    if directoryPat.match(value):
                        self.directories.append(self.baseUrl + value)
                    if leafPat.match(value):
                        self.leaves.append(self.baseUrl + value)


def runCmd(cmd):
    print(" ".join(cmd))
    return subprocess.call(cmd)


def execCmd(cmd, args):
    print("%s %s" % (cmd, " ".join(args)))
    if subprocess.call([cmd, ] + args):
        print("Command failed.")
        sys.exit(2)


def runShell(shellCmd):
    print(shellCmd)
    return subprocess.call(shellCmd, shell=True)


def removeIfExists(filePath):
    if os.path.exists(filePath):
        print("Removing %s" % filePath)
        os.unlink(filePath)


def removeIfDirExists(dirPath):
    if os.path.exists(dirPath):
        print("Removing %s" % dirPath)
        shutil.rmtree(dirPath)


def ensureDirExists(dirPath):
    if not os.path.exists(dirPath):
        os.makedirs(dirPath)


def findFilesWithExtension(directory, extension):
    rv = []
    ext = '.' + extension
    for root, dirs, files in os.walk(directory):
        for filename in files:
            if filename.endswith(ext):
                rv.append(os.path.join(root, filename))
    return rv


def findFiles(directory):
    rv = []
    for root, dirs, files in os.walk(directory):
        for filename in files:
            candidate = os.path.join(root, filename)
            if candidate.find("/.svn") == -1:
                rv.append(candidate)
    return rv


def jarNamesToPaths(names):
    return [os.path.join(jarsDir, name + ".jar") for name in names]


def jingJarPath():
    return [os.path.join(buildRoot, "jing-trang", "build", "jing.jar"), ]


def cssValidatorJarPath():
    return [os.path.join(buildRoot, "css-validator", "css-validator.jar"), ]


def runJavac(sourceDir, classDir, classPath):
    ensureDirExists(classDir)
    sourceFiles = findFilesWithExtension(sourceDir, "java")
    f = open("temp-javac-list", "w")
    f.write("\n".join(sourceFiles))
    f.close()
    args = [
        javacCmd,
        '-g',
        '-nowarn',
        '-classpath',
        classPath,
        '-sourcepath',
        sourceDir,
        '-d',
        classDir,
        '-encoding',
        'UTF-8',
    ]
    if javaTargetVersion != "":
        args.append('-target')
        args.append(javaTargetVersion)
        args.append('-source')
        args.append(javaTargetVersion)
        args.append('@temp-javac-list')
    if runCmd(args):
        sys.exit(1)
    removeIfExists("temp-javac-list")


def copyFiles(sourceDir, classDir):
    files = findFiles(sourceDir)
    for f in files:
        destFile = os.path.join(classDir, f[len(sourceDir) + 1:])
        head, tail = os.path.split(destFile)
        if not os.path.exists(head):
            os.makedirs(head)
        shutil.copyfile(f, destFile)


def runJar(classDir, jarFile, sourceDir):
    classFiles = []
    for file in findFiles(classDir):
        if file.endswith(".java"):
            continue
        classFiles.append(file)
    classList = ["-C " + classDir + " " + x[len(classDir) + 1:] + "" for x in classFiles]   # nopep8
    f = open("temp-jar-list", "w")
    f.write("\n".join(classList))
    f.close()
    runCmd([jarCmd, 'cf', jarFile, '@temp-jar-list'])
    removeIfExists("temp-jar-list")


def buildModule(rootDir, jarName, classPath):
    sourceDir = os.path.join(rootDir, "src")
    classDir = os.path.join(rootDir, "classes")
    modDistDir = os.path.join(rootDir, "dist")
    jarFile = os.path.join(modDistDir, jarName + ".jar")
    removeIfExists(jarFile)
    removeIfDirExists(classDir)
    ensureDirExists(classDir)
    ensureDirExists(modDistDir)
    runJavac(sourceDir, classDir, classPath)
    copyFiles(sourceDir, classDir)
    runJar(classDir, jarFile, sourceDir)
    ensureDirExists(jarsDir)
    shutil.copyfile(jarFile, os.path.join(jarsDir, jarName + ".jar"))
    removeIfDirExists(classDir)
    removeIfDirExists(modDistDir)


def dependencyJarPaths(depList=dependencyJars):
    extrasDir = os.path.join(buildRoot, "extras")
    pathList = [os.path.join(dependencyDir, dep) for dep in depList]
    for jar in ["saxon9.jar", "isorelax.jar"]:
        pathList += [os.path.join(jingTrangDir, "lib", jar)]
    ensureDirExists(extrasDir)
    pathList += findFilesWithExtension(extrasDir, "jar")
    return pathList


def buildSchemaDrivers():
    baseDir = os.path.join(buildRoot, "schema")
    html5Dir = os.path.join(baseDir, "html5")
    driversDir = os.path.join(baseDir, ".drivers")
    legacyRnc = os.path.join(driversDir, "legacy.rnc")
    itsRnc = os.path.join(os.path.join(baseDir, "its2/its20-html5.rnc"))
    itsTypesRnc = os.path.join(os.path.join(baseDir, "its2/its20-html5-types.rnc"))  # nopep8
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
        print("Copying %s to %s" % (os.path.join(driversDir, file), os.path.join(baseDir, file)))  # nopep8
        shutil.copy(os.path.join(driversDir, file), baseDir)
    xhtmlSourceDir = os.path.join(driversDir, "xhtml10")
    xhtmlTargetDir = os.path.join(baseDir, "xhtml10")
    removeIfDirExists(xhtmlTargetDir)
    shutil.copytree(xhtmlSourceDir, xhtmlTargetDir)
    print("Copying %s to %s" % (xhtmlSourceDir, xhtmlTargetDir))
    rdfDir = os.path.join(baseDir, "rdf")
    removeIfDirExists(rdfDir)
    os.mkdir(rdfDir)
    print("Copying %s to %s/rdf.rnc" % (os.path.join(driversDir, "rdf.rnc"), rdfDir))  # nopep8
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

htmlSchemaDriverFiles = [
    "html5core.rnc",
    "html5full-no-microdata.rnc",
    "html5full-rdfa.rnc",
    "html5full-rdfalite.rnc",
    "html5full.rnc",
    "its20-html5-types.rnc",
    "its20-html5.rnc",
    "legacy.rnc",
    "xhtml5core-plus-web-forms2.rnc",
    "xhtml5core.rnc",
    "xhtml5full-html-no-microdata.rnc",
    "xhtml5full-html-rdfalite.rnc",
    "xhtml5full-html.rnc",
    "xhtml5full-xhtml-no-microdata.rnc",
    "xhtml5full-xhtml-rdfa.rnc",
    "xhtml5full-xhtml-rdfalite.rnc",
    "xhtml5full-xhtml.rnc"
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
            (    head.elem
            ,    (    body.elem
                 |    frameset.elem
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
        shutil.copyfile(
            os.path.join(schemaDir, sourceName),
            os.path.join(schemaDir, driverName))
    f = open(os.path.join(schemaDir, driverName), "a")
    return f


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
    f.write(schemaDriverBase)
    f.write(schemaDriverHtml5NoMicrodata)
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
    f = openDriver(schemaDir, "xhtml5core-plus-web-forms2.rnc", "xhtml5core.rnc")  # nopep8
    f.write(schemaDriverPlusWebForms2)
    f.close()


def buildSchemaDriverXhtml5htmlNoMicrodata(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-html-no-microdata.rnc")
    f.write(schemaDriverNamespace)
    f.write(schemaDriverToggle_Xhtml5html)
    f.write(schemaDriverBase)
    f.write(schemaDriverHtml5NoMicrodata)
    f.close()


def buildSchemaDriverXhtml5xhtmlNoMicrodata(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-xhtml-no-microdata.rnc")
    f.write(schemaDriverNamespace)
    f.write(schemaDriverToggle_Xhtml5xhtml)
    f.write(schemaDriverBase)
    f.write(schemaDriverHtml5NoMicrodata)
    f.close()


def buildSchemaDriverXhtml5xhtml(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-xhtml.rnc", "xhtml5full-xhtml-no-microdata.rnc")  # nopep8
    f.write(schemaDriverHtml5Microdata)
    f.close()


def buildSchemaDriverXhtml5xhtmlRDFa(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-xhtml-rdfa.rnc", "xhtml5full-xhtml.rnc")  # nopep8
    f.write(schemaDriverHtml5RDFa)
    f.close()


def buildSchemaDriverXhtml5xhtmlRDFaLite(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-xhtml-rdfalite.rnc", "xhtml5full-xhtml.rnc")  # nopep8
    f.write(schemaDriverHtml5RDFaLite)
    f.close()


def buildSchemaDriverXhtml5html(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-html.rnc", "xhtml5full-html-no-microdata.rnc")  # nopep8
    f.write(schemaDriverHtml5Microdata)
    f.close()


def buildSchemaDriverXhtml5htmlRDFa(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-html-rdfa.rnc", "xhtml5full-html.rnc")
    f.write(schemaDriverHtml5RDFa)
    f.close()


def buildSchemaDriverXhtml5htmlRDFaLite(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-html-rdfalite.rnc", "xhtml5full-html.rnc")  # nopep8
    f.write(schemaDriverHtml5RDFaLite)
    f.close()

#################################################################
# end of data and functions for building schema drivers
#################################################################


def buildGalimatias():
    classPath = os.pathsep.join(dependencyJarPaths())
    buildModule(os.path.join(buildRoot, "galimatias"), "galimatias", classPath)


def buildHtmlParser():
    classPath = os.pathsep.join(dependencyJarPaths())
    buildModule(os.path.join(buildRoot, "htmlparser"), "htmlparser", classPath)


def buildLangdetect():
    classPath = os.pathsep.join(dependencyJarPaths() +
                                [os.path.join(dependencyDir,
                                              "jetty-util-ajax-9.2.25.v20180606.jar")])  # nopep8
    buildModule(os.path.join(buildRoot, "langdetect"), "langdetect", classPath)


def buildJing():
    os.chdir("jing-trang")
    if os.name == 'nt':
        runCmd([os.path.join(".", "ant.bat")])
    else:
        runCmd([os.path.join(".", "ant")])
    os.chdir("..")


def buildCssValidator():
    os.chdir("css-validator")
    if runCmd([javaCmd, "-jar",
               os.path.join("..", "jing-trang", "lib", "ant-launcher.jar"),
               "jar-without-dependencies"]):
        sys.exit(1)
    os.chdir("..")


def buildEmitters():
    compilerFile = os.path.join(vnuSrc, "xml", "SaxCompiler.java")
    compilerClass = "nu.validator.xml.SaxCompiler"
    classDir = os.path.join(buildRoot, "classes")
    ensureDirExists(classDir)
    args = [
        javacCmd,
        '-g',
        '-nowarn',
        '-d',
        classDir,
        '-encoding',
        'UTF-8',
    ]
    if javaTargetVersion != "":
        args.append('-target')
        args.append(javaTargetVersion)
        args.append('-source')
        args.append(javaTargetVersion)
    args.append(compilerFile)
    if runCmd(args):
        sys.exit(1)
    pageEmitter = os.path.join(vnuSrc, "servlet", "PageEmitter.java")
    formEmitter = os.path.join(vnuSrc, "servlet", "FormEmitter.java")
    if runCmd([javaCmd, '-cp', classDir, compilerClass, pageTemplate, pageEmitter]):  # nopep8
        sys.exit(1)
    if runCmd([javaCmd, '-cp', classDir, compilerClass, formTemplate, formEmitter]):  # nopep8
        sys.exit(1)
    removeIfDirExists(classDir)


def buildValidator():
    classPath = os.pathsep.join(
        dependencyJarPaths() +
        jarNamesToPaths(["galimatias", "htmlparser", "langdetect"]) +
        cssValidatorJarPath() +
        jingJarPath())
    buildEmitters()
    buildModule(buildRoot, "validator", classPath)


def ownJarList():
    return jarNamesToPaths(["galimatias", "htmlparser", "langdetect", "validator"]) + cssValidatorJarPath() + jingJarPath()  # nopep8


def buildRunJarPathList():
    return dependencyJarPaths(runDependencyJars) + ownJarList()


def getRunArgs(heap="$((HEAP))"):
    classPath = os.pathsep.join(buildRunJarPathList())
    args = [
        '-XX:-DontCompileHugeMethods',
        '-Xms%sk' % heap,
        '-Xmx%sk' % heap,
        '-Djava.security.properties=' + os.path.join(buildRoot, "resources", "security.properties"),  # nopep8
        '-classpath',
        classPath,
        '-Dnu.validator.datatype.warn=true',
        '-Dnu.validator.messages.limit=%d' % messagesLimit,
        '-Dnu.validator.servlet.about-page=' + aboutPage,
        '-Dnu.validator.servlet.deny-list=' + denyList,
        '-Dnu.validator.servlet.connection-timeout=%d' % (connectionTimeoutSeconds * 1000),  # nopep8
        '-Dnu.validator.servlet.filterfile=' + filterFile,
        '-Dnu.validator.servlet.host.generic=' + genericHost,
        '-Dnu.validator.servlet.host.html5=' + html5Host,
        '-Dnu.validator.servlet.host.parsetree=' + parsetreeHost,
        '-Dnu.validator.servlet.icon=' + icon,
        '-Dnu.validator.servlet.log4j-properties=' + log4jProps,
        '-Dnu.validator.servlet.max-file-size=%d' % (maxFileSize * 1024),
        '-Dnu.validator.servlet.path.generic=' + genericPath,
        '-Dnu.validator.servlet.path.html5=' + html5Path,
        '-Dnu.validator.servlet.path.parsetree=' + parsetreePath,
        '-Dnu.validator.servlet.service-name=' + serviceName,
        '-Dnu.validator.servlet.read-local-log4j-properties=1',
        '-Dnu.validator.servlet.results-title=' + resultsTitle,
        '-Dnu.validator.servlet.script=' + script,
        '-Dnu.validator.servlet.script-additional=' + scriptAdditional,
        '-Dnu.validator.servlet.socket-timeout=%d' % (socketTimeoutSeconds * 1000),  # nopep8
        '-Dnu.validator.servlet.max-requests=%d' % maxConnPerRoute,
        '-Dnu.validator.servlet.max-total-connections=%d' % maxTotalConnections,  # nopep8
        '-Dnu.validator.servlet.max-redirects=%d' % maxRedirects,
        '-Dnu.validator.servlet.statistics=%d' % statistics,
        '-Dnu.validator.servlet.style-sheet=' + stylesheet,
        '-Dnu.validator.servlet.user-agent=' + userAgent,
        '-Dnu.validator.servlet.version=' + validatorVersion,
        '-Dnu.validator.spec.html5-link=' + html5specLink,
        '-Dorg.mortbay.http.HttpRequest.maxFormContentSize=%d' % (maxFileSize * 1024),  # nopep8
    ]

    if stackSize != "":
        args.append('-Xss' + stackSize + 'k')
        args.append('-XX:ThreadStackSize=' + stackSize + 'k')

    if disablePromiscuousSsl:
        args.append('-Dnu.validator.xml.promiscuous-ssl=false')

    args.append('nu.validator.servlet.Main')

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
    f.write(" ".join([javaCmd, ] + [("'{0}'".format(x) if " " in x else x) for x in args]))  # nopep8
    if controlPort:
        f.write(" <&- 1>/dev/null 2>&1 &")
    f.write("\n")
    f.close()


def runValidator():
    ensureDirExists(os.path.join(buildRoot, "logs"))
    args = getRunArgs(str(int(heapSize) * 1024))
    execCmd(javaCmd, args)


def checkService():
    doc = miniDoc.replace(" ", "%20")
    query = "?out=gnu&doc=data:text/html;charset=utf-8,%s" % doc
    url = "http://localhost:%s/%s" % (portNumber, query)
    args = getRunArgs(str(int(heapSize) * 1024))
    daemon = subprocess.Popen([javaCmd, ] + args)
    time.sleep(25)
    print("Checking %s" % url)
    try:
        print(urlopen(url).read())
    except HTTPError as e:
        print(e.reason)
        sys.exit(1)
    except URLError as e:
        print(e.reason)
        sys.exit(1)
    time.sleep(5)
    daemon.terminate()


def clean():
    removeIfDirExists(distDir)
    removeIfDirExists(distWarDir)
    removeIfDirExists(dependencyDir)
    removeIfDirExists(jarsDir)


class Release():

    def __init__(self, artifactId="validator"):
        self.artifactId = artifactId
        self.version = validatorVersion
        self.buildXml = os.path.join(buildRoot, "build", "build.xml")
        self.vnuJar = os.path.join(distDir, "vnu.jar")
        self.vnuImageDirname = "vnu-runtime-image"
        self.vnuImageDir = os.path.join(distDir, self.vnuImageDirname)
        self.vnuModuleInfoDir = os.path.join(distDir, "vnu")
        self.minDocPath = os.path.join(buildRoot, 'minDoc.html')
        self.docs = ["index.html", "README.md", "CHANGELOG.md", "LICENSE"]
        self.setClasspath()

    def setClasspath(self):
        self.classpath = os.pathsep.join([
            antJar, antLauncherJar,
            os.pathsep.join(dependencyJarPaths()),
            os.path.join(jingTrangDir, "build", "jing.jar"),
            os.path.join(jarsDir, "validator.jar"),
            os.path.join(jarsDir, "htmlparser.jar"),
            os.path.join(jarsDir, "galimatias.jar"),
            os.path.join(jarsDir, "langdetect.jar"),
            os.path.join(cssValidatorDir, "css-validator.jar"),
        ])

    def reInitDistDir(self, whichDir):
        removeIfDirExists(whichDir)
        ensureDirExists(whichDir)

    def setVersion(self, whichDir, url=None):
        if self.artifactId == "jing":
            self.version = jingVersion
        if self.artifactId == "htmlparser":
            self.version = htmlparserVersion
        if self.artifactId == "cssvalidator":
            self.version = cssvalidatorVersion
        if self.artifactId == "galimatias":
            self.version = galimatiasVersion
        if self.artifactId == "langdetect":
            self.version = langdetectVersion
        if url == snapshotsRepoUrl:
            self.version += "-SNAPSHOT"
        self.writeVersion(whichDir)

    def writeVersion(self, whichDir):
        f = open(os.path.join(whichDir, "VERSION"), "w")
        f.write(self.version)
        f.close()

    def writeHash(self, filename, md5OrSha1):
        BLOCKSIZE = 65536
        hasher = md5()
        if md5OrSha1 == "sha1":
            hasher = sha1()
        with open(filename, 'rb') as f:
            buf = f.read(BLOCKSIZE)
            while len(buf) > 0:
                hasher.update(buf)
                buf = f.read(BLOCKSIZE)
        o = open("%s.%s" % (filename, md5OrSha1), 'wb')
        o.write(hasher.hexdigest().encode())
        o.close

    def writeHashes(self, whichDir):
        for filename in findFiles(whichDir):
            if os.path.basename(filename) in self.docs:
                continue
            self.writeHash(filename, "md5")
            self.writeHash(filename, "sha1")

    def sign(self, whichDir):
        for filename in findFiles(whichDir):
            if os.path.basename(filename) in self.docs:
                continue
            runCmd([gpgCmd, '--yes', '-ab', filename])

    def downloadMavenAntTasksJar(self):
        url = "https://repo1.maven.org/maven2/org/apache/maven/maven-ant-tasks/2.1.3/maven-ant-tasks-2.1.3.jar"  # nopep8
        md5sum = "7ce48382d1aa4138027a58ec2f29beda"
        extrasDir = os.path.join(buildRoot, "extras")
        ensureDirExists(extrasDir)
        path = os.path.join(extrasDir, url[url.rfind("/") + 1:])
        if not os.path.exists(path):
            fetchUrlTo(url, path, md5sum)
        self.setClasspath()

    def createArtifacts(self, jarOrWar, url=None):
        whichDir = distDir
        distJarOrWar = "dist"
        if jarOrWar == "war":
            whichDir = distWarDir
            distJarOrWar = "dist-war"
        self.reInitDistWarDir(whichDir)
        self.setVersion(whichDir, url)
        runCmd([javaCmd,
                '-Ddist=' + distJarOrWar,
                '-cp', self.classpath, 'org.apache.tools.ant.Main',
                '-f', self.buildXml, ('%s-artifacts' % self.artifactId)])

    def createBundle(self):
        self.downloadMavenAntTasksJar()
        self.createArtifacts()
        print("Building %s/%s-%s-bundle.jar" %
              (distDir, self.artifactId, self.version))
        self.sign()
        self.writeVersion()
        runCmd([javaCmd,
                '-cp', self.classpath, 'org.apache.tools.ant.Main',
                '-f', self.buildXml, ('%s-bundle' % self.artifactId)])

    def createExecutable(self, jarOrWar):
        whichDir = distDir
        distJarOrWar = "dist"
        if jarOrWar == "war":
            distJarOrWar = "dist-war"
            whichDir = distWarDir
            ensureDirExists(whichDir)
            os.mkdir(os.path.join(whichDir, "war"))
        self.reInitDistDir(whichDir)
        self.setVersion(whichDir)
        runCmd([javaCmd,
                '-Ddist=' + distJarOrWar,
                '-cp', self.classpath, 'org.apache.tools.ant.Main',
                '-f', self.buildXml, jarOrWar])
        if jarOrWar == "jar":
            release.checkJar()

    def createRuntimeImage(self):
        if javaEnvVersion < 9:
            return
        runCmd([jdepsCmd, '--generate-open-module', distDir, self.vnuJar])
        runCmd([javacCmd, '-nowarn', '--patch-module', 'vnu=' + self.vnuJar,
                os.path.join(distDir, 'vnu', 'module-info.java')])
        runCmd([jarCmd, '--update',
                '--file', self.vnuJar,
                '--module-version=' + validatorVersion,
                '-C', os.path.join(distDir, 'vnu'), 'module-info.class'])
        removeIfDirExists(self.vnuModuleInfoDir)
        removeIfDirExists(self.vnuImageDir)
        runCmd([jlinkCmd, '--launcher',
                'vnu=vnu/nu.validator.client.SimpleCommandLineValidator',
                '--strip-debug', '--no-header-files', '--no-man-pages',
                '--compress=2',
                '--output', self.vnuImageDir, '--module-path', self.vnuJar,
                '--add-modules', 'vnu'])
        release.checkRuntimeImage()
        os.chdir(distDir)
        os_platform = 'linux'
        if platform.system() == 'Darwin':
            os_platform = 'osx'
        if platform.system() == 'Windows':
            os_platform = 'windows'
        distroFile = os.path.join('vnu.%s' % (os_platform))
        removeIfExists(distroFile)
        shutil.make_archive(distroFile, 'zip', ".", self.vnuImageDirname)
        os.chdir(os.path.join('..', '..'))
        removeIfDirExists(self.vnuImageDir)
        self.writeHashes(distDir)

    def createDistribution(self, jarOrWar, isNightly=False):
        whichDir = distDir
        if jarOrWar == "war":
            whichDir = distWarDir
        self.setVersion(whichDir)
        if isNightly:
            self.version = "nightly.%s" % time.strftime('%Y-%m-%d')
        self.createExecutable(jarOrWar)
        self.prepareDist(jarOrWar)

    def prepareDist(self, jarOrWar):
        whichDir = distDir
        distJarOrWar = "dist"
        if jarOrWar == "war":
            whichDir = distWarDir
            distJarOrWar = "dist-war"
        self.removeExtras(whichDir)
        print("Building %s/vnu.%s_%s.zip" % (distWarDir, jarOrWar,
                                             self.version))
        if "nightly" not in self.version:
            for filename in self.docs:
                shutil.copy(os.path.join(buildRoot, filename), whichDir)
        os.chdir("build")
        distroFile = os.path.join("vnu.%s_%s.zip" % (jarOrWar, self.version))
        removeIfExists(distroFile)
        zf = zipfile.ZipFile(distroFile, "w")
        for dirname, subdirs, files in os.walk(distJarOrWar):
            zf.write(dirname)
        for filename in files:
            zf.write(os.path.join(dirname, filename))
        zf.close()
        shutil.move(distroFile, distJarOrWar)
        os.chdir("..")
        self.writeHashes(whichDir)
        self.sign(whichDir)

    def createOrUpdateGithubData(self):
        runCmd([gitCmd, 'tag', '-s', '-f', ('v%s' % validatorVersion)])
        args = [
            "-u",
            "validator",
            "-r",
            "validator",
            "-t",
            validatorVersion,
        ]
        devnull = open(os.devnull, 'wb')
        infoArgs = [ghRelCmd, 'info'] + args
        print(" ".join(infoArgs))
        if subprocess.call(infoArgs, stdout=devnull, stderr=subprocess.STDOUT):
            runCmd([ghRelCmd, 'release', '-p'] + args)
        else:
            runCmd([ghRelCmd, 'delete'] + args)
            runCmd([ghRelCmd, 'release', '-p'] + args)
        devnull.close()
        args.append('-n')
        args.append(releaseDate)
        args.append('-d')
        args.append(os.path.join(buildRoot, "WHATSNEW.md"))
        runCmd([ghRelCmd, 'edit', '-p'] + args)

    def createPackageJson(self, packageJson, packageJsonCopy):
        shutil.move(packageJson, packageJsonCopy)
        f = open(packageJson, 'w')
        with open(packageJsonCopy, 'r') as original:
            for line in original:
                if line.find('  "version":') != -1:
                    f.write('  "version": "%s",\n' % validatorVersion)
                else:
                    f.write(line)
        f.close

    def createNpmReadme(self, readMe, readMeCopy):
        drop = "It is released as two packages:"
        splitAt = "is a portable standalone version for"
        shutil.move(readMe, readMeCopy)
        npmFragment = os.path.join(buildRoot, "npm.md")
        npmReadme = open(readMe, 'w')
        with open(readMeCopy, 'r') as original:
            skip = False
            for line in original:
                if line.find(drop) != -1:
                    npmReadme.write(line.replace(drop, ""))
                elif line.find(splitAt) != -1:
                    skip = True
                    with open(npmFragment, 'r') as fragment:
                        for line in fragment:
                            npmReadme.write(line)
                elif line.find("## Usage") != -1:
                    skip = False
                    npmReadme.write(line)
                elif line.find("## Web-based checking") != -1:
                    skip = True
                elif skip:
                    continue
                else:
                    npmReadme.write(line)
        npmReadme.close()

    def removeExtras(self, whichDir):
        removeIfExists(os.path.join(whichDir, "VERSION"))
        sigsums = re.compile("^.+\.asc$|^.+\.md5$|.+\.sha1$")
        for filename in findFiles(whichDir):
            if (os.path.basename(filename) in self.docs or
                    sigsums.match(filename)):
                removeIfExists(filename)

    def uploadToCentral(self, url):
        self.downloadMavenAntTasksJar()
        self.createArtifacts(url)
        basename = "%s-%s" % (self.artifactId, self.version)
        mvnArgs = [
            mvnCmd,
            "-f",
            "%s.pom" % os.path.join(distDir, basename),
            "gpg:sign-and-deploy-file",
            "-Dgpg.executable=%s" % gpgCmd,
            "-DrepositoryId=ossrh",
            "-Durl=%s" % url,
            "-DpomFile=%s.pom" % basename,
            "-Dfile=%s.jar" % basename,
            "-Djavadoc=%s-javadoc.jar" % basename,
            "-Dsources=%s-sources.jar" % basename,
        ]
        runCmd(mvnArgs)
        mvnArgs = [
            mvnCmd,
            "-f",
            "%s.pom" % os.path.join(distDir, basename),
            "org.sonatype.plugins:nexus-staging-maven-plugin:rc-list",
            "-DnexusUrl=https://oss.sonatype.org/",
            "-DserverId=ossrh",
        ]
        output = subprocess.check_output(mvnArgs)
        for line in output.decode('utf-8').split('\n'):
            if "nuvalidator" in line:
                stagingRepositoryId = "nuvalidator-" + line[19:23]
                mvnArgs = [
                    mvnCmd,
                    "-f",
                    "%s.pom" % os.path.join(distDir, basename),
                    "org.sonatype.plugins:nexus-staging-maven-plugin:rc-close",     # nopep8
                    "-DnexusUrl=https://oss.sonatype.org/",
                    "-DserverId=ossrh",
                    "-DautoReleaseAfterClose=true",
                    "-DstagingRepositoryId=" + stagingRepositoryId
                ]
                runCmd(mvnArgs)
                mvnArgs = [
                    mvnCmd,
                    "-f",
                    "%s.pom" % os.path.join(distDir, basename),
                    "org.sonatype.plugins:nexus-staging-maven-plugin:rc-release",   # nopep8
                    "-DnexusUrl=https://oss.sonatype.org/",
                    "-DserverId=ossrh",
                    "-DautoReleaseAfterClose=true",
                    "-DstagingRepositoryId=" + stagingRepositoryId
                ]
                runCmd(mvnArgs)

    def uploadToHeroku(self):
        self.createExecutable("war")
        runCmd([herokuCmd,
                'deploy:war', '--war',
                os.path.join(distWarDir, "vnu.war"), '--app', 'vnu'])

    def uploadToGithub(self, jarOrWar):
        whichDir = distDir
        if jarOrWar == "war":
            whichDir = distWarDir
        for filename in findFiles(whichDir):
            if "zip" in filename:
                args = [
                    ghRelCmd,
                    'upload',
                    "-u",
                    "validator",
                    "-r",
                    "validator",
                    "-t",
                    validatorVersion,
                    "-n",
                    os.path.basename(filename),
                    "-f",
                    filename,
                ]
                runCmd(args)

    def uploadNpm(self, tag=None):
        removeIfExists(os.path.join(buildRoot, "README.md~"))
        removeIfExists(os.path.join(buildRoot, "CHANGELOG.md~"))
        readMe = os.path.join(buildRoot, "README.md")
        readMeCopy = readMe + ".GOOD"
        packageJson = os.path.join(buildRoot, "package.json")
        packageJsonCopy = packageJson + ".GOOD"
        self.createNpmReadme(readMe, readMeCopy)
        self.createPackageJson(packageJson, packageJsonCopy)
        if tag:
            runCmd([npmCmd, 'publish', '--tag', tag])
        else:
            runCmd([npmCmd, 'publish'])
        shutil.move(readMeCopy, readMe)
        shutil.move(packageJsonCopy, packageJson)

    def uploadToReleasesHost(self, jarOrWar, isNightly=False):
        whichDir = distDir
        if jarOrWar == "war":
            whichDir = distWarDir
        path = "%s/%s" % (releasesPath, jarOrWar)
        if isNightly:
            path = "%s/%s" % (nightliesPath, jarOrWar)
        for filename in findFiles(whichDir):
            runCmd([scpCmd, filename, ('%s:%s' % (releasesHost, path))])

    def checkJar(self):
        if not os.path.exists(self.vnuJar):
            return

        with open(self.minDocPath, 'w') as f:
            f.write(miniDoc)

        formats = ["gnu", "xml", "json", "text"]
        for _format in formats:
            if runCmd([javaCmd, '-jar', self.vnuJar, '--format', _format,
                       self.minDocPath]):
                sys.exit(1)
        # also make sure it works even w/o --format value; returns gnu output
        if runCmd([javaCmd, '-jar', self.vnuJar, self.minDocPath]):
            sys.exit(1)
        if runCmd([javaCmd, '-jar', self.vnuJar, '--version']):
            sys.exit(1)
        os.remove(self.minDocPath)

    def checkRuntimeImage(self):
        if javaEnvVersion < 9:
            return
        vnuRunScript = os.path.join(self.vnuImageDir, 'bin', 'vnu')
        if os.path.exists(os.path.join(self.vnuImageDir, 'bin', 'vnu.bat')):
            vnuRunScript = os.path.join(self.vnuImageDir, 'bin', 'vnu.bat')

        with open(self.minDocPath, 'w') as f:
            f.write(miniDoc)

        formats = ["gnu", "xml", "json", "text"]
        for _format in formats:
            if runCmd([vnuRunScript, '--format', _format, self.minDocPath]):
                sys.exit(1)
        # also make sure it works even w/o --format value; returns gnu output
        if runCmd([vnuRunScript, self.minDocPath]):
            sys.exit(1)
        if runCmd([vnuRunScript, '--version']):
            sys.exit(1)
        os.remove(self.minDocPath)


def createTarball():
    args = [
        tarCmd,
        "zcf",
        os.path.join(buildRoot, "jars.tar.gz"),
        os.path.join(buildRoot, "run-validator.sh"),
        os.path.join(buildRoot, "site", "style.css"),
        os.path.join(buildRoot, "site", "script.js"),
        os.path.join(buildRoot, "site", "icon.png"),
    ] + ownJarList()
    runCmd(args)


def createDepTarball():
    args = [
        tarCmd,
        "zcf",
        os.path.join(buildRoot, "deps.tar.gz"),
    ] + dependencyJarPaths(runDependencyJars)
    runCmd(args)


def deployOverScp():
    if not deploymentTarget:
        print("No target")
        return
    runCmd([scpCmd, os.path.join(buildRoot, "deps.tar.gz"),
            ('%s/deps.tar.gz' % deploymentTarget)])
    runCmd([scpCmd, os.path.join(buildRoot, "jars.tar.gz"),
            ('%s/jars.tar.gz' % deploymentTarget)])
    emptyPath = os.path.join(buildRoot, "EMPTY")
    f = open(emptyPath, 'wb')
    f.close()
    runCmd([scpCmd, emptyPath, ('%s/DEPLOY' % deploymentTarget)])
    os.remove(emptyPath)


def fetchUrlTo(url, path, md5sum=None):
    print(url)
    completed = False
    defaultTimeout = socket.getdefaulttimeout()
    while not completed:
        try:
            socket.setdefaulttimeout(httpTimeoutSeconds)
            try:
                f = urlopen(url, cafile=CAFILE)  # Python 2.7.9+
            except TypeError:
                f = urlopen(url)
            data = f.read()
            f.close()
            completed = True
        except SSLError as e:
            print(e.reason)
            print(
                "If you encounter an [SSL: CERTIFICATE_VERIFY_FAILED] error," +
                " try `pip install certifi` to use newer certifications")
        except BadStatusLine:
            print("received error, retrying")
        finally:
            socket.setdefaulttimeout(defaultTimeout)
    if md5sum:
        m = md5(data)
        if md5sum != m.hexdigest():
            print("Bad MD5 hash for %s." % url)
            sys.exit(1)
    head, tail = os.path.split(path)
    if not os.path.exists(head):
        os.makedirs(head)
    f = open(path, "wb")
    f.write(data)
    f.close()


def spiderApacheDirectories(baseUrl, baseDir):
    f = urlopen(baseUrl, timeout=httpTimeoutSeconds)
    parser = UrlExtractor(baseUrl)
    parser.feed(f.read())
    f.close()
    parser.close()
    for leaf in parser.leaves:
        fetchUrlTo(leaf, os.path.join(baseDir, leaf[7:]))
    for directory in parser.directories:
        spiderApacheDirectories(directory, baseDir)


def downloadLocalEntities():
    removeIfDirExists(os.path.join(buildRoot, "local-entities"))
    fileMap = {}
    fileMap["html5spec"] = "https://html.spec.whatwg.org/"
    ensureDirExists(filesDir)
    for filename in fileMap:
        fetchUrlTo(fileMap[filename], os.path.join(filesDir, filename))


def localPathToJarCompatName(path):
    return javaSafeNamePat.sub('_', path)


def preparePropertiesFile():
    f = open(os.path.join(filesDir, "misc.properties"), 'w')
    f.write("nu.validator.servlet.service-name=%s\n" % serviceName)
    f.write("nu.validator.servlet.results-title=%s\n" % resultsTitle)
    f.write("nu.validator.servlet.version=%s\n" % validatorVersion)
    f.write("nu.validator.servlet.user-agent=%s\n" % userAgent)
    f.close()


def prepareLocalEntityJar():
    ensureDirExists(filesDir)
    preparePropertiesFile()
    shutil.copyfile(os.path.join(buildRoot, presetsFile),
                    os.path.join(filesDir, "presets"))
    shutil.copyfile(os.path.join(buildRoot, aboutFile),
                    os.path.join(filesDir, "about.html"))
    shutil.copyfile(os.path.join(buildRoot, stylesheetFile),
                    os.path.join(filesDir, "style.css"))
    shutil.copyfile(os.path.join(buildRoot, scriptFile),
                    os.path.join(filesDir, "script.js"))
    shutil.copyfile(os.path.join(buildRoot, "site", "icon.png"),
                    os.path.join(filesDir, "icon.png"))
    shutil.copyfile(os.path.join(buildRoot, "resources", "existing-rel-values"),  # nopep8
                    os.path.join(filesDir, "existing-rel-values"))
    shutil.copyfile(os.path.join(buildRoot, "docs", "Microsyntax-descriptions.md"),  # nopep8
                    os.path.join(filesDir, "syntax-descriptions"))
    shutil.copyfile(os.path.join(buildRoot, "resources", "language-profiles-list.txt"),  # nopep8
                    os.path.join(filesDir, "language-profiles-list.txt"))
    shutil.copyfile(os.path.join(buildRoot, "resources", "alt_advice.html"),
                    os.path.join(filesDir, "vnu-alt-advice"))
    shutil.copyfile(os.path.join(buildRoot, "resources", "language-subtag-registry.txt"),  # nopep8
                    os.path.join(filesDir, "subtag-registry"))
    languageProfilesTargetDir = os.path.join(filesDir, "language-profiles")
    removeIfDirExists(languageProfilesTargetDir)
    shutil.copytree(os.path.join(buildRoot, "resources", "language-profiles"), languageProfilesTargetDir)  # nopep8
    shutil.copyfile(os.path.join(buildRoot, "resources", "log4j.properties"),
                    os.path.join(filesDir, "log4j.properties"))
    shutil.copyfile(os.path.join(buildRoot, "README.md"),
                    os.path.join(filesDir, "cli-help"))
    f = open(os.path.join(buildRoot, "resources", "entity-map.txt"))
    o = open(os.path.join(filesDir, "entitymap"), 'w')
    try:
        for line in f:
            url, path = line.strip().split("\t")
            entPath = ""
            if path.startswith("schema/html5/"):
                entPath = os.path.join(buildRoot, "schema", "html5", path[13:])
            elif path.startswith("schema/"):
                entPath = os.path.join(buildRoot, path)
            safeName = localPathToJarCompatName(path)
            safePath = os.path.join(filesDir, safeName)
            if os.path.exists(entPath):
                o.write("%s\t%s\n" % (url, safeName))
                shutil.copyfile(entPath, safePath)
    finally:
        f.close()
        o.close()
    schemaDir = os.path.join(buildRoot, "schema")
    for file in coreSchemaDriverFiles:
        removeIfExists(os.path.join(schemaDir, file))
    for file in htmlSchemaDriverFiles:
        removeIfExists(os.path.join(schemaDir, "html5", file))
    removeIfDirExists(os.path.join(schemaDir, "xhtml10"))
    removeIfDirExists(os.path.join(schemaDir, "rdf"))


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
    ensureDirExists(dependencyDir)
    path = os.path.join(dependencyDir, url[url.rfind("/") + 1:])
    if not os.path.exists(path):
        fetchUrlTo(url, path, md5sum)
        if path.endswith(".zip"):
            zipExtract(path, dependencyDir)


def updateSubmodules():
    runCmd([gitCmd, 'submodule', 'update', '--remote', '--merge', '--init'])


def updateSubmodulesShallow():
    runCmd([gitCmd, 'submodule', 'update', '--init', '--depth', '1'])


def downloadDependencies():
    for url, md5sum in dependencyPackages:
        downloadDependency(url, md5sum)
    for url, md5sum in moduleDependencyPackages:
        downloadDependency(url, md5sum)


def buildAll():
    if 'JAVA_HOME' not in os.environ:
        print("Error: The JAVA_HOME environment variable is not set.")
        print("Set the JAVA_HOME environment variable to the pathname" +
              " of the directory where your JDK is installed.")
        sys.exit(1)
    buildCssValidator()
    buildJing()
    buildSchemaDrivers()
    prepareLocalEntityJar()
    buildLangdetect()
    buildGalimatias()
    buildHtmlParser()
    buildValidator()


def runTests():
    args = ["tests/messages.json"]
    className = "nu.validator.client.TestRunner"
    classPath = os.pathsep.join(
        buildRunJarPathList() +
        jarNamesToPaths(["galimatias", "htmlparser", "langdetect",
                         "validator"]) +
        cssValidatorJarPath() +
        jingJarPath())
    if runCmd([javaCmd, '-classpath', classPath, className] + args):
        sys.exit(1)


def splitHostSpec(spec):
    index = spec.find('/')
    return (spec[0:index], spec[index:])


def printHelp():
    print("Usage: python build/build.py [options] [tasks]")
    print("")
    print("Options:")
    print("  --about=https://about.validator.nu/")
    print("                                Sets URL for the about page")
    print("  --control-port=-1")
    print("                                Sets server control port number")
    print("                                (necessary for daemonizing)")
    print("  --filter-file=resources/message-filters.text")
    print("                                Sets path to the filter file")
    print("  --git=/usr/bin/git         -- Sets path to the git binary")
    print("  --heap=512                 -- Sets Java heap size in MB")
    print("  --html5link=https://html.spec.whatwg.org/")
    print("                                Sets link URL of the HTML5 spec")
    print("  --html5load=https://html.spec.whatwg.org/")
    print("                                Sets load URL of the HTML5 spec")
    print("  --jar=/usr/bin/jar         -- Sets path to the jar binary")
    print("  --java=/usr/bin/java       -- Sets path to the java binary")
    print("  --javac=/usr/bin/javac     -- Sets path to the javac binary")
    print("  --javadoc=/usr/bin/javadoc -- Sets path to the javadoc binary")
    print("  --javaversion=N.N          -- Sets Java VM version to build for")
    print("  --jdk-bin=/j2se/bin        -- Sets paths for all JDK tools")
    print("  --log4j=log4j.properties   -- Sets path to log4 configuration")
    print("  --messages-limit=1000")
    print("                                Sets limit on the maximum number")
    print("                                of error+warning messages to report")
    print("                                for any document before stopping")
    print("  --name=Validator.nu        -- Sets service name")
    print("  --port=8888                -- Sets server port number")
    print("  --promiscuous-ssl=on       -- Don't check SSL/TLS trust chain")
    print("  --results-title=Validation results")
    print("                                Sets title to show on results page")
    print("  --script=script.js")
    print("                                Sets the URL for the script")
    print("                                Defaults to just script.js relative")
    print("                                to the validator URL")
    print("  --script-additional=<URL>")
    print("                                Sets the URL for a script file to")
    print("                                include in addition to the file")
    print("                                specified by the --script option.")
    print("  --stacksize=NN             -- Sets Java thread stack size in KB")
    print("  --stylesheet=style.css")
    print("                                Sets URL for the style sheet")
    print("                                Defaults to just style.css relative")
    print("                                to the validator URL")
    print("  --user-agent                  Sets User-Agent string for checker")
    print("")
    print("Tasks:")
    print("  update   -- Update git submodules")
    print("  dldeps   -- Download missing dependency libraries and entities")
    print("  build    -- Build the source")
    print("  test     -- Run regression tests")
    print("  check    -- Perform self-test of the system")
    print("  run      -- Run the system")
    print("  all      -- update dldeps build test run")
    print("  bundle   -- Create a Maven release bundle")
    print("  jar      -- Create a JAR file containing a release distribution")
    print("  war      -- Create a WAR file containing a release distribution")
    print("  script   -- Make run-validator.sh script for running the system")


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
            javaTargetVersion = arg[14:]
        elif arg.startswith("--html5link="):
            html5specLink = arg[12:]
        elif arg.startswith("--about="):
            aboutPage = arg[8:]
        elif arg.startswith("--denylist="):
            denyList = arg[11:]
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
        elif arg.startswith("--script-additional="):
            scriptAdditional = arg[20:]
        elif arg.startswith("--name="):
            serviceName = arg[7:]
        elif arg.startswith("--results-title="):
            resultsTitle = arg[16:]
        elif arg.startswith("--messages-limit="):
            messagesLimit = int(arg[17:])
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
        elif arg.startswith("--filter-file="):
            filterFile = arg[14:]
        elif arg == '--promiscuous-ssl=on':
            disablePromiscuousSsl = 0
        elif arg == '--promiscuous-ssl=off':
            disablePromiscuousSsl = 1
        elif arg == '--no-self-update':
            pass
        elif arg == '--local':
            pass
        elif arg.startswith("--connection-timeout="):
            connectionTimeoutSeconds = int(arg[21:])
        elif arg.startswith("--socket-timeout="):
            socketTimeoutSeconds = int(arg[17:])
        elif arg.startswith("--max-requests="):
            maxConnPerRoute = int(arg[15:])
        elif arg.startswith("--max-total-connections="):
            maxTotalConnections = int(arg[24:])
        elif arg.startswith("--max-redirects="):
            maxConnPerRoute = int(arg[16:])
        elif arg == '--statistics':
            statistics = 1
        elif arg == '--help':
            printHelp()
        elif arg == 'update':
            updateSubmodules()
        elif arg == 'update-shallow':
            updateSubmodulesShallow()
        elif arg == 'dldeps':
            downloadDependencies()
            downloadLocalEntities()
        elif arg == 'checkout':
            pass
        elif arg == 'build':
            buildAll()
        elif arg == 'bundle':
            release = Release()
            release.createBundle()
        elif arg == 'snapshot':
            release = Release()
            release.uploadToCentral(snapshotsRepoUrl)
        elif arg == 'release':
            release = Release()
            release.uploadToCentral(stagingRepoUrl)
            release.createOrUpdateGithubData()
            release.createDistribution("war")
            release.uploadToReleasesHost("war")
            release.uploadToGithub("war")
            release.createDistribution("jar")
            release.uploadToReleasesHost("jar")
            release.uploadToGithub("jar")
            release.uploadNpm()
        elif arg == 'npm-snapshot':
            release = Release()
            release.createExecutable("jar")
            release.uploadNpm("next")
        elif arg == 'npm-release':
            release = Release()
            release.createExecutable("jar")
            release.uploadNpm()
        elif arg == 'github-release':
            isNightly = True
            release = Release()
            release.createOrUpdateGithubData()
            release.createDistribution("war", isNightly)
            release.uploadToReleasesHost("war", isNightly)
            release.uploadToGithub("war")
            release.createDistribution("jar", isNightly)
            release.uploadToReleasesHost("jar", isNightly)
            release.uploadToGithub("jar")
        elif arg == 'nightly':
            isNightly = True
            release = Release()
            release.createDistribution("war", isNightly)
            release.uploadToReleasesHost("war", isNightly)
            release.createDistribution("jar", isNightly)
            release.uploadToReleasesHost("jar", isNightly)
            release.uploadNpm("next")
        elif arg == 'heroku':
            release = Release()
            release.uploadToHeroku()
        elif arg == 'galimatias-bundle':
            release = Release("galimatias")
            release.createBundle()
        elif arg == 'galimatias-snapshot':
            release = Release("galimatias")
            release.uploadToCentral(snapshotsRepoUrl)
        elif arg == 'galimatias-release':
            release = Release("galimatias")
            release.uploadToCentral(stagingRepoUrl)
        elif arg == 'langdetect-bundle':
            release = Release("langdetect")
            release.createBundle()
        elif arg == 'langdetect-snapshot':
            release = Release("langdetect")
            release.uploadToCentral(snapshotsRepoUrl)
        elif arg == 'langdetect-release':
            release = Release("langdetect")
            release.uploadToCentral(stagingRepoUrl)
        elif arg == 'htmlparser-bundle':
            release = Release("htmlparser")
            release.createBundle()
        elif arg == 'htmlparser-snapshot':
            release = Release("htmlparser")
            release.uploadToCentral(snapshotsRepoUrl)
        elif arg == 'htmlparser-release':
            release = Release("htmlparser")
            release.uploadToCentral(stagingRepoUrl)
        elif arg == 'cssvalidator-bundle':
            release = Release("cssvalidator")
            release.createBundle()
        elif arg == 'cssvalidator-snapshot':
            release = Release("cssvalidator")
            release.uploadToCentral(snapshotsRepoUrl)
        elif arg == 'cssvalidator-release':
            release = Release("cssvalidator")
            release.uploadToCentral(stagingRepoUrl)
        elif arg == 'jing-bundle':
            release = Release("jing")
            release.createBundle()
        elif arg == 'jing-snapshot':
            release = Release("jing")
            release.uploadToCentral(snapshotsRepoUrl)
        elif arg == 'jing-release':
            release = Release("jing")
            release.uploadToCentral(stagingRepoUrl)
        elif arg == 'jar':
            release = Release()
            release.createExecutable("jar")
            release.createRuntimeImage()
        elif arg == 'war':
            release = Release()
            release.createExecutable("war")
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
        elif arg == 'clean':
            clean()
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
            print("Unknown option %s." % arg)
