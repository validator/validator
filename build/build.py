#!/usr/bin/env python3
# -*- coding: utf-8 -*- vim: set fileencoding=utf-8 :

# Copyright (c) 2007 Henri Sivonen
# Copyright (c) 2008-2020 Mozilla Foundation
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
import shlex
import shutil
import json
import xml.etree.ElementTree as ET
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
import argparse
try:
    import argcomplete
    ARGCOMPLETE_AVAILABLE = True
except ImportError:
    ARGCOMPLETE_AVAILABLE = False
# Use newer https certifications from certifi package if available
try:
    import certifi
    CAFILE = certifi.where()
except ImportError:
    CAFILE = None
from pathlib import Path


class CustomHelpAction(argparse.Action):
    def __init__(self, option_strings, dest, script_name=None, **kwargs):
        kwargs.pop('script_name', None)
        super(CustomHelpAction, self).__init__(option_strings, dest,
                                               nargs=0, **kwargs)
        self.script_name = script_name

    def __call__(self, parser, namespace, values, option_string=None):
        parser.print_help()
        printCompletionInstructions(self.script_name)
        sys.exit(0)


class TasksFormatter(argparse.RawTextHelpFormatter):
    def _format_action(self, action):
        if action.dest == 'tasks':
            task_choices = sorted(getTaskChoices())
            help_text = ''
            for task in task_choices:
                help_text += f'  {task}\n'
            action.help = help_text
            # Return custom format with colon and newline
            return f'tasks:\n{action.help}'
        return super()._format_action(action)

    def _format_usage(self, usage, actions, groups, prefix):
        sorted_actions = sorted(actions, key=lambda a: a.dest if a.dest !=
                                'help' else '')
        return super()._format_usage(usage, sorted_actions, groups, prefix)

    def add_arguments(self, actions):
        sorted_actions = sorted(actions, key=lambda a: a.option_strings[0]
                                if a.option_strings else a.dest)
        super().add_arguments(sorted_actions)

    def format_help(self):
        help_text = super().format_help()
        # Find and extract the tasks section (which appears right after usage)
        lines = help_text.split('\n')
        filteredLines = []
        tasks_lines = []
        in_tasks_section = False

        for line in lines:
            if line.strip() == 'positional arguments:':
                continue

            if line.strip() == 'tasks:':
                in_tasks_section = True
                continue

            if in_tasks_section:
                # Check if we've reached optional arguments
                if line.strip() == 'optional arguments:':
                    in_tasks_section = False
                    filteredLines.append(line)
                else:
                    # Extract task names
                    if line.strip():
                        tasks_lines.append(line.strip())
            else:
                filteredLines.append(line)

        # Insert tasks section at the end, before the completion instructions
        insert_pos = len(filteredLines)
        for i, line in enumerate(filteredLines):
            if 'To enable shell tab completion' in line:
                insert_pos = i
                break

        if tasks_lines:
            filteredLines.insert(insert_pos, 'tasks:')
            for task in tasks_lines:
                filteredLines.insert(insert_pos + 1, f'    {task}')
                insert_pos += 1
            filteredLines.insert(insert_pos + 1, '')

        return '\n'.join(filteredLines)


os.environ["PYTHONIOENCODING"] = "utf-8"
javaTargetVersion = '11'
dockerCmd = 'docker'
curlCmd = 'curl'
makeCmd = 'make'
tarCmd = 'tar'
scpCmd = 'scp'
gitCmd = 'git'
mvnCmd = 'mvn'
gpgCmd = 'gpg'
npmCmd = 'npm'
antCmd = 'ant'
antCommonArgs = []
offline = False
verbose = False

releaseDate = time.strftime('%d %B %Y')
year = time.strftime('%y')
month = time.strftime('%m').lstrip('0')
day = time.strftime('%d').lstrip('0')
validatorVersion = "%s.%s.%s" % (year, month, day)

buildRoot = '.'
distDir = os.path.join(buildRoot, "build", "dist")
distWarDir = os.path.join(buildRoot, "build", "dist-war")
mavenArtifactsDir = os.path.join(distDir, "nu", "validator", "validator",
                                 validatorVersion)
vnuCmd = os.path.join(distDir, "vnu-runtime-image", "bin", "vnu")
vnuJar = os.path.join(distDir, "vnu.jar")
os.environ["VNUJAR"] = str(Path(vnuJar).resolve())
dependencyDir = os.path.join(buildRoot, "dependencies")
extrasDir = os.path.join(buildRoot, "extras")
jarsDir = os.path.join(buildRoot, "jars")
jingTrangDir = os.path.join(buildRoot, "jing-trang")
cssValidatorDir = os.path.join(buildRoot, "css-validator")
# filesDir is the dir where built resources are stored
filesDir = os.path.join(buildRoot, "build", "validator", "resources", "nu",
                        "validator", "localentities", "files")

pageTemplate = os.path.join("site", "PageEmitter.xml")
formTemplate = os.path.join("site", "FormEmitter.xml")
presetsFile = os.path.join("resources", "presets.txt")
aboutFile = os.path.join("site", "about.html")
stylesheetFile = os.path.join("site", "style.css")
scriptFile = os.path.join("site", "script.js")
filterFile = os.path.join("resources", "message-filters.txt")
gitSubtreesFile = os.path.join(buildRoot, ".gitsubtrees.yaml")

bindAddress = '0.0.0.0'
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
maxFileSize = 15360
disablePromiscuousSsl = 0
allowedAddressType = 'all'
allowForbiddenHosts = False
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
additionalJavaSystemProperties = ''

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
            for name, value in attrs:
                if name == "href":
                    if directoryPat.match(value):
                        self.directories.append(self.baseUrl + value)
                    if leafPat.match(value):
                        self.leaves.append(self.baseUrl + value)


def runCmd(cmd):
    print(shlex.join(cmd))
    subprocess.check_call(cmd)


def runCmdFromString(cmdString):
    print(cmdString)
    subprocess.check_call(cmdString, shell=True)


def execCmd(cmd, args, silent=False):
    print(shlex.join([cmd] + args))
    if subprocess.call([cmd, ] + args):
        if not silent:
            print("Command failed.")
        sys.exit(2)


def runShell(shellCmd):
    print(shlex.join(shellCmd))
    return subprocess.call(shellCmd, shell=True)


def runAnt(opts, targets):
    # Append antCommonArgs after the default values so that they can be
    # overridden; e.g. with --ant-extra-arg options passed to this script.
    antOpts = ['-Dbuild.java.target.version=' + javaTargetVersion,
               '-Ddist=' + distDir,
               '-Dvalidator.param.aboutFile=' + aboutFile,
               '-Dvalidator.param.formTemplate=' + formTemplate,
               '-Dvalidator.param.pageTemplate=' + pageTemplate,
               '-Dvalidator.param.presetsFile=' + presetsFile,
               '-Dvalidator.param.resultsTitle=' + resultsTitle,
               '-Dvalidator.param.scriptFile=' + scriptFile,
               '-Dvalidator.param.serviceName=' + serviceName,
               '-Dvalidator.param.stylesheetFile=' + stylesheetFile,
               '-Dvalidator.param.userAgent=' + userAgent,
               '-Dversion=' + validatorVersion,
               '-f', os.path.join(buildRoot, "build", "build.xml"),
               ] + antCommonArgs

    if isinstance(targets, str):
        if targets != "":
            antTargets = [targets]
        else:
            antTargets = []
    else:
        antTargets = targets

    # Append the options received in 'opts' at the end of 'antOpts'.
    # If a property is defined twice, and will take the value of the last one.
    if isinstance(opts, str):
        antOpts = antOpts + [opts]
    else:
        antOpts = antOpts + opts

    runCmd([antCmd] + antOpts + antTargets)


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
        if root.endswith(os.path.join("nu", "validator", "htmlparser", "xom")):
            continue
        for filename in files:
            if filename.endswith(ext):
                rv.append(os.path.join(root, filename))
    return rv


def findFiles(directory):
    rv = []
    for root, dirs, files in os.walk(directory):
        for filename in files:
            candidate = os.path.join(root, filename)
            rv.append(candidate)
    return rv


def findFilesShallow(directory):
    rv = []
    for root, dirs, files in os.walk(directory):
        if root[len(directory)+1:].count(os.sep) < 1:
            for filename in files:
                candidate = os.path.join(root, filename)
                rv.append(candidate)
    return rv


def jarNamesToPaths(names):
    return [os.path.join(jarsDir, name + ".jar") for name in names]


def jingJarPath():
    return [os.path.join(buildRoot, "jing-trang", "build", "jing.jar"), ]


def cssValidatorJarPath():
    return [os.path.join(buildRoot, "css-validator", "css-validator.jar"), ]


def copyFiles(sourceDir, classDir):
    files = findFiles(sourceDir)
    for f in files:
        destFile = os.path.join(classDir, f[len(sourceDir) + 1:])
        head, tail = os.path.split(destFile)
        if not os.path.exists(head):
            os.makedirs(head)
        shutil.copyfile(f, destFile)


def extrasJarPaths():
    return findFilesWithExtension(extrasDir, "jar")


def dependencyJarPaths():
    pathList = findFilesWithExtension(dependencyDir, "jar")
    for jar in ["saxon9.jar", "isorelax.jar"]:
        pathList += [os.path.join(jingTrangDir, "lib", jar)]
    ensureDirExists(extrasDir)
    pathList += findFilesWithExtension(extrasDir, "jar")
    return pathList


def buildSchemaDrivers():
    schemaSrcDir = os.path.join(buildRoot, "schema")
    schemaBuildDir = os.path.join(buildRoot, "build", "schema")
    ensureDirExists(schemaBuildDir)
    html5Dir = os.path.join(schemaBuildDir, "html5")
    ensureDirExists(html5Dir)
    driversSrcDir = os.path.join(schemaSrcDir, ".drivers")
    srcLegacyRnc = os.path.join(driversSrcDir, "legacy.rnc")
    srcItsRnc = os.path.join(os.path.join(schemaSrcDir, "its2/its20-html5.rnc"))  # nopep8
    srcItsTypesRnc = os.path.join(schemaSrcDir, "its2/its20-html5-types.rnc")  # nopep8
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
        print("Copying %s to %s" % (os.path.join(driversSrcDir, file), os.path.join(schemaBuildDir, file)))  # nopep8
        shutil.copy(os.path.join(driversSrcDir, file), schemaBuildDir)
    xhtmlSourceDir = os.path.join(driversSrcDir, "xhtml10")
    xhtmlTargetDir = os.path.join(schemaBuildDir, "xhtml10")
    removeIfDirExists(xhtmlTargetDir)
    shutil.copytree(xhtmlSourceDir, xhtmlTargetDir)
    print("Copying %s to %s" % (xhtmlSourceDir, xhtmlTargetDir))
    rdfDir = os.path.join(schemaBuildDir, "rdf")
    removeIfDirExists(rdfDir)
    os.mkdir(rdfDir)
    print("Copying %s to %s/rdf.rnc" % (os.path.join(driversSrcDir, "rdf.rnc"), rdfDir))  # nopep8
    shutil.copy(os.path.join(driversSrcDir, "rdf.rnc"), rdfDir)
    removeIfExists(os.path.join(html5Dir, "legacy.rnc"))
    removeIfExists(os.path.join(html5Dir, "its20-html5.rnc"))
    removeIfExists(os.path.join(html5Dir, "its20-html5-types.rnc"))
    shutil.copy(srcLegacyRnc, html5Dir)
    shutil.copy(srcItsRnc, html5Dir)
    shutil.copy(srcItsTypesRnc, html5Dir)

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
    f = openDriver(schemaDir, "xhtml5full-html-rdfa.rnc", "xhtml5full-html.rnc")  # nopep8
    f.write(schemaDriverHtml5RDFa)
    f.close()


def buildSchemaDriverXhtml5htmlRDFaLite(schemaDir):
    f = openDriver(schemaDir, "xhtml5full-html-rdfalite.rnc", "xhtml5full-html.rnc")  # nopep8
    f.write(schemaDriverHtml5RDFaLite)
    f.close()

#################################################################
# end of data and functions for building schema drivers
#################################################################


def dockerBuild():
    args = [
        dockerCmd,
        "build",
        "--platform",
        "linux/amd64",
        "-t",
        "ghcr.io/validator/validator",
        "."
    ]
    runCmd(args)


def dockerRun():
    args = [
        dockerCmd,
        "run",
        "-it",
        "--rm",
        "-e",
        "CONNECTION_TIMEOUT_SECONDS=15",
        "-e",
        "SOCKET_TIMEOUT_SECONDS=15",
        "-p",
        "8888:8888",
        "ghcr.io/validator/validator"
    ]
    runCmd(args)


def gitHubUser():
    return subprocess.run([gitCmd, 'config', 'github.user'],
                          capture_output=True).stdout.decode("utf-8")


def dockerPush():
    args = [
        "echo",
        os.environ['GITHUB_TOKEN'],
        "|",
        dockerCmd,
        "login",
        "ghcr.io",
        "--username",
        gitHubUser(),
        "--password-stdin",
    ]
    runCmd(args)
    args = [
        dockerCmd,
        "push",
        "ghcr.io/validator/validator:latest",
    ]
    runCmd(args)


def ownJarList():
    return jarNamesToPaths(["galimatias", "htmlparser", "langdetect", "validator"]) + cssValidatorJarPath() + jingJarPath()  # nopep8


def getRunArgs(heap="$((HEAP))", _type="jar"):
    args = [
        '-XX:-DontCompileHugeMethods',
        '-Xms%sk' % heap,
        '-Xmx%sk' % heap,
        '-Djava.security.properties=' + os.path.join(buildRoot, "resources", "security.properties"),  # nopep8
        '-Dnu.validator.datatype.warn=true',
        '-Dnu.validator.messages.limit=%d' % messagesLimit,
        '-Dnu.validator.servlet.about-page=' + aboutPage,
        '-Dnu.validator.servlet.bind-address=' + bindAddress,
        '-Dnu.validator.servlet.allowed-address-type=' + allowedAddressType,
        '-Dnu.validator.servlet.allow-forbidden-hosts=' + str(allowForbiddenHosts).lower(),  # nopep8
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

    if _type == "jar":
        classpath_item = []
        classpath_item.append(os.pathsep.join(extrasJarPaths()))
        classpath_item.append(vnuJar)
        args.append('-classpath')
        args.append(os.pathsep.join(classpath_item))

    if stackSize != "":
        args.append('-Xss' + stackSize + 'k')
        args.append('-XX:ThreadStackSize=' + stackSize)

    if disablePromiscuousSsl:
        args.append('-Dnu.validator.xml.promiscuous-ssl=false')

    args.extend(additionalJavaSystemProperties.split())

    args.append('nu.validator.servlet.Main')

    args.append(portNumber)
    if controlPort:
        args.append(controlPort)
    return args


def generateRunScript():
    args = getRunArgs()
    f = open(os.path.join(buildRoot, "run-validator.sh"), 'w')
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


def clean():
    for directory in ["classes", "html", "logs"]:
        try:
            os.rmdir(directory)
        except Exception:
            pass
    removeIfDirExists(distDir)
    removeIfDirExists(distWarDir)
    runAnt([], "clean")


def realclean():
    clean()
    runAnt([], "distclean")

    buildFilesToCleanup = []
    buildFilesToCleanup.append(os.path.join(buildRoot, "run-validator.sh"))
    buildFilesToCleanup.append(os.path.join(buildRoot, "jars.tar.gz"))
    buildFilesToCleanup.append(os.path.join(buildRoot, "deps.tar.gz"))

    for aFile in buildFilesToCleanup:
        try:
            os.remove(aFile)
        except Exception:
            pass


def getRuntimeDistroBasename():
    os_platform = 'linux'
    if platform.system() == 'Darwin':
        os_platform = 'osx'
    if platform.system() == 'Windows':
        os_platform = 'windows'
    return os.path.join('vnu.%s' % (os_platform))


class Release():

    def __init__(self, artifactId="validator"):
        self.artifactId = artifactId
        self.version = validatorVersion
        self.buildXml = os.path.join(buildRoot, "build", "build.xml")
        self.distroFile = None
        self.runtimeDistroBasename = None
        self.runtimeDistroFile = None
        self.vnuImageDirname = "vnu-runtime-image"
        self.vnuImageDir = os.path.join(distDir, self.vnuImageDirname)
        self.vnuModuleInfoDir = os.path.join(distDir, "vnu")
        self.minDocPath = os.path.join(buildRoot, 'build', 'minDoc.html')
        self.docs = ["README.md", "LICENSE"]

    def writeHash(self, filename, md5OrSha1):
        if Path(filename).suffix not in {".jar", ".war", ".zip"}:
            return
        BLOCKSIZE = 65536
        hasher = md5()
        if md5OrSha1 == "sha1":
            hasher = sha1()
        with open(filename, 'rb') as f:
            buf = f.read(BLOCKSIZE)
            while len(buf) > 0:
                hasher.update(buf)
                buf = f.read(BLOCKSIZE)
        o = open("%s.%s" % (filename, md5OrSha1), 'w')
        o.write(f"{hasher.hexdigest()}  {os.path.basename(filename)}")
        o.close()

    def writeHashes(self, whichDir):
        files = [f for f in os.listdir(whichDir)
                 if os.path.isfile(os.path.join(whichDir, f))]
        for filename in files:
            if os.path.basename(filename) in self.docs:
                continue
            self.writeHash(os.path.join(whichDir, filename), "md5")
            self.writeHash(os.path.join(whichDir, filename), "sha1")

    def sign(self, whichDir):
        if not os.path.exists(whichDir):
            return
        files = [f for f in os.listdir(whichDir)
                 if os.path.isfile(os.path.join(whichDir, f))]
        for filename in files:
            if os.path.basename(filename) in self.docs:
                continue
            if Path(filename).suffix == ".asc":
                continue
            cmd = f"{gpgCmd} --yes -ab {os.path.join(whichDir, filename)}"
            runCmdFromString(cmd)

    def checkMavenCentralVersion(self):
        latest = getMavenCentralLatestVersion()
        return latest == validatorVersion

    def createMavenArtifacts(self):
        ensureDirExists(distDir)
        runAnt(shlex.split(f"-Dversion={self.version} -f {self.buildXml}"),
               "validator-artifacts")

    def signMavenArtifacts(self):
        self.sign(os.path.join(mavenArtifactsDir))

    def testMavenArtifact(self):
        print("Testing Maven artifact...")
        testScriptPath = os.path.join(buildRoot, "tests", "maven-integration",
                                      "test-maven-integration.sh")
        if not os.path.exists(testScriptPath):
            print(f"Error: Test script not found: {testScriptPath}")
            sys.exit(1)

        # Change to test directory and run the test script
        testDir = os.path.join(buildRoot, "tests", "maven-integration")
        originalDir = os.getcwd()
        try:
            os.chdir(testDir)
            runCmd(["./test-maven-integration.sh", "local"])
        finally:
            os.chdir(originalDir)

    def createMavenBundle(self):
        print(f"Building {distDir}/validator-{self.version}-bundle.jar")
        runAnt(shlex.split(f"-Dversion={self.version} -f {self.buildXml}"),
               "validator-bundle")

    def createJarOrWar(self, jarOrWar):
        whichDir = distDir
        distJarOrWar = "build/dist"
        if jarOrWar == "war":
            distJarOrWar = "build/dist-war"
            whichDir = distWarDir
            removeIfDirExists(os.path.join(whichDir, "war"))
            ensureDirExists(whichDir)
            os.mkdir(os.path.join(whichDir, "war"))
        ensureDirExists(distDir)
        self.version = validatorVersion
        runAnt(['-Ddist=' + distJarOrWar,
                '-Dversion=' + self.version,
                '-f', self.buildXml],
               jarOrWar)
        if jarOrWar == "jar":
            self.checkJar(call_createJarOrWar=False)
        else:
            self.writeHashes(distWarDir)

    def createRuntimeImage(self):
        if javaEnvVersion < 9:
            return
        if not os.path.exists(vnuJar):
            self.createJarOrWar("jar")
        runCmd([jdepsCmd, '--ignore-missing-deps',
                '--generate-open-module', distDir, vnuJar])

        moduleInfoPath = os.path.join(self.vnuModuleInfoDir,
                                      "module-info.java")
        with open(moduleInfoPath, 'r') as f:
            content = f.read()

        content = content.replace('org.relaxng.datatype',
                                  'nu.validator.vendor.relaxng.datatype')
        content = content.replace('com.thaiopensource',
                                  'nu.validator.vendor.thaiopensource')
        content = content.replace('org.iso_relax',
                                  'nu.validator.vendor.iso_relax')
        content = content.replace('jp.gr.xml.relax',
                                  'nu.validator.vendor.jp.gr.xml.relax')

        # Handle duplicate “provides” declarations
        lines = content.split('\n')
        filteredLines = []
        skipUntilSemicolon = False
        seenProvides = set()

        for line in lines:
            if line.strip().startswith('provides '):
                # Extract the service interface name
                serviceMatch = line.strip().split(' with')[0].replace('provides ', '').strip()  # nopep8
                if serviceMatch in seenProvides:
                    # This is a duplicate - skip this “provides” block
                    skipUntilSemicolon = True
                    continue
                else:
                    seenProvides.add(serviceMatch)
                    filteredLines.append(line)
            elif skipUntilSemicolon:
                # Skip lines til we hit semicolon ending the “provides” block
                if line.strip().endswith(';'):
                    skipUntilSemicolon = False
                continue
            else:
                filteredLines.append(line)

        # Remove the closing brace and last “provides” block, if needed
        while filteredLines and (filteredLines[-1].strip() == '}' or
                                 filteredLines[-1].strip() == ''):
            filteredLines.pop()

        # Write back the modified content
        with open(moduleInfoPath, 'w') as f:
            f.write('\n'.join(filteredLines))
            f.write('\n    uses org.eclipse.jetty.http.HttpFieldPreEncoder;\n')
            f.write('    uses javax.json.spi.JsonProvider;\n')
            f.write('}\n')
        runCmd([javacCmd, '-nowarn', '--patch-module', 'vnu=' + vnuJar,
                os.path.join(distDir, 'vnu', 'module-info.java')])
        runCmd([jarCmd, '--update',
                '--file', vnuJar,
                '--module-version=' + validatorVersion,
                '-C', os.path.join(distDir, 'vnu'), 'module-info.class'])
        removeIfDirExists(self.vnuModuleInfoDir)
        removeIfDirExists(self.vnuImageDir)
        runCmd([jlinkCmd, '--launcher',
                'vnu=vnu/nu.validator.client.SimpleCommandLineValidator',
                '--strip-debug', '--no-header-files', '--no-man-pages',
                '--compress=zip-6',
                '--output', self.vnuImageDir, '--module-path', vnuJar,
                '--add-modules', 'jdk.crypto.ec',
                '--add-modules', 'vnu'])
        self.checkRuntimeImage()
        os.chdir(distDir)
        removeIfExists(self.runtimeDistroFile)
        for fname in {"LICENSE", "README.md"}:
            shutil.copy(os.path.join("..", "..", fname), self.vnuImageDirname)
        shutil.make_archive(self.runtimeDistroBasename, 'zip', ".",
                            self.vnuImageDirname)
        os.chdir(os.path.join('..', '..'))
        self.writeHashes(distDir)

    def updatePackageJsonAndReadme(self):
        packageJson = os.path.join(buildRoot, "package.json")
        removeIfExists(os.path.join(buildRoot, "README.md~"))
        readMe = os.path.join(buildRoot, "README.md")
        npmMd = os.path.join(buildRoot, "npm.md")
        shutil.copy(npmMd, readMe)
        with open(packageJson, 'r') as original:
            copy = json.load(original)
        copy['version'] = validatorVersion
        with open(packageJson, 'w') as f:
            json.dump(copy, f)

    def createGitHubPackageJson(self, packageJson):
        with open(packageJson, 'r') as original:
            copy = json.load(original)
        copy['name'] = "@validator/vnu-jar"
        copy['version'] = validatorVersion
        copy['publishConfig'] = {"registry": "https://npm.pkg.github.com"}
        with open(packageJson, 'w') as f:
            json.dump(copy, f)

    def createNpmReadme(self, readMe, readMeCopy):
        splitAt = "see **Pulling the Docker image** below"
        npmFragment = os.path.join(buildRoot, "npm.md")
        npmReadme = open(readMe, 'w')
        skip = False
        for line in readMeCopy.splitlines():
            if line.find(splitAt) != -1:
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
                npmReadme.write(line + '\n')
        npmReadme.close()

    def removeExtras(self, whichDir):
        sigsums = re.compile(r"^.+\.asc$|^.+\.md5$|.+\.sha1$")
        for filename in findFiles(whichDir):
            if (os.path.basename(filename) in self.docs or
                    sigsums.match(filename)):
                removeIfExists(filename)

    def uploadToMavenCentral(self):
        if self.checkMavenCentralVersion():
            return
        self.createMavenBundle()
        cmd = f"""{curlCmd} --request POST \
             --form "bundle=@{distDir}/validator-{self.version}-bundle.jar" \
             --header "Authorization: Bearer {os.getenv("MAVEN_USER_TOKEN")}" \
             "https://central.sonatype.com/api/v1/publisher/upload?name=validator-{self.version}&publishingType=AUTOMATIC"
             """  # nopep8
        runCmdFromString(cmd)

    def installNpm(self,):
        print("npmjs package version: " + self.version)
        self.updatePackageJsonAndReadme()
        runCmdFromString(f"{npmCmd} install --save")
        runCmdFromString(f"{npmCmd} link vnu-jar")
        runCmdFromString(f"{npmCmd} link")
        runCmdFromString(f"{gitCmd} checkout package.json")
        runCmdFromString(f"{gitCmd} checkout README.md")

    def uploadNpm(self, tag=None):
        self.version = validatorVersion
        url = f"https://registry.npmjs.org/vnu-jar/{self.version}"
        try:
            with urlopen(url):
                print(f"npmjs package version {self.version} already exists. Skipping.")  # nopep8
                return
        except HTTPError as e:
            if e.code != 404:
                raise
        if not os.path.exists(vnuJar):
            print(f"Error: {vnuJar} not found.")
            sys.exit(1)
        # Verify the jar file is valid (not a placeholder or error page)
        jar_size = os.path.getsize(vnuJar)
        if jar_size < 1000000:
            print(f"Error: {vnuJar} is too small ({jar_size} bytes), likely corrupted!")  # nopep8
            sys.exit(1)
        print(f"vnu.jar size: {jar_size} bytes")
        self.updatePackageJsonAndReadme()
        if tag:
            runCmdFromString(
                    f"{npmCmd} publish --provenance --access public --tag {tag}")  # nopep8
        else:
            runCmdFromString(
                    f"{npmCmd} publish --provenance --access public")
        runCmdFromString(f"{gitCmd} checkout package.json")
        runCmdFromString(f"{gitCmd} checkout README.md")

    def checkJar(self, call_createJarOrWar=True):
        if not os.path.exists(vnuJar):
            if call_createJarOrWar:
                self.createJarOrWar("jar")

        args = [javaCmd]
        if stackSize != "":
            args.append('-Xss' + stackSize + 'k')

        args.append('-jar')
        args.append(vnuJar)

        formats = ["gnu", "xml", "json", "text"]
        for _format in formats:
            runCmd(args + ['--format', _format, self.minDocPath])
        # also make sure it works even w/o --format value; returns gnu output
        runCmd(args + [self.minDocPath])
        runCmd(args + ['--version'])

    def checkRuntimeImage(self):
        if javaEnvVersion < 9:
            return
        vnuRunScript = os.path.join(self.vnuImageDir, 'bin', 'vnu')
        if os.path.exists(os.path.join(self.vnuImageDir, 'bin', 'vnu.bat')):
            vnuRunScript = os.path.join(self.vnuImageDir, 'bin', 'vnu.bat')
        formats = ["gnu", "xml", "json", "text"]
        for _format in formats:
            runCmd([vnuRunScript, '--format', _format, self.minDocPath])
        # also make sure it works even w/o --format value; returns gnu output
        runCmd([vnuRunScript, self.minDocPath])
        runCmd([vnuRunScript, '--version'])

    def checkUrlWithService(self, url):
        print("Checking %s" % url)
        try:
            print(urlopen(url).read())
        except HTTPError as e:
            print(e.reason)
            sys.exit(1)
        except URLError as e:
            print(e.reason)
            sys.exit(1)

    def checkServiceWithJar(self, url):
        if not os.path.exists(os.path.join(buildRoot, "jars")):
            self.buildAll()
        if not os.path.exists(vnuJar):
            self.createJarOrWar("jar")
        print("Checking service using jar...")
        if isServiceUp(False):
            print("Service is already/still running at " + bindAddress +
                  ":" + portNumber)
            print("Stop it first, then retry.")
            sys.exit(1)
        args = getRunArgs(str(int(heapSize) * 1024))
        daemon = subprocess.Popen([javaCmd, ] + args)
        waitUntilServiceIsReady()
        self.checkUrlWithService(url)
        daemon.terminate()
        waitUntilServiceIsDown()

    def checkServiceWithRuntimeImage(self, url):
        if javaEnvVersion < 9:
            return
        if self.runtimeDistroFile is None \
                or not os.path.exists(os.path.join(distDir,
                                                   self.runtimeDistroFile)):
            self.createRuntimeImage()
        print("Checking service using runtime image...")
        if isServiceUp(False):
            print("Service is already/still running at " + bindAddress +
                  ":" + portNumber)
            print("Stop it first, then retry.")
            sys.exit(1)
        args = getRunArgs(str(int(heapSize) * 1024), "image")
        daemon = subprocess.Popen([os.path.join(distDir, self.vnuImageDirname,
                                                "bin", "java")] + args)
        waitUntilServiceIsReady()
        self.checkUrlWithService(url)
        daemon.terminate()
        waitUntilServiceIsDown()

    def checkService(self):
        with open(self.minDocPath, "r") as file:
            doc = file.read()
        doc = doc.rstrip().replace(" ", "%20")
        query = "?out=gnu&doc=data:text/html;charset=utf-8,%s" % doc
        url = "http://127.0.0.1:%s/%s" % (portNumber, query)
        self.checkServiceWithJar(url)
        self.checkServiceWithRuntimeImage(url)

    def runValidator(self):
        if not os.path.exists(vnuJar):
            self.createJarOrWar("jar")
        ensureDirExists(os.path.join(buildRoot, "logs"))
        args = getRunArgs(str(int(heapSize) * 1024))
        execCmd(javaCmd, args)

    def runUnitTests(self):
        if not os.path.exists(vnuJar):
            self.createJarOrWar("jar")
        # List of unit test classes to run
        testClasses = [
            'nu.validator.messages.test.MessageEmitterAdapterTest',
            'nu.validator.collections.test.SortedSetTest',
            'nu.validator.io.test.DataUriTest',
            'nu.validator.checker.test.SpeculationRulesCheckerTest',
        ]
        for testClass in testClasses:
            print(f"\nRunning {testClass}...")
            args = [javaCmd]
            if stackSize != "":
                args.append('-Xss' + stackSize + 'k')
            args.append('-classpath')
            args.append(vnuJar)
            args.append(testClass)
            runCmd(args)

    def runTests(self):
        if not os.path.exists(vnuJar):
            self.createJarOrWar("jar")

        self.runUnitTests()

        args = [javaCmd]
        if stackSize != "":
            args.append('-Xss' + stackSize + 'k')

        classpath_item = []
        classpath_item.append(vnuJar)
        args.append('-classpath')
        args.append(os.pathsep.join(classpath_item))
        args.append('nu.validator.client.TestRunner')

        if verbose:
            args.append("--verbose")

        args.append("tests/messages.json")

        runCmd(args)

        # TestRunner only checks HTML files, but we need to test these
        # particular cases, too; so we do that here.
        svgTestArgs = ["--also-check-svg", "--Werror"]
        svgTestArgs.append(os.path.join(
            buildRoot, "tests", "html", "attributes",
            "lang", "missing-lang-attribute-non-html-isvalid.svg"))
        svgTestArgs.append(os.path.join(
            buildRoot, "tests", "svg",
            "stoplight-titles-compatibility.svg"))
        if platform.system() == 'Windows':
            # Something about either the createRuntimeImage() or execCmd()
            # below doesn’t work as expected in a Windows environment.
            return
        if not os.path.exists(vnuCmd):
            self.createRuntimeImage()
        execCmd(vnuCmd, svgTestArgs, True)
        cssTestArgs = ["--skip-non-css"]
        cssTestArgs.append(os.path.join(buildRoot, "tests", "css"))
        execCmd(vnuCmd, cssTestArgs, True)
        docbookTestArgs = ["--schema",
                           "https://docbook.org/xml/5.1/rng/docbook.rng",
                           "--xml"]
        # Test valid DocBook document; expect no output/messages/errors
        testdoc = [os.path.join("tests", "schema-validation",
                                "docbook-valid.xml")]
        execCmd(vnuCmd, docbookTestArgs + testdoc, True)
        # Test invalid DocBook document; expect output (an error message)
        testdoc = [os.path.join("tests", "schema-validation",
                                "docbook-invalid.xml")]
        cmd = [vnuCmd] + docbookTestArgs + testdoc
        print(shlex.join(cmd))
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode == 0:
            print("Expected validation errors in output, but found none.")
            sys.exit(2)
        # Check for expected error patterns in output (stdout or stderr)
        output = result.stdout + result.stderr
        if "error:" not in output.lower():
            print("Expected validation errors in output, but found none.")
            sys.exit(2)

    def runSpecTests(self):
        if platform.system() == 'Windows':
            # Something about either the createRuntimeImage() or execCmd()
            # below doesn’t work as expected in a Windows environment.
            return
        if not os.path.exists(vnuCmd):
            self.createRuntimeImage()
        specTestArgs = ["--verbose"]
        specTestArgs.extend(["--filterpattern",
                             ".*which is less than the column count.*"])
        specTestArgs.extend(["--filterpattern",
                             ".*Bad value “directory” for attribute “role”.*"])
        specTestArgs.extend([
            "https://html.spec.whatwg.org/",
            "https://dom.spec.whatwg.org/",
            "https://encoding.spec.whatwg.org/",
            "https://fetch.spec.whatwg.org/",
            "https://infra.spec.whatwg.org/",
            "https://notifications.spec.whatwg.org/",
            "https://streams.spec.whatwg.org/",
            "https://url.spec.whatwg.org/",
            "https://urlpattern.spec.whatwg.org/",
            "https://webidl.spec.whatwg.org/",
            ])
        execCmd(vnuCmd, specTestArgs, True)
        legacyEncodingCoverageTestArgs = ["--verbose"]
        legacyEncodingCoverageTestArgs.extend(
                ["--filterpattern",
                 ".*Text run is not in Unicode Normalization Form C.*"])
        legacyEncodingCoverageTestArgs.extend(
                ["--filterpattern",
                 ".*This document appears to be written in.*"])
        legacyEncodingCoverageTestArgs.extend(
                ["--filterpattern",
                 ".*Document uses the Unicode Private Use Area.*"])
        legacyEncodingCoverageTestArgs.extend([
            "https://encoding.spec.whatwg.org/macintosh-bmp.html",
            "https://encoding.spec.whatwg.org/shift_jis.html",
            ])
        execCmd(vnuCmd, legacyEncodingCoverageTestArgs, True)

    def makeTestMessages(self):
        os.chdir("tests")
        runCmdFromString(makeCmd)

    def buildAll(self):
        self.createJarOrWar("jar")


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
    ] + dependencyJarPaths()
    runCmd(args)


def getMavenCentralLatestVersion():
    """Fetch and return the latest version from Maven Central."""
    url = "https://repo1.maven.org/maven2/nu/validator/validator/maven-metadata.xml"  # nopep8
    try:
        with urlopen(url) as response:
            maven_metadata = response.read()
    except HTTPError as e:
        print(f"HTTP Error fetching Maven metadata: {e.reason}")
        sys.exit(1)
    except URLError as e:
        print(f"URL Error fetching Maven metadata: {e.reason}")
        sys.exit(1)
    root = ET.fromstring(maven_metadata)
    latest = root.findtext("./versioning/latest")
    if not latest:
        latest = root.findtext("./versioning/release")
    if not latest:
        print("Could not find latest version on Maven Central")
        sys.exit(1)
    return latest


def checkMavenVersionExists():
    latest = getMavenCentralLatestVersion()
    print(f"Latest version on Maven Central: {latest}")
    print(f"Current version: {validatorVersion}")
    if latest == validatorVersion:
        print(f"Version {validatorVersion} already exists on Maven Central")
        sys.exit(0)
    else:
        print(f"Version {validatorVersion} does not exist on Maven Central")
        sys.exit(1)


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


def localPathToJarCompatName(path):
    return javaSafeNamePat.sub('_', path)


# This function builds all the resources.
# It requires to have built the schemas before through buildSchemaDrivers()
def prepareLocalEntityJar():
    ensureDirExists(filesDir)

    makeUsage(validatorVersion)
    makeCliHelp(validatorVersion)

    buildSchemaDrivers()
    f = open(os.path.join(buildRoot, "resources", "entity-map.txt"))
    o = open(os.path.join(filesDir, "entitymap"), 'w')
    try:
        for line in f:
            url, path = line.strip().split("\t")
            entPath = []
            index = -1
            if path.startswith("schema/html5/"):
                entPath.append(os.path.join(buildRoot, "schema", "html5",
                                            path[13:]))
                entPath.append(os.path.join(buildRoot, "build", "schema",
                                            "html5", path[13:]))
            elif path.startswith("schema/"):
                entPath.append(os.path.join(buildRoot, path))
                entPath.append(os.path.join(buildRoot, "build", path))
            else:
                continue
            if os.path.exists(entPath[0]):
                index = 0
            elif os.path.exists(entPath[1]):
                index = 1
            if index >= 0:
                safeName = localPathToJarCompatName(path)
                safePath = os.path.join(filesDir, safeName)
                o.write("%s\t%s\n" % (url, safeName))
                shutil.copyfile(entPath[index], safePath)
    finally:
        f.close()
        o.close()


def makeUsage(version):
    if os.path.exists(os.path.join(filesDir, "usage")):
        return
    usageLines = []
    with open(os.path.join(buildRoot, "docs", "vnu.1.md"),
              encoding="utf-8") as f:
        for line in f:
            usageLines.append(stripLeadingHashes(line))
            if line.startswith("# OPTIONS"):
                break
    usageLines.append("\n")
    usageLines.append("For details on all options and usage,"
                      + " try the \"--help\" option or see:\n")
    usageLines.append("https://validator.github.io/validator/\n")
    usageLines.append("\n")
    match = re.search(r'\(([^)]*)\)[^()]*$', version)
    if match:
        usageLines.append(f"👉 {version} changelog: https://github.com/validator/validator/commits/{match.group(1)}")  # nopep8
    with open(os.path.join(filesDir, "usage"), "w", encoding="utf-8") as f:
        f.writelines(usageLines)


def makeCliHelp(version):
    if os.path.exists(os.path.join(filesDir, "cli-help")):
        return
    usageLines = []
    with open(os.path.join(buildRoot, "docs", "vnu.1.md"),
              encoding="utf-8") as f:
        for line in f:
            usageLines.append(stripLeadingHashes(line))
    usageLines.append("\n")
    match = re.search(r'\(([^)]*)\)[^()]*$', version)
    if match:
        usageLines.append(f"👉 {version} changelog: https://github.com/validator/validator/commits/{match.group(1)}")  # nopep8
    with open(os.path.join(filesDir, "cli-help"), "w", encoding="utf-8") as f:
        f.writelines(usageLines)


def stripLeadingHashes(line):
    if line.lstrip().startswith("#"):
        i = 0
        while i < len(line) and line[i] == "#":
            i += 1
        while i < len(line) and line[i] == " ":
            i += 1
        return line[i:]
    else:
        return line


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


def runGit(*args, check=True, capture_output=True, text=True):
    """Run a git command and return stdout."""
    return subprocess.run(
        [gitCmd, *args], check=check, capture_output=capture_output, text=text
    ).stdout.strip()


def loadGitSubtreesFile(path: Path):
    if not path.exists():
        print(f"Error: {path} not found.", file=sys.stderr)
        sys.exit(1)

    result = {}
    current_section = None

    for line in path.read_text().splitlines():
        if not line.strip() or line.strip().startswith("#"):
            continue
        if not line.startswith(" "):  # new top-level key
            if not line.endswith(":"):
                print(f"Error: malformed section header: {line}", file=sys.stderr)  # nopep8
                sys.exit(1)
            current_section = line.rstrip(":").strip()
            result[current_section] = {}
        else:
            if current_section is None:
                print(f"Error: key-value pair outside section: {line}", file=sys.stderr)  # nopep8
                sys.exit(1)
            key, _, value = line.strip().partition(":")
            result[current_section][key.strip()] = value.strip()
    return result


def saveGitSubtreesFile(path: Path, data):
    lines = []
    for section, values in data.items():
        lines.append(f"{section}:")
        for key, value in values.items():
            lines.append(f"    {key}: {value}")
    path.write_text("\n".join(lines) + "\n")


def getLastCommit(prefix: str):
    """Return the last commit SHA in the subtree prefix."""
    try:
        return runGit("log", "-1", "--format=%H", "--", prefix)
    except subprocess.CalledProcessError:
        return None


def updateSubtree(dir_name, info):
    remote_url = info.get("remote_url")
    remote_name = info.get("remote_name", Path(dir_name).name)
    branch = info.get("remote_branch", "master")
    last_local_merge = info.get("last_local_merge")
    last_remote_commit = info.get("current_commit")
    skip_updates = info.get("skip_updates")

    if not remote_url:
        print(f"⚠️ Skipping {dir_name}: no remote URL")
        return False, last_local_merge, last_remote_commit

    print(f"\n==> Checking {dir_name} ({branch})...")

    if skip_updates and skip_updates == "true":
        print(f"⏭️ Skipping {dir_name} because it has “skip_updates: true”")
        return False, last_local_merge, last_remote_commit

    try:
        runGit("remote", "add", "-f", remote_name, remote_url)
    except subprocess.CalledProcessError:
        pass

    try:
        runGit("fetch", remote_name,
               f"+refs/heads/*:refs/remotes/{remote_name}/*")
    except subprocess.CalledProcessError:
        print(f"⚠️ Failed to fetch remote {remote_name}, skipping {dir_name}")
        return False, last_local_merge, last_remote_commit

    try:
        remote_sha = runGit("rev-parse",
                            f"refs/remotes/{remote_name}/{branch}")
    except subprocess.CalledProcessError:
        available = runGit("branch", "-r")
        print(f"⚠️ Remote branch '{branch}' not found for {dir_name}, skipping")  # nopep8
        print(f"    Available remote branches:\n{available}")
        return False, last_local_merge, last_remote_commit

    current_last = getLastCommit(dir_name)

    print(f"    Last local commit in subtree: {current_last}")
    print(f"    Last local merge commit:      {last_local_merge}")
    print(f"    Last pulled remote commit:    {last_remote_commit}")
    print(f"    Remote HEAD commit:           {remote_sha}")

    if current_last != last_local_merge:
        print(f"⏭️ {dir_name} has local commits ahead of last merge; skipping update")  # nopep8
        return False, last_local_merge, last_remote_commit

    if last_remote_commit == remote_sha:
        print(f"✅ {dir_name} is already up to date")
        return False, last_local_merge, last_remote_commit

    print(f"🔄 Updating {dir_name} from {remote_name}/{branch}")
    runGit("subtree", "pull", "--prefix", dir_name, remote_name, branch, "--squash")  # nopep8

    new_merge_commit = getLastCommit(dir_name)
    return True, new_merge_commit, remote_sha


def updateSubtrees():
    data = loadGitSubtreesFile(Path(gitSubtreesFile))
    updated_count = 0

    for dir_name, info in data.items():
        updated, new_local_merge, new_remote_commit = updateSubtree(dir_name, info)  # nopep8
        if updated:
            updated_count += 1
            info["last_local_merge"] = new_local_merge
            info["current_commit"] = new_remote_commit

    if updated_count:
        saveGitSubtreesFile(Path(gitSubtreesFile), data)

    print(
        f"\nSummary: {updated_count} subtree(s) updated, "
        f"{len(data) - updated_count} already up to date or skipped due to local commits."  # nopep8
    )


def downloadExtras():
    url = "https://repo1.maven.org/maven2/log4j/apache-log4j-extras/1.2.17/apache-log4j-extras-1.2.17.jar"  # nopep8
    md5sum = "f32ed7ae770c83a4ac6fe6714f98f1bd"
    ensureDirExists(extrasDir)
    path = os.path.join(extrasDir, url[url.rfind("/") + 1:])
    if not os.path.exists(path):
        fetchUrlTo(url, path, md5sum)


def downloadDependencies():
    runAnt([], "dl-all")
    downloadExtras()


def splitHostSpec(spec):
    index = spec.find('/')
    return (spec[0:index], spec[index:])


def waitUntilServiceIsReady():
    if shutil.which("nc"):
        isReady = False
        while (isReady is False):
            isReady = True
            try:
                subprocess.run(["nc", "-z", bindAddress, portNumber],
                               check=True)
            except Exception:
                isReady = False
                time.sleep(1)
    else:
        # TODO: Support an equivalent command on Windows
        time.sleep(15)


def waitUntilServiceIsDown():
    if shutil.which("nc"):
        isReady = True
        while (isReady is True):
            isReady = True
            try:
                subprocess.run(["nc", "-z", bindAddress, portNumber],
                               check=True, stdout=subprocess.DEVNULL,
                               stderr=subprocess.STDOUT)
            except Exception:
                isReady = False
            time.sleep(1)
    else:
        # TODO: Support an equivalent command on Windows
        time.sleep(5)


def isServiceUp(defaultReply):
    if shutil.which("nc"):
        try:
            subprocess.run(["nc", "-z", bindAddress, portNumber],
                           check=True, stdout=subprocess.DEVNULL,
                           stderr=subprocess.STDOUT)
        except Exception:
            return False
        return True
    # TODO: Support an equivalent command on Windows
    return defaultReply


def getTaskChoices():
    return [
        'update-subtrees', 'dldeps', 'checkout', 'build', 'docker-build',
        'docker-run', 'docker-push', 'bundle', 'npm-install',
        'npm-release', 'maven-artifacts', 'maven-sign', 'maven-test',
        'maven-bundle', 'maven-release', 'maven-version-exists', 'image',
        'jar', 'war', 'sign', 'localent', 'deploy', 'tar', 'script',
        'test', 'test-specs', 'unit-tests', 'make-messages', 'check',
        'self-test', 'clean', 'realclean', 'run', 'all', 'completion',
    ]


def getAntTargets():
    antTargetsFile = os.path.join(buildRoot, 'build', '.ant-targets-build.xml')
    if not os.path.exists(antTargetsFile):
        return []
    try:
        with open(antTargetsFile, 'r') as f:
            # Each line contains a target name
            targets = [line.strip() for line in f if line.strip()]
        return targets
    except (IOError, OSError):
        return []


def taskCompleter(prefix, parsed_args, **kwargs):
    if not ARGCOMPLETE_AVAILABLE:
        return []
    from argcomplete.completers import FilesCompleter
    comp_line = os.environ.get('COMP_LINE', '')
    # Only use FilesCompleter if "check" is explicitly typed as a task
    # Look for "check" as a complete word at the end or followed by space
    if comp_line.rstrip().endswith(' check') or ' check ' in comp_line:
        # Make sure "check" is not part of an option value, by checking context
        parts = comp_line.split()
        if 'check' in parts:
            # If "check" is in the parts and at least one item after script,
            # and it looks like it's not in an option value (no "=" sign in
            # the same part), then use FilesCompleter
            for part in parts:
                if part == 'check':
                    return FilesCompleter()(prefix, **kwargs)

    task_choices = getTaskChoices()
    completions = [t for t in task_choices if t.startswith(prefix)]
    if 'ant:'.startswith(prefix):
        completions.append('ant:')
    if prefix.startswith('ant:'):
        antPrefix = prefix[4:]  # Remove 'ant:' prefix
        antTargets = getAntTargets()
        antCompletions = ['ant:' + t for t in antTargets if
                          t.startswith(antPrefix)]
        completions.extend(antCompletions)
    return completions


def detectShell():
    shell_path = os.environ.get('SHELL', '/bin/bash')
    shell_name = os.path.basename(shell_path)
    shell_map = {
        'bash': 'bash',
        'zsh': 'zsh',
        'tcsh': 'tcsh',
        'fish': 'fish',
        'pwsh': 'powershell',
        'powershell': 'powershell',
    }
    return shell_map.get(shell_name, 'bash')


def generateCompletion(script_name):
    if not ARGCOMPLETE_AVAILABLE:
        return 'To enable shell tab completion for this script’s options, do this:\n    pip install argcomplete'  # nopep8
    shell = detectShell()
    if (shell in ["bash", "zsh", "tcsh"]):
        return f'eval "`register-python-argcomplete -s {shell} {script_name}`"'
    else:
        return 'https://github.com/kislyuk/argcomplete/blob/main/contrib/README.rst'  # nopep8


def printCompletionInstructions(script_name):
    if not ARGCOMPLETE_AVAILABLE:
        print('\nTo enable shell tab completion for this script’s options, do this:')  # nopep8
        print('\n    pip install argcomplete')
        return
    print("\nTo enable shell tab completion for this script's options,", end="")  # nopep8
    if (detectShell() in ["bash", "zsh", "tcsh"]):
        print(' run this:')
    else:
        print(' see the following:')  # nopep8
    print(f'\n    {generateCompletion(script_name)}')


def applyArgsToGlobals(args):
    global gitCmd, javaCmd, jarCmd, javacCmd, javadocCmd, antCmd, \
        bindAddress, portNumber, controlPort, log4jProps, heapSize, \
        stackSize, javaTargetVersion, html5specLink, aboutPage, \
        denyList, userAgent, deploymentTarget, script, scriptAdditional, \
        serviceName, resultsTitle, messagesLimit, pageTemplate, \
        formTemplate, presetsFile, aboutFile, stylesheetFile, scriptFile, \
        filterFile, allowedAddressType, allowForbiddenHosts, \
        disablePromiscuousSsl, connectionTimeoutSeconds, \
        socketTimeoutSeconds, maxConnPerRoute, maxTotalConnections, \
        statistics, stylesheet, icon, additionalJavaSystemProperties, \
        offline, verbose, antCommonArgs, validatorVersion, genericHost, \
        genericPath, html5Host, html5Path, parsetreeHost, parsetreePath

    simpleMapping = {
        'git': 'gitCmd',
        'ant': 'antCmd',
        'bind_address': 'bindAddress',
        'port': 'portNumber',
        'log4j': 'log4jProps',
        'heap': 'heapSize',
        'html5link': 'html5specLink',
        'about': 'aboutPage',
        'denylist': 'denyList',
        'stylesheet': 'stylesheet',
        'user_agent': 'userAgent',
        'script': 'script',
        'name': 'serviceName',
        'results_title': 'resultsTitle',
        'messages_limit': 'messagesLimit',
        'page_template': 'pageTemplate',
        'form_template': 'formTemplate',
        'presets_file': 'presetsFile',
        'about_file': 'aboutFile',
        'stylesheet_file': 'stylesheetFile',
        'script_file': 'scriptFile',
        'filter_file': 'filterFile',
        'allowed_address_type': 'allowedAddressType',
        'allow_forbidden_hosts': 'allowForbiddenHosts',
        'connection_timeout': 'connectionTimeoutSeconds',
        'socket_timeout': 'socketTimeoutSeconds',
        'max_requests': 'maxConnPerRoute',
        'max_total_connections': 'maxTotalConnections',
        'additional_java_system_properties': 'additionalJavaSystemProperties',
    }

    for argName, globalName in simpleMapping.items():
        if hasattr(args, argName):
            globals()[globalName] = getattr(args, argName)

    # Handle conditional/special mappings
    antCommonArgs.extend(args.ant_extra_arg)
    if args.offline:
        antCommonArgs.append('-Doffline=true')
        offline = True
    if args.control_port:
        controlPort = args.control_port
    if args.stacksize is not None:
        stackSize = args.stacksize
    if args.javaversion:
        javaTargetVersion = args.javaversion
    if args.icon:
        icon = args.icon
    if args.scp_target:
        deploymentTarget = args.scp_target
    if args.script_additional is not None:
        scriptAdditional = args.script_additional
    if args.genericpath:
        (genericHost, genericPath) = splitHostSpec(args.genericpath)
    if args.html5path:
        (html5Host, html5Path) = splitHostSpec(args.html5path)
    if args.parsetreepath:
        (parsetreeHost, parsetreePath) = splitHostSpec(args.parsetreepath)
    if args.promiscuous_ssl == 'off':
        disablePromiscuousSsl = 1
    else:
        disablePromiscuousSsl = 0
    if args.statistics:
        statistics = 1
    if args.version:
        validatorVersion = args.version
        ensureDirExists(filesDir)
        makeUsage(validatorVersion)
        makeCliHelp(validatorVersion)
    if args.verbose:
        antCommonArgs.append('-verbose')
        verbose = True

    if args.jdk_bin:
        jdkBinDir = args.jdk_bin
        javaExecutable = 'java.exe' if sys.platform == 'win32' else 'java'
        jarExecutable = 'jar.exe' if sys.platform == 'win32' else 'jar'
        javacExecutable = 'javac.exe' if sys.platform == 'win32' else 'javac'
        javadocExecutable = 'javadoc.exe' if sys.platform == 'win32' else 'javadoc'  # nopep8
        javaCmd = os.path.join(jdkBinDir, javaExecutable)
        jarCmd = os.path.join(jdkBinDir, jarExecutable)
        javacCmd = os.path.join(jdkBinDir, javacExecutable)
        javadocCmd = os.path.join(jdkBinDir, javadocExecutable)


def main(argv, script_name=None):
    global gitCmd, javaCmd, jarCmd, javacCmd, javadocCmd, portNumber, \
        controlPort, log4jProps, heapSize, stackSize, javaTargetVersion, \
        html5specLink, aboutPage, denyList, userAgent, deploymentTarget, \
        scriptAdditional, serviceName, resultsTitle, messagesLimit, \
        pageTemplate, formTemplate, presetsFile, aboutFile, stylesheetFile, \
        scriptFile, filterFile, allowedAddressType, disablePromiscuousSsl, \
        connectionTimeoutSeconds, socketTimeoutSeconds, maxTotalConnections, \
        maxConnPerRoute, statistics, stylesheet, script, icon, bindAddress, \
        jdepsCmd, jlinkCmd, javaEnvVersion, additionalJavaSystemProperties, \
        offline, antCmd, validatorVersion, verbose, extrasDir

    if script_name is None:
        script_name = "build/build.py"

    parser = argparse.ArgumentParser(add_help=False, formatter_class=TasksFormatter)  # nopep8
    parser.add_argument("-h", "--help", action=CustomHelpAction, script_name=script_name, help="show this help message and exit")  # nopep8
    parser.add_argument("--about", default="https://about.validator.nu/", help="Sets URL for the about page")  # nopep8
    parser.add_argument("--control-port", help="Sets server control port number (necessary for daemonizing)")  # nopep8
    parser.add_argument("--filter-file", default="resources/message-filters.txt", help="Sets path to the filter file")  # nopep8
    parser.add_argument("--git", default="/usr/bin/git", help="Sets path to the git binary")  # nopep8
    parser.add_argument("--heap", default="512", help="Sets Java heap size in MB")  # nopep8
    parser.add_argument("--html5link", default="https://html.spec.whatwg.org/", help="Sets link URL of the HTML5 spec")  # nopep8
    parser.add_argument("--html5load", default="https://html.spec.whatwg.org/", help="Sets load URL of the HTML5 spec")  # nopep8
    parser.add_argument("--jar", default="/usr/bin/jar", help="Sets path to the jar binary")  # nopep8
    parser.add_argument("--java", default="/usr/bin/java", help="Sets path to the java binary")  # nopep8
    parser.add_argument("--javac", default="/usr/bin/javac", help="Sets path to the javac binary")  # nopep8
    parser.add_argument("--javadoc", default="/usr/bin/javadoc", help="Sets path to the javadoc binary")  # nopep8
    parser.add_argument("--ant", default="ant", help="Sets path to the ant binary")  # nopep8
    parser.add_argument("--javaversion", help="Sets Java VM version to build for")  # nopep8
    parser.add_argument("--jdk-bin", help="Sets paths for all JDK tools")  # nopep8
    parser.add_argument("--log4j", default="resources/log4j.properties", help="Sets path to log4 configuration")  # nopep8
    parser.add_argument("--messages-limit", type=int, default=1000, help="Sets limit on the maximum number of errors+warnings to report for any document before stopping")  # nopep8
    parser.add_argument("--name", default="Validator.nu", help="Sets service name")  # nopep8
    parser.add_argument("--bind-address", default="0.0.0.0", help="Sets server bind address")  # nopep8
    parser.add_argument("--port", default="8888", help="Sets server port number")  # nopep8
    parser.add_argument("--allowed-address-type", choices=['all', 'same-origin', 'none'], default='all', help="Sets which URLs the checker allows.")  # nopep8
    parser.add_argument("--allow-forbidden-hosts", action="store_true", help="Allow requests to \"forbidden\" hosts (localhost, 127.0.0.1, etc.)")  # nopep8
    parser.add_argument("--promiscuous-ssl", choices=['on', 'off'], default='on', help="Don't check SSL/TLS trust chain")  # nopep8
    parser.add_argument("--results-title", default="Validation results", help="Sets title to show on results page")  # nopep8
    parser.add_argument("--script", default="script.js", help="Sets the URL for the script")  # nopep8
    parser.add_argument("--script-additional", help="Sets the URL for a script file to include in addition to the file specified by the --script option.")  # nopep8
    parser.add_argument("--stacksize", help="Sets Java thread stack size in KB")  # nopep8
    parser.add_argument("--stylesheet", default="style.css", help="Sets URL for the style sheet")  # nopep8
    parser.add_argument("--user-agent", default='Validator.nu/LV', help="Sets User-Agent string for checker")  # nopep8
    parser.add_argument("--offline", action="store_true", help="Build offline. Needs prior download of the dependencies with 'dldeps'.")  # nopep8
    parser.add_argument("--version", help="Sets the version of vnu to VERSION")  # nopep8
    parser.add_argument("--verbose", action="store_true", help="Run build & tests verbosely")  # nopep8
    parser.add_argument("--additional-java-system-properties", default="", help="Additional Java system properties")  # nopep8
    parser.add_argument("--ant-extra-arg", action="append", default=[], help="Additional arguments for ant")  # nopep8
    parser.add_argument("--denylist", default="", help="Deny list")  # nopep8
    parser.add_argument("--icon", help="Icon")  # nopep8
    parser.add_argument("--scp-target", help="SCP target")  # nopep8
    parser.add_argument("--genericpath", help="Generic path")  # nopep8
    parser.add_argument("--html5path", help="HTML5 path")  # nopep8
    parser.add_argument("--parsetreepath", help="Parsetree path")  # nopep8
    parser.add_argument("--page-template", default="site/PageEmitter.xml", help="Page template")  # nopep8
    parser.add_argument("--form-template", default="site/FormEmitter.xml", help="Form template")  # nopep8
    parser.add_argument("--presets-file", default="resources/presets.txt", help="Presets file")  # nopep8
    parser.add_argument("--about-file", default="site/about.html", help="About file")  # nopep8
    parser.add_argument("--stylesheet-file", default="site/style.css", help="Stylesheet file")  # nopep8
    parser.add_argument("--script-file", default="site/script.js", help="Script file")  # nopep8
    parser.add_argument("--connection-timeout", type=int, default=120, help="Connection timeout in seconds")  # nopep8
    parser.add_argument("--socket-timeout", type=int, default=5, help="Socket timeout in seconds")  # nopep8
    parser.add_argument("--max-requests", type=int, default=100, help="Max requests per route")  # nopep8
    parser.add_argument("--max-total-connections", type=int, default=200, help="Max total connections")  # nopep8
    parser.add_argument("--max-redirects", type=int, default=20, help="Max redirects")  # nopep8
    parser.add_argument("--statistics", action="store_true", help="Enable statistics")  # nopep8
    if ARGCOMPLETE_AVAILABLE:
        parser.add_argument('tasks', nargs='*', help='Tasks to run').completer = taskCompleter  # nopep8
    else:
        parser.add_argument('tasks', nargs='*', help='Tasks to run')  # nopep8

    # Preprocess argv to handle special cases where arguments after “check”
    # should be passed through to vnu command (not parsed by argparse)
    checkInArgv = False
    checkIndex = -1
    processedArgv = []
    passthroughArgs = []
    for i, arg in enumerate(argv):
        if arg == 'check':
            checkInArgv = True
            checkIndex = i
            processedArgv.append(arg)
        elif checkInArgv and checkIndex >= 0 and i > checkIndex:
            # Everything after “check” should be passed through
            passthroughArgs.append(arg)
        else:
            processedArgv.append(arg)
    if ARGCOMPLETE_AVAILABLE:
        argcomplete.autocomplete(parser)
    # Parse only the processed args (without “pass through” args)
    if checkInArgv:
        args, unknown = parser.parse_known_args(processedArgv)
        # Add “pass through” args to tasks
        checkTaskIndex = args.tasks.index('check')
        args.tasks = args.tasks[:checkTaskIndex + 1] + passthroughArgs
    else:
        args = parser.parse_args(argv)

    # Only check JAVA_HOME if we're not just doing completion
    if '_ARGCOMPLETE' not in os.environ and 'JAVA_HOME' not in os.environ:
        print("Error: The JAVA_HOME environment variable is not set.")
        print("Set the JAVA_HOME environment variable to the pathname" +
              " of the directory where your JDK is installed.")
        sys.exit(1)

    # Only initialize Java-related variables if not in completion mode
    if '_ARGCOMPLETE' not in os.environ:
        JAVA_HOME = os.getenv('JAVA_HOME')
        javaExecutable = 'java.exe' if sys.platform == 'win32' else 'java'
        javacExecutable = 'javac.exe' if sys.platform == 'win32' else 'javac'
        jarExecutable = 'jar.exe' if sys.platform == 'win32' else 'jar'
        jdepsExecutable = 'jdeps.exe' if sys.platform == 'win32' else 'jdeps'
        jlinkExecutable = 'jlink.exe' if sys.platform == 'win32' else 'jlink'
        javadocExecutable = 'javadoc.exe' if sys.platform == 'win32' else 'javadoc'  # nopep8
        javacCmd = os.path.join(JAVA_HOME, 'bin', javacExecutable)
        jarCmd = os.path.join(JAVA_HOME, 'bin', jarExecutable)
        javaCmd = os.path.join(JAVA_HOME, 'bin', javaExecutable)
        jdepsCmd = os.path.join(JAVA_HOME, 'bin', jdepsExecutable)
        jlinkCmd = os.path.join(JAVA_HOME, 'bin', jlinkExecutable)
        javadocCmd = os.path.join(JAVA_HOME, 'bin', javadocExecutable)
        try:
            javaRawVersion = subprocess.check_output([javaCmd, '-version'],
                                                     universal_newlines=True,
                                                     stderr=subprocess.STDOUT)
        except TypeError:
            javaRawVersion = subprocess.check_output([javaCmd, '-version'],
                                                     stderr=subprocess.STDOUT)
        javaRawVersion = list(filter(lambda x: 'version' in x,
                                     javaRawVersion.splitlines()))
        javaEnvVersion = \
            int(javaRawVersion[0].split()[2].strip('"').split('.')[0]
                .replace('-ea', ''))
        if javaEnvVersion < 9:
            javaTargetVersion = ''
        release = Release()
        release.runtimeDistroBasename = getRuntimeDistroBasename()
        release.runtimeDistroFile = release.runtimeDistroBasename + ".zip"
    else:
        return

    applyArgsToGlobals(args)

    tasks = args.tasks
    if not tasks:
        parser.print_help()
        printCompletionInstructions(script_name)
        sys.exit(0)

    for taskIndex, task in enumerate(tasks):
        if task == 'update-subtrees':
            updateSubtrees()
        elif task == 'dldeps':
            downloadDependencies()
        elif task == 'checkout':
            pass
        elif task == 'build':
            release.buildAll()
        elif task == 'docker-build':
            dockerBuild()
        elif task == 'docker-run':
            dockerRun()
        elif task == 'docker-push':
            dockerPush()
        elif task == 'bundle':
            release.createMavenBundle()
        elif task == 'npm-install':
            release.installNpm()
        elif task == 'npm-release':
            release.uploadNpm()
        elif task == 'maven-artifacts':
            release.createMavenArtifacts()
        elif task == 'maven-sign':
            release.signMavenArtifacts()
        elif task == 'maven-test':
            release.testMavenArtifact()
        elif task == 'maven-bundle':
            release.createMavenBundle()
        elif task == 'maven-release':
            release.uploadToMavenCentral()
        elif task == 'maven-version-exists':
            checkMavenVersionExists()
        elif task == 'image':
            release.createRuntimeImage()
        elif task == 'jar':
            release.createJarOrWar("jar")
        elif task == 'war':
            release.createJarOrWar("war")
        elif task == 'sign':
            release.sign(distDir)
            release.sign(distWarDir)
        elif task == 'localent':
            prepareLocalEntityJar()
        elif task == 'deploy':
            deployOverScp()
        elif task == 'tar':
            createTarball()
            createDepTarball()
        elif task == 'script':
            if not stylesheet:
                stylesheet = 'style.css'
            if not script:
                script = 'script.js'
            if not icon:
                icon = 'icon.png'
            generateRunScript()
        elif task == 'test':
            release.runTests()
        elif task == 'test-specs':
            release.runSpecTests()
        elif task == 'unit-tests':
            release.runUnitTests()
        elif task == 'make-messages':
            release.makeTestMessages()
        elif task == 'check':
            if not os.path.exists(vnuCmd):
                release.createRuntimeImage()
            execCmd(vnuCmd, tasks[taskIndex + 1:], True)
            break
        elif task == 'self-test':
            if not stylesheet:
                stylesheet = 'style.css'
            if not script:
                script = 'script.js'
            if not icon:
                icon = 'icon.png'
            release.checkService()
        elif task == 'clean':
            clean()
        elif task == 'realclean':
            realclean()
        elif task == 'run':
            if not stylesheet:
                stylesheet = 'style.css'
            if not script:
                script = 'script.js'
            if not icon:
                icon = 'icon.png'
            release.runValidator()
        elif task == 'all':
            release.buildAll()
            release.runTests()
            if not stylesheet:
                stylesheet = 'style.css'
            if not script:
                script = 'script.js'
            if not icon:
                icon = 'icon.png'
            release.runValidator()
        elif task.startswith("ant:"):
            runAnt([], task[4:])
        elif task == "completion":
            printCompletionInstructions(script_name)
        else:
            parser.error("unrecognized arguments: %s" % task)


if __name__ == '__main__':
    main(sys.argv[1:])
