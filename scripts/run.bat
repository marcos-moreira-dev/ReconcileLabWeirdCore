@echo off
cd /d "%~dp0\.."
if not exist "bin\ReconcileLab.exe" (
    call "scripts\build.bat" /nopause
    if errorlevel 1 exit /b 1
)
start "" "bin\ReconcileLab.exe"
