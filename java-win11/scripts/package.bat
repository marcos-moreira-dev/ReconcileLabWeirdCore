@echo off
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%"
call "%ROOT%\mvnw.cmd" clean package
exit /b %ERRORLEVEL%
