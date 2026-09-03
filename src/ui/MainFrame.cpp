#include "MainFrame.h"
#include "HelpFrame.h"
#include "NewCaseDialog.h"
#include "../io/CaseFile.h"
#include "../app/Version.h"

#include <wx/artprov.h>
#include <wx/menu.h>
#include <wx/toolbar.h>
#include <wx/panel.h>
#include <wx/collpane.h>
#include <wx/textctrl.h>
#include <wx/filedlg.h>
#include <wx/filename.h>
#include <wx/stdpaths.h>
#include <wx/dir.h>
#include <wx/msgdlg.h>
#include <wx/sizer.h>
#include <wx/statbox.h>
#include <wx/button.h>
#include <wx/statbmp.h>
#include <wx/dialog.h>
#include <wx/icon.h>
#include <wx/imaglist.h>
#include <wx/dcmemory.h>
#include <sstream>

BEGIN_EVENT_TABLE(MainFrame, wxFrame)
    EVT_MENU(wxID_NEW, MainFrame::OnNew)
    EVT_MENU(ID_RUN, MainFrame::OnRun)
    EVT_MENU(ID_PAUSE, MainFrame::OnPause)
    EVT_MENU(ID_RESET, MainFrame::OnReset)
    EVT_MENU(ID_STEP, MainFrame::OnStep)
    EVT_MENU(wxID_OPEN, MainFrame::OnOpen)
    EVT_MENU(wxID_SAVEAS, MainFrame::OnSaveAs)
    EVT_MENU(ID_EXPORT_IMAGE, MainFrame::OnExportImage)
    EVT_MENU(wxID_EXIT, MainFrame::OnExit)
    EVT_MENU(ID_OPEN_EXAMPLE, MainFrame::OnOpenExample)
    EVT_MENU(wxID_ABOUT, MainFrame::OnAbout)
    EVT_MENU(ID_HELP_CONTENTS, MainFrame::OnHelpContents)
    EVT_LIST_ITEM_ACTIVATED(ID_EXAMPLE_LIST, MainFrame::OnExampleActivated)
    EVT_CHOICE(ID_STRATEGY, MainFrame::OnStrategy)
    EVT_CHOICE(ID_MODE, MainFrame::OnMode)
    EVT_CHOICE(ID_COMPATIBLE_STATE, MainFrame::OnCompatibleState)
    EVT_SPINCTRL(ID_REFRESH, MainFrame::OnRefreshChanged)
    EVT_COLLAPSIBLEPANE_CHANGED(wxID_ANY, MainFrame::OnPaneChanged)
    EVT_TIMER(ID_TIMER, MainFrame::OnTimer)
    EVT_CLOSE(MainFrame::OnClose)
END_EVENT_TABLE()

static wxString ULong(unsigned long value)
{
    std::ostringstream out;
    out << value;
    return wxString(out.str().c_str(), wxConvLocal);
}

MainFrame::MainFrame()
    : wxFrame(NULL, wxID_ANY,
              wxT("ReconcileLab 2006 - Conciliacion y trazabilidad"),
              wxDefaultPosition, wxSize(1280, 820)),
      m_caseLoaded(false),
      m_workspace(0), m_canvas(0),
      m_examples(0), m_strategy(0), m_mode(0),
      m_refreshMs(0),
      m_compatibleState(0),
      m_rightScroller(0),
      m_metrics(0), m_coverage(0),
      m_timeline(0), m_solutions(0),
      m_selectedRoute(0), m_caseTitle(0), m_caseSummary(0),
      m_lastEventCount(0), m_lastEventTick(0),
      m_lastSolutionCount(0), m_lastHiddenSolutions(0),
      m_rateLastTick(0), m_rateLastMs(0), m_observedStatesPerSecond(0.0),
      m_lastPresentedState(EXECUTION_READY),
      m_timer(this, ID_TIMER)
{
    SetMinSize(wxSize(1000, 680));
    LoadBrandIcon();

    BuildMenus();
    BuildToolbar();
    BuildBody();
    BuildStatus();

    // El timer sólo decide cada cuánto observar la ejecución. El motor usa
    // su propio tamaño de lote; no existe una relación paso = repaint.
    m_timer.Start(50);

    LoadStartupExample();
    Centre();
}

MainFrame::~MainFrame()
{
    m_timer.Stop();
    m_runtime.Stop();
}

wxString MainFrame::AppRoot() const
{
    // El EXE vive en `bin`. Subimos un nivel desde su ruta real para no
    // depender del directorio de trabajo con el que Windows lanzó el programa.
    wxFileName exe(wxStandardPaths::Get().GetExecutablePath());
    wxFileName root(exe.GetPath(), wxEmptyString);
    root.RemoveLastDir();
    return root.GetPath();
}

wxString MainFrame::ExamplesDir() const
{
    return AppRoot() + wxFILE_SEP_PATH + wxT("examples");
}

wxString MainFrame::HelpIndex() const
{
    return AppRoot() + wxFILE_SEP_PATH + wxT("help")
           + wxFILE_SEP_PATH + wxT("index.html");
}

wxString MainFrame::BrandingDir() const
{
    return AppRoot() + wxFILE_SEP_PATH + wxT("assets")
           + wxFILE_SEP_PATH + wxT("branding");
}

void MainFrame::LoadBrandIcon()
{
    // El ICO queda incrustado en el ejecutable para Explorer. Para la barra de
    // título usamos PNG porque wxWidgets 3.0.5/XP reportó un falso error al
    // intentar abrir el ICO externo aunque el archivo existiera.
    wxString path = BrandingDir()
                    + wxFILE_SEP_PATH
                    + wxT("reconcilelab-icon-32.png");

    if (!wxFileExists(path))
        return;

    wxBitmap bitmap(path, wxBITMAP_TYPE_PNG);
    if (!bitmap.IsOk())
        return;

    wxIcon icon;
    icon.CopyFromBitmap(bitmap);

    if (icon.IsOk())
        SetIcon(icon);
}

wxString MainFrame::Money(int cents) const
{
    return wxString::Format(wxT("$%d.%02d"), cents / 100, cents % 100);
}

wxString MainFrame::DescribeSolution(const CompatibleSolution& solution,
                                     size_t number) const
{
    wxString text;
    text << wxT("#") << (unsigned long)(number + 1) << wxT("  ");

    bool first = true;
    for (size_t i = 0; i < m_problem.valuesCents.size(); ++i)
    {
        if ((solution.selectionMask & (1UL << i)) == 0)
            continue;

        if (!first)
            text << wxT(" + ");

        text << wxT("M") << (unsigned long)(i + 1)
             << wxT(" ") << Money(m_problem.valuesCents[i]);
        first = false;
    }

    if (first)
        text << wxT("$0.00");

    text << wxT(" = ") << Money(solution.totalCents);
    return text;
}

wxString MainFrame::EventLabel(RunEventType type) const
{
    switch (type)
    {
        case RUN_EVENT_READY: return wxT("Preparado");
        case RUN_EVENT_STARTED: return wxT("Iniciada");
        case RUN_EVENT_PRUNED: return wxT("Descartado");
        case RUN_EVENT_MATCH: return wxT("Compatible");
        case RUN_EVENT_COMPLETED: return wxT("Completada");
        default: return wxT("Evento");
    }
}

wxPanel* MainFrame::CreateDarkTableHeader(
    wxWindow* parent,
    const wxArrayString& labels,
    const wxArrayInt& widths) const
{
    wxPanel* header = new wxPanel(parent, wxID_ANY);
    const wxColour dark(54, 72, 92);
    const wxColour separator(91, 111, 131);

    header->SetBackgroundColour(dark);

    wxBoxSizer* sizer = new wxBoxSizer(wxHORIZONTAL);

    for (size_t i = 0; i < labels.GetCount(); ++i)
    {
        wxPanel* cell = new wxPanel(header, wxID_ANY);
        cell->SetBackgroundColour(dark);

        wxBoxSizer* cellSizer = new wxBoxSizer(wxHORIZONTAL);

        wxStaticText* label =
            new wxStaticText(cell, wxID_ANY, labels[i]);

        label->SetForegroundColour(*wxWHITE);
        label->SetBackgroundColour(dark);

        wxFont font = label->GetFont();
        font.SetWeight(wxFONTWEIGHT_BOLD);
        label->SetFont(font);

        cellSizer->Add(label, 1, wxALIGN_CENTER_VERTICAL | wxLEFT, 5);
        cell->SetSizer(cellSizer);

        int width = -1;
        if (i < widths.GetCount())
            width = widths[i];

        if (width > 0)
            sizer->Add(cell, 0, wxEXPAND, 0)->SetMinSize(wxSize(width, 23));
        else
            sizer->Add(cell, 1, wxEXPAND, 0);

        if (i + 1 < labels.GetCount())
        {
            wxPanel* divider = new wxPanel(
                header, wxID_ANY,
                wxDefaultPosition, wxSize(1, 23));
            divider->SetBackgroundColour(separator);
            sizer->Add(divider, 0, wxEXPAND);
        }
    }

    header->SetSizer(sizer);
    header->SetMinSize(wxSize(-1, 23));
    return header;
}

void MainFrame::BuildMenus()
{
    wxMenu* file = new wxMenu;
    file->Append(wxID_NEW, wxT("&Nuevo caso...\tCtrl+N"));
    file->Append(wxID_OPEN, wxT("&Abrir caso...\tCtrl+O"));
    file->Append(ID_OPEN_EXAMPLE, wxT("Abrir &ejemplo..."));
    file->Append(wxID_SAVEAS, wxT("&Guardar caso como..."));
    file->AppendSeparator();
    file->Append(wxID_EXIT, wxT("&Salir"));

    wxMenu* run = new wxMenu;
    run->Append(ID_RUN, wxT("&Ejecutar\tF5"));
    run->Append(ID_PAUSE, wxT("&Pausar\tF6"));
    run->Append(ID_STEP, wxT("&Paso\tF7"));
    run->Append(ID_RESET, wxT("&Reiniciar\tF8"));

    wxMenu* help = new wxMenu;
    help->Append(ID_HELP_CONTENTS, wxT("&Contenido...\tF1"));
    help->AppendSeparator();
    help->Append(wxID_ABOUT, wxT("&Acerca de ReconcileLab 2006..."));

    wxMenuBar* bar = new wxMenuBar;
    bar->Append(file, wxT("&Archivo"));
    bar->Append(run, wxT("&Ejecucion"));
    bar->Append(help, wxT("A&yuda"));
    SetMenuBar(bar);
}

void MainFrame::BuildToolbar()
{
    wxToolBar* toolbar =
        CreateToolBar(wxTB_FLAT | wxTB_HORIZONTAL | wxTB_NODIVIDER);
    toolbar->SetToolBitmapSize(wxSize(16, 16));

    toolbar->AddTool(wxID_NEW, wxT("Nuevo"),
                     wxArtProvider::GetBitmap(wxART_NORMAL_FILE, wxART_TOOLBAR,
                                              wxSize(16, 16)),
                     wxT("Crear un caso de conciliacion"));

    toolbar->AddTool(wxID_OPEN, wxT("Abrir"),
                     wxArtProvider::GetBitmap(wxART_FILE_OPEN, wxART_TOOLBAR,
                                              wxSize(16, 16)),
                     wxT("Abrir un caso de conciliacion"));

    toolbar->AddTool(ID_EXPORT_IMAGE, wxT("Exportar imagen"),
                     wxArtProvider::GetBitmap(wxART_FILE_SAVE, wxART_TOOLBAR,
                                              wxSize(16, 16)),
                     wxT("Exportar una lamina PNG del caso y su diagrama"));

    toolbar->AddSeparator();

    toolbar->AddTool(ID_RUN, wxT("Ejecutar"),
                     wxArtProvider::GetBitmap(wxART_GO_FORWARD, wxART_TOOLBAR,
                                              wxSize(16, 16)),
                     wxT("Ejecutar"));

    toolbar->AddTool(ID_PAUSE, wxT("Pausar"),
                     wxArtProvider::GetBitmap(wxART_CROSS_MARK, wxART_TOOLBAR,
                                              wxSize(16, 16)),
                     wxT("Pausar"));

    toolbar->AddTool(ID_STEP, wxT("Paso"),
                     wxArtProvider::GetBitmap(wxART_GO_DOWN, wxART_TOOLBAR,
                                              wxSize(16, 16)),
                     wxT("Avanzar un paso logico"));

    toolbar->AddTool(ID_RESET, wxT("Reiniciar"),
                     wxArtProvider::GetBitmap(wxART_UNDO, wxART_TOOLBAR,
                                              wxSize(16, 16)),
                     wxT("Reiniciar el caso actual"));

    toolbar->Realize();
}

void MainFrame::BuildBody()
{
    wxPanel* body = new wxPanel(this);
    wxBoxSizer* root = new wxBoxSizer(wxVERTICAL);

    wxPanel* controls = new wxPanel(body);
    wxBoxSizer* controlSizer = new wxBoxSizer(wxHORIZONTAL);

    controlSizer->Add(
        new wxStaticText(controls, wxID_ANY, wxT("Metodo de busqueda:")),
        0, wxALIGN_CENTER_VERTICAL | wxRIGHT, 5);

    wxArrayString strategies;
    strategies.Add(wxT("Busqueda exhaustiva"));
    strategies.Add(wxT("Busqueda con poda segura"));

    m_strategy = new wxChoice(controls, ID_STRATEGY, wxDefaultPosition,
                              wxSize(190, -1), strategies);
    m_strategy->SetSelection(1);
    controlSizer->Add(m_strategy, 0, wxRIGHT, 14);

    controlSizer->Add(
        new wxStaticText(controls, wxID_ANY, wxT("Ejecucion:")),
        0, wxALIGN_CENTER_VERTICAL | wxRIGHT, 5);

    wxArrayString modes;
    modes.Add(wxT("Estudio"));
    modes.Add(wxT("Equilibrado"));
    modes.Add(wxT("Maximo"));

    m_mode = new wxChoice(controls, ID_MODE, wxDefaultPosition,
                          wxSize(120, -1), modes);
    m_mode->SetSelection(1);
    controlSizer->Add(m_mode, 0, wxRIGHT, 14);

    controlSizer->Add(
        new wxStaticText(controls, wxID_ANY, wxT("Actualizacion visual:")),
        0, wxALIGN_CENTER_VERTICAL | wxRIGHT, 5);

    m_refreshMs = new wxSpinCtrl(controls, ID_REFRESH, wxT("50"),
                                 wxDefaultPosition, wxSize(75, -1),
                                 wxSP_ARROW_KEYS, 20, 1000, 50);
    controlSizer->Add(m_refreshMs, 0, wxRIGHT, 5);
    controlSizer->Add(new wxStaticText(controls, wxID_ANY, wxT("ms")),
                      0, wxALIGN_CENTER_VERTICAL | wxRIGHT, 14);

    controlSizer->Add(
        new wxStaticText(
            controls, wxID_ANY,
            wxT("Estados de busqueda compatibles:")),
        0, wxALIGN_CENTER_VERTICAL | wxRIGHT, 5);

    wxArrayString compatibleInitial;
    compatibleInitial.Add(wxT("Sin resultados compatibles"));

    m_compatibleState = new wxChoice(
        controls, ID_COMPATIBLE_STATE,
        wxDefaultPosition, wxSize(205, -1),
        compatibleInitial);

    m_compatibleState->SetSelection(0);
    m_compatibleState->Enable(false);
    m_compatibleState->SetToolTip(
        wxT("Selecciona un estado compatible para centrarlo en el lienzo"));

    controlSizer->Add(
        m_compatibleState, 0,
        wxALIGN_CENTER_VERTICAL);

    controls->SetSizer(controlSizer);
    root->Add(controls, 0, wxEXPAND | wxALL, 5);

    wxBoxSizer* main = new wxBoxSizer(wxHORIZONTAL);

    // ---------------------------------------------------------------------
    // Columna izquierda
    // ---------------------------------------------------------------------
    wxPanel* left = new wxPanel(body, wxID_ANY, wxDefaultPosition,
                                wxSize(285, -1), wxBORDER_SUNKEN);
    wxBoxSizer* leftSizer = new wxBoxSizer(wxVERTICAL);

    wxCollapsiblePane* examplesPane =
        new wxCollapsiblePane(left, ID_PANE_EXAMPLES,
                              wxT("Casos de ejemplo"),
                              wxDefaultPosition, wxDefaultSize,
                              wxCP_NO_TLW_RESIZE);

    wxWindow* examplesContent = examplesPane->GetPane();
    wxBoxSizer* examplesSizer = new wxBoxSizer(wxVERTICAL);

    // wxListCtrl permite diferenciar cada preset con fondo alternado y usar
    // filas más altas. Una imagen transparente de 1x26 fuerza una altura
    // cómoda sin introducir iconos decorativos.
    m_examples = new wxListCtrl(
        examplesContent, ID_EXAMPLE_LIST,
        wxDefaultPosition, wxSize(-1, 260),
        wxLC_REPORT | wxLC_NO_HEADER |
        wxLC_SINGLE_SEL | wxLC_HRULES |
        wxBORDER_SUNKEN);

    m_examples->InsertColumn(
        0, wxEmptyString,
        wxLIST_FORMAT_LEFT, 250);

    wxImageList* rowHeight =
        new wxImageList(1, 26, true, 1);

    wxBitmap spacer(1, 26);
    {
        wxMemoryDC spacerDC;
        spacerDC.SelectObject(spacer);
        spacerDC.SetBackground(*wxWHITE_BRUSH);
        spacerDC.Clear();
        spacerDC.SelectObject(wxNullBitmap);
    }

    rowHeight->Add(spacer);
    m_examples->AssignImageList(
        rowHeight, wxIMAGE_LIST_SMALL);

    examplesSizer->Add(
        m_examples, 1,
        wxEXPAND | wxALL, 4);
    examplesContent->SetSizer(examplesSizer);
    leftSizer->Add(examplesPane, 1, wxEXPAND | wxALL, 5);

    wxCollapsiblePane* casePane =
        new wxCollapsiblePane(left, ID_PANE_CASE,
                              wxT("Caso actual"),
                              wxDefaultPosition, wxDefaultSize,
                              wxCP_NO_TLW_RESIZE);

    wxWindow* caseContent = casePane->GetPane();
    wxBoxSizer* caseSizer = new wxBoxSizer(wxVERTICAL);

    m_caseTitle = new wxStaticText(
        caseContent, wxID_ANY, wxT("No hay un caso cargado"));

    m_caseSummary = new wxTextCtrl(
        caseContent, wxID_ANY, wxEmptyString,
        wxDefaultPosition, wxSize(-1, 125),
        wxTE_MULTILINE | wxTE_READONLY | wxTE_WORDWRAP);

    caseSizer->Add(m_caseTitle, 0, wxEXPAND | wxALL, 5);
    caseSizer->Add(m_caseSummary, 1,
                   wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 5);
    caseContent->SetSizer(caseSizer);

    leftSizer->Add(casePane, 0,
                   wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 5);

    left->SetSizer(leftSizer);

    // ---------------------------------------------------------------------
    // Lienzo central + navegación flotante
    // ---------------------------------------------------------------------
    m_workspace = new WorkspacePanel(body);
    m_canvas = m_workspace->GetCanvas();

    // ---------------------------------------------------------------------
    // Columna derecha desplazable
    // ---------------------------------------------------------------------
    m_rightScroller = new wxScrolledWindow(
        body, wxID_ANY,
        wxDefaultPosition, wxSize(335, -1),
        wxVSCROLL | wxBORDER_SUNKEN);

    // La scrollbar vertical de XP consume parte del ancho cliente. Reservamos
    // unos píxeles adicionales para que Resumen/Ruta/Combinaciones no parezcan
    // aplastados cuando aparece, sin robar demasiado espacio al canvas.
    m_rightScroller->SetMinSize(wxSize(335, -1));
    m_rightScroller->SetScrollRate(0, 18);

    // Contenedor físico del inspector. Todos los controles de la columna
    // derecha serán hijos de ESTE panel, no del wxScrolledWindow. De esta forma
    // la scrollbar exterior no puede dibujarse encima de ellos.
    wxPanel* rightContent =
        new wxPanel(m_rightScroller, wxID_ANY);

    wxBoxSizer* rightSizer =
        new wxBoxSizer(wxVERTICAL);

    wxCollapsiblePane* metricsPane =
        new wxCollapsiblePane(rightContent, ID_PANE_SUMMARY,
                              wxT("Resumen de ejecucion"),
                              wxDefaultPosition, wxDefaultSize,
                              wxCP_NO_TLW_RESIZE);

    wxWindow* metricsContent = metricsPane->GetPane();
    wxBoxSizer* metricsBox = new wxBoxSizer(wxVERTICAL);

    wxArrayString metricHeaders;
    metricHeaders.Add(wxT("Medida"));
    metricHeaders.Add(wxT("Valor"));

    wxArrayInt metricWidths;
    metricWidths.Add(130);
    metricWidths.Add(130);

    metricsBox->Add(
        CreateDarkTableHeader(metricsContent, metricHeaders, metricWidths),
        0, wxEXPAND | wxLEFT | wxRIGHT | wxTOP, 5);

    m_metrics = new wxListCtrl(
        metricsContent, wxID_ANY,
        wxDefaultPosition, wxSize(-1, 190),
        wxLC_REPORT | wxLC_NO_HEADER |
        wxLC_SINGLE_SEL | wxLC_HRULES | wxLC_VRULES |
        wxBORDER_SUNKEN);

    m_metrics->InsertColumn(0, wxEmptyString, wxLIST_FORMAT_LEFT, 130);
    m_metrics->InsertColumn(1, wxEmptyString, wxLIST_FORMAT_LEFT, 130);

    static const wxChar* metricLabels[] =
    {
        wxT("Tick logico"),
        wxT("Visitados"),
        wxT("Descartados"),
        wxT("Compatibles"),
        wxT("Pendientes"),
        wxT("Tasa observada"),
        wxT("Evitados"),
        wxT("Potenciales"),
        wxT("Espacio contabilizado"),
        wxT("Fuera de vista")
    };

    for (size_t i = 0;
         i < sizeof(metricLabels) / sizeof(metricLabels[0]); ++i)
    {
        long row = m_metrics->InsertItem((long)i, metricLabels[i]);
        m_metrics->SetItem(row, 1, wxT("0"));
        StyleTableRow(m_metrics, row);
    }

    metricsBox->Add(m_metrics, 0, wxEXPAND | wxLEFT | wxRIGHT, 5);

    // Porcentaje y barra viven en el mismo control para que XP no pueda
    // presentar dos frames visuales de instantes distintos.
    m_coverage = new CoverageGauge(metricsContent);

    metricsBox->Add(
        m_coverage, 0,
        wxEXPAND | wxLEFT | wxRIGHT | wxTOP | wxBOTTOM, 5);

    metricsContent->SetSizer(metricsBox);
    rightSizer->Add(metricsPane, 0, wxEXPAND | wxALL, 5);

    wxCollapsiblePane* routePane =
        new wxCollapsiblePane(rightContent, ID_PANE_ROUTE,
                              wxT("Ruta seleccionada"),
                              wxDefaultPosition, wxDefaultSize,
                              wxCP_NO_TLW_RESIZE);

    wxWindow* routeContent = routePane->GetPane();
    wxBoxSizer* routeSizer = new wxBoxSizer(wxVERTICAL);

    m_selectedRoute = new wxTextCtrl(
        routeContent, wxID_ANY,
        wxT("Haz clic en una tarjeta del lienzo para inspeccionarla."),
        wxDefaultPosition, wxSize(-1, 100),
        wxTE_MULTILINE | wxTE_READONLY | wxTE_WORDWRAP);

    routeSizer->Add(m_selectedRoute, 0, wxEXPAND | wxALL, 5);
    routeContent->SetSizer(routeSizer);

    rightSizer->Add(routePane, 0,
                    wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 5);

    wxCollapsiblePane* solutionsPane =
        new wxCollapsiblePane(rightContent, ID_PANE_SOLUTIONS,
                              wxT("Combinaciones compatibles"),
                              wxDefaultPosition, wxDefaultSize,
                              wxCP_NO_TLW_RESIZE);

    wxWindow* solutionsContent = solutionsPane->GetPane();
    wxBoxSizer* solutionsSizer = new wxBoxSizer(wxVERTICAL);

    m_solutions = new wxListBox(
        solutionsContent, wxID_ANY,
        wxDefaultPosition, wxSize(-1, 165),
        0, 0, wxLB_SINGLE | wxLB_HSCROLL);

    solutionsSizer->Add(m_solutions, 0, wxEXPAND | wxALL, 5);
    solutionsContent->SetSizer(solutionsSizer);

    rightSizer->Add(solutionsPane, 0,
                    wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 5);

    // El contenido vive en un panel hijo más estrecho. El gutter queda como
    // una franja vacía entre rightContent y la scrollbar de Windows XP.
    int scrollBarGutter =
        wxSystemSettings::GetMetric(wxSYS_VSCROLL_X);

    if (scrollBarGutter <= 0)
        scrollBarGutter = 17;

    scrollBarGutter += 8;

    rightContent->SetSizer(rightSizer);

    wxBoxSizer* rightOuterSizer =
        new wxBoxSizer(wxHORIZONTAL);

    rightOuterSizer->Add(
        rightContent, 1,
        wxEXPAND | wxLEFT | wxTOP | wxBOTTOM,
        4);

    rightOuterSizer->AddSpacer(
        scrollBarGutter);

    m_rightScroller->SetSizer(
        rightOuterSizer);

    m_rightScroller->FitInside();

    main->Add(left, 0, wxEXPAND);

    // El canvas conserva el rol expansivo, pero dejamos una separación visible
    // con las columnas laterales para que la carcasa no se perciba "pegada".
    main->Add(
        m_workspace, 1,
        wxEXPAND | wxLEFT | wxRIGHT, 4);

    // Pequeño margen exterior. Es especialmente útil cuando aparece la
    // scrollbar vertical del inspector derecho: el borde ya no queda contra el
    // extremo físico de la ventana.
    main->Add(
        m_rightScroller, 0,
        wxEXPAND | wxLEFT | wxRIGHT, 5);

    root->Add(main, 1, wxEXPAND);

    // ---------------------------------------------------------------------
    // Linea de tiempo colapsable
    // ---------------------------------------------------------------------
    wxCollapsiblePane* timelinePane =
        new wxCollapsiblePane(body, ID_PANE_TIMELINE,
                              wxT("Linea de tiempo de ejecucion"),
                              wxDefaultPosition, wxDefaultSize,
                              wxCP_NO_TLW_RESIZE);
    wxWindow* timelineContent = timelinePane->GetPane();
    wxBoxSizer* timelineSizer = new wxBoxSizer(wxVERTICAL);

    wxArrayString timelineHeaders;
    timelineHeaders.Add(wxT("Tick"));
    timelineHeaders.Add(wxT("Evento"));
    timelineHeaders.Add(wxT("Detalles"));

    wxArrayInt timelineWidths;
    timelineWidths.Add(90);
    timelineWidths.Add(115);
    timelineWidths.Add(-1);

    timelineSizer->Add(
        CreateDarkTableHeader(
            timelineContent, timelineHeaders, timelineWidths),
        0, wxEXPAND | wxLEFT | wxRIGHT | wxTOP, 4);

    m_timeline = new wxListCtrl(
        timelineContent, ID_TIMELINE,
        wxDefaultPosition, wxSize(-1, 145),
        wxLC_REPORT | wxLC_NO_HEADER |
        wxLC_SINGLE_SEL | wxLC_HRULES | wxLC_VRULES |
        wxBORDER_SUNKEN);

    m_timeline->InsertColumn(0, wxEmptyString, wxLIST_FORMAT_RIGHT, 90);
    m_timeline->InsertColumn(1, wxEmptyString, wxLIST_FORMAT_LEFT, 115);
    m_timeline->InsertColumn(2, wxEmptyString, wxLIST_FORMAT_LEFT, 760);

    timelineSizer->Add(
        m_timeline, 1,
        wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 4);
    timelineContent->SetSizer(timelineSizer);

    root->Add(timelinePane, 0, wxEXPAND | wxALL, 5);

    body->SetSizer(root);

    wxBoxSizer* frameSizer = new wxBoxSizer(wxVERTICAL);
    frameSizer->Add(body, 1, wxEXPAND);
    SetSizer(frameSizer);

    // Los presets son los mismos archivos .case del repositorio, pero en la
    // interfaz mostramos el nombre humano del caso, no el nombre del archivo.
    wxDir dir(ExamplesDir());
    if (dir.IsOpened())
    {
        wxArrayString files;
        wxString filename;
        bool more = dir.GetFirst(&filename, wxT("*.case"), wxDIR_FILES);

        while (more)
        {
            files.Add(filename);
            more = dir.GetNext(&filename);
        }

        files.Sort();

        for (size_t i = 0; i < files.GetCount(); ++i)
        {
            ProblemInstance example;
            std::string error;
            wxString fullPath =
                ExamplesDir() + wxFILE_SEP_PATH + files[i];

            wxString label = files[i];

            if (CaseFile::Load(
                    std::string(fullPath.mb_str()),
                    example, error))
            {
                label =
                    wxString(
                        example.name.c_str(),
                        wxConvLocal);
            }

            const long row =
                m_examples->InsertItem(
                    (long)i, label, 0);

            const wxColour white(255, 255, 255);
            const wxColour lightGray(244, 244, 244);

            m_examples->SetItemBackgroundColour(
                row,
                (row % 2 == 0)
                    ? white
                    : lightGray);

            m_exampleFiles.Add(files[i]);
        }

        if (m_examples->GetItemCount() > 0)
        {
            m_examples->SetColumnWidth(
                0, wxLIST_AUTOSIZE);

            if (m_examples->GetColumnWidth(0) < 250)
                m_examples->SetColumnWidth(0, 250);
        }
    }

    Layout();
}

void MainFrame::BuildStatus()
{
    CreateStatusBar(4);
    int widths[4] = {-1, 180, 180, 170};
    SetStatusWidths(4, widths);

    SetStatusText(wxT("Preparado"), 0);
    SetStatusText(wxT("Tick logico: 0"), 1);
    SetStatusText(wxT("Compatibles: 0"), 2);
    SetStatusText(wxT("Actualizacion: 50 ms"), 3);
}

void MainFrame::LoadStartupExample()
{
    wxString path = ExamplesDir() + wxFILE_SEP_PATH
                    + wxT("01-small-exact.case");

    if (wxFileExists(path))
        LoadCase(path);
}

bool MainFrame::LoadCase(const wxString& path)
{
    ProblemInstance problem;
    std::string error;

    if (!CaseFile::Load(std::string(path.mb_str()), problem, error))
    {
        wxMessageBox(wxString(error.c_str(), wxConvLocal),
                     wxT("No se pudo abrir el caso"),
                     wxOK | wxICON_ERROR, this);
        return false;
    }

    ApplyProblem(problem);
    return true;
}

void MainFrame::ApplyProblem(const ProblemInstance& problem)
{
    m_problem = problem;
    m_caseLoaded = true;

    m_runtime.LoadProblem(m_problem);

    int strategy = m_strategy->GetSelection();
    if (strategy == 0) m_runtime.SetStrategy(STRATEGY_EXHAUSTIVE);
    else m_runtime.SetStrategy(STRATEGY_PRUNING);

    int mode = m_mode->GetSelection();
    if (mode == 0) m_runtime.SetMode(MODE_STUDY);
    else if (mode == 2) m_runtime.SetMode(MODE_MAXIMUM);
    else m_runtime.SetMode(MODE_BALANCED);

    m_caseTitle->SetLabel(wxString(m_problem.name.c_str(), wxConvLocal));

    wxString summary;
    summary << wxT("Monto a conciliar: ") << Money(m_problem.targetCents)
            << wxT("\nMovimientos candidatos: ")
            << (unsigned long)m_problem.valuesCents.size();

    if (!m_problem.description.empty())
    {
        summary << wxT("\n\n")
                << wxString(m_problem.description.c_str(), wxConvLocal);
    }

    m_caseSummary->SetValue(summary);
    m_caseSummary->SetInsertionPoint(0);

    m_lastEventCount = 0;
    m_lastEventTick = 0;
    m_lastSolutionCount = 0;
    m_lastHiddenSolutions = 0;
    ResetRateSample();

    m_timeline->DeleteAllItems();
    m_solutions->Clear();

    m_compatibleNodeIds.clear();
    m_compatibleState->Clear();
    m_compatibleState->Append(
        wxT("Sin resultados compatibles"));
    m_compatibleState->SetSelection(0);
    m_compatibleState->Enable(false);

    m_lastPresentedState = EXECUTION_READY;

    SetStatusText(wxT("Caso cargado"), 0);
    RefreshFromRuntime();
    UpdateCommandState();
}

void MainFrame::ResetRateSample()
{
    m_rateWatch.Start(0);
    m_rateLastTick = 0;
    m_rateLastMs = 0;
    m_observedStatesPerSecond = 0.0;
}

void MainFrame::RefreshMetrics(const ExecutionSnapshot& snapshot)
{
    long nowMs = m_rateWatch.Time();

    if (snapshot.logicalTick < m_rateLastTick)
    {
        ResetRateSample();
        nowMs = 0;
    }

    long elapsedMs = nowMs - m_rateLastMs;

    if (elapsedMs >= 250)
    {
        unsigned long tickDelta =
            snapshot.logicalTick - m_rateLastTick;

        m_observedStatesPerSecond =
            elapsedMs > 0
                ? ((double)tickDelta * 1000.0) / (double)elapsedMs
                : 0.0;

        m_rateLastTick =
            snapshot.logicalTick;

        m_rateLastMs =
            nowMs;
    }

    // ---------------------------------------------------------------------
    // Espacio contabilizado
    //
    // Esta magnitud es matemática: qué fracción del árbol potencial ya fue
    // visitada o evitada por una regla segura. NO significa necesariamente
    // "qué tan cerca estaba de encontrar la primera coincidencia".
    // ---------------------------------------------------------------------
    double spaceRatio = 0.0;

    if (snapshot.potentialStates > 0)
    {
        unsigned long accounted =
            snapshot.visited + snapshot.avoidedStates;

        if (accounted > snapshot.potentialStates)
            accounted = snapshot.potentialStates;

        spaceRatio =
            (double)accounted
            / (double)snapshot.potentialStates;
    }

    // Tabla fija: actualizamos celdas sin reconstruir el control.
    wxString values[10];

    values[0] = ULong(snapshot.logicalTick);
    values[1] = ULong(snapshot.visited);
    values[2] = ULong(snapshot.pruned);
    values[3] = ULong(snapshot.matches);
    values[4] = wxString::Format(
        wxT("%lu"), snapshot.pendingTasks);
    values[5] = wxString::Format(
        wxT("%.0f estados/s"), m_observedStatesPerSecond);
    values[6] = ULong(snapshot.avoidedStates);
    values[7] = ULong(snapshot.potentialStates);
    values[8] = wxString::Format(
        wxT("%.1f%%"), spaceRatio * 100.0);
    values[9] = ULong(snapshot.unretainedTraceNodes);

    m_metrics->Freeze();

    for (long row = 0; row < 10; ++row)
    {
        if (m_metrics->GetItemText(row, 1) != values[row])
            m_metrics->SetItem(row, 1, values[row]);
    }

    m_metrics->Thaw();

    // ---------------------------------------------------------------------
    // Progreso de ejecución
    //
    // Mientras el caso corre, el espacio contabilizado sirve como indicador
    // conservador del avance visible. Cuando el objetivo TERMINA, el progreso
    // de la ejecución es 100% aunque una búsqueda "primera coincidencia" haya
    // necesitado inspeccionar sólo 8.7% del árbol potencial.
    // ---------------------------------------------------------------------
    double executionRatio =
        snapshot.completed
            ? 1.0
            : spaceRatio;

    int progressValue =
        (int)(executionRatio * 1000.0 + 0.5);

    if (progressValue < 0) progressValue = 0;
    if (progressValue > 1000) progressValue = 1000;

    m_coverage->SetTargetValue(
        progressValue);
}

void MainFrame::RefreshTimeline(const ExecutionSnapshot& snapshot)
{
    unsigned long lastTick = 0;
    if (!snapshot.events.empty())
        lastTick = snapshot.events.back().tick;

    if (snapshot.events.size() == m_lastEventCount
        && lastTick == m_lastEventTick)
    {
        return;
    }

    // La traza es corta y acotada. Se reconstruye como un solo lote para
    // evitar que el control nativo muestre el estado intermedio vacio.
    m_timeline->Freeze();
    m_timeline->DeleteAllItems();

    for (size_t i = 0; i < snapshot.events.size(); ++i)
    {
        const RunEvent& event = snapshot.events[i];

        long row = m_timeline->InsertItem(
            (long)i, ULong(event.tick));

        m_timeline->SetItem(row, 1, EventLabel(event.type));
        m_timeline->SetItem(
            row, 2, wxString(event.message.c_str(), wxConvLocal));
        StyleTableRow(m_timeline, row);
    }

    if (!snapshot.events.empty())
        m_timeline->EnsureVisible((long)snapshot.events.size() - 1);

    m_timeline->Thaw();

    m_lastEventCount = snapshot.events.size();
    m_lastEventTick = lastTick;
}

void MainFrame::RefreshSolutions(const ExecutionSnapshot& snapshot)
{
    if (snapshot.solutions.size() == m_lastSolutionCount
        && snapshot.hiddenSolutions == m_lastHiddenSolutions)
    {
        return;
    }

    m_solutions->Clear();

    for (size_t i = 0; i < snapshot.solutions.size(); ++i)
        m_solutions->Append(DescribeSolution(snapshot.solutions[i], i));

    if (snapshot.hiddenSolutions > 0)
    {
        wxString more;
        more << wxT("... ") << ULong(snapshot.hiddenSolutions)
             << wxT(" combinaciones compatibles adicionales no mostradas");
        m_solutions->Append(more);
    }

    m_lastSolutionCount = snapshot.solutions.size();
    m_lastHiddenSolutions = snapshot.hiddenSolutions;
}

void MainFrame::RefreshCompatibleStates(
    const ExecutionSnapshot& snapshot)
{
    std::vector<unsigned long> ids;

    for (size_t i = 0; i < snapshot.nodes.size(); ++i)
    {
        if (snapshot.nodes[i].status == NODE_MATCH)
            ids.push_back(snapshot.nodes[i].id);
    }

    // Si nada cambió, conservamos selección y evitamos reconstruir el control.
    if (ids == m_compatibleNodeIds)
        return;

    unsigned long selectedId = 0;

    const int oldSelection =
        m_compatibleState->GetSelection();

    if (oldSelection != wxNOT_FOUND
        && oldSelection >= 0
        && (size_t)oldSelection < m_compatibleNodeIds.size())
    {
        selectedId =
            m_compatibleNodeIds[(size_t)oldSelection];
    }

    m_compatibleNodeIds = ids;
    m_compatibleState->Clear();

    if (m_compatibleNodeIds.empty())
    {
        if (snapshot.matches > 0)
        {
            // Hay una coincidencia global, pero su nodo no está dentro del
            // límite de traza detallada; por eso no podemos centrarla.
            m_compatibleState->Append(
                wxT("Compatibles fuera de la vista detallada"));
        }
        else
        {
            m_compatibleState->Append(
                wxT("Sin resultados compatibles"));
        }

        m_compatibleState->SetSelection(0);
        m_compatibleState->Enable(false);
        return;
    }

    int selectionToRestore = wxNOT_FOUND;

    for (size_t i = 0; i < m_compatibleNodeIds.size(); ++i)
    {
        const unsigned long id =
            m_compatibleNodeIds[i];

        const ExecutionNode* node = 0;

        for (size_t j = 0; j < snapshot.nodes.size(); ++j)
        {
            if (snapshot.nodes[j].id == id)
            {
                node = &snapshot.nodes[j];
                break;
            }
        }

        wxString label;

        if (node)
        {
            label = wxString::Format(
                wxT("Estado #%lu - %s"),
                id,
                Money(node->sumCents).c_str());
        }
        else
        {
            label = wxString::Format(
                wxT("Estado #%lu"), id);
        }

        m_compatibleState->Append(label);

        if (id == selectedId)
            selectionToRestore = (int)i;
    }

    m_compatibleState->Enable(true);

    if (selectionToRestore != wxNOT_FOUND)
        m_compatibleState->SetSelection(selectionToRestore);
    else
        m_compatibleState->SetSelection(0);
}

void MainFrame::RefreshSelectedRoute()
{
    ExecutionNode node;
    if (!m_canvas->GetSelectedNode(node))
    {
        wxString empty =
            wxT("Haz clic en una tarjeta del lienzo para inspeccionarla.");

        if (m_selectedRoute->GetValue() != empty)
            m_selectedRoute->SetValue(empty);

        m_selectedRoute->SetInsertionPoint(0);
        return;
    }

    wxString status = wxT("Procesado");
    if (node.status == NODE_PRUNED) status = wxT("Descartado");
    else if (node.status == NODE_MATCH) status = wxT("Compatible");

    wxString value;
    value << wxT("Posicion del movimiento: ")
          << node.index << wxT(" / ")
          << (unsigned long)m_problem.valuesCents.size()
          << wxT("\nMonto acumulado: ")
          << Money(node.sumCents)
          << wxT("\nResultado: ")
          << status;

    if (node.depth > 0 && node.index > 0
        && (size_t)(node.index - 1) < m_problem.valuesCents.size())
    {
        value << wxT("\nUltima decision: ")
              << (node.tookValue ? wxT("incluido ") : wxT("omitido "))
              << Money(m_problem.valuesCents[node.index - 1]);
    }

    if (m_selectedRoute->GetValue() != value)
        m_selectedRoute->SetValue(value);

    m_selectedRoute->SetInsertionPoint(0);
}

void MainFrame::RefreshFromRuntime()
{
    if (!m_caseLoaded)
        return;

    ExecutionSnapshot snapshot = m_runtime.GetSnapshot();
    ExecutionState state = m_runtime.GetState();

    m_canvas->SetSnapshot(snapshot);
    RefreshMetrics(snapshot);
    RefreshTimeline(snapshot);
    RefreshSolutions(snapshot);
    RefreshCompatibleStates(snapshot);
    RefreshSelectedRoute();

    wxString stateText = wxT("Preparado");
    if (state == EXECUTION_RUNNING) stateText = wxT("Ejecutando");
    else if (state == EXECUTION_PAUSED) stateText = wxT("Pausado");
    else if (state == EXECUTION_COMPLETED) stateText = wxT("Completado");

    SetStatusText(stateText, 0);
    SetStatusText(wxT("Tick logico: ") + ULong(snapshot.logicalTick), 1);
    SetStatusText(wxT("Compatibles: ") + ULong(snapshot.matches), 2);

    m_lastPresentedState = state;
    UpdateCommandState();
}

void MainFrame::OnNew(wxCommandEvent&)
{
    NewCaseDialog dialog(this);

    if (dialog.ShowModal() == wxID_OK)
        ApplyProblem(dialog.GetProblem());
}

void MainFrame::OnRun(wxCommandEvent&)
{
    if (!m_caseLoaded)
        return;

    ExecutionState state = m_runtime.GetState();

    if (state == EXECUTION_COMPLETED)
    {
        SetStatusText(
            wxT("La conciliacion termino. Pulsa Reiniciar para ejecutarla de nuevo."),
            0);
        UpdateCommandState();
        return;
    }

    if (state == EXECUTION_PAUSED)
    {
        // Una pausa larga no debe reducir artificialmente la tasa observada.
        ExecutionSnapshot before = m_runtime.GetSnapshot();
        m_rateWatch.Start(0);
        m_rateLastTick = before.logicalTick;
        m_rateLastMs = 0;
        m_observedStatesPerSecond = 0.0;
    }

    if (!m_runtime.Start())
    {
        wxMessageBox(wxT("No se pudo iniciar o reanudar la conciliacion."),
                     wxT("Error de ejecucion"),
                     wxOK | wxICON_ERROR, this);
        return;
    }

    UpdateCommandState();
}

void MainFrame::OnPause(wxCommandEvent&)
{
    if (!m_caseLoaded)
        return;

    m_runtime.Pause();
    RefreshFromRuntime();
}

void MainFrame::OnReset(wxCommandEvent&)
{
    if (!m_caseLoaded)
        return;

    m_runtime.Reset();

    m_lastEventCount = 0;
    m_lastEventTick = 0;
    m_lastSolutionCount = 0;
    m_lastHiddenSolutions = 0;
    ResetRateSample();

    RefreshFromRuntime();
}

void MainFrame::OnStep(wxCommandEvent&)
{
    if (!m_caseLoaded)
        return;

    if (m_runtime.GetState() == EXECUTION_RUNNING)
        m_runtime.Pause();

    if (!m_runtime.StepOnce())
    {
        UpdateCommandState();
        return;
    }

    RefreshFromRuntime();
}

void MainFrame::OnOpen(wxCommandEvent&)
{
    wxFileDialog dialog(
        this, wxT("Abrir caso de conciliacion"),
        AppRoot(), wxEmptyString,
        wxT("Casos ReconcileLab (*.case)|*.case|Todos los archivos (*.*)|*.*"),
        wxFD_OPEN | wxFD_FILE_MUST_EXIST);

    if (dialog.ShowModal() == wxID_OK)
        LoadCase(dialog.GetPath());
}

void MainFrame::OnSaveAs(wxCommandEvent&)
{
    if (!m_caseLoaded)
        return;

    wxFileDialog dialog(
        this, wxT("Guardar caso de conciliacion"),
        AppRoot(), wxT("reconciliation.case"),
        wxT("Casos ReconcileLab (*.case)|*.case"),
        wxFD_SAVE | wxFD_OVERWRITE_PROMPT);

    if (dialog.ShowModal() != wxID_OK)
        return;

    std::string error;
    if (!CaseFile::Save(std::string(dialog.GetPath().mb_str()),
                        m_problem, error))
    {
        wxMessageBox(wxString(error.c_str(), wxConvLocal),
                     wxT("No se pudo guardar el caso"),
                     wxOK | wxICON_ERROR, this);
        return;
    }

    SetStatusText(wxT("Caso guardado"), 0);
}

void MainFrame::OnExportImage(wxCommandEvent&)
{
    if (!m_caseLoaded)
    {
        wxMessageBox(
            wxT("Carga un caso antes de exportar una imagen."),
            wxT("Exportar imagen"),
            wxOK | wxICON_INFORMATION,
            this);
        return;
    }

    wxFileDialog dialog(
        this,
        wxT("Exportar imagen de estudio"),
        wxEmptyString,
        wxT("reconcilelab-estudio.png"),
        wxT("Imagen PNG (*.png)|*.png"),
        wxFD_SAVE | wxFD_OVERWRITE_PROMPT);

    if (dialog.ShowModal() != wxID_OK)
        return;

    wxString path = dialog.GetPath();

    wxFileName fileName(path);
    if (fileName.GetExt().IsEmpty())
    {
        fileName.SetExt(wxT("png"));
        path = fileName.GetFullPath();
    }

    wxString strategyLabel =
        m_strategy->GetStringSelection();

    wxString error;

    if (!m_canvas->ExportStudyImage(
            path,
            m_problem,
            strategyLabel,
            error))
    {
        wxMessageBox(
            error,
            wxT("No se pudo exportar la imagen"),
            wxOK | wxICON_ERROR,
            this);
        return;
    }

    wxMessageBox(
        wxT("La imagen de estudio se guardo correctamente."),
        wxT("Exportacion completada"),
        wxOK | wxICON_INFORMATION,
        this);
}

void MainFrame::OnExit(wxCommandEvent&)
{
    Close(true);
}

void MainFrame::OnOpenExample(wxCommandEvent&)
{
    wxFileDialog dialog(
        this, wxT("Abrir caso de ejemplo"),
        ExamplesDir(), wxEmptyString,
        wxT("Casos ReconcileLab (*.case)|*.case"),
        wxFD_OPEN | wxFD_FILE_MUST_EXIST);

    if (dialog.ShowModal() == wxID_OK)
        LoadCase(dialog.GetPath());
}

void MainFrame::OnExampleActivated(wxListEvent& event)
{
    const long selection = event.GetIndex();

    if (selection < 0
        || (size_t)selection >= m_exampleFiles.GetCount())
    {
        return;
    }

    LoadCase(
        ExamplesDir()
        + wxFILE_SEP_PATH
        + m_exampleFiles[(size_t)selection]);
}

void MainFrame::OnStrategy(wxCommandEvent&)
{
    if (!m_caseLoaded)
        return;

    // Cambiar la estrategia define una ejecución nueva. Reiniciar impide
    // mezclar métricas y estados producidos bajo reglas distintas.
    m_runtime.Pause();

    int selection = m_strategy->GetSelection();
    if (selection == 0) m_runtime.SetStrategy(STRATEGY_EXHAUSTIVE);
    else m_runtime.SetStrategy(STRATEGY_PRUNING);

    m_runtime.Reset();

    // Reset() conserva la estrategia que acabamos de seleccionar.
    m_lastEventCount = 0;
    m_lastEventTick = 0;
    m_lastSolutionCount = 0;
    m_lastHiddenSolutions = 0;
    ResetRateSample();

    RefreshFromRuntime();
}

void MainFrame::OnMode(wxCommandEvent&)
{
    if (!m_caseLoaded)
        return;

    int selection = m_mode->GetSelection();
    if (selection == 0) m_runtime.SetMode(MODE_STUDY);
    else if (selection == 2) m_runtime.SetMode(MODE_MAXIMUM);
    else m_runtime.SetMode(MODE_BALANCED);
}

void MainFrame::OnCompatibleState(wxCommandEvent&)
{
    if (!m_compatibleState
        || !m_compatibleState->IsEnabled())
    {
        return;
    }

    const int selection =
        m_compatibleState->GetSelection();

    if (selection == wxNOT_FOUND
        || selection < 0
        || (size_t)selection >= m_compatibleNodeIds.size())
    {
        return;
    }

    const unsigned long nodeId =
        m_compatibleNodeIds[(size_t)selection];

    if (m_canvas->FocusNode(nodeId))
    {
        // FocusNode selecciona la tarjeta; proyectamos inmediatamente el
        // detalle para que combo, canvas e inspector respondan como una unidad.
        RefreshSelectedRoute();
    }
}


void MainFrame::OnRefreshChanged(wxSpinEvent&)
{
    int milliseconds = m_refreshMs->GetValue();
    m_timer.Start(milliseconds);
    SetStatusText(
        wxString::Format(wxT("Actualizacion: %d ms"), milliseconds), 3);
}

void MainFrame::OnPaneChanged(wxCollapsiblePaneEvent& event)
{
    // Las secciones de la derecha comparten un único wxScrolledWindow. Al
    // expandir/contraer una sección recalculamos su tamaño virtual para que una
    // sola scrollbar vertical permita recorrer todo el grupo.
    if (m_rightScroller)
    {
        m_rightScroller->Layout();
        m_rightScroller->FitInside();
    }

    Layout();
    SendSizeEvent();

    // En wxWidgets 3.0.5 conviene pedir el size event del workspace DESPUÉS de
    // que el frame y su body terminaron de repartir el nuevo espacio. Así el
    // ZoomRail recibe la altura final, no la anterior.
    if (m_workspace)
        m_workspace->SendSizeEvent();

    event.Skip();
}

void MainFrame::StyleTableRow(wxListCtrl* table, long row) const
{
    if (!table || row < 0)
        return;

    // Alternancia discreta, típica de tablas de escritorio: blanco y un
    // celeste muy suave. La selección activa sigue usando el color nativo XP.
    const wxColour white(255, 255, 255);
    const wxColour paleBlue(235, 244, 252);

    table->SetItemBackgroundColour(
        row, (row % 2 == 0) ? white : paleBlue);
}

void MainFrame::UpdateCommandState()
{
    const ExecutionState state = m_runtime.GetState();

    const bool loaded = m_caseLoaded;
    const bool running = state == EXECUTION_RUNNING;
    const bool completed = state == EXECUTION_COMPLETED;

    const bool canRun =
        loaded && !running && !completed;

    const bool canPause =
        loaded && running;

    const bool canStep =
        loaded && !running && !completed;

    const bool canReset =
        loaded;

    if (GetMenuBar())
    {
        GetMenuBar()->Enable(ID_RUN, canRun);
        GetMenuBar()->Enable(ID_PAUSE, canPause);
        GetMenuBar()->Enable(ID_STEP, canStep);
        GetMenuBar()->Enable(ID_RESET, canReset);
    }

    if (GetToolBar())
    {
        GetToolBar()->EnableTool(ID_RUN, canRun);
        GetToolBar()->EnableTool(ID_PAUSE, canPause);
        GetToolBar()->EnableTool(ID_STEP, canStep);
        GetToolBar()->EnableTool(ID_RESET, canReset);
        GetToolBar()->EnableTool(ID_EXPORT_IMAGE, loaded);
    }

    // Cambiar la estrategia mientras el motor recorre el frontier haría que
    // una sola ejecución mezclara dos reglas. El ritmo sí puede cambiarse.
    if (m_strategy)
        m_strategy->Enable(loaded && !running);
}

void MainFrame::OnTimer(wxTimerEvent&)
{
    if (!m_caseLoaded)
        return;

    ExecutionState state = m_runtime.GetState();

    if (state == EXECUTION_RUNNING
        || state != m_lastPresentedState)
    {
        RefreshFromRuntime();
        return;
    }

    // Mientras el modelo está estable solo puede cambiar la selección por un
    // clic del usuario; no reconstruimos el resto de la interfaz.
    RefreshSelectedRoute();
}

void MainFrame::OnAbout(wxCommandEvent&)
{
    wxDialog dialog(
        this, wxID_ANY,
        wxT("Acerca de ReconcileLab 2006"),
        wxDefaultPosition, wxSize(560, 430),
        wxDEFAULT_DIALOG_STYLE | wxRESIZE_BORDER);

    wxBoxSizer* root = new wxBoxSizer(wxVERTICAL);
    wxBoxSizer* heading = new wxBoxSizer(wxHORIZONTAL);

    wxString iconPath =
        BrandingDir()
        + wxFILE_SEP_PATH
        + wxT("reconcilelab-icon-64.png");

    if (wxFileExists(iconPath))
    {
        wxBitmap bitmap(iconPath, wxBITMAP_TYPE_PNG);

        if (bitmap.IsOk())
        {
            heading->Add(
                new wxStaticBitmap(&dialog, wxID_ANY, bitmap),
                0, wxALL | wxALIGN_TOP, 10);
        }
    }

    wxBoxSizer* titleBox = new wxBoxSizer(wxVERTICAL);

    wxStaticText* title =
        new wxStaticText(&dialog, wxID_ANY, wxT("ReconcileLab 2006"));

    wxFont titleFont = title->GetFont();
    titleFont.SetPointSize(titleFont.GetPointSize() + 4);
    titleFont.SetWeight(wxFONTWEIGHT_BOLD);
    title->SetFont(titleFont);

    titleBox->Add(title, 0, wxTOP | wxRIGHT, 12);
    titleBox->Add(
        new wxStaticText(
            &dialog, wxID_ANY,
            wxT("Conciliacion y trazabilidad")),
        0, wxTOP | wxRIGHT, 3);

    wxString version =
        wxT("Version ")
        + wxString(RECONCILELAB_VERSION_TEXT, wxConvLocal)
        + wxT(" - Edicion de laboratorio para Windows XP");

    titleBox->Add(
        new wxStaticText(&dialog, wxID_ANY, version),
        0, wxTOP | wxRIGHT, 6);

    heading->Add(titleBox, 1, wxEXPAND);
    root->Add(heading, 0, wxEXPAND);

    wxString body =
        wxT("ReconcileLab ayuda a examinar un monto total recibido y un ")
        wxT("conjunto de movimientos candidatos cuando la relacion entre el ")
        wxT("total y sus componentes falta, esta incompleta o no es evidente.\n\n")
        wxT("Un caso tipico es un deposito o abono bancario que puede ")
        wxT("corresponder a varios pagos, facturas u otros movimientos. El ")
        wxT("programa identifica combinaciones cuyos montos son compatibles ")
        wxT("con el total observado y facilita revisar la conciliacion.\n\n")
        wxT("Una combinacion compatible no demuestra por si sola lo que ")
        wxT("ocurrio historicamente. Si varias combinaciones explican el mismo ")
        wxT("monto, pueden hacer falta referencias u otros documentos.");

    wxTextCtrl* text = new wxTextCtrl(
        &dialog, wxID_ANY, body,
        wxDefaultPosition, wxDefaultSize,
        wxTE_MULTILINE | wxTE_READONLY | wxTE_WORDWRAP);

    root->Add(text, 1, wxEXPAND | wxLEFT | wxRIGHT, 10);

    wxSizer* buttons = dialog.CreateStdDialogButtonSizer(wxOK);
    root->Add(buttons, 0, wxEXPAND | wxALL, 10);

    dialog.SetSizer(root);
    dialog.CentreOnParent();
    dialog.ShowModal();
}

void MainFrame::OnHelpContents(wxCommandEvent&)
{
    HelpFrame* frame = new HelpFrame(this, HelpIndex());
    frame->Show(true);
}

void MainFrame::OnClose(wxCloseEvent& event)
{
    m_timer.Stop();
    m_runtime.Stop();
    event.Skip();
}
