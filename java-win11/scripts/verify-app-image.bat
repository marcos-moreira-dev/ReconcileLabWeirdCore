@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "APPROOT=%ROOT%\target\app-image\ReconcileLab"
set "EXE=%APPROOT%\ReconcileLab.exe"
set "APPJAR=%APPROOT%\app\reconcilelab-java-win11-0.7.5-SNAPSHOT.jar"
set "CONFIG=%APPROOT%\app\ReconcileLab.cfg"
set "JVM=%APPROOT%\runtime\bin\server\jvm.dll"
set "SMOKE=%ROOT%\.local\app-image-smoke.txt"
set "FAILED=0"

echo ReconcileLab - verificar app-image
echo ==================================

if not exist "%EXE%" (
  echo FAIL: no existe %EXE%
  set "FAILED=1"
)

if not exist "%APPJAR%" (
  echo FAIL: no existe %APPJAR%
  set "FAILED=1"
)

if not exist "%CONFIG%" (
  echo FAIL: no existe %CONFIG%
  set "FAILED=1"
)

if not exist "%JVM%" (
  echo FAIL: no existe %JVM%
  set "FAILED=1"
)

if not "%FAILED%"=="0" (
  echo.
  echo RESULTADO: FAIL
  exit /b 1
)

if exist "%SMOKE%" del /q "%SMOKE%"

start "" /wait "%EXE%" "--smoke-file=%SMOKE%"
if errorlevel 1 (
  echo FAIL: ReconcileLab.exe devolvio un error en smoke test.
  exit /b 1
)

if not exist "%SMOKE%" (
  echo FAIL: la app-image no produjo evidencia de arranque.
  exit /b 1
)

findstr /L /C:"product=ReconcileLab" "%SMOKE%" >nul
if errorlevel 1 (
  echo FAIL: evidencia sin identidad de producto.
  exit /b 1
)

findstr /L /C:"vendor=Marcos Moreira Dev" "%SMOKE%" >nul
if errorlevel 1 (
  echo FAIL: evidencia sin vendor canonico.
  exit /b 1
)

findstr /L /C:"package=com.marcosmoreiradev.reconcilelab" "%SMOKE%" >nul
if errorlevel 1 (
  echo FAIL: evidencia sin package canonico.
  exit /b 1
)

echo PASS: EXE presente.
echo PASS: JAR principal presente.
echo PASS: configuracion jpackage presente.
echo PASS: runtime embebido presente.
echo PASS: launcher ejecuto usando el runtime de la app-image.
echo.
type "%SMOKE%"
echo.
echo RESULTADO: PASS
exit /b 0
