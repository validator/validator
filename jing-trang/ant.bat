@echo off
setlocal
if exist "%JAVA_HOME%\bin\java.exe" goto found
echo You must set JAVA_HOME to the directory containing the JDK
exit /b 1
:found
rem %~dp0 gives the directory including the trailing slash
rem we need the directory without the trailing slash, so add the dot
set JING_TRANG_HOME=%~dp0.
"%JAVA_HOME%\bin\java.exe" -classpath "%JING_TRANG_HOME%\lib\ant-launcher.jar" "-Dant.home=%JING_TRANG_HOME%" org.apache.tools.ant.launch.Launcher -buildfile "%JING_TRANG_HOME%\build.xml" %*
