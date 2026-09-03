# Algoritmo de disposición de cajas para diagramas

Este archivo describe una técnica pequeña y reutilizable para colocar cajas
(rectángulos con texto) en un lienzo sin que se superpongan. Está escrito de
forma independiente de ReconcileLab para poder reutilizarlo, por ejemplo, en
un diagrama de clases.

## Objetivo

Dado un conjunto de elementos relacionados, producir coordenadas `(x, y)` que
cumplan al menos estas reglas:

1. dos cajas de una misma capa no se superponen;
2. existe un margen horizontal y vertical constante;
3. la posición es determinista;
4. durante una construcción incremental, una caja ya colocada no necesita
   moverse cada vez que aparece otra.

## Variante incremental por capas

Es la variante adecuada cuando el diagrama crece mientras se ejecuta el
programa.

Cada elemento recibe una `capa` o `profundidad`.

```text
capa 0        [A]

capa 1   [B]  [C]  [D]

capa 2   [E]  [F]  [G]  [H]
```

Se mantienen dos constantes:

```text
ANCHO_CAJA
HUECO_HORIZONTAL
```

y una tabla:

```text
siguienteX[capa]
```

Al insertar una caja nueva:

```text
y = MARGEN_SUPERIOR
    + capa * (ALTO_CAJA + HUECO_VERTICAL)

x = siguienteX[capa]

posicion[caja] = (x, y)

siguienteX[capa] =
    x + ANCHO_CAJA + HUECO_HORIZONTAL
```

La propiedad importante es inmediata:

```text
x(nueva) >= x(anterior) + ANCHO_CAJA + HUECO_HORIZONTAL
```

Por tanto, dos cajas de la misma capa no pueden solaparse.

### Ventaja

Las cajas existentes no cambian de posición. Esto evita que un diagrama
animado "tiemble" cuando aparecen elementos nuevos.

### Desventaja

No minimiza cruces de líneas y puede producir un lienzo ancho. Para un
laboratorio pequeño esto suele ser un intercambio razonable.

## Variante tidy-tree para un árbol terminado

Si el diagrama ya está completo y las relaciones forman un árbol, se puede
obtener una composición más compacta calculando primero el ancho de cada
subárbol.

```text
ancho(hoja) = ANCHO_CAJA

ancho(nodo) =
    max(
        ANCHO_CAJA,
        suma(ancho(hijos))
        + HUECO_HORIZONTAL * (cantidadHijos - 1)
    )
```

Después se coloca el padre en el centro del intervalo reservado a sus hijos.

```text
xPadre =
    izquierdaSubarbol
    + (anchoSubarbol - ANCHO_CAJA) / 2
```

Los hijos reciben intervalos no superpuestos dentro del intervalo del padre.

Esta variante produce árboles visualmente más bonitos, pero si el árbol crece
en tiempo real puede recolocar nodos anteriores y dar sensación de parpadeo o
salto.

## Adaptación a diagramas de clases

Un diagrama de clases puede ser un grafo y no un árbol. Una versión sencilla:

1. asignar cada clase a una capa;
2. colocar primero las clases de la capa 0;
3. colocar las demás de izquierda a derecha;
4. usar el centro promedio de sus padres como posición deseada;
5. si esa posición invade una caja ya colocada, desplazar la nueva caja hacia
   la derecha hasta respetar el hueco mínimo;
6. dibujar las relaciones después de fijar todas las cajas.

Pseudocódigo:

```text
para cada capa:
    siguienteX = margen

    para cada caja de la capa:
        deseado = centroPromedioDePadres(caja)
        x = max(siguienteX, deseado - ANCHO_CAJA/2)

        colocar(caja, x, yDeLaCapa)

        siguienteX =
            x + ANCHO_CAJA + HUECO_HORIZONTAL
```

Esto no pretende competir con un motor profesional de layout de grafos. Su
valor es que es pequeño, explicable, determinista y suficiente para diagramas
académicos o herramientas de escritorio con pocas decenas de elementos.

## Separación recomendada

No mezclar el objeto representado con sus coordenadas:

```text
Clase / Nodo de dominio
        |
        v
Modelo de relaciones
        |
        v
Algoritmo de layout
        |
        v
(x, y, ancho, alto)
        |
        v
Renderer
```

El renderer dibuja. El algoritmo de layout decide posiciones. El modelo de
dominio no conoce píxeles, scrollbars ni primitivas gráficas.
