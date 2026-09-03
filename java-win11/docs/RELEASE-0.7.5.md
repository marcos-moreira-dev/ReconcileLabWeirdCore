# Release 0.7.5 — checklist de promoción

Estado objetivo: release pública estable de ReconcileLab Java Edition para
Windows 11.

## Identidad

```text
Version: 0.7.5
Tag Git: v0.7.5
Vendor: Marcos Moreira Dev
Package: com.marcosmoreiradev.reconcilelab
JDK: Eclipse Temurin 21
JavaFX: 21
Installer: EXE / jpackage + WiX 3
```

## Gate automatizado

Desde `java-win11\scripts`:

```bat
release-final.bat
```

Debe terminar en `RELEASE FINAL: PASS`.

## Smoke manual del instalador

Después del gate automatizado:

- instalar `ReconcileLab-Java-Windows11-Setup-0.7.5.exe`;
- iniciar desde menú Inicio;
- abrir un preset;
- ejecutar una búsqueda;
- abrir un `.case`;
- comprobar ayuda;
- exportar una lámina PNG;
- confirmar que funciona sin depender del JDK del sistema;
- desinstalar correctamente.

## Promoción Git

Sólo después del smoke:

```bash
git add .
git commit -m "Release ReconcileLab Java 0.7.5"
git tag -a v0.7.5 -m "ReconcileLab v0.7.5"
git push origin main
git push origin v0.7.5
```

El tag debe apuntar al commit final sin `-SNAPSHOT`.

## GitHub Release

Crear la release desde `v0.7.5` y adjuntar:

```text
ReconcileLab-Java-Windows11-Setup-0.7.5.exe
ReconcileLab-Java-Windows11-Setup-0.7.5.sha256
```

GitHub genera automáticamente los archivos fuente `.zip` y `.tar.gz`.

No subir `target/` al repositorio: los binarios de distribución pertenecen a
GitHub Releases, no al árbol Git.
