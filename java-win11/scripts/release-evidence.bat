@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "JAR=%ROOT%\target\reconcilelab-java-win11-0.7.5.jar"
set "EXE=%ROOT%\target\app-image\ReconcileLab\ReconcileLab.exe"
set "OUT=%ROOT%\.local\release-evidence.txt"

if not exist "%JAR%" (
  echo ERROR: no existe %JAR%
  exit /b 1
)

if not exist "%EXE%" (
  echo ERROR: no existe %EXE%
  exit /b 1
)

java -cp "%JAR%" ^
  com.marcosmoreiradev.reconcilelab.app.ReleaseEvidence ^
  "%OUT%" ^
  "%JAR%" ^
  "%EXE%"

if errorlevel 1 (
  echo RELEASE EVIDENCE: FAIL
  exit /b 1
)

echo.
echo RELEASE EVIDENCE: PASS
type "%OUT%"
exit /b 0
