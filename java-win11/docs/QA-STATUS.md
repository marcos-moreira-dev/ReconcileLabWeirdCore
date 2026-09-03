# Estado de QA — Java / Windows 11

## Base confirmada: v0.3.0

En Windows 11 con Eclipse Temurin 21.0.12 se confirmó:

```text
JDK 21                 PASS
namespace MMDEV        PASS
Maven 3.9.11           PASS
compilación JavaFX     PASS
23 tests               PASS
JAR                     PASS
recursos                PASS
jpackage                PASS
app-image autocontenida PASS
release-check           PASS
```

La compilación de v0.3.0 reportó un warning `unchecked` en `MainWindow`; v0.4.0 elimina el uso de
`addAll(varargs)` genérico que lo producía y activa `-Xlint:all` como disciplina permanente.

## v0.4.0 — validación local disponible

Se ha validado sin JavaFX en el entorno de construcción:

- compilación Java 21 del dominio/motor/runtime con `-Xlint:all`;
- paridad de los siete presets;
- step/reset del runtime;
- ningún `.java` supera 500 líneas;
- ninguna línea no-import supera 120 caracteres;
- estructura de namespace, versión, Checkstyle, Enforcer y Javadoc.

## Pendiente en Windows 11 para v0.4.0

1. `scripts\verify-all.bat`;
2. comprobar maximización/restauración;
3. revisar color de cabeceras y scrollbars;
4. ejecutar un caso pequeño y uno de estrés;
5. `scripts\release-check.bat`;
6. installer gate cuando WiX esté disponible.

## Hotfix 0.6.2

El primer `verify-all` de 0.6.1 detectó correctamente un error de compilación:
`MainWindowLayout` conservaba un helper `TitledPane` residual después de mover el
docking a `WorkspaceDockLayout`. El helper fue eliminado. El resto del cambio de
0.6.1 no se considera validado en Windows hasta repetir `verify-all`.
