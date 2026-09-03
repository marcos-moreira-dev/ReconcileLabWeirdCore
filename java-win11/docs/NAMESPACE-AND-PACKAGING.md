# Namespace, coordenadas Maven e identidad de Windows

## Decisión canónica

La edición Java usa:

```text
groupId:      com.marcosmoreiradev
package base: com.marcosmoreiradev.reconcilelab
vendor:       Marcos Moreira Dev
```

No se usa `dev.reconcilelab` ni se incluye `win11` dentro del namespace Java.

Windows 11 es la plataforma objetivo de esta edición, pero no forma parte de la
identidad lógica de las clases. Esto permite que `domain`, `engine`, `io` y
`runtime` sigan describiendo el producto y no el sistema operativo.

## Capas

```text
com.marcosmoreiradev.reconcilelab
├── app
├── domain
├── engine
├── io
├── runtime
└── ui
    ├── component
    ├── help
    └── workspace
```

## Contrato de release

`verify-all.bat` inspecciona el JAR y falla si:

- falta el launcher bajo el namespace canónico;
- falta `AppMetadata`;
- aparecen clases bajo el namespace histórico `dev/reconcilelab/`.

Esto evita que una migración parcial quede escondida detrás de una compilación
exitosa.

## Packaging

`jpackage` usa la misma identidad:

```text
Vendor: Marcos Moreira Dev
Main class:
com.marcosmoreiradev.reconcilelab.app.Launcher
```

El instalador conserva un `--win-upgrade-uuid` estable. Ese valor es parte de la
identidad de entrega y no debe cambiar por versión.
