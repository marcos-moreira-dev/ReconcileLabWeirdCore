@echo off
setlocal EnableExtensions
set "ROOT=%~dp0.."
cd /d "%ROOT%"
call "%ROOT%\mvnw.cmd" -q javadoc:javadoc
if errorlevel 1 (
  echo JAVADOC: FAIL
  exit /b 1
)
echo.
echo JAVADOC: PASS
echo Resultado: %ROOT%\target\site\apidocs\index.html
exit /b 0
