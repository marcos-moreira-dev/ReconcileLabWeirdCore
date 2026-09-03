@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "INPUT=%ROOT%\target\jpackage-input"
set "DEST=%ROOT%\target\installer"
set "APPJAR=reconcilelab-java-win11-0.7.5-SNAPSHOT.jar"
set "MAIN_CLASS=com.marcosmoreiradev.reconcilelab.app.Launcher"
set "ICON=%ROOT%\..\assets\branding\reconcilelab.ico"
set "ASSOC=%ROOT%\packaging\reconcilelab-case.properties"

cd /d "%ROOT%"

where jpackage >nul 2>nul
if errorlevel 1 (
  echo ERROR: jpackage no esta disponible. Usa Eclipse Temurin JDK 21 completo.
  exit /b 1
)

call "%ROOT%\scripts\_resolve-wix.bat"
if errorlevel 1 (
  echo ERROR: no se encontro WiX Toolset 3 compatible.
  echo Se comprobaron PATH, %%WIX%% y las rutas estandar de WiX 3.14/3.11.
  echo Consulta packaging\README.md y docs\BUILD-ONBOARDING.md.
  exit /b 1
)

echo WiX: %WIX_BIN%

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
  --type exe ^
  --input "%INPUT%" ^
  --dest "%DEST%" ^
  --name "ReconcileLab" ^
  --main-jar "%APPJAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --app-version "0.7.5" ^
  --vendor "Marcos Moreira Dev" ^
  --description "Conciliacion y trazabilidad por monto" ^
  --icon "%ICON%" ^
  --file-associations "%ASSOC%" ^
  --win-menu ^
  --win-menu-group "Marcos Moreira Dev" ^
  --win-shortcut ^
  --win-dir-chooser ^
  --win-upgrade-uuid "dfeca1bd-c205-5e23-8e33-74ca121367d1" ^
  --install-dir "Marcos Moreira Dev\ReconcileLab"

if errorlevel 1 exit /b 1

dir /b "%DEST%\*.exe" >nul 2>nul
if errorlevel 1 (
  echo ERROR: jpackage termino sin producir un instalador EXE.
  exit /b 1
)

echo.
echo INSTALLER PASS
echo Vendor: Marcos Moreira Dev
echo Upgrade UUID: dfeca1bd-c205-5e23-8e33-74ca121367d1
echo Asociacion: .case
echo Revisa: %DEST%
exit /b 0
