# Changelog — ReconcileLab Java Edition

## 0.7.5 — 2026-09-03 — Canvas sin avisos superpuestos

- Se corrige la autodetección de WiX en Windows cuando `cmd.exe` interpreta rutas con `ProgramFiles(x86)` dentro de bloques parentizados.
- El gate `verify-installer-prereqs.bat` evita ahora construcciones frágiles del parser de batch y conserva `WIX_BIN`/`PATH` tras el helper.
- Se elimina por completo el toast de límite de traza.
- El canvas queda reservado a estados, conexiones, pan, zoom y selección.
- La diferencia entre búsqueda lógica y traza retenida se comunica únicamente
  mediante las métricas `Traza retenida` y `Fuera de traza`.
- Se conserva la documentación que explica por qué miles de IDs pueden ocupar
  pocas bandas verticales y muchas columnas horizontales.

## 0.7.4-SNAPSHOT — Toast temporal de retención

- El aviso de límite de traza deja de pintarse permanentemente sobre el canvas.
- Ahora es un overlay JavaFX independiente y no bloquea la interacción.
- Permanece 5,5 segundos y se desvanece durante 1,5 segundos.
- No reaparece en cada refresh; se rearma al cambiar o reiniciar el caso.
- Se documenta que los IDs de nodo no equivalen a profundidad: con 20
  movimientos puede haber miles de nodos distribuidos en unas pocas bandas
  verticales y muchas columnas horizontales.
- Las métricas permanentes `Traza retenida` y `Fuera de traza` se conservan.

## 0.7.3-SNAPSHOT — Traza observable y onboarding de build

- El canvas explica cuando alcanzó el límite de traza detallada.
- Se muestran `Traza retenida` y `Fuera de traza` como métricas distintas.
- Un HUD discreto sigue actualizando el contador de estados no retenidos, por lo
  que una búsqueda larga ya no parece congelada.
- Se documenta que el progress bar mide ejecución lógica, no crecimiento del
  canvas.
- WiX 3.14/3.11 se autodetecta aunque no esté agregado al PATH global.
- `verify-installer-prereqs` valida además que el JDK activo sea 21.
- Se añade onboarding formal del entorno de construcción y distinción entre
  fallos de producto y fallos de estación de build.

## 0.7.2-SNAPSHOT — Exportación PNG adaptativa

- La lámina de estudio conserva el mismo formato visual.
- Trazas grandes se reorganizan con perfiles STANDARD, DENSE y COMPACT.
- El exportador busca automáticamente una cantidad de columnas adecuada.
- Se reducen huecos antes de reducir detalle; las tarjetas retenidas conservan
  título, movimiento, acumulado y estado.
- El PNG sigue siendo lossless y se mantiene dentro de un presupuesto seguro.
- Se añade prueba de exportación sobre `06-stress.case` con 2.500 nodos.

## 0.7.1-SNAPSHOT — Hotfix JavaFX y diagnóstico dependiente

- `TimelineDockPane` instala el tooltip mediante `Tooltip.install(...)` porque
  `HBox` es un `Node`, no un `Control`, y no expone `setTooltip`.
- `verify-all.bat` deja de repetir tests/package cuando la compilación JavaFX
  ya falló; los gates dependientes se registran como `SKIP`.
- JaCoCo sólo se evalúa cuando los tests terminaron satisfactoriamente.
- No cambia la gramática espacial, el HUD de zoom ni el formato `.case`.

## 0.7.0-SNAPSHOT — HUD de zoom y docking estable

- El control de zoom deja de ocupar una columna: ahora flota sobre el canvas
  como un HUD fijo del viewport.
- El HUD permanece inmóvil durante pan y scroll.
- El paneo gana margen adicional constante en pantalla para compensar el HUD.
- El centrado de nodos reserva una zona segura a la derecha.
- `ZoomOverlay` encapsula slider, botones e indicador.
- `ViewportNavigationPolicy` formaliza overscroll y centrado seguro.
- La línea de tiempo abandona TitledPane como mecanismo de expand/collapse.
- `TimelineDockPane` controla explícitamente cabecera y contenido.
- Contraer y volver a expandir restaura una tabla visible y un tamaño útil.
- Se conserva SplitPane vertical y divisor redimensionable.

## 0.6.2-SNAPSHOT — Hotfix de compilación del docking

- Elimina el helper `titledPane(...)` residual que quedó en `MainWindowLayout`
  después de extraer la maquetación a `WorkspaceDockLayout`.
- Corrige el `cannot find symbol: TitledPane` observado por Maven/Javadoc.
- JaCoCo distingue ahora entre ausencia de `jacoco.exec` y fallo del generador
  de reporte, evitando un diagnóstico engañoso cuando los tests no llegaron a correr.
- No cambia el comportamiento del docking, zoom adaptativo, dominio ni formato `.case`.

## 0.6.1-SNAPSHOT — Docking del timeline y zoom adaptativo

- La línea de tiempo usa un SplitPane vertical real y deja de quedar
  visualmente expandida con sólo la cabecera visible.
- El usuario puede redimensionar la franja inferior con un divisor.
- Colapsar conserva sólo la cabecera y expandir restaura un tamaño útil.
- El canvas incorpora tres niveles de detalle según zoom.
- A zoom mínimo se muestran tarjetas overview sin texto microscópico.
- Entre 55% y 80% se muestra un modo compacto; a partir de 80%, detalle pleno.
- La política de zoom vive en `SearchCanvasRenderSettings`.
- La política geométrica del timeline vive en `TimelineDockModel`.

## 0.6.0-SNAPSHOT — Estabilidad y pulido visual

- Corrige el ciclo recursivo ChoiceBox -> focusNode -> presenter que podía
  congelar la interfaz al aparecer resultados compatibles.
- El selector no repone la misma lista/selección innecesariamente.
- `focusNode` puede centrar un nodo sin volver a emitir el evento origen.
- La línea de tiempo inferior conserva espacio visible al maximizar.
- Toolbar con iconografía vectorial propia y pequeños acentos de color.
- Botones de zoom convertidos en lupas vectoriales +/-.
- ChoiceBox, Spinner y franja de ejecución reciben un acabado más definido.
- Sin dependencias gráficas externas ni cambios al formato `.case`.

## 0.5.1-SNAPSHOT — Hotfix de gates de calidad

- El gate de arquitectura inspecciona únicamente imports `javafx.*`; las
  menciones de JavaFX en Javadoc ya no son falsos positivos.
- Checkstyle y Javadoc usan el POM explícito después de `popd`.
- JaCoCo usa coordenadas completas del plugin y el POM explícito.
- `LayerDependencySourceTest` replica el contrato de capas dentro de JUnit.
- No cambia dominio, algoritmo, GUI ni formato `.case`.

## 0.5.0-SNAPSHOT — Release engineering reproducible

- Smoke test real de la app-image antes de levantar JavaFX.
- `verify-app-image.bat` comprueba EXE, JAR, configuración y runtime embebido.
- `release-evidence.bat` genera hashes SHA-256 del JAR y del launcher.
- `release-check.bat` incorpora smoke test y evidencia.
- Gate automático para impedir dependencias JavaFX en domain/engine/runtime.
- Reporte JaCoCo integrado al diagnóstico.
- CI Windows con Temurin 21 mediante GitHub Actions.
- Prerequisitos de instalador comprueban `candle.exe` y `light.exe`.
- Documentación de CI, mantenimiento y checklist de release.

## 0.4.1-SNAPSHOT — Hotfix de gates Maven

- `verify-all.bat` deja de depender del directorio de trabajo después de `popd`.
- Todas las invocaciones al Maven Wrapper usan `%ROOT%\mvnw.cmd`.
- Corrige el falso FAIL observado en Checkstyle y Javadoc cuando el resto del
  build ya había pasado correctamente.
- No cambia dominio, runtime, GUI ni formato `.case`.

## 0.4.0-SNAPSHOT — Ingeniería y pulido Windows 11

- Primera ejecución maximizada y persistencia del estado de ventana.
- Cabeceras oscuras con texto blanco y scrollbars de mayor contraste.
- `MainWindow` dividido en coordinación, layout, controles y presentación.
- Render del workspace extraído a `SearchCanvasPainter`.
- Exportador dividido en layout/header/grafo.
- Ningún `.java` supera 500 líneas.
- Checkstyle, Maven Enforcer, `-Xlint:all` y Javadoc como gates.
- Eliminado el warning `unchecked` de las tablas.
- Estándar de ingeniería, SemVer/release policy y ADRs.

## 0.3.0-SNAPSHOT

- Namespace `com.marcosmoreiradev.reconcilelab`.
- App-image autocontenida.
- Asociación `.case`.
- Paridad con siete casos compartidos.
