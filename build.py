#!/usr/bin/python
import os

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

def runJavac(sourceDir, classDir, classPath):
  sourceFiles = findFilesWithExtension(sourceDir, "java")
  runCmd("%s -classpath '%s' -sourcepath '%s' -d '%s' %s"\
		% (javacCmd, classPath, sourceDir, classDir, " ".join(sourceFiles)))

def runJar(classDir, jarFile, sourceDir):
  classFiles = findFilesWithExtension(classDir, "class")
  metaDir = os.path.join(sourceDir, "META-INF")
  classList = map(lambda x: "-C '" + classDir + "' " + x[len(classDir)+1:], 
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

def dependencyJarPaths():
  return findFilesWithExtension("dependencies", "jar")

def buildDatatypeLibrary():
  classPath = os.pathsep.join(dependencyJarPaths())
  buildModule(
    os.path.join(buildRoot, "syntax", "relaxng", "datatype", "java"), 
    "html5-datatypes", 
    classPath)

def buildNonSchema():
  classPath = os.pathsep.join(dependencyJarPaths())
  buildModule(
    os.path.join(buildRoot, "syntax", "relaxng", "datatype", "java"), 
    "html5-datatypes", 
    classPath)


def buildValidator():
  pass

def ensureValidator():
  buildValidator()

def run():
  ensureValidator()
  
buildDatatypeLibrary()
