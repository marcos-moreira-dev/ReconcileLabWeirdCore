@echo off
rem Helper interno. Debe invocarse con CALL para conservar WIX_BIN y PATH.
setlocal EnableExtensions

set "WIX_BIN="
set "PROGRAM_FILES_X86=%ProgramFiles(x86)%"
set "PROGRAM_FILES=%ProgramFiles%"

rem 1. Preferir una instalacion ya disponible en PATH.
for /f "delims=" %%I in ('where candle.exe 2^>nul') do (
  if not defined WIX_BIN set "WIX_BIN=%%~dpI"
)

if defined WIX_BIN (
  if exist "%WIX_BIN%light.exe" goto :publish
  set "WIX_BIN="
)

rem 2. Variable WIX, cuando el instalador la haya configurado.
if defined WIX call :probe "%WIX%\bin"
if defined WIX_BIN goto :append

rem 3. Rutas estandar conocidas.
call :probe "%PROGRAM_FILES_X86%\WiX Toolset v3.14\bin"
if defined WIX_BIN goto :append

call :probe "%PROGRAM_FILES_X86%\WiX Toolset v3.11\bin"
if defined WIX_BIN goto :append

call :probe "%PROGRAM_FILES%\WiX Toolset v3.14\bin"
if defined WIX_BIN goto :append

call :probe "%PROGRAM_FILES%\WiX Toolset v3.11\bin"
if defined WIX_BIN goto :append

endlocal
exit /b 1

:probe
if "%~1"=="" exit /b 0
if not exist "%~1\candle.exe" exit /b 0
if not exist "%~1\light.exe" exit /b 0
set "WIX_BIN=%~1"
exit /b 0

:append
set "PATH=%WIX_BIN%;%PATH%"

:publish
endlocal & set "WIX_BIN=%WIX_BIN%" & set "PATH=%PATH%"
exit /b 0
