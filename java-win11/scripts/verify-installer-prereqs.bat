@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "FAILED=0"

echo ReconcileLab - prerequisitos de instalador
echo ==========================================

where java
if errorlevel 1 set "FAILED=1"

where javac
if errorlevel 1 set "FAILED=1"

where jpackage
if errorlevel 1 set "FAILED=1"

set "JAVAC_VERSION="
for /f "tokens=2" %%V in ('javac -version 2^>^&1') do set "JAVAC_VERSION=%%V"

echo %JAVAC_VERSION% | findstr /B /C:"21." >nul
if errorlevel 1 goto :jdk_fail

echo PASS: JDK 21 activo para packaging.
goto :check_wix

:jdk_fail
echo FAIL: el javac activo no es JDK 21.
set "FAILED=1"

:check_wix
call "%ROOT%\scripts\_resolve-wix.bat"
if errorlevel 1 goto :wix_fail

echo PASS: WiX detectado en "%WIX_BIN%".
where candle.exe
where light.exe
goto :finish

:wix_fail
echo FAIL: no se encontro WiX Toolset 3 compatible.
echo Rutas comprobadas:
echo   PATH
echo   variable WIX
echo   directorios estandar de WiX Toolset 3.14 y 3.11
set "FAILED=1"

:finish
echo.
if "%FAILED%"=="0" goto :pass

echo RESULTADO: FAIL
echo Consulta packaging\README.md y docs\BUILD-ONBOARDING.md.
exit /b 1

:pass
echo RESULTADO: PASS
exit /b 0
