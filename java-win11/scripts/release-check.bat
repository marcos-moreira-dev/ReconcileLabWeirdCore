@echo off
setlocal EnableExtensions

set "ROOT=%~dp0.."

call "%ROOT%\scripts\verify-all.bat"
if errorlevel 1 (
  echo.
  echo RELEASE CHECK: FAIL en verify-all.
  exit /b 1
)

call "%ROOT%\scripts\build-app-image.bat"
if errorlevel 1 (
  echo.
  echo RELEASE CHECK: FAIL en build-app-image.
  exit /b 1
)

call "%ROOT%\scripts\verify-app-image.bat"
if errorlevel 1 (
  echo.
  echo RELEASE CHECK: FAIL en smoke test de app-image.
  exit /b 1
)

call "%ROOT%\scripts\release-evidence.bat"
if errorlevel 1 (
  echo.
  echo RELEASE CHECK: FAIL al generar evidencia.
  exit /b 1
)

echo.
echo RELEASE CHECK: PASS
echo Compilacion, tests, calidad, Javadoc y app-image autocontenida verificados.
echo La app-image ejecuto su launcher con el runtime embebido.
echo Evidencia: %ROOT%\.local\release-evidence.txt
echo Para cerrar una release publica ejecuta scripts\release-final.bat.
exit /b 0
