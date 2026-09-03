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

for /f "tokens=2" %%V in ('javac -version 2^>^&1') do set "JAVAC_VERSION=%%V"
echo %JAVAC_VERSION% | findstr /B /C:"21." >nul
if errorlevel 1 (
  echo FAIL: el javac activo no es JDK 21.
  set "FAILED=1"
) else (
  echo PASS: JDK 21 activo para packaging.
)

call "%ROOT%\scripts\_resolve-wix.bat"
if errorlevel 1 (
  echo FAIL: no se encontro WiX Toolset 3 compatible.
  echo Rutas comprobadas:
  echo   PATH
  echo   %%WIX%%\bin
  echo   %%ProgramFiles(x86)%%\WiX Toolset v3.14\bin
  echo   %%ProgramFiles(x86)%%\WiX Toolset v3.11\bin
  set "FAILED=1"
) else (
  echo PASS: WiX detectado en "%WIX_BIN%".
  where candle.exe
  where light.exe
)

echo.
if "%FAILED%"=="0" (
  echo RESULTADO: PASS
) else (
  echo RESULTADO: FAIL
  echo Consulta packaging\README.md y docs\BUILD-ONBOARDING.md.
)

exit /b %FAILED%
