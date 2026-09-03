# Checklist de release — Java Edition

## Gate 1 — repositorio

```bat
scripts\verify-all.bat
```

Debe terminar en `RESULTADO: PASS`.

Incluye JDK 21, namespace, compilación, tests, recursos, contrato de capas,
Checkstyle, límite de 500 líneas, JaCoCo y Javadoc.

## Gate 2 — distribución autocontenida

```bat
scripts\release-check.bat
```

Además de construir la app-image comprueba:

- `ReconcileLab.exe`;
- JAR principal;
- configuración jpackage;
- runtime embebido;
- ejecución real del launcher;
- evidencia SHA-256.

Archivos locales:

```text
.local/app-image-smoke.txt
.local/release-evidence.txt
```

## Gate 3 — QA visual

Usar `WINDOWS11-QA-CHECKLIST.md`.

El smoke test no sustituye revisar canvas, resize, zoom, ayuda, diálogo Nuevo,
exportación y asociación `.case`.

## Gate 4 — instalador

```bat
scripts\verify-installer-prereqs.bat
scripts\build-installer.bat
```

Después se valida instalación, ejecución, asociación, actualización y
desinstalación en Windows 11.

## Promoción

Sólo con estos gates se considera retirar `-SNAPSHOT`.
