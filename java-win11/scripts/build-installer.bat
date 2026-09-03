@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "INPUT=%ROOT%\target\jpackage-input"
set "DEST=%ROOT%\target\installer"
set "APPJAR=reconcilelab-java-win11-0.7.5.jar"
set "MAIN_CLASS=com.marcosmoreiradev.reconcilelab.app.Launcher"
set "ICON=%ROOT%\..\assets\branding\reconcilelab.ico"
set "ASSOC=%ROOT%\packaging\reconcilelab-case.properties"
set "RELEASE_EXE=ReconcileLab-Java-Windows11-Setup-0.7.5.exe"
set "RELEASE_SHA=ReconcileLab-Java-Windows11-Setup-0.7.5.sha256"

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

set "GENERATED_EXE="
for %%F in ("%DEST%\*.exe") do (
  if not defined GENERATED_EXE set "GENERATED_EXE=%%~fF"
)

if not defined GENERATED_EXE (
  echo ERROR: jpackage termino sin producir un instalador EXE.
  exit /b 1
)

if exist "%DEST%\%RELEASE_EXE%" del /q "%DEST%\%RELEASE_EXE%"
move /y "%GENERATED_EXE%" "%DEST%\%RELEASE_EXE%" >nul
if errorlevel 1 (
  echo ERROR: no se pudo normalizar el nombre del instalador.
  exit /b 1
)

powershell.exe -NoProfile -ExecutionPolicy Bypass -Command ^
  "$h=(Get-FileHash -Algorithm SHA256 '%DEST%\%RELEASE_EXE%').Hash.ToLower(); " ^
  "Set-Content -Encoding ASCII -NoNewline -Path '%DEST%\%RELEASE_SHA%' " ^
  "-Value ($h + '  %RELEASE_EXE%')"

if errorlevel 1 (
  echo ERROR: no se pudo generar SHA-256 del instalador.
  exit /b 1
)

echo.
echo INSTALLER PASS
echo Version: 0.7.5
echo Vendor: Marcos Moreira Dev
echo Upgrade UUID: dfeca1bd-c205-5e23-8e33-74ca121367d1
echo Asociacion: .case
echo Instalador: %DEST%\%RELEASE_EXE%
echo SHA-256:   %DEST%\%RELEASE_SHA%
exit /b 0
