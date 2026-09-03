# Interfaz

## Workspace y zoom

Windows XP mostró artefactos cuando el zoom se pintaba dentro del mismo
`wxScrolledWindow` que el grafo. Desde 0.5.3 la composición es física:

```text
WorkspacePanel
├── GraphCanvas
└── ZoomRail
```

`ZoomRail` no pertenece al mundo desplazable. En 0.5.4 su fondo usa el color
nativo claro de panel de Windows (`wxSYS_COLOUR_BTNFACE`).

El control se ancla al borde inferior del workspace actual. Cuando se expande
la línea de tiempo, el sizer reduce la altura del workspace y el zoom sube
automáticamente con él.

## Navegación directa a coincidencias

La franja superior incluye:

```text
Estados de busqueda compatibles: [ Estado #203 - $287.40 v ]
```

La lista se alimenta de nodos `NODE_MATCH` que continúan retenidos en la traza
detallada.

Al elegir uno:

1. `MainFrame` obtiene su id;
2. `GraphCanvas::FocusNode()` lo selecciona;
3. calcula su centro con el zoom actual;
4. ajusta las scrollbars;
5. el inspector `Ruta seleccionada` se actualiza inmediatamente.

Si todavía no existe una coincidencia, el control muestra
`Sin resultados compatibles` y queda deshabilitado. Si el motor sí encontró
coincidencias pero todas quedaron fuera del límite de traza, el texto lo indica
sin prometer una navegación imposible.

## Tarjetas

`NodeWidth()` adapta el ancho a id, movimiento y monto. El layout reserva ese
ancho real, así una tarjeta más larga tampoco invade la siguiente.

## Casos de ejemplo

La lista utiliza filas altas con alternancia blanco/gris claro y selección azul
nativa de XP.

## Progreso

`CoverageGauge` es un control dibujado por la aplicación. Sólo invalida su
propia región cuando cambia y llama `Update()` para que XP refleje el avance
incluso durante una ejecución rápida.

## Exportación

La exportación usa un layout de póster por profundidad. No miniaturiza el
workspace interactivo completo: reordena la traza para conservar tarjetas
legibles.
