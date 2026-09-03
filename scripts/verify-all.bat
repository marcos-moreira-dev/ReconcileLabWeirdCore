@echo off
setlocal EnableExtensions

cd /d "%~dp0\.."

if not exist ".local" mkdir ".local"
set "LOG=.local\verify-all.txt"

> "%LOG%" echo RECONCILELAB 2006 - VERIFICAR TODO
>>"%LOG%" echo ========================================
>>"%LOG%" echo Fecha: %DATE%
>>"%LOG%" echo Hora: %TIME%
>>"%LOG%" echo Repositorio: %CD%
>>"%LOG%" echo.

set "FAILED=0"

>>"%LOG%" echo [ENTORNO]
call "scripts\toolchain.bat" >nul 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL  No se pudo resolver un compilador g++.exe utilizable.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS  Compilador GCC resuelto: %GXX%
  "%GXX%" --version >>"%LOG%" 2>&1
)

if exist "%WX%\include\wx\wx.h" (
  >>"%LOG%" echo PASS  Headers de wxWidgets encontrados: %WX%
) else (
  >>"%LOG%" echo FAIL  Faltan headers de wxWidgets: %WX%
  set "FAILED=1"
)

if exist "%WX%\lib\gcc_lib\libwxmsw30u_core.a" (
  >>"%LOG%" echo PASS  Biblioteca estatica principal de wxWidgets encontrada
) else (
  >>"%LOG%" echo FAIL  Falta la biblioteca estatica principal de wxWidgets
  set "FAILED=1"
)

if exist "%GCC_BIN%windres.exe" (
  >>"%LOG%" echo PASS  windres.exe encontrado: %GCC_BIN%windres.exe
) else (
  >>"%LOG%" echo FAIL  no se encontro windres.exe
  set "FAILED=1"
)

if exist "%WX%\lib\gcc_lib\libwxmsw30u_html.a" (
  >>"%LOG%" echo PASS  Biblioteca HTML de wxWidgets encontrada
) else (
  >>"%LOG%" echo FAIL  Falta la biblioteca HTML de wxWidgets
  set "FAILED=1"
)

>>"%LOG%" echo.
>>"%LOG%" echo [ESTRUCTURA]
for %%D in (src examples help scripts tests docs assets resources) do (
  if exist "%%D\" (
    >>"%LOG%" echo PASS  %%D\
  ) else (
    >>"%LOG%" echo FAIL  %%D\
    set "FAILED=1"
  )
)

for %%F in (
  README.md
  CHANGELOG.md
  ALGORITMO-DISPOSICION-DE-DIAGRAMAS.md
  assets\branding\reconcilelab-github.png
  assets\branding\reconcilelab-icon.png
  assets\branding\reconcilelab.ico
  resources\reconcilelab.rc
  app.manifest
  src\engine\SearchEngine.cpp
  src\runtime\RuntimeController.cpp
  src\ui\MainFrame.cpp
  src\ui\GraphCanvas.cpp
  src\ui\WorkspacePanel.cpp
  src\ui\CoverageGauge.cpp
  src\ui\NewCaseDialog.cpp
  src\io\CaseFile.cpp
  src\io\MoneyText.cpp
  help\index.html
  help\images\flujo-conciliacion.png
  help\images\lectura-lienzo.png
  help\images\ambiguedad.png
  docs\ARCHITECTURE.md
  docs\CODE-READING-GUIDE.md
  docs\COMENTARIOS-Y-LECTURA-DEL-CODIGO.md
  scripts\toolchain.bat
) do (
  if exist "%%F" (
    >>"%LOG%" echo PASS  %%F
  ) else (
    >>"%LOG%" echo FAIL  %%F
    set "FAILED=1"
  )
)

>>"%LOG%" echo.
>>"%LOG%" echo [EJEMPLOS]
for %%F in (
  01-small-exact.case
  02-multiple-solutions.case
  03-no-solution.case
  04-pruning-showcase.case
  05-medium-reconciliation.case
  06-stress.case
  07-horizontal-compact.case
) do (
  if exist "examples\%%F" (
    >>"%LOG%" echo PASS  examples\%%F
  ) else (
    >>"%LOG%" echo FAIL  examples\%%F
    set "FAILED=1"
  )
)

>>"%LOG%" echo.
>>"%LOG%" echo [LIMPIEZA]
call "scripts\clean.bat" /nopause >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL  limpieza
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS  limpieza
)

>>"%LOG%" echo.
>>"%LOG%" echo [COMPILACION]
call "scripts\build.bat" /nopause >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL  compilacion de la aplicacion
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS  compilacion de la aplicacion
)

>>"%LOG%" echo.
>>"%LOG%" echo [PRUEBAS]
call "scripts\test.bat" /nopause >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL  pruebas del nucleo
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS  pruebas del nucleo
)

>>"%LOG%" echo.
>>"%LOG%" echo [SMOKE]
if exist "bin\ReconcileLab.exe" (
  >>"%LOG%" echo PASS  bin\ReconcileLab.exe creado
) else (
  >>"%LOG%" echo FAIL  no se creo el ejecutable
  set "FAILED=1"
)

if exist "help\index.html" (
  >>"%LOG%" echo PASS  existe el punto de entrada de ayuda integrada
) else (
  >>"%LOG%" echo FAIL  falta el punto de entrada de ayuda integrada
  set "FAILED=1"
)

>>"%LOG%" echo.
>>"%LOG%" echo ========================================
if "%FAILED%"=="0" (
  >>"%LOG%" echo RESULTADO: PASS
) else (
  >>"%LOG%" echo RESULTADO: FAIL
)
>>"%LOG%" echo ========================================

type "%LOG%"
echo.
echo Archivo de diagnostico: %CD%\.local\verify-all.txt
pause

if "%FAILED%"=="0" exit /b 0
exit /b 1
