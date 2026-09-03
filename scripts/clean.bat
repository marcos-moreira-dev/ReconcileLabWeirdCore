@echo off
setlocal
cd /d "%~dp0\.."

if exist "bin\ReconcileLab.exe" del /Q "bin\ReconcileLab.exe"
if exist "bin\ReconcileLab.exe.manifest" del /Q "bin\ReconcileLab.exe.manifest"
if exist "bin\core-tests.exe" del /Q "bin\core-tests.exe"
if exist ".local\reconcilelab-res.o" del /Q ".local\reconcilelab-res.o"

echo LIMPIEZA PASS
if /I not "%1"=="/nopause" pause
exit /b 0
