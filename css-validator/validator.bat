@echo off
set old_classpath=%CLASSPATH%
set old_path=%PATH%

REM set the CLASSPATH
set CLASSPATH=D:\validator\classes.zip

REM set the PATH
set PATH=D:\jdk1.1.5\bin;%PATH%

REM the default package
set PACKAGE=CSS.CSS

java -Djava.protocol.handler.pkgs="org.w3c.www.protocol|sun.net.www.protocol" %PACKAGE%.StyleSheetCom %1 %2 %3 %4 %5 %6 %7 %8 %9

set CLASSPATH=%old_classpath%
set PATH=%old_path%

set PACKAGE=
set old_classpath=
set old_path=
