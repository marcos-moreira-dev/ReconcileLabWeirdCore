# ADR-0002 — La ventana principal no es monolítica

**Estado:** Aceptado  
**Fecha:** 2026-09-03

En v0.3.0 `MainWindow.java` superó mil líneas y `StudyImageExporter` superó 500. Desde v0.4.0:

```text
MainWindow            coordinación
MainWindowLayout      composición visual
MainWindowControls    inventario de controles
SnapshotPresenter     snapshot -> tablas/textos
SearchWorkspace       navegación/viewport
SearchCanvasPainter   render
StudyImageExporter    fachada
ui.export/*           layout/render del póster
```

Checkstyle impide que cualquier `.java` supere 500 líneas.
