# Release audit — Java 0.3.0-SNAPSHOT

## Evidencia de Windows anterior a esta ronda

La edición 0.2.0 pasó en Windows 11 con Eclipse Temurin 21:

```text
JDK 21                 PASS
Maven wrapper          PASS
Compilación JavaFX     PASS
Tests                   PASS
JAR                     PASS
Contenido del JAR      PASS
jpackage disponible    PASS
7 presets compartidos  PASS
branding                PASS
ayuda                   PASS
CSS                     PASS
RESULTADO               PASS
```

## Cambio de identidad 0.3.0

```text
groupId: com.marcosmoreiradev
package: com.marcosmoreiradev.reconcilelab
vendor:  Marcos Moreira Dev
```

El JAR anterior todavía usaba `dev/reconcilelab/`; 0.3.0 migra físicamente las
fuentes y tests al namespace canónico.

`verify-all.bat` ahora falla si detecta clases antiguas dentro del JAR.

## Gates ejecutados en el entorno de construcción

```text
POM XML bien formado                         PASS
package Java == ruta física                  PASS
sin imports/package del namespace antiguo    PASS
compilación Java 21 del núcleo               PASS
paridad de 7 presets                         PASS
StartupArguments                             PASS
ExecutionProgressModel                       PASS
NodeLayoutModel                              PASS
StudyImageExporter                           PASS
export PNG real 2966x2107                    PASS
scripts .bat con CRLF                        PASS
```

La compilación completa JavaFX de **esta** revisión no se marca como PASS en
este entorno porque no puede resolver Maven Central. Ese gate debe repetirse en
Windows 11.

## Gate siguiente

```bat
java-win11\scripts\verify-all.bat
```

Si pasa:

```bat
java-win11\scripts\release-check.bat
```

`release-check` añade la creación real de la app-image autocontenida.

Después, con WiX disponible:

```bat
java-win11\scripts\build-installer.bat
```
