# Guía de lectura del código — Java Edition

La edición Java está organizada para poder estudiarse sin empezar por JavaFX.

## Ruta recomendada

```text
1. domain/ProblemInstance
2. io/MoneyText
3. io/CaseFile
4. engine/SearchEngine
5. domain/ExecutionSnapshot
6. runtime/RuntimeController
7. ui/component/ExecutionProgressModel
8. ui/workspace/NodeLayoutModel
9. ui/workspace/SearchWorkspace
10. ui/MainWindow
11. ui/dialog/NewCaseDialog
12. ui/StudyImageExporter
13. app/ReconcileLabApplication
```

## 1. `ProblemInstance`

Empieza aquí porque fija las reglas que todos los demás componentes pueden dar
por verdaderas:

```text
target > 0
1..30 movimientos
cada movimiento > 0
dinero = centavos enteros
```

## 2. `CaseFile`

Muestra cómo una representación textual externa se convierte en un objeto del
dominio. Es también un buen ejemplo de por qué formato de archivo y modelo no
son la misma cosa.

## 3. `SearchEngine`

Es el corazón algorítmico.

Conviene seguir primero:

```text
reset
  ↓
runBatch
  ↓
shouldPrune
  ↓
addTraceNode
  ↓
pushChildren
```

La pila contiene tareas lógicas; no threads.

## 4. `ExecutionSnapshot`

Es la frontera entre el motor mutable y el resto del programa.

La GUI nunca dibuja directamente leyendo estructuras que el worker está
modificando.

## 5. `RuntimeController`

Introduce la diferencia:

```text
estado de búsqueda
!=
thread
!=
frame visual
```

Existe un worker único y una máquina READY/RUNNING/PAUSED/COMPLETED.

## 6. `ExecutionProgressModel`

Es pequeño, pero documenta una lección importante del proyecto:

```text
espacio contabilizado
!=
progreso de ejecución
```

Una búsqueda `first` puede terminar al 100% de su objetivo después de
contabilizar sólo una fracción del árbol potencial.

## 7. `NodeLayoutModel`

Asigna posiciones lógicas y las mantiene estables a medida que llegan nuevos
snapshots.

Todavía no hay Canvas aquí.

## 8. `SearchWorkspace`

Recién aquí aparece JavaFX.

Transforma:

```text
coordenada lógica
+ pan
+ zoom
+ viewport
        ↓
coordenada física
        ↓
GraphicsContext
```

## 9. `MainWindow`

Debe leerse al final. Su función es orquestar superficies:

```text
acción del usuario -> runtime
snapshot -> controles
```

No debería contener reglas para determinar si una combinación es válida.

## 10. Packaging

Después del código funcional, leer:

```text
pom.xml
scripts/verify-all.bat
scripts/build-app-image.bat
scripts/build-installer.bat
packaging/reconcilelab-case.properties
```

Ahí se ve cómo el programa fuente se convierte en una aplicación de Windows
autocontenida.
