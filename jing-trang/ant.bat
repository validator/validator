@echo off
setlocal
set JING_HOME=%~dp0
%JAVA_HOME%\bin\java -Dant.home=%JING_HOME% -cp %JING_HOME%lib/ant-launcher.jar org.apache.tools.ant.launch.Launcher -buildfile %JING_HOME%build.xml %*
