# Exportación de lámina de estudio

La exportación PNG no es una captura del viewport. Reconstruye el diagrama con
la misma composición pedagógica de ReconcileLab:

1. cabecera del caso;
2. métricas;
3. movimientos candidatos;
4. explicación breve;
5. bloque gris con tarjetas y conexiones.

## Política para trazas grandes

La traza retenida puede contener hasta 2.500 estados. El exportador prueba tres
perfiles de densidad:

- `STANDARD`: aspecto normal de las tarjetas;
- `DENSE`: reduce huecos y tipografía de forma moderada;
- `COMPACT`: último nivel, todavía con título, movimiento, acumulado y estado.

No se elimina una tarjeta retenida ni se sustituye por un bloque sin texto.

Para cada perfil se prueban distintas cantidades de columnas y se selecciona
una disposición que respete:

- presupuesto del bloque gráfico;
- dimensión máxima por eje;
- legibilidad mínima;
- proporción razonable del póster.

El PNG permanece lossless. La compresión buscada es principalmente espacial:
menos espacio vacío y menos separación entre tarjetas, no pérdida deliberada de
información.

## Criterio de aceptación

Para el caso `06-stress.case`:

- la traza retenida alcanza 2.500 nodos;
- la exportación termina sin el antiguo error de imagen demasiado grande;
- el PNG final queda por debajo del límite de 72 millones de píxeles;
- al hacer zoom siguen visibles título, movimiento, acumulado y estado de cada
  tarjeta retenida.
