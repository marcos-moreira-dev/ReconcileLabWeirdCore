# Historial de cambios

## Java 0.4.0-SNAPSHOT - Formalización de ingeniería y pulido Win11

- Refactor de MainWindow, workspace y exportador para respetar <=500 líneas.
- Checkstyle, Enforcer, -Xlint:all y Javadoc como gates.
- Maximización/restauración de ventana.
- Cabeceras y scrollbars con mayor contraste.
- Estándar de ingeniería, release policy y ADRs.

## 0.5.11 - Separación física real de la scrollbar derecha

- El intento de 0.5.10 con un spacer dentro del mismo scroller no fue suficiente
  en Windows XP.
- Se crea `rightContent`, un panel hijo real dentro de `m_rightScroller`.
- Resumen de ejecución, Ruta seleccionada y Combinaciones compatibles pasan a
  ser hijos de `rightContent`.
- Entre `rightContent` y la scrollbar se reserva `wxSYS_VSCROLL_X + 8 px`.
- Aunque XP pinte la scrollbar sobre su área cliente, sólo puede ocupar el
  gutter vacío; ya no puede pisar controles.


## 0.5.10 - Canal reservado para la scrollbar derecha

- La scrollbar vertical del panel derecho deja de compartir ancho visual con
  los controles.
- Se obtiene el ancho nativo mediante `wxSYS_VSCROLL_X`.
- Se reserva ese ancho más 4 px como canal vacío dentro del scroller.
- Resumen, Ruta seleccionada y Combinaciones compatibles nunca pueden ocupar
  ese canal, incluso cuando la scrollbar aparece.
- No cambia el canvas, el motor, el runtime ni el progreso.

## 0.5.9 - Pulido de la columna derecha

- La columna `Resumen / Ruta / Combinaciones` pasa de 310 px a 335 px.
- Se fija ese ancho mínimo para que la scrollbar vertical de Windows XP no
  comprima visualmente el contenido.
- Se añade un pequeño margen a izquierda y derecha de la columna.
- El workspace central sigue siendo expansivo y absorbe la reducción de ancho.
- No cambia el motor, el runtime, el progreso ni el layout lógico del grafo.

## 0.5.8 - Ciclo de progreso y ejemplo horizontal

- Separados conceptualmente `Progreso de ejecucion` y `Espacio contabilizado`.
- Una ejecución COMPLETED lleva la barra a 100% incluso si un objetivo
  `first` necesitó explorar sólo una fracción del árbol.
- `Espacio contabilizado` queda como métrica independiente y exacta.
- `CoverageGauge` incorpora un `wxTimer` propio de 20 ms para animar cambios.
- Texto y barra siguen naciendo del mismo valor visible.
- Un objetivo menor se aplica inmediatamente para limpiar progreso de una
  ejecución anterior.
- En modo Equilibrado, casos de hasta 8 movimientos procesan 1 estado por lote
  y ceden 25 ms, de modo que no terminen completamente entre dos refrescos.
- Añadido `07-horizontal-compact.case`, pequeño en profundidad pero ancho en las
  últimas capas, para practicar paneo horizontal y zoom.

## 0.5.7 - Refresh atómico del indicador

- Porcentaje y barra se pintan en un solo control.

## 0.5.6 - Hotfix TDM-GCC 9.2

- Corregida deducción de tipos en std::min.
