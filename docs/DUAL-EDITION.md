# ReconcileLab: XP y Windows 11

Este repositorio funciona como un experimento de continuidad tecnológica.

## Edición C++ / Windows XP

```text
C++
TDM-GCC 9.2
wxWidgets 3.0.5
Windows XP SP3 x86
```

La interfaz usa controles nativos y dibujo directo mediante wxWidgets.

## Edición Java / Windows 11

```text
Java 21
Eclipse Temurin 21
JavaFX 21
Maven
Windows 11
```

No intenta imitar visualmente Windows XP. Sí conserva la misma gramática
espacial y el mismo problema.

## Paridad conceptual

Ambas ediciones comparten:

- formato `.case`;
- presets;
- centavos enteros;
- árbol INCLUIR/OMITIR;
- búsqueda exhaustiva;
- poda segura;
- objetivo `first`, `all`, `count`;
- traza acotada;
- soluciones y eventos;
- separación entre algoritmo y actualización visual.

La edición Java puede utilizar componentes más limpios y CSS transversal sin
cambiar el producto en otra cosa.

## Estado

La edición C++ `0.5.11` es la referencia retro ya validada visualmente en
Windows XP SP3 x86.

La edición Java `0.7.5` es la release moderna estable para Windows 11. Usa Eclipse Temurin 21 y JavaFX 21, conserva la gramática espacial de la edición XP y dispone de build reproducible, tests, Javadoc, Checkstyle, JaCoCo, app-image autocontenida y packaging EXE mediante `jpackage` + WiX.

El instalador y su SHA-256 se publican como assets de GitHub Releases; el árbol Git conserva únicamente fuentes, documentación y configuración de build.

## Identidad de la edición Java

La implementación moderna usa como base:

```text
com.marcosmoreiradev.reconcilelab
```

y `com.marcosmoreiradev` como `groupId` Maven. Windows 11 es la plataforma
de destino, no una parte del namespace del dominio.
