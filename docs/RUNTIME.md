# Runtime y ritmo visual

ReconcileLab separa tres ritmos:

1. trabajo lógico del algoritmo;
2. muestreo de snapshots por la ventana;
3. animación local de pequeños controles como la barra de progreso.

Una operación lógica no equivale a un repaint.

## Máquina de estados

```text
READY --Ejecutar--> RUNNING
READY --Paso------> PAUSED

RUNNING --Pausar--> PAUSED

PAUSED --Paso------> PAUSED
PAUSED --Ejecutar-> RUNNING

RUNNING/PAUSED --fin--> COMPLETED
COMPLETED --Reiniciar-> READY
```

## Modo Equilibrado

Para casos de hasta 8 movimientos:

```text
1 estado por lote
25 ms de cesión entre lotes
```

Eso evita que un ejemplo diminuto termine completamente entre dos ticks
visuales de 50 ms. Casos mayores usan lotes crecientes.

`Máximo` conserva la ruta rápida y `Estudio` mantiene una pausa mucho mayor.

## Dos porcentajes distintos

### Espacio contabilizado

```text
(visitados + evitados) / potenciales
```

Mide cuánto del árbol potencial quedó explicado por trabajo real o poda segura.

### Progreso de ejecución

Es el progress bar de la GUI.

Mientras corre usa el espacio contabilizado como referencia. Cuando el objetivo
termina pasa a 100%, aunque un caso `first` haya encontrado una coincidencia
tras explorar sólo una parte del árbol.

Ejemplo:

```text
Ejecución completada: 100%
Espacio contabilizado: 8.7%
```

No existe contradicción: el objetivo terminó precisamente porque no hacía falta
examinar el 91.3% restante.

## Animación de la barra

`CoverageGauge` tiene un timer propio de 20 ms. MainFrame publica objetivos y
la barra interpola su valor visible. Así un cambio rápido no depende de que el
motor permanezca activo durante varios ticks de GUI.
