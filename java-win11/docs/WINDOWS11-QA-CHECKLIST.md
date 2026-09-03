# Checklist de QA visual — Windows 11

## Arranque

- La ventana abre sin consola adicional.
- El icono aparece en barra de título/taskbar.
- El primer preset se carga.
- No hay excepciones visibles.

## Gramática espacial

- MenuBar, ToolBar y franja de ejecución permanecen arriba.
- Casos y caso actual permanecen a la izquierda.
- Workspace ocupa el centro y absorbe resize.
- Resumen/Ruta/Compatibles permanecen a la derecha.
- Timeline ocupa la franja inferior.
- Status bar permanece abajo.

## Casos

- Ctrl+N abre `Nuevo caso`.
- crear un caso válido con punto decimal;
- crear otro usando coma decimal;
- un movimiento inválido no cierra el diálogo;
- Ctrl+O abre un `.case`;
- Ctrl+Shift+S guarda con extensión `.case`;
- doble clic sobre un `.case` asociado abre ese caso al arrancar.

## Ejecución

Probar:

```text
Paso -> Paso -> Ejecutar -> Pausar -> Paso -> Ejecutar -> Reiniciar
```

Confirmar:

- botones habilitados según estado;
- progreso llega a 100% al completar;
- Espacio contabilizado puede quedar por debajo de 100% en objetivo FIRST;
- combo de compatibles centra la tarjeta elegida;
- Tasa observada cambia durante la ejecución;
- la ruta seleccionada enumera movimientos incluidos/omitidos.

## Workspace

- pan con botón central;
- pan con botón derecho;
- Ctrl + rueda modifica zoom;
- rueda normal desplaza verticalmente;
- Shift + rueda desplaza horizontalmente;
- botones +/- funcionan;
- slider no se desplaza con el mundo;
- scrollbars reflejan mundo virtual;
- tarjetas no se superponen;
- ids largos permanecen dentro de la tarjeta.

## Panel derecho

- abrir las tres secciones;
- scrollbar vertical no pisa contenido;
- redimensionar ventana estrecha/ancha;
- texto y tablas conservan margen.

## Timeline

- expandir/contraer;
- el workspace gana/pierde altura correctamente;
- no hay saltos del modelo lógico.

## Exportación

- exportar un caso pequeño;
- exportar `07-horizontal-compact.case`;
- texto de las tarjetas legible;
- cabecera incluye entrada, método y métricas;
- la imagen explica que es una reorganización por profundidad.

## Ayuda

- abre ventana HTML local;
- imágenes cargan;
- enlaces internos funcionan;
- contenido habla como producto, no como documentación de desarrollo.


## Packaging

Primero ejecutar:

```bat
scripts\release-check.bat
```

Confirmar:

- `verify-all` termina PASS;
- el JAR sólo contiene `com/marcosmoreiradev/reconcilelab/`;
- `target\app-image\ReconcileLab\ReconcileLab.exe` existe;
- la app-image abre aunque el usuario no use el JDK del sistema.

Después, con WiX:

```bat
scripts\build-installer.bat
```

Validar:

- vendor `Marcos Moreira Dev`;
- grupo de menú `Marcos Moreira Dev`;
- acceso directo;
- asociación `.case`;
- actualización sobre una versión previa;
- desinstalación.


## Regresión de congelamiento del selector

Ejecutar un caso hasta que aparezcan estados compatibles.

Confirmar:

- `Ejecutar` no congela la interfaz;
- el selector de compatibles se actualiza sin recursión;
- elegir un compatible centra exactamente una tarjeta;
- la ruta seleccionada se actualiza;
- no aparece una cadena repetitiva
  `ChoiceBox -> focusNode -> SnapshotPresenter -> ChoiceBox`.

## Línea de tiempo maximizada

- la cabecera `Línea de tiempo de ejecución` permanece visible;
- puede expandirse y contraerse;
- maximizar/restaurar no la expulsa por debajo del canvas.

## Pulido visual v0.6

- toolbar con iconos vectoriales de color moderado;
- controles superiores con borde y hover legibles;
- zoom con lupas `+` y `−`;
- iconos nítidos con escalado de Windows.


## Zoom adaptativo por nivel de detalle

Probar 25%, 40%, 60%, 80% y 100%.

Esperado:

- por debajo de 55%: tarjetas tipo overview, sin texto microscópico;
- entre 55% y 80%: sólo id y monto acumulado;
- desde 80%: tarjeta pedagógica completa;
- colores de compatible/descartado/procesado siguen distinguiéndose siempre;
- selección permanece visible con borde blanco.

## Docking de la línea de tiempo

- expandida debe mostrar tabla, no sólo cabecera;
- el divisor horizontal puede arrastrarse;
- al contraer queda sólo la cabecera;
- al expandir restaura el último tamaño razonable;
- maximizar/restaurar no destruye la proporción.


## HUD flotante de zoom

- el control de zoom aparece sobre el extremo derecho del canvas;
- panear no mueve el HUD;
- el HUD ya no consume una columna de la maquetación;
- botones y slider siguen recibiendo clics normalmente;
- el mundo puede panear aproximadamente 140 px adicionales por cada borde;
- al centrar un resultado, la tarjeta no debe quedar escondida bajo el HUD.

## Timeline dock v0.7

- contraer deja sólo una cabecera de 36 px;
- volver a expandir muestra inmediatamente la tabla;
- el último tamaño expandido razonable se conserva;
- arrastrar el divisor modifica la proporción;
- maximizar/restaurar no deja un panel vacío;
- el timeline no se superpone visualmente al zoom ni a las scrollbars.




## Límite de traza sin overlays

En `06-stress.case`:

- al superar la capacidad de traza el canvas no muestra popup ni toast;
- `Traza retenida` permanece en 2.500 cuando se alcanza el límite;
- `Fuera de traza` continúa aumentando mientras avanza la búsqueda;
- el progress bar continúa representando progreso lógico;
- pan, zoom, selección y scroll permanecen libres de elementos superpuestos.
