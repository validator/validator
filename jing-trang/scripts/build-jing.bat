@echo off
setlocal
SET JDK=c:\jdk1.3
set JING_HOME=%~d0%~p0..
%JDK%\bin\java -Dant.home=%JING_HOME% -cp %JING_HOME%\lib\ant.jar;%JING_HOME%\lib\saxon.jar;%JING_HOME%\lib\crimson.jar;%JING_HOME%\lib\optional.jar;%JDK%\lib\tools.jar;%JING_HOME%\lib\junit.jar;%JING_HOME%\lib\regex.jar org.apache.tools.ant.Main -buildfile %JING_HOME%\build.xml %*
