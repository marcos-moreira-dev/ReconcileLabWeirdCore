# Packaging de ReconcileLab para Windows 11

Identidad canónica de esta edición:

```text
Vendor:      Marcos Moreira Dev
GroupId:     com.marcosmoreiradev
Package:     com.marcosmoreiradev.reconcilelab
Versión:     0.7.5
Upgrade UUID: dfeca1bd-c205-5e23-8e33-74ca121367d1
```

El `Upgrade UUID` es estable. No debe regenerarse entre releases porque permite
que Windows identifique versiones sucesivas de la misma aplicación.

## Gate 1 — app image autocontenida

```bat
scripts\build-app-image.bat
```

Genera:

```text
target\app-image\ReconcileLab\
```

La carpeta contiene un runtime privado y no exige Java instalado en la máquina
del usuario final.

Antes de pensar en el instalador, validar manualmente:

```text
abrir la aplicación
cargar un preset
Ejecutar / Pausar / Paso / Reiniciar
pan / zoom / scroll
seleccionar un estado compatible
abrir ayuda
exportar PNG
cerrar y volver a abrir
```

## Gate 2 — instalador EXE

```bat
scripts\build-installer.bat
```

Además de Temurin 21 + `jpackage`, el entorno debe tener WiX Toolset 3 compatible. Desde 0.7.3 no es obligatorio agregarlo manualmente a `PATH`: `scripts/_resolve-wix.bat` autodetecta las rutas estándar de WiX 3.14/3.11 y `%WIX%\bin`.

El instalador queda preparado con:

- vendor `Marcos Moreira Dev`;
- grupo de menú `Marcos Moreira Dev`;
- acceso directo;
- selector de carpeta;
- ruta sugerida `Marcos Moreira Dev\ReconcileLab`;
- asociación del formato `.case`;
- UUID de actualización estable.

La asociación se define en:

```text
packaging\reconcilelab-case.properties
```

## Release check

```bat
scripts\release-check.bat
```

Encadena:

```text
verify-all
    ↓
build-app-image
```

Si ambos pasan, la aplicación, tests, JAR y distribución autocontenida están
listas para QA. El instalador sigue siendo un gate separado porque añade WiX y
la instalación del sistema operativo a la ecuación.

## Gate final de release

Para una publicación formal se recomienda ejecutar:

```bat
scripts\release-final.bat
```

Este gate encadena verificación del producto, app-image, smoke con runtime
embebido, prerrequisitos WiX, creación del instalador y generación del SHA-256.

Produce los artefactos canónicos:

```text
target\installer\ReconcileLab-Java-Windows11-Setup-0.7.5.exe
target\installer\ReconcileLab-Java-Windows11-Setup-0.7.5.sha256
```

## Gate final de instalador

No se considera cerrado hasta validar en Windows 11:

- instalación limpia;
- inicio desde menú Inicio;
- acceso directo;
- ejecución sin depender de un JDK del sistema;
- apertura de `.case`;
- exportación PNG;
- actualización de una versión anterior;
- desinstalación;
- reinstalación limpia.


Onboarding completo: `docs/BUILD-ONBOARDING.md`.
