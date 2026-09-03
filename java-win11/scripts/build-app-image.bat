@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "INPUT=%ROOT%\target\jpackage-input"
set "DEST=%ROOT%\target\app-image"
set "APPJAR=reconcilelab-java-win11-0.7.5.jar"
set "MAIN_CLASS=com.marcosmoreiradev.reconcilelab.app.Launcher"
set "ICON=%ROOT%\..\assets\branding\reconcilelab.ico"

cd /d "%ROOT%"

where jpackage >nul 2>nul
if errorlevel 1 (
  echo ERROR: jpackage no esta disponible. Usa Eclipse Temurin JDK 21 completo.
  exit /b 1
)

call "%ROOT%\mvnw.cmd" clean package dependency:copy-dependencies ^
  -DincludeScope=runtime ^
  -DoutputDirectory=target\jpackage-deps

if errorlevel 1 exit /b 1

if exist "%INPUT%" rmdir /s /q "%INPUT%"
if exist "%DEST%" rmdir /s /q "%DEST%"

mkdir "%INPUT%"
mkdir "%DEST%"

copy /y "%ROOT%\target\%APPJAR%" "%INPUT%\" >nul
copy /y "%ROOT%\target\jpackage-deps\*.jar" "%INPUT%\" >nul

jpackage ^
  --type app-image ^
  --input "%INPUT%" ^
  --dest "%DEST%" ^
  --name "ReconcileLab" ^
  --main-jar "%APPJAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --app-version "0.7.5" ^
  --vendor "Marcos Moreira Dev" ^
  --description "Conciliacion y trazabilidad por monto" ^
  --icon "%ICON%"

if errorlevel 1 exit /b 1

if not exist "%DEST%\ReconcileLab\ReconcileLab.exe" (
  echo ERROR: jpackage termino sin producir ReconcileLab.exe.
  exit /b 1
)

echo.
echo APP-IMAGE PASS
echo Vendor: Marcos Moreira Dev
echo Namespace: com.marcosmoreiradev.reconcilelab
echo Resultado: %DEST%\ReconcileLab
exit /b 0
