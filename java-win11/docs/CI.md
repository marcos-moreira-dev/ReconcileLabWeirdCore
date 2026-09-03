# Integración continua

La edición Java incorpora:

```text
.github/workflows/java-win11-ci.yml
```

El job se ejecuta en Windows con Eclipse Temurin 21 y usa el mismo gate local:

```bat
java-win11\scripts\verify-all.bat
```

No existe una segunda definición de calidad exclusiva de CI.

## Evidencias conservadas

Cuando existen, GitHub Actions adjunta:

- `.local/verify-all.txt`;
- reporte JaCoCo;
- Javadoc.

## Alcance

El CI normal no publica releases ni instaladores. La promoción de una versión
sigue requiriendo QA visual y un `release-check.bat` consciente.
