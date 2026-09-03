@echo off
setlocal EnableExtensions

cd /d "%~dp0\.."

call "scripts\toolchain.bat"
if errorlevel 1 (
  echo COMPILACION FAIL
  echo No se pudo resolver un compilador g++.exe utilizable.
  if /I not "%1"=="/nopause" pause
  exit /b 1
)

if not exist "%WX%\include\wx\wx.h" (
  echo COMPILACION FAIL
  echo No se encontraron los headers de wxWidgets en %WX%
  if /I not "%1"=="/nopause" pause
  exit /b 1
)

if not exist bin mkdir bin
if not exist .local mkdir .local

set "WINDRES=%GCC_BIN%windres.exe"

echo Compilando ReconcileLab 2006...
echo Compilador: %GXX%
echo Recursos: %WINDRES%
echo wxWidgets: %WX%

if not exist "%WINDRES%" (
  echo COMPILACION FAIL
  echo No se encontro windres.exe.
  if /I not "%1"=="/nopause" pause
  exit /b 1
)

"%WINDRES%" -i "resources\reconcilelab.rc" ^
  -o ".local\reconcilelab-res.o" ^
  --output-format=coff

if errorlevel 1 (
  echo COMPILACION FAIL
  echo No se pudo compilar el recurso de icono.
  if /I not "%1"=="/nopause" pause
  exit /b 1
)

"%GXX%" -std=gnu++98 -O2 -Wall -Wextra -mthreads -D__WXMSW__ -D_UNICODE -DUNICODE ^
  -I"%WX%\include" ^
  -I"%WX%\lib\gcc_lib\mswu" ^
  "src\app\App.cpp" ^
  "src\io\CaseFile.cpp" ^
  "src\io\MoneyText.cpp" ^
  "src\engine\SearchEngine.cpp" ^
  "src\runtime\RuntimeController.cpp" ^
  "src\ui\GraphCanvas.cpp" ^
  "src\ui\WorkspacePanel.cpp" ^
  "src\ui\CoverageGauge.cpp" ^
  "src\ui\HelpFrame.cpp" ^
  "src\ui\NewCaseDialog.cpp" ^
  "src\ui\MainFrame.cpp" ^
  ".local\reconcilelab-res.o" ^
  -o "bin\ReconcileLab.exe" ^
  -L"%WX%\lib\gcc_lib" ^
  -mwindows -mthreads ^
  -lwxmsw30u_html ^
  -lwxmsw30u_core ^
  -lwxbase30u ^
  -lwxtiff -lwxjpeg -lwxpng -lwxzlib -lwxregexu -lwxexpat ^
  -lkernel32 -luser32 -lgdi32 -lcomdlg32 -lwinspool -lwinmm ^
  -lshell32 -lshlwapi -lcomctl32 -lole32 -loleaut32 -luuid ^
  -lrpcrt4 -ladvapi32 -lversion -lws2_32 -lwsock32 -loleacc -luxtheme

if errorlevel 1 (
  echo COMPILACION FAIL
  if /I not "%1"=="/nopause" pause
  exit /b 1
)

copy /Y "app.manifest" "bin\ReconcileLab.exe.manifest" >nul

echo COMPILACION PASS
if /I not "%1"=="/nopause" pause
exit /b 0
