@echo off
echo @echo off >ant.bat
echo %~dp0ant.bat -Dtopbuild.dir=%CD% %%* >>ant.bat
