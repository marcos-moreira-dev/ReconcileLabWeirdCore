#ifndef RECONCILELAB_MAIN_FRAME_H
#define RECONCILELAB_MAIN_FRAME_H

#include "../runtime/RuntimeController.h"
#include "../model/ProblemInstance.h"
#include "GraphCanvas.h"
#include "WorkspacePanel.h"
#include "CoverageGauge.h"

#include <wx/frame.h>
#include <wx/timer.h>
#include <wx/listbox.h>
#include <wx/choice.h>
#include <wx/spinctrl.h>
#include <wx/listctrl.h>
#include <wx/stattext.h>
#include <wx/stopwatch.h>
#include <wx/textctrl.h>
#include <wx/collpane.h>
#include <wx/scrolwin.h>
#include <vector>

/**
 * Ventana principal y punto de composición de la interfaz.
 *
 * MainFrame crea los controles, recibe eventos de usuario y proyecta
 * ExecutionSnapshot. No resuelve el problema matemático: SearchEngine lo hace
 * detrás de RuntimeController.
 *
 * Para estudiar el código conviene pensar en esta clase como "controlador de
 * presentación": traduce botones/menús a operaciones del runtime y traduce un
 * snapshot a texto, tablas y dibujo.
 */
class MainFrame : public wxFrame
{
public:
    /** Construye toda la ventana, inicia el timer visual y carga un ejemplo. */
    MainFrame();

    /** Detiene timer e hilo antes de destruir la ventana. */
    virtual ~MainFrame();

private:
    enum
    {
        ID_RUN = wxID_HIGHEST + 100,
        ID_PAUSE,
        ID_RESET,
        ID_STEP,
        ID_OPEN_EXAMPLE,
        ID_EXPORT_IMAGE,
        ID_TIMER,
        ID_STRATEGY,
        ID_MODE,
        ID_REFRESH,
        ID_COMPATIBLE_STATE,
        ID_EXAMPLE_LIST,
        ID_HELP_CONTENTS,
        ID_TIMELINE,
        ID_PANE_EXAMPLES,
        ID_PANE_CASE,
        ID_PANE_SUMMARY,
        ID_PANE_ROUTE,
        ID_PANE_SOLUTIONS,
        ID_PANE_TIMELINE
    };

    /** Crea Archivo, Ejecución y Ayuda. */
    void BuildMenus();

    /** Crea los comandos rápidos con iconos nativos de wxWidgets. */
    void BuildToolbar();

    /** Compone panel izquierdo, workspace, columna derecha y timeline. */
    void BuildBody();

    /** Crea la barra de estado inferior. */
    void BuildStatus();

    /** Intenta abrir el primer caso de ejemplo del repositorio. */
    void LoadStartupExample();

    /**
     * Lee un `.case` desde disco y, si es válido, lo convierte en caso actual.
     */
    bool LoadCase(const wxString& path);

    /**
     * Instala un ProblemInstance ya validado en runtime y en la presentación.
     */
    void ApplyProblem(const ProblemInstance& problem);

    /**
     * Obtiene un snapshot consistente y actualiza las proyecciones visibles.
     */
    void RefreshFromRuntime();

    /** Actualiza valores del resumen sin destruir/recrear sus filas. */
    void RefreshMetrics(const ExecutionSnapshot& snapshot);

    /** Reinicia la muestra de estados/segundo. */
    void ResetRateSample();

    /** Reconstruye la timeline sólo cuando su contenido realmente cambió. */
    void RefreshTimeline(const ExecutionSnapshot& snapshot);

    /** Actualiza la lista de combinaciones compatibles retenidas. */
    void RefreshSolutions(const ExecutionSnapshot& snapshot);

    /**
     * Sincroniza el combo superior con estados NODE_MATCH retenidos.
     *
     * Sólo se reconstruye cuando cambia el conjunto de ids para no introducir
     * parpadeo ni perder innecesariamente la selección del usuario.
     */
    void RefreshCompatibleStates(const ExecutionSnapshot& snapshot);

    /** Proyecta la tarjeta seleccionada en el inspector textual. */
    void RefreshSelectedRoute();

    /**
     * Habilita/deshabilita comandos según READY/RUNNING/PAUSED/COMPLETED.
     */
    void UpdateCommandState();

    /** Aplica alternancia blanco/celeste a una fila de tabla. */
    void StyleTableRow(wxListCtrl* table, long row) const;

    /**
     * Construye un encabezado oscuro portable sobre una tabla sin header nativo.
     *
     * wxWidgets 3.0.5 no ofrece una API cómoda para recolorear el header
     * Win32. Por eso usamos un wxPanel normal encima del wxListCtrl.
     */
    wxPanel* CreateDarkTableHeader(
        wxWindow* parent,
        const wxArrayString& labels,
        const wxArrayInt& widths) const;

    /**
     * Carga el icono pequeño desde PNG.
     *
     * El `.ico` sigue incrustado en el EXE para Explorer. En la VM de XP el
     * loader de wxWidgets falló al abrir el ICO externo, por eso la barra de
     * título usa el PNG de 32 px, que ya está validado por el visor de XP.
     */
    void LoadBrandIcon();

    /** Devuelve la raíz del repositorio tomando `bin\ReconcileLab.exe` como base. */
    wxString AppRoot() const;

    /** Ruta de los presets/casos de ejemplo. */
    wxString ExamplesDir() const;

    /** Página inicial del manual HTML local. */
    wxString HelpIndex() const;

    /** Carpeta canónica de identidad visual. */
    wxString BrandingDir() const;

    /** Formatea centavos para la GUI. */
    wxString Money(int cents) const;

    /** Convierte una solución binaria en `M1 + M4 = $...`. */
    wxString DescribeSolution(
        const CompatibleSolution& solution,
        size_t number) const;

    /** Traduce el tipo interno de evento a una etiqueta breve en español. */
    wxString EventLabel(RunEventType type) const;

    /** Abre el diálogo para crear un caso manualmente. */
    void OnNew(wxCommandEvent& event);

    /** Inicia o reanuda la ejecución actual. */
    void OnRun(wxCommandEvent& event);

    /** Solicita una pausa cooperativa. */
    void OnPause(wxCommandEvent& event);

    /** Devuelve el caso actual a tick 0. */
    void OnReset(wxCommandEvent& event);

    /** Ejecuta exactamente una unidad lógica y permanece pausado. */
    void OnStep(wxCommandEvent& event);

    /** Abre un `.case` seleccionado por el usuario. */
    void OnOpen(wxCommandEvent& event);

    /** Guarda el caso actual en el mismo formato que los ejemplos. */
    void OnSaveAs(wxCommandEvent& event);

    /**
     * Exporta una lámina PNG con entrada, contexto teórico y grafo retenido.
     */
    void OnExportImage(wxCommandEvent& event);

    /** Cierra la ventana usando el flujo normal de wxWidgets. */
    void OnExit(wxCommandEvent& event);

    /** Abre un selector comenzando en `examples\`. */
    void OnOpenExample(wxCommandEvent& event);

    /** Carga el ejemplo activado con doble clic/Enter. */
    void OnExampleActivated(wxListEvent& event);

    /**
     * Cambia la estrategia y reinicia para no mezclar dos semánticas en una
     * misma ejecución.
     */
    void OnStrategy(wxCommandEvent& event);

    /** Cambia el ritmo del runtime sin cambiar el problema. */
    void OnMode(wxCommandEvent& event);

    /**
     * Centra en el canvas el estado compatible elegido en el combo superior.
     */
    void OnCompatibleState(wxCommandEvent& event);

    /** Modifica la cadencia máxima de muestreo visual. */
    void OnRefreshChanged(wxSpinEvent& event);

    /**
     * Muestrea el runtime mientras corre o cuando cambia de estado.
     *
     * En reposo evita reconstruir toda la interfaz cada 50 ms.
     */
    void OnTimer(wxTimerEvent& event);

    /** Recalcula sizers/scroll virtual al abrir o cerrar un panel. */
    void OnPaneChanged(wxCollapsiblePaneEvent& event);

    /** Abre el diálogo de identidad y contexto del producto. */
    void OnAbout(wxCommandEvent& event);

    /** Abre la ayuda HTML local. */
    void OnHelpContents(wxCommandEvent& event);

    /** Detiene el runtime de forma segura antes de destruir el frame. */
    void OnClose(wxCloseEvent& event);

private:
    /** Modelo/controlador de ejecución; único dueño del hilo de búsqueda. */
    RuntimeController m_runtime;

    /** Caso que la interfaz presenta actualmente. */
    ProblemInstance m_problem;
    bool m_caseLoaded;

    /** Workspace gráfico y su canvas desplazable. */
    WorkspacePanel* m_workspace;
    GraphCanvas* m_canvas;

    /** Controles superiores e izquierda. */
    wxListCtrl* m_examples;
    wxArrayString m_exampleFiles;
    wxChoice* m_strategy;
    wxChoice* m_mode;
    wxSpinCtrl* m_refreshMs;
    wxChoice* m_compatibleState;
    std::vector<unsigned long> m_compatibleNodeIds;

    /** Scroll común para las tres secciones de la columna derecha. */
    wxScrolledWindow* m_rightScroller;

    /** Controles que proyectan métricas, soluciones y selección. */
    wxListCtrl* m_metrics;
    CoverageGauge* m_coverage;
    wxListCtrl* m_timeline;
    wxListBox* m_solutions;
    wxTextCtrl* m_selectedRoute;
    wxStaticText* m_caseTitle;
    wxTextCtrl* m_caseSummary;

    /** Marcas para evitar reconstrucciones visuales innecesarias. */
    size_t m_lastEventCount;
    unsigned long m_lastEventTick;
    size_t m_lastSolutionCount;
    unsigned long m_lastHiddenSolutions;

    /** Estado de la estimación observacional de rendimiento. */
    wxStopWatch m_rateWatch;
    unsigned long m_rateLastTick;
    long m_rateLastMs;
    double m_observedStatesPerSecond;

    /** Último estado de runtime ya reflejado en pantalla. */
    ExecutionState m_lastPresentedState;

    /** Timer exclusivo de muestreo visual. */
    wxTimer m_timer;

    DECLARE_EVENT_TABLE()
};

#endif
