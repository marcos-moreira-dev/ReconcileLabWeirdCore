@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0.."
set "LOG=%ROOT%\.local\verify-all.txt"
set "FAILED=0"
set "COMPILE_OK=0"
set "TESTS_OK=0"
set "PACKAGE_OK=0"
set "APPJAR=%ROOT%\target\reconcilelab-java-win11-0.7.5-SNAPSHOT.jar"
set "MAIN_CLASS=com.marcosmoreiradev.reconcilelab.app.Launcher"

if not exist "%ROOT%\.local" mkdir "%ROOT%\.local"

> "%LOG%" echo ReconcileLab Java - verify-all
>>"%LOG%" echo =================================
>>"%LOG%" echo Fecha: %DATE% %TIME%
>>"%LOG%" echo Namespace: com.marcosmoreiradev.reconcilelab
>>"%LOG%" echo.

>>"%LOG%" echo [Java / JDK]
where java >>"%LOG%" 2>&1
if errorlevel 1 set "FAILED=1"

where javac >>"%LOG%" 2>&1
if errorlevel 1 set "FAILED=1"

java -version >>"%LOG%" 2>&1
if errorlevel 1 set "FAILED=1"

javac -version >>"%LOG%" 2>&1
if errorlevel 1 set "FAILED=1"

java -XshowSettings:properties -version 2>&1 ^
  | findstr /C:"java.vendor" /C:"java.version" /C:"java.home" >>"%LOG%"

set "JAVAC_VERSION="
for /f "tokens=2" %%V in ('javac -version 2^>^&1') do (
  set "JAVAC_VERSION=%%V"
)

if not defined JAVAC_VERSION (
  >>"%LOG%" echo FAIL: no se pudo determinar la version de javac.
  set "FAILED=1"
) else (
  if "!JAVAC_VERSION:~0,3!"=="21." (
    >>"%LOG%" echo PASS: JDK 21 detectado por javac ^(!JAVAC_VERSION!^).
  ) else (
    >>"%LOG%" echo FAIL: se requiere JDK 21; javac actual: !JAVAC_VERSION!
    set "FAILED=1"
  )
)

>>"%LOG%" echo.
>>"%LOG%" echo [Identidad Java]
findstr /L /C:"<groupId>com.marcosmoreiradev</groupId>" "%ROOT%\pom.xml" >nul 2>nul
if errorlevel 1 (
  >>"%LOG%" echo FAIL: groupId Maven no es com.marcosmoreiradev.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: groupId com.marcosmoreiradev.
)

if exist "%ROOT%\src\main\java\com\marcosmoreiradev\reconcilelab\app\Launcher.java" (
  >>"%LOG%" echo PASS: arbol fuente en namespace canonico.
) else (
  >>"%LOG%" echo FAIL: no existe el Launcher en el namespace canonico.
  set "FAILED=1"
)

if exist "%ROOT%\src\main\java\dev\reconcilelab" (
  >>"%LOG%" echo FAIL: aun existe el arbol fuente antiguo dev\reconcilelab.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: no existe el arbol fuente antiguo.
)

>>"%LOG%" echo.
>>"%LOG%" echo [Maven wrapper]
call "%ROOT%\mvnw.cmd" -version >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL: Maven wrapper.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: Maven wrapper.
)

pushd "%ROOT%"

>>"%LOG%" echo.
>>"%LOG%" echo [Limpieza Maven]
call "%ROOT%\mvnw.cmd" -q clean >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL: limpieza Maven.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: limpieza Maven.
)

>>"%LOG%" echo.
>>"%LOG%" echo [Compilacion JavaFX]
call "%ROOT%\mvnw.cmd" -q -DskipTests compile >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL: compilacion JavaFX.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: compilacion JavaFX.
  set "COMPILE_OK=1"
)

>>"%LOG%" echo.
>>"%LOG%" echo [Tests]
if "!COMPILE_OK!"=="1" (
  call "%ROOT%\mvnw.cmd" -q test >>"%LOG%" 2>&1
  if errorlevel 1 (
    >>"%LOG%" echo FAIL: tests.
    set "FAILED=1"
  ) else (
    >>"%LOG%" echo PASS: tests.
    set "TESTS_OK=1"
  )
) else (
  >>"%LOG%" echo SKIP: compilacion JavaFX fallida; no se reintentan tests.
)

>>"%LOG%" echo.
>>"%LOG%" echo [Paquete JAR]
if "!COMPILE_OK!"=="1" (
  call "%ROOT%\mvnw.cmd" -q -DskipTests package >>"%LOG%" 2>&1
  if errorlevel 1 (
    >>"%LOG%" echo FAIL: paquete JAR.
    set "FAILED=1"
  ) else (
    >>"%LOG%" echo PASS: paquete JAR.
    set "PACKAGE_OK=1"
  )
) else (
  >>"%LOG%" echo SKIP: compilacion JavaFX fallida; no se reintenta package.
)

>>"%LOG%" echo.
>>"%LOG%" echo [Contenido del JAR]
if "!PACKAGE_OK!"=="1" (
  jar tf "%APPJAR%" > "%ROOT%\.local\jar-contents.txt" 2>&1

  call :checkjar "Launcher canonico" "com/marcosmoreiradev/reconcilelab/app/Launcher.class"
  call :checkjar "Metadata de producto" "com/marcosmoreiradev/reconcilelab/app/AppMetadata.class"
  call :checkjar "CSS" "css/reconcilelab.css"
  call :checkjar "Ayuda HTML" "help/index.html"
  call :checkjar "Ayuda Java" "java-help/index.html"
  call :checkjar "Preset 01" "examples/01-small-exact.case"
  call :checkjar "Preset 07" "examples/07-horizontal-compact.case"
  call :checkjar "Icono 64" "branding/reconcilelab-icon-64.png"

  findstr /B /C:"dev/reconcilelab/" "%ROOT%\.local\jar-contents.txt" >nul 2>nul
  if errorlevel 1 (
    >>"%LOG%" echo PASS: no quedan clases en el namespace antiguo dev.reconcilelab.
  ) else (
    >>"%LOG%" echo FAIL: el JAR aun contiene clases bajo dev.reconcilelab.
    set "FAILED=1"
  )

  findstr /B /C:"com/marcosmoreiradev/reconcilelab/" "%ROOT%\.local\jar-contents.txt" >nul 2>nul
  if errorlevel 1 (
    >>"%LOG%" echo FAIL: no se encontro el namespace canonico en el JAR.
    set "FAILED=1"
  ) else (
    >>"%LOG%" echo PASS: namespace canonico presente.
  )
) else (
  >>"%LOG%" echo SKIP: no hay paquete JAR valido para inspeccionar.
)

>>"%LOG%" echo.
>>"%LOG%" echo [jpackage]
where jpackage >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo INFO: jpackage no disponible; el build normal sigue siendo valido.
) else (
  jpackage --version >>"%LOG%" 2>&1
  >>"%LOG%" echo PASS: jpackage disponible.
)

popd

call :checkfile "Caso compartido 01" "%ROOT%\..\examples\01-small-exact.case"
call :checkfile "Caso compartido 02" "%ROOT%\..\examples\02-multiple-solutions.case"
call :checkfile "Caso compartido 03" "%ROOT%\..\examples\03-no-solution.case"
call :checkfile "Caso compartido 04" "%ROOT%\..\examples\04-pruning-showcase.case"
call :checkfile "Caso compartido 05" "%ROOT%\..\examples\05-medium-reconciliation.case"
call :checkfile "Caso compartido 06" "%ROOT%\..\examples\06-stress.case"
call :checkfile "Caso compartido 07" "%ROOT%\..\examples\07-horizontal-compact.case"

call :checkfile "Branding 32" "%ROOT%\..\assets\branding\reconcilelab-icon-32.png"
call :checkfile "Branding 64" "%ROOT%\..\assets\branding\reconcilelab-icon-64.png"
call :checkfile "Branding 128" "%ROOT%\..\assets\branding\reconcilelab-icon-128.png"

call :checkfile "Ayuda index" "%ROOT%\..\help\index.html"
call :checkfile "Ayuda workspace" "%ROOT%\..\help\workspace.html"
call :checkfile "CSS JavaFX" "%ROOT%\src\main\resources\css\reconcilelab.css"
call :checkfile "Asociacion .case" "%ROOT%\packaging\reconcilelab-case.properties"

>>"%LOG%" echo.
>>"%LOG%" echo [Arquitectura / dependencias]
set "ARCH_FAIL=0"

for %%P in (domain engine runtime) do (
  findstr /S /R /I /M /C:"^import javafx\." ^
    "%ROOT%\src\main\java\com\marcosmoreiradev\reconcilelab\%%P\*.java" ^
    >nul 2>nul

  if not errorlevel 1 (
    >>"%LOG%" echo FAIL: el paquete %%P contiene imports JavaFX.
    set "ARCH_FAIL=1"
  )
)

if "!ARCH_FAIL!"=="0" (
  >>"%LOG%" echo PASS: domain/engine/runtime no importan JavaFX.
) else (
  set "FAILED=1"
)

>>"%LOG%" echo.
>>"%LOG%" echo [Calidad estructural]
call "%ROOT%\mvnw.cmd" -q -f "%ROOT%\pom.xml" -DskipTests checkstyle:check >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL: Checkstyle / limite de 500 lineas.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: Checkstyle / limite de 500 lineas.
)

>>"%LOG%" echo.
>>"%LOG%" echo [Cobertura JaCoCo]
if "!TESTS_OK!"=="1" (
  if not exist "%ROOT%\target\jacoco.exec" (
    >>"%LOG%" echo FAIL: tests PASS pero falta target\jacoco.exec.
    set "FAILED=1"
  ) else (
    call "%ROOT%\mvnw.cmd" -q -f "%ROOT%\pom.xml" org.jacoco:jacoco-maven-plugin:0.8.12:report >>"%LOG%" 2>&1
    if errorlevel 1 (
      >>"%LOG%" echo FAIL: reporte JaCoCo.
      set "FAILED=1"
    ) else (
      if exist "%ROOT%\target\site\jacoco\index.html" (
        >>"%LOG%" echo PASS: reporte JaCoCo generado.
      ) else (
        >>"%LOG%" echo FAIL: JaCoCo no produjo target\site\jacoco\index.html.
        set "FAILED=1"
      )
    )
  )
) else (
  >>"%LOG%" echo SKIP: cobertura depende de una ejecucion de tests satisfactoria.
)

>>"%LOG%" echo.
>>"%LOG%" echo [Javadoc]
call "%ROOT%\mvnw.cmd" -q -f "%ROOT%\pom.xml" javadoc:javadoc >>"%LOG%" 2>&1
if errorlevel 1 (
  >>"%LOG%" echo FAIL: generacion Javadoc.
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: Javadoc generado.
)

>>"%LOG%" echo.
if "%FAILED%"=="0" (
  >>"%LOG%" echo RESULTADO: PASS
) else (
  >>"%LOG%" echo RESULTADO: FAIL
)

type "%LOG%"
exit /b %FAILED%

:checkfile
>>"%LOG%" echo.
>>"%LOG%" echo [%~1]
if exist "%~2" (
  >>"%LOG%" echo PASS: %~2
) else (
  >>"%LOG%" echo FAIL: no existe %~2
  set "FAILED=1"
)
exit /b 0

:checkjar
findstr /L /X /C:"%~2" "%ROOT%\.local\jar-contents.txt" >nul 2>nul
if errorlevel 1 (
  >>"%LOG%" echo FAIL: JAR no contiene %~1 ^(%~2^)
  set "FAILED=1"
) else (
  >>"%LOG%" echo PASS: JAR contiene %~1.
)
exit /b 0
