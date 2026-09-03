@echo off
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%"
call "%ROOT%\mvnw.cmd" clean
exit /b %ERRORLEVEL%
