@echo off
echo @echo off >ant.bat
echo %~dp0ant.bat -Dbuild.dir=%CD%\build %%* >>ant.bat
