# Arquitectura — ReconcileLab Java Edition

## Dirección de dependencias

```text
domain <- engine <- runtime <- ui
          ^
          io adapta archivos hacia domain
```

## Paquetes

```text
app/          ciclo JavaFX, metadata, argumentos
domain/       problema, nodos, snapshots, soluciones
engine/       búsqueda exhaustiva y poda segura
runtime/      READY/RUNNING/PAUSED/COMPLETED
io/           .case, dinero, presets
ui/           coordinación y presentación
ui/component/ componentes transversales
ui/dialog/    diálogos
ui/help/      ayuda
ui/workspace/ layout, navegación y canvas
ui/export/    láminas PNG
```

## Ventana principal v0.4

```text
MainWindow
├── MainWindowControls
├── MainWindowLayout
├── SnapshotPresenter
└── WindowStateManager
```

## Workspace

```text
ExecutionSnapshot -> NodeLayoutModel -> SearchWorkspace -> SearchCanvasPainter -> GraphicsContext
```

El canvas físico mide lo mismo que el viewport; el mundo virtual es matemático.

## Exportación

```text
StudyImageExporter -> StudyPosterRenderer
                     ├── StudyPosterLayout
                     ├── StudyPosterHeaderPainter
                     └── StudyPosterGraphPainter
```

## Calidad

Maven usa Enforcer (JDK 21/Maven 3.9), `--release 21`, `-Xlint:all`, JUnit, Checkstyle con máximo
500 líneas por `.java` y generación de Javadoc.


## Viewport y controles flotantes

El zoom no forma parte del mundo lógico:

```text
SearchWorkspace
└── viewport StackPane
    ├── Canvas               ← mundo desplazable
    └── ZoomOverlay          ← HUD fijo
```

`ViewportNavigationPolicy` añade overscroll y reserva una zona segura al
centrar nodos para evitar que el HUD tape el objetivo.

La línea de tiempo usa:

```text
SplitPane vertical
├── body
└── TimelineDockPane
```

`TimelineDockPane` gestiona explícitamente visibilidad de contenido; el
SplitPane sólo decide geometría.
