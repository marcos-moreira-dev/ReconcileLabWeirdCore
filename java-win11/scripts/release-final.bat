@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."
set "INSTALLER=%ROOT%\target\installer\ReconcileLab-Java-Windows11-Setup-0.7.5.exe"
set "SHA=%ROOT%\target\installer\ReconcileLab-Java-Windows11-Setup-0.7.5.sha256"

echo ReconcileLab Java 0.7.5 - release final
echo ======================================

call "%ROOT%\scripts\release-check.bat"
if errorlevel 1 (
  echo.
  echo RELEASE FINAL: FAIL en release-check.
  exit /b 1
)

call "%ROOT%\scripts\verify-installer-prereqs.bat"
if errorlevel 1 (
  echo.
  echo RELEASE FINAL: FAIL en prerequisitos del instalador.
  exit /b 1
)

call "%ROOT%\scripts\build-installer.bat"
if errorlevel 1 (
  echo.
  echo RELEASE FINAL: FAIL al generar el instalador.
  exit /b 1
)

if not exist "%INSTALLER%" (
  echo RELEASE FINAL: FAIL; falta el instalador canonico.
  exit /b 1
)

if not exist "%SHA%" (
  echo RELEASE FINAL: FAIL; falta el SHA-256.
  exit /b 1
)

echo.
echo RELEASE FINAL: PASS
echo Version: 0.7.5
echo Instalador: %INSTALLER%
echo SHA-256: %SHA%
echo.
echo Siguiente paso manual:
echo 1. Instalar el EXE y realizar smoke de distribucion.
echo 2. Commit final.
echo 3. Crear tag anotado v0.7.5.
echo 4. Push de main y del tag.
echo 5. Publicar GitHub Release y adjuntar EXE + SHA-256.
exit /b 0
