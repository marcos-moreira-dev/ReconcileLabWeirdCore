# Motor de búsqueda

El caso contiene un monto objetivo y movimientos positivos.

Cada estado conserva:

```text
posición del siguiente movimiento
monto acumulado
selección construida hasta el momento
```

En cada decisión se puede incluir u omitir el siguiente movimiento.

La búsqueda con poda añade dos reglas seguras para montos positivos:

1. si el acumulado supera el objetivo, la ruta se descarta;
2. si ni sumando todos los movimientos restantes se alcanza el objetivo, la
   ruta se descarta.

El motor no crea widgets. Solo produce estado, métricas, soluciones y una traza
acotada para que la interfaz decida cómo representarlos.
