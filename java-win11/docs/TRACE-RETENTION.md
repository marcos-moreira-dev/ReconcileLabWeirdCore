# Traza retenida y progreso lógico

El canvas no representa necesariamente cada estado lógico visitado.

El motor conserva una traza detallada acotada para evitar que un caso de estrés
con millones de estados convierta la GUI en una estructura de memoria
incontrolable. La configuración actual retiene hasta 2.500 tarjetas detalladas.

Por tanto, en un caso grande pueden ocurrir simultáneamente estas dos cosas:

```text
el progress bar continúa avanzando
el tamaño lógico del canvas deja de crecer
```

Eso no significa que el algoritmo se haya congelado. Significa que el límite
de traza detallada ya se alcanzó.

Desde 0.7.3 la interfaz lo hace explícito:

- métrica `Traza retenida`;
- métrica `Fuera de traza`;
- aviso flotante en el canvas;
- contador vivo de estados procesados fuera de la vista;
- indicación de si la búsqueda sigue ejecutándose o ya terminó.

La exportación de la lámina utiliza la traza retenida, no los millones de
estados no almacenados. Esta frontera es deliberada y forma parte del contrato
de observabilidad del producto.


## Por qué hay pocas filas y muchas columnas

El identificador de un estado (`#2497`, por ejemplo) no representa su
profundidad vertical. Con 20 movimientos candidatos existen como máximo unas
21 profundidades lógicas relevantes (raíz + decisiones). Muchos estados
diferentes pertenecen a la misma profundidad y por eso el layout los distribuye
horizontalmente.

En consecuencia es normal observar:

```text
ID de nodo cercano a 2500
pocas bandas verticales
muchas tarjetas a lo ancho
```

La barra horizontal refleja esa anchura real de la traza retenida.

## Presentación en la interfaz

La frontera de retención se comunica de forma permanente y no intrusiva en el
inspector derecho:

```text
Traza retenida
Fuera de traza
```

No se muestra popup, toast ni cartel superpuesto al canvas. El lienzo queda
reservado exclusivamente para navegar y estudiar los estados retenidos.
