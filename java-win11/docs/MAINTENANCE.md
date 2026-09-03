# Mantenimiento

## Baseline

Antes y después de una ronda:

```bat
scripts\verify-all.bat
```

No se continúa sobre un baseline rojo salvo que la propia tarea sea reparar
ese gate.

## Dependencias

Actualizar JavaFX, JUnit, Checkstyle, JaCoCo o plugins Maven exige:

1. Temurin 21;
2. tests;
3. Javadoc;
4. revisión de warnings;
5. `release-check` si afecta runtime o packaging.

## Límite estructural

500 líneas por `.java` es un detector de responsabilidades mezcladas. No se
cumple moviendo código arbitrariamente a una clase `Utils`.

## Capas

`domain`, `engine` y `runtime` no deben importar JavaFX. `verify-all` audita
este contrato automáticamente.

## Evidencia

`.local/` conserva diagnóstico de máquina. Sólo su README se versiona.

## Formato `.case`

Es un contrato compartido con C++/Windows XP. Cambios incompatibles requieren
decisión explícita y documentación de migración.
