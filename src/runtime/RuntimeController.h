#ifndef RECONCILELAB_RUNTIME_CONTROLLER_H
#define RECONCILELAB_RUNTIME_CONTROLLER_H

#include "../engine/SearchEngine.h"
#include <wx/thread.h>

/**
 * Ritmos de ejecución visibles para el usuario.
 *
 * Afectan cuántas unidades lógicas procesa cada lote y cuánto cede el hilo
 * entre lotes. La frecuencia de dibujo de la GUI sigue siendo independiente.
 */
enum ExecutionMode
{
    MODE_STUDY = 0,
    MODE_BALANCED = 1,
    MODE_MAXIMUM = 2
};

/**
 * Estados explícitos del flujo de ejecución.
 *
 * V1 evita reutilizar un solo booleano "paused" para significar al mismo
 * tiempo "todavía no empezó", "está pausado" y "ya terminó". Esa ambigüedad
 * hacía más difícil razonar sobre Ejecutar / Pausar / Paso / Reiniciar.
 */
enum ExecutionState
{
    EXECUTION_READY = 0,
    EXECUTION_RUNNING,
    EXECUTION_PAUSED,
    EXECUTION_COMPLETED
};

class RuntimeController;

/**
 * Único hilo de ejecución en segundo plano.
 *
 * La VM objetivo puede disponer de un solo núcleo; por ello V1 separa GUI y
 * motor, pero no crea un pool CPU-bound innecesario.
 */
class EngineThread : public wxThread
{
public:
    explicit EngineThread(RuntimeController* owner);

protected:
    /** Procesa lotes mientras el runtime esté en estado RUNNING. */
    virtual ExitCode Entry();

private:
    RuntimeController* m_owner;
};

/**
 * Coordina el ciclo de vida del motor sin exponer sus detalles a la GUI.
 *
 * Todo acceso a SearchEngine está protegido por una wxCriticalSection. La
 * interfaz obtiene copias consistentes mediante ExecutionSnapshot.
 */
class RuntimeController
{
    friend class EngineThread;

public:
    RuntimeController();
    ~RuntimeController();

    /** Detiene cualquier ejecución previa y prepara un nuevo problema. */
    void LoadProblem(const ProblemInstance& problem);

    /** Selecciona la estrategia utilizada por los lotes siguientes. */
    void SetStrategy(SearchStrategy strategy);

    /** Cambia el ritmo del motor sin modificar el temporizador de la GUI. */
    void SetMode(ExecutionMode mode);

    /**
     * Inicia desde READY o reanuda desde PAUSED.
     *
     * No reinicia automáticamente una ejecución COMPLETED; el usuario debe
     * pulsar Reiniciar de forma explícita.
     */
    bool Start();

    /** Pasa de RUNNING a PAUSED al terminar el lote protegido actual. */
    void Pause();

    /** Devuelve el caso actual a su estado inicial y READY. */
    void Reset();

    /**
     * Procesa exactamente una unidad lógica.
     *
     * Se admite desde READY o PAUSED. Al terminar, permanece PAUSED salvo que
     * ese paso complete el problema.
     */
    bool StepOnce();

    /** Solicita parada cooperativa y espera al hilo joinable. */
    void Stop();

    /** Devuelve el estado explícito actual. */
    ExecutionState GetState() const;

    /** Compatibilidad con código de presentación existente. */
    bool IsPaused() const;

    /** Indica al hilo si debe abandonar su bucle. */
    bool ShouldStop() const;

    /** Devuelve una copia consistente apta para representación. */
    ExecutionSnapshot GetSnapshot() const;

private:
    /** Ejecuta un lote si el runtime continúa en estado RUNNING. */
    void RunThreadBatch();

    /**
     * Calcula un lote adaptativo para el modo Equilibrado.
     *
     * Casos pequeños avanzan en grupos muy chicos para que el progreso sea
     * visible; casos grandes usan lotes mayores para no convertir la animación
     * en el cuello de botella.
     */
    unsigned long BatchSize() const;

    /** Devuelve la pausa cooperativa entre lotes. */
    unsigned long SleepMillis() const;

private:
    mutable wxCriticalSection m_cs;
    SearchEngine m_engine;
    ProblemInstance m_problem;

    EngineThread* m_thread;

    bool m_stopRequested;
    ExecutionMode m_mode;
    ExecutionState m_state;
};

#endif
