@echo off
setlocal EnableExtensions

cd /d "%~dp0\.."

call "scripts\toolchain.bat"
if errorlevel 1 (
  echo PRUEBAS FAIL
  echo No se pudo resolver un compilador g++.exe utilizable.
  if /I not "%1"=="/nopause" pause
  exit /b 1
)

if not exist bin mkdir bin
if not exist .local mkdir .local

echo Compilador de pruebas: %GXX%

"%GXX%" -std=gnu++98 -O2 -Wall -Wextra ^
  "tests\test_core.cpp" ^
  "src\engine\SearchEngine.cpp" ^
  "src\io\CaseFile.cpp" ^
  "src\io\MoneyText.cpp" ^
  -o "bin\core-tests.exe"

if errorlevel 1 (
  echo PRUEBAS FAIL
  echo No se pudo compilar el ejecutable de pruebas.
  if /I not "%1"=="/nopause" pause
  exit /b 1
)

"bin\core-tests.exe"
set "RC=%ERRORLEVEL%"

if /I not "%1"=="/nopause" pause
exit /b %RC%
