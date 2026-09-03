#ifndef RECONCILELAB_PROBLEM_INSTANCE_H
#define RECONCILELAB_PROBLEM_INSTANCE_H

#include <string>
#include <vector>

/**
 * Define cuándo una búsqueda de conciliación puede darse por terminada.
 *
 * OBJECTIVE_FIRST es útil cuando basta con una explicación compatible.
 * OBJECTIVE_ALL obliga a buscar todas las combinaciones.
 * OBJECTIVE_COUNT cuenta todas, aunque la GUI sólo conserve una muestra
 * acotada para no gastar memoria sin límite.
 */
enum SearchObjective
{
    OBJECTIVE_FIRST = 0,
    OBJECTIVE_ALL = 1,
    OBJECTIVE_COUNT = 2
};

/**
 * Entrada completa de una ejecución de conciliación.
 *
 * Es una estructura de modelo puro: no contiene wxString, ventanas ni
 * coordenadas. Esto permite probar el problema sin levantar la GUI.
 *
 * Los montos se guardan como centavos enteros. Por ejemplo, $12.35 se
 * representa como 1235. Evitamos así errores de redondeo binario propios de
 * `float` o `double` cuando se trabaja con dinero decimal.
 */
struct ProblemInstance
{
    /** Nombre humano mostrado por la aplicación. */
    std::string name;

    /** Explicación breve del caso, también visible en la ayuda contextual. */
    std::string description;

    /** Montos candidatos, cada uno expresado en centavos enteros. */
    std::vector<int> valuesCents;

    /** Monto total que se intenta conciliar. */
    int targetCents;

    /** Regla que determina cuándo puede detenerse la búsqueda. */
    SearchObjective objective;

    /** Construye un caso vacío con objetivo "primera coincidencia". */
    ProblemInstance()
        : targetCents(0), objective(OBJECTIVE_FIRST)
    {
    }
};

#endif
