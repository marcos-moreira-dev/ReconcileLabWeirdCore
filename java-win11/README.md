# ReconcileLab — Java Edition para Windows 11

Java 21 + Eclipse Temurin 21 + JavaFX 21 + Maven Wrapper.

Conserva la gramática espacial de la edición C++/Windows XP y moderniza toolkit, estilo, runtime y
packaging sin reinventar el producto.

## Verificar

```bat
scripts\verify-all.bat
```

Gates: JDK/namespace, Maven, compile `-Xlint:all`, JUnit, JAR/recursos, Checkstyle <=500 líneas,
Javadoc y disponibilidad de `jpackage`.

## Ejecutar

```bat
scripts\run-dev.bat
```

La primera ejecución abre maximizada y luego conserva el estado de ventana.

## Release local

```bat
scripts\release-check.bat
```

Javadoc manual:

```bat
scripts\generate-javadoc.bat
```

## Documentos de ingeniería

- `docs/ENGINEERING-STANDARD.md`
- `docs/RELEASE-POLICY.md`
- `docs/ARCHITECTURE.md`
- `docs/GUI-GRAMMAR.md`
- `docs/TRACE-RETENTION.md`
- `docs/BUILD-ONBOARDING.md`
- `docs/ADR/`
- `CONTRIBUTING.md`

## Estado de la rama

`0.7.5-SNAPSHOT` representa la edición moderna actualmente validada en Windows
11. Conserva la gramática espacial de la edición XP y añade HUD de zoom,
timeline redimensionable, controles refinados e iconografía vectorial propia.

Las capturas canónicas para GitHub están en:

```text
../docs/screenshots/reconcilelab-java-win11-main-window.png
../docs/screenshots/reconcilelab-java-win11-help-window.png
```


## Release engineering

```bat
scripts\release-check.bat
```

Genera y valida la app-image autocontenida. El launcher se ejecuta en modo
smoke usando el runtime embebido y deja:

```text
.local/app-image-smoke.txt
.local/release-evidence.txt
```

CI: `.github/workflows/java-win11-ci.yml`.


## Capturas

<p align="center">
  <img src="../docs/screenshots/reconcilelab-java-win11-main-window.png"
       alt="ReconcileLab Java Edition en Windows 11"
       width="1000">
</p>

<p align="center">
  <img src="../docs/screenshots/reconcilelab-java-win11-help-window.png"
       alt="Ayuda integrada de ReconcileLab Java Edition"
       width="1000">
</p>
