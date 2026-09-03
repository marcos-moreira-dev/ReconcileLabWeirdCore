#ifndef RECONCILELAB_COMPATIBLE_SOLUTION_H
#define RECONCILELAB_COMPATIBLE_SOLUTION_H

/**
 * Descripción compacta de una combinación compatible.
 *
 * V1 limita cada caso a 30 movimientos. Gracias a ello, un `unsigned long`
 * de 32 bits puede funcionar como máscara: el bit i vale 1 cuando el
 * movimiento i participa en la combinación.
 *
 * Este diseño evita crear un `std::vector` nuevo para cada solución o tarea,
 * algo especialmente conveniente en la VM x86 de Windows XP.
 */
struct CompatibleSolution
{
    /** Máscara binaria de movimientos incluidos. */
    unsigned long selectionMask;

    /** Total alcanzado por la combinación, en centavos. */
    int totalCents;

    /** Tick lógico en el que el motor encontró la combinación. */
    unsigned long foundAtTick;

    /** Inicializa una solución vacía. */
    CompatibleSolution()
        : selectionMask(0), totalCents(0), foundAtTick(0)
    {
    }
};

#endif
