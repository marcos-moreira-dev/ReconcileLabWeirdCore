@echo off
rem Helper interno. Debe invocarse con CALL para conservar WIX_BIN y PATH.
set "WIX_BIN="

where candle.exe >nul 2>nul
if not errorlevel 1 (
  where light.exe >nul 2>nul
  if not errorlevel 1 (
    for /f "delims=" %%I in ('where candle.exe') do (
      if not defined WIX_BIN set "WIX_BIN=%%~dpI"
    )
    goto :found
  )
)

if defined WIX (
  if exist "%WIX%\bin\candle.exe" if exist "%WIX%\bin\light.exe" (
    set "WIX_BIN=%WIX%\bin"
    goto :append
  )
)

for %%D in (
  "%ProgramFiles(x86)%\WiX Toolset v3.14\bin"
  "%ProgramFiles(x86)%\WiX Toolset v3.11\bin"
  "%ProgramFiles%\WiX Toolset v3.14\bin"
  "%ProgramFiles%\WiX Toolset v3.11\bin"
) do (
  if exist "%%~D\candle.exe" if exist "%%~D\light.exe" (
    set "WIX_BIN=%%~D"
    goto :append
  )
)

exit /b 1

:append
echo;%PATH%; | find /I ";%WIX_BIN%;" >nul
if errorlevel 1 set "PATH=%WIX_BIN%;%PATH%"

:found
exit /b 0
