# Arquitectura

ReconcileLab 2006 es pequeño a propósito, pero mantiene fronteras claras:

```text
archivo .case
    |
    v
ProblemInstance
    |
    v
SearchEngine
    |
    v
RuntimeController
    |
    v
ExecutionSnapshot
    |
    v
MainFrame / WorkspacePanel / GraphCanvas
```

## Modelo

Datos C++ sin dependencias de wxWidgets.

## Motor

Resuelve la conciliación y conserva métricas, soluciones y una traza acotada.
No sabe cómo se dibuja una tarjeta.

## Runtime

Ejecuta el motor en un hilo joinable separado del hilo de interfaz.

## Presentación

- `MainFrame`: composición general y comandos.
- `WorkspacePanel`: viewport y navegación fija.
- `ZoomControl`: zoom dibujado, independiente del scroll.
- `GraphCanvas`: mundo desplazable y tarjetas.
- columna derecha: panel desplazable común para inspector/resumen/resultados.

## Recursos

`assets/branding/` contiene la identidad visual. `resources/reconcilelab.rc`
enlaza el `.ico` al ejecutable mediante `windres`.

## Regla central

```text
estado del dominio != coordenada != control de interfaz
```

Eso mantiene la idea portable hacia una futura versión Java/JavaFX.
