# Disposición del grafo

## Requisito

Las tarjetas no deben superponerse y, mientras la ejecución crece, las tarjetas
ya visibles no deben saltar continuamente de posición.

## V0.3: layout incremental por capas

- una fila por profundidad;
- ancho y alto de tarjeta fijos;
- hueco horizontal y vertical fijos;
- cada fila mantiene su siguiente coordenada X disponible;
- una tarjeta nueva se coloca al final del espacio libre de su fila;
- una tarjeta existente nunca se recoloca durante la ejecución.

Esto sacrifica algo de compactación a cambio de estabilidad visual, una
prioridad importante en Windows XP.

El documento raíz `ALGORITMO-DISPOSICION-DE-DIAGRAMAS.md` explica la técnica de
forma reutilizable.
