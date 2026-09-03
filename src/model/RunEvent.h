#ifndef RECONCILELAB_RUN_EVENT_H
#define RECONCILELAB_RUN_EVENT_H

#include <string>

/**
 * Tipos de hitos guardados en la línea de tiempo.
 *
 * La línea de tiempo no intenta grabar cada operación microscópica. Conserva
 * solamente eventos útiles para que el usuario pueda seguir la ejecución sin
 * convertir la instrumentación en el principal costo del programa.
 */
enum RunEventType
{
    RUN_EVENT_READY = 0,
    RUN_EVENT_STARTED,
    RUN_EVENT_PRUNED,
    RUN_EVENT_MATCH,
    RUN_EVENT_COMPLETED
};

/**
 * Hito legible asociado a un tick lógico del motor.
 */
struct RunEvent
{
    /** Momento lógico en el que ocurrió el evento. */
    unsigned long tick;

    /** Categoría del evento. */
    RunEventType type;

    /** Nodo retenido relacionado, o 0 cuando no aplica. */
    unsigned long nodeId;

    /** Texto breve preparado por el motor para la línea de tiempo. */
    std::string message;

    /** Inicializa un evento en estado preparado. */
    RunEvent()
        : tick(0), type(RUN_EVENT_READY), nodeId(0)
    {
    }
};

#endif
