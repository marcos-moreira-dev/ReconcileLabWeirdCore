@echo off
setlocal EnableExtensions
set "ROOT=%~dp0.."
set "LOG=%ROOT%\.local\bootstrap-local.txt"

if not exist "%ROOT%\.local" mkdir "%ROOT%\.local"

(
  echo ReconcileLab Java - bootstrap local
  echo ===================================
  echo.
  echo [JAVA]
  where java
  java -version
  echo.
  echo [JAVAC]
  where javac
  javac -version
  echo.
  echo [MAVEN WRAPPER]
  call "%ROOT%\mvnw.cmd" -version
) > "%LOG%" 2>&1

set "RC=%ERRORLEVEL%"
type "%LOG%"

if not "%RC%"=="0" (
  echo.
  echo Bootstrap: FAIL
  exit /b %RC%
)

echo.
echo Bootstrap: PASS
exit /b 0
