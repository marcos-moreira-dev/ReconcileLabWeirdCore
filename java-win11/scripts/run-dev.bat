@echo off
setlocal
set "ROOT=%~dp0.."
cd /d "%ROOT%"
call "%ROOT%\mvnw.cmd" javafx:run
exit /b %ERRORLEVEL%
