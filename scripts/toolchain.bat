@echo off
rem ---------------------------------------------------------------------------
rem ReconcileLab 2006 - local toolchain resolver
rem
rem This batch file is intentionally label-free. Windows XP cmd.exe is the
rem compatibility target, so the resolver uses only simple SET/IF/FOR commands
rem and returns its findings to the calling build/test/verify script.
rem
rem Resolution order:
rem   1. Existing GXX variable, if it points to a real compiler.
rem   2. Existing GCC variable plus \bin\g++.exe.
rem   3. The XP laboratory path detected during verification: C:\TDM-GCC-9.X.
rem   4. Historical hint C:\TDM-GCC-9.8.
rem   5. Any C:\TDM-GCC-* installation.
rem   6. g++.exe already reachable through PATH.
rem ---------------------------------------------------------------------------

if not defined WX set "WX=C:\wxWidgets-3.0.5"

set "FOUND_GXX="

if defined GXX if exist "%GXX%" set "FOUND_GXX=%GXX%"

if not defined FOUND_GXX if defined GCC if exist "%GCC%\bin\g++.exe" set "FOUND_GXX=%GCC%\bin\g++.exe"

if not defined FOUND_GXX if exist "C:\TDM-GCC-9.X\bin\g++.exe" set "FOUND_GXX=C:\TDM-GCC-9.X\bin\g++.exe"

if not defined FOUND_GXX if exist "C:\TDM-GCC-9.8\bin\g++.exe" set "FOUND_GXX=C:\TDM-GCC-9.8\bin\g++.exe"

if not defined FOUND_GXX for /D %%D in ("C:\TDM-GCC-*") do if not defined FOUND_GXX if exist "%%~fD\bin\g++.exe" set "FOUND_GXX=%%~fD\bin\g++.exe"

if not defined FOUND_GXX for %%I in (g++.exe) do set "FOUND_GXX=%%~$PATH:I"

if not defined FOUND_GXX exit /b 1

set "GXX=%FOUND_GXX%"
for %%I in ("%GXX%") do set "GCC_BIN=%%~dpI"
set "PATH=%GCC_BIN%;%PATH%"

exit /b 0
