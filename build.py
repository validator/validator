#!/usr/bin/python
import os
import shutil

javacCmd = 'javac'
jarCmd = 'jar'
javaCmd = 'java'
javadocCmd = 'javadoc'

buildRoot = '.'

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
  
buildUtil()
buildDatatypeLibrary()
buildNonSchema()
buildXmlParser()
buildHtmlParser()
buildValidator()

runValidator()