/**
 * Implementación del ciclo de vida concurrente.
 *
 * La GUI nunca ejecuta el bucle de búsqueda completo. RuntimeController
 * protege SearchEngine con una sección crítica y expone snapshots copiados.
 */
#include "RuntimeController.h"
#include <wx/utils.h>

EngineThread::EngineThread(RuntimeController* owner)
    : wxThread(wxTHREAD_JOINABLE), m_owner(owner)
{
}

wxThread::ExitCode EngineThread::Entry()
{
    while (true)
    {
        if (m_owner->ShouldStop())
            break;

        if (m_owner->GetState() != EXECUTION_RUNNING)
        {
            wxMilliSleep(10);
            continue;
        }

        m_owner->RunThreadBatch();
        wxMilliSleep(m_owner->SleepMillis());
    }

    return (wxThread::ExitCode)0;
}

RuntimeController::RuntimeController()
    : m_thread(0),
      m_stopRequested(false),
      m_mode(MODE_BALANCED),
      m_state(EXECUTION_READY)
{
}

RuntimeController::~RuntimeController()
{
    Stop();
}

void RuntimeController::LoadProblem(const ProblemInstance& problem)
{
    Stop();

    wxCriticalSectionLocker lock(m_cs);
    m_problem = problem;
    m_engine.Reset(problem);
    m_stopRequested = false;
    m_state = EXECUTION_READY;
}

void RuntimeController::SetStrategy(SearchStrategy strategy)
{
    wxCriticalSectionLocker lock(m_cs);
    m_engine.SetStrategy(strategy);
}

void RuntimeController::SetMode(ExecutionMode mode)
{
    wxCriticalSectionLocker lock(m_cs);
    m_mode = mode;
}

bool RuntimeController::Start()
{
    wxCriticalSectionLocker lock(m_cs);

    if (m_engine.IsComplete())
    {
        m_state = EXECUTION_COMPLETED;
        return false;
    }

    if (m_state == EXECUTION_RUNNING)
        return true;

    ExecutionState previous = m_state;
    m_stopRequested = false;
    m_state = EXECUTION_RUNNING;

    if (!m_thread)
    {
        m_thread = new EngineThread(this);

        if (m_thread->Create() != wxTHREAD_NO_ERROR)
        {
            delete m_thread;
            m_thread = 0;
            m_state = previous;
            return false;
        }

        if (m_thread->Run() != wxTHREAD_NO_ERROR)
        {
            delete m_thread;
            m_thread = 0;
            m_state = previous;
            return false;
        }
    }

    return true;
}

void RuntimeController::Pause()
{
    wxCriticalSectionLocker lock(m_cs);

    if (m_state == EXECUTION_RUNNING)
        m_state = EXECUTION_PAUSED;
}

void RuntimeController::Reset()
{
    Stop();

    wxCriticalSectionLocker lock(m_cs);
    m_engine.Reset(m_problem);
    m_stopRequested = false;
    m_state = EXECUTION_READY;
}

bool RuntimeController::StepOnce()
{
    wxCriticalSectionLocker lock(m_cs);

    if (m_state == EXECUTION_RUNNING || m_engine.IsComplete())
    {
        if (m_engine.IsComplete())
            m_state = EXECUTION_COMPLETED;

        return false;
    }

    m_engine.Step();

    m_state = m_engine.IsComplete()
        ? EXECUTION_COMPLETED
        : EXECUTION_PAUSED;

    return true;
}

void RuntimeController::Stop()
{
    EngineThread* thread = 0;
    ExecutionState priorState = EXECUTION_READY;

    {
        wxCriticalSectionLocker lock(m_cs);

        if (!m_thread)
            return;

        priorState = m_state;
        m_stopRequested = true;
        thread = m_thread;
    }

    // Esperamos fuera de la sección crítica. El worker debe poder adquirir el
    // mismo bloqueo para leer la bandera de parada y abandonar Entry().
    thread->Wait();

    {
        wxCriticalSectionLocker lock(m_cs);

        if (m_thread == thread)
            m_thread = 0;

        m_stopRequested = false;

        if (m_engine.IsComplete())
            m_state = EXECUTION_COMPLETED;
        else if (priorState == EXECUTION_RUNNING)
            m_state = EXECUTION_PAUSED;
        else
            m_state = priorState;
    }

    delete thread;
}

ExecutionState RuntimeController::GetState() const
{
    wxCriticalSectionLocker lock(m_cs);
    return m_state;
}

bool RuntimeController::IsPaused() const
{
    wxCriticalSectionLocker lock(m_cs);
    return m_state != EXECUTION_RUNNING;
}

bool RuntimeController::ShouldStop() const
{
    wxCriticalSectionLocker lock(m_cs);
    return m_stopRequested;
}

ExecutionSnapshot RuntimeController::GetSnapshot() const
{
    wxCriticalSectionLocker lock(m_cs);
    return m_engine.Snapshot();
}

void RuntimeController::RunThreadBatch()
{
    wxCriticalSectionLocker lock(m_cs);

    if (m_state != EXECUTION_RUNNING)
        return;

    if (m_engine.IsComplete())
    {
        m_state = EXECUTION_COMPLETED;
        return;
    }

    m_engine.RunBatch(BatchSize());

    if (m_engine.IsComplete())
        m_state = EXECUTION_COMPLETED;
}

unsigned long RuntimeController::BatchSize() const
{
    if (m_mode == MODE_STUDY)
        return 1;

    if (m_mode == MODE_MAXIMUM)
        return 10000;

    // Equilibrado: los casos de hasta 8 movimientos procesan una sola unidad
    // por lote. Con una pausa corta de 25 ms, un caso diminuto no puede
    // nacer y terminar completamente entre dos refrescos visuales de 50 ms.
    // Casos mayores escalan el lote para no sacrificar rendimiento.
    const size_t count = m_problem.valuesCents.size();

    if (count <= 8)  return 1;
    if (count <= 12) return 16;
    if (count <= 16) return 128;
    return 1024;
}

unsigned long RuntimeController::SleepMillis() const
{
    wxCriticalSectionLocker lock(m_cs);

    if (m_mode == MODE_STUDY)
        return 120;

    if (m_mode == MODE_MAXIMUM)
        return 1;

    const size_t count = m_problem.valuesCents.size();

    if (count <= 8)  return 25;
    if (count <= 12) return 10;
    return 5;
}
