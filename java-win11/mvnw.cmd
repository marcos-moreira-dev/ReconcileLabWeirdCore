@echo off
setlocal EnableExtensions

set "ROOT=%~dp0"
set "MAVEN_VERSION=3.9.11"
set "TOOLS=%ROOT%.local\tools"
set "MAVEN_HOME=%TOOLS%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
set "DOWNLOADS=%ROOT%.local\downloads"
set "ZIP=%DOWNLOADS%\apache-maven-%MAVEN_VERSION%-bin.zip"
set "URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if exist "%MAVEN_CMD%" goto run

if not exist "%TOOLS%" mkdir "%TOOLS%"
if not exist "%DOWNLOADS%" mkdir "%DOWNLOADS%"

echo [mvnw] Maven %MAVEN_VERSION% no esta en .local. Se descargara una sola vez.
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop'; $ProgressPreference='SilentlyContinue';" ^
  "Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile '%ZIP%';" ^
  "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%TOOLS%' -Force"
if errorlevel 1 (
  echo [mvnw] ERROR: no se pudo preparar Maven.
  exit /b 1
)

:run
call "%MAVEN_CMD%" %*
exit /b %ERRORLEVEL%
