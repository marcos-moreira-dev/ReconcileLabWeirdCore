/**
 * Implementación del lienzo lógico.
 *
 * El canvas sólo pinta el mundo que puede desplazarse. El zoom visual se
 * mantiene fuera de esta superficie para no mezclarse con el blitting que
 * wxScrolledWindow utiliza al hacer scroll en Windows XP.
 */
#include "GraphCanvas.h"

#include <wx/dcbuffer.h>
#include <wx/dcmemory.h>
#include <wx/image.h>
#include <wx/settings.h>

#include <sstream>
#include <algorithm>
#include <cstdlib>
#include <cmath>

BEGIN_EVENT_TABLE(GraphCanvas, wxScrolledWindow)
    EVT_PAINT(GraphCanvas::OnPaint)
    EVT_ERASE_BACKGROUND(GraphCanvas::OnEraseBackground)
    EVT_LEFT_DOWN(GraphCanvas::OnLeftDown)
    EVT_MIDDLE_DOWN(GraphCanvas::OnMiddleDown)
    EVT_MIDDLE_UP(GraphCanvas::OnMiddleUp)
    EVT_RIGHT_DOWN(GraphCanvas::OnRightDown)
    EVT_RIGHT_UP(GraphCanvas::OnRightUp)
    EVT_MOTION(GraphCanvas::OnMotion)
END_EVENT_TABLE()

GraphCanvas::GraphCanvas(wxWindow* parent)
    : wxScrolledWindow(parent, wxID_ANY, wxDefaultPosition, wxDefaultSize,
                       wxHSCROLL | wxVSCROLL | wxBORDER_SUNKEN),
      m_selectedNodeId(0),
      m_panning(false),
      m_zoomPercent(100),
      m_logicalVirtualWidth(1200),
      m_logicalVirtualHeight(800)
{
    SetBackgroundStyle(wxBG_STYLE_PAINT);
    SetBackgroundColour(wxColour(70, 74, 82));

    // Unidades de un píxel: el arrastre del mouse no pierde movimiento por
    // divisiones enteras y se siente natural incluso en la VM.
    SetScrollRate(1, 1);
    UpdateVirtualSize();
}

void GraphCanvas::SetSnapshot(const ExecutionSnapshot& snapshot)
{
    const bool reset =
        snapshot.logicalTick < m_snapshot.logicalTick
        || snapshot.nodes.size() < m_snapshot.nodes.size();

    const bool structureChanged =
        reset || snapshot.nodes.size() != m_snapshot.nodes.size();

    const bool summaryChanged =
        snapshot.unretainedTraceNodes != m_snapshot.unretainedTraceNodes;

    m_snapshot = snapshot;

    if (reset)
        ResetLayout();

    if (structureChanged)
        ExtendLayout();

    if (m_selectedNodeId != 0)
    {
        bool stillPresent = false;

        for (std::vector<ExecutionNode>::size_type i = 0;
             i < m_snapshot.nodes.size(); ++i)
        {
            if (m_snapshot.nodes[i].id == m_selectedNodeId)
            {
                stillPresent = true;
                break;
            }
        }

        if (!stillPresent)
            m_selectedNodeId = 0;
    }

    if (structureChanged || summaryChanged)
        Refresh(false);
}

bool GraphCanvas::GetSelectedNode(ExecutionNode& out) const
{
    if (m_selectedNodeId == 0)
        return false;

    for (std::vector<ExecutionNode>::size_type i = 0;
         i < m_snapshot.nodes.size(); ++i)
    {
        if (m_snapshot.nodes[i].id == m_selectedNodeId)
        {
            out = m_snapshot.nodes[i];
            return true;
        }
    }

    return false;
}

void GraphCanvas::SetZoomPercent(int percent)
{
    if (percent < 25) percent = 25;
    if (percent > 200) percent = 200;

    if (m_zoomPercent == percent)
        return;

    int viewX = 0;
    int viewY = 0;
    int pixelsX = 1;
    int pixelsY = 1;

    GetViewStart(&viewX, &viewY);
    GetScrollPixelsPerUnit(&pixelsX, &pixelsY);

    if (pixelsX <= 0) pixelsX = 1;
    if (pixelsY <= 0) pixelsY = 1;

    const wxSize client = GetClientSize();
    const double oldScale = (double)m_zoomPercent / 100.0;

    const double logicalCenterX =
        ((double)(viewX * pixelsX) + client.x / 2.0) / oldScale;

    const double logicalCenterY =
        ((double)(viewY * pixelsY) + client.y / 2.0) / oldScale;

    m_zoomPercent = percent;
    UpdateVirtualSize();

    const double newScale = (double)m_zoomPercent / 100.0;

    int desiredX =
        (int)(logicalCenterX * newScale - client.x / 2.0);

    int desiredY =
        (int)(logicalCenterY * newScale - client.y / 2.0);

    if (desiredX < 0) desiredX = 0;
    if (desiredY < 0) desiredY = 0;

    Scroll(desiredX / pixelsX, desiredY / pixelsY);
    Refresh(false);
}

int GraphCanvas::GetZoomPercent() const
{
    return m_zoomPercent;
}

bool GraphCanvas::FocusNode(unsigned long nodeId)
{
    std::map<unsigned long, NodePosition>::const_iterator pos =
        m_positions.find(nodeId);

    if (pos == m_positions.end())
        return false;

    const ExecutionNode* node = 0;

    for (std::vector<ExecutionNode>::size_type i = 0;
         i < m_snapshot.nodes.size(); ++i)
    {
        if (m_snapshot.nodes[i].id == nodeId)
        {
            node = &m_snapshot.nodes[i];
            break;
        }
    }

    if (!node)
        return false;

    m_selectedNodeId = nodeId;

    // El layout está expresado en coordenadas lógicas. Scroll(), en cambio,
    // trabaja con coordenadas del mundo ya escaladas. Centramos la tarjeta
    // seleccionada para que el usuario no tenga que buscarla manualmente.
    const double scale =
        (double)m_zoomPercent / 100.0;

    const wxSize client =
        GetClientSize();

    const int nodeCenterX =
        (int)((pos->second.x + NodeWidth(*node) / 2) * scale);

    const int nodeCenterY =
        (int)((pos->second.y + NODE_H / 2) * scale);

    int targetX =
        nodeCenterX - client.x / 2;

    int targetY =
        nodeCenterY - client.y / 2;

    if (targetX < 0) targetX = 0;
    if (targetY < 0) targetY = 0;

    int pixelsX = 1;
    int pixelsY = 1;

    GetScrollPixelsPerUnit(
        &pixelsX, &pixelsY);

    if (pixelsX <= 0) pixelsX = 1;
    if (pixelsY <= 0) pixelsY = 1;

    Scroll(
        targetX / pixelsX,
        targetY / pixelsY);

    Refresh(false);
    Update();

    return true;
}

void GraphCanvas::ResetLayout()
{
    m_positions.clear();
    m_nextXByDepth.clear();

    m_logicalVirtualWidth = 1200;
    m_logicalVirtualHeight = 800;

    UpdateVirtualSize();
}

int GraphCanvas::NodeWidth(const ExecutionNode& node) const
{
    // El título es el texto que más crece. Aproximamos 7 px por carácter más
    // márgenes internos. La cota máxima evita tarjetas absurdamente anchas si
    // el formato cambia en el futuro.
    const wxString title =
        wxString::Format(wxT("ESTADO DE BUSQUEDA #%lu"), node.id);

    int width = 24 + (int)title.Length() * 8;

    // Movimiento y acumulado pueden superar al título en casos extremos.
    wxString movement =
        wxString::Format(
            wxT("Movimiento: %d / %d"),
            node.index, m_snapshot.itemCount);

    width = std::max(width, 24 + (int)movement.Length() * 8);

    wxString amount =
        wxT("Acumulado: ") + Money(node.sumCents);

    width = std::max(width, 24 + (int)amount.Length() * 8);

    if (width < NODE_MIN_W) width = NODE_MIN_W;
    if (width > NODE_MAX_W) width = NODE_MAX_W;

    return width;
}

void GraphCanvas::ExtendLayout()
{
    int maxRight = m_logicalVirtualWidth;
    int maxBottom = m_logicalVirtualHeight;

    for (std::vector<ExecutionNode>::size_type i = 0;
         i < m_snapshot.nodes.size(); ++i)
    {
        const ExecutionNode& node = m_snapshot.nodes[i];

        if (m_positions.find(node.id) != m_positions.end())
            continue;

        int x = 30;
        std::map<int, int>::iterator next =
            m_nextXByDepth.find(node.depth);

        if (next != m_nextXByDepth.end())
        {
            x = next->second;
        }
        else if (node.hasParent)
        {
            std::map<unsigned long, NodePosition>::const_iterator parent =
                m_positions.find(node.parentId);

            if (parent != m_positions.end())
                x = std::max(30, parent->second.x);
        }

        const int width = NodeWidth(node);

        m_positions[node.id] =
            NodePosition(x, 30 + node.depth * (NODE_H + GAP_Y));

        m_nextXByDepth[node.depth] =
            x + width + GAP_X;

        maxRight = std::max(maxRight, x + width + 80);
        maxBottom = std::max(
            maxBottom,
            30 + node.depth * (NODE_H + GAP_Y) + NODE_H + 80);
    }

    m_logicalVirtualWidth = maxRight;
    m_logicalVirtualHeight = maxBottom;

    UpdateVirtualSize();
}

void GraphCanvas::UpdateVirtualSize()
{
    const double scale = (double)m_zoomPercent / 100.0;

    int width = (int)(m_logicalVirtualWidth * scale);
    int height = (int)(m_logicalVirtualHeight * scale);

    if (width < 400) width = 400;
    if (height < 300) height = 300;

    SetVirtualSize(width, height);
}

wxString GraphCanvas::Money(int cents) const
{
    return wxString::Format(
        wxT("$%d.%02d"),
        cents / 100,
        std::abs(cents % 100));
}

void GraphCanvas::DrawNode(
    wxDC& dc,
    const ExecutionNode& node,
    const NodePosition& pos) const
{
    const int width = NodeWidth(node);

    wxColour frame =
        wxSystemSettings::GetColour(wxSYS_COLOUR_WINDOWFRAME);

    wxColour face =
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNFACE);

    wxColour caption =
        wxSystemSettings::GetColour(wxSYS_COLOUR_ACTIVECAPTION);

    wxColour captionText =
        wxSystemSettings::GetColour(wxSYS_COLOUR_CAPTIONTEXT);

    if (node.status == NODE_PRUNED)
        caption = wxColour(145, 85, 85);
    else if (node.status == NODE_MATCH)
        caption = wxColour(70, 135, 80);

    if (node.id == m_selectedNodeId)
        dc.SetPen(wxPen(*wxWHITE, 2));
    else
        dc.SetPen(wxPen(frame));

    dc.SetBrush(wxBrush(face));
    dc.DrawRectangle(pos.x, pos.y, width, NODE_H);

    dc.SetBrush(wxBrush(caption));
    dc.DrawRectangle(pos.x + 1, pos.y + 1, width - 2, 20);

    dc.SetTextForeground(captionText);
    dc.SetFont(wxFont(
        8, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_BOLD));

    dc.DrawText(
        wxString::Format(wxT("ESTADO DE BUSQUEDA #%lu"), node.id),
        pos.x + 6, pos.y + 3);

    dc.SetTextForeground(
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNTEXT));

    dc.SetFont(wxFont(
        8, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_NORMAL));

    dc.DrawText(
        wxString::Format(
            wxT("Movimiento: %d / %d"),
            node.index, m_snapshot.itemCount),
        pos.x + 6, pos.y + 27);

    dc.DrawText(
        wxT("Acumulado: ") + Money(node.sumCents),
        pos.x + 6, pos.y + 43);

    wxString status = wxT("PROCESADO");

    if (node.status == NODE_PRUNED)
        status = wxT("DESCARTADO");
    else if (node.status == NODE_MATCH)
        status = wxT("COMPATIBLE");

    dc.DrawText(status, pos.x + 6, pos.y + 60);
}

void GraphCanvas::DrawGraphWithPositions(
    wxDC& dc,
    const std::map<unsigned long, NodePosition>& positions,
    const wxRect& visible,
    bool drawHiddenSummary) const
{
    dc.SetPen(wxPen(wxColour(120, 125, 135), 1));

    // Conexiones primero; las tarjetas deben quedar por encima.
    for (std::vector<ExecutionNode>::size_type i = 0;
         i < m_snapshot.nodes.size(); ++i)
    {
        const ExecutionNode& node = m_snapshot.nodes[i];

        if (!node.hasParent)
            continue;

        std::map<unsigned long, NodePosition>::const_iterator parent =
            positions.find(node.parentId);

        std::map<unsigned long, NodePosition>::const_iterator child =
            positions.find(node.id);

        if (parent == positions.end() || child == positions.end())
            continue;

        // Buscamos el nodo padre para conocer su ancho adaptativo.
        const ExecutionNode* parentNode = 0;

        for (std::vector<ExecutionNode>::size_type j = 0;
             j < m_snapshot.nodes.size(); ++j)
        {
            if (m_snapshot.nodes[j].id == node.parentId)
            {
                parentNode = &m_snapshot.nodes[j];
                break;
            }
        }

        if (!parentNode)
            continue;

        const int x1 =
            parent->second.x + NodeWidth(*parentNode) / 2;

        const int y1 =
            parent->second.y + NODE_H;

        const int x2 =
            child->second.x + NodeWidth(node) / 2;

        const int y2 =
            child->second.y;

        wxRect bounds(
            std::min(x1, x2),
            std::min(y1, y2),
            std::max(1, std::abs(x2 - x1)),
            std::max(1, std::abs(y2 - y1)));

        bounds.Inflate(4, 4);

        if (!bounds.Intersects(visible))
            continue;

        const int midY = (y1 + y2) / 2;

        dc.DrawLine(x1, y1, x1, midY);
        dc.DrawLine(x1, midY, x2, midY);
        dc.DrawLine(x2, midY, x2, y2);
    }

    for (std::vector<ExecutionNode>::size_type i = 0;
         i < m_snapshot.nodes.size(); ++i)
    {
        const ExecutionNode& node = m_snapshot.nodes[i];

        std::map<unsigned long, NodePosition>::const_iterator pos =
            positions.find(node.id);

        if (pos == positions.end())
            continue;

        wxRect bounds(
            pos->second.x,
            pos->second.y,
            NodeWidth(node),
            NODE_H);

        if (!bounds.Intersects(visible))
            continue;

        DrawNode(dc, node, pos->second);
    }

    if (drawHiddenSummary && m_snapshot.unretainedTraceNodes > 0)
    {
        std::ostringstream ss;
        ss << m_snapshot.unretainedTraceNodes
           << " estados logicos adicionales fueron procesados "
              "fuera de la vista detallada.";

        dc.SetTextForeground(*wxWHITE);
        dc.DrawText(
            wxString(ss.str().c_str(), wxConvLocal),
            visible.GetLeft() + 20,
            visible.GetTop() + 10);
    }
}

void GraphCanvas::DrawGraph(
    wxDC& dc,
    const wxRect& visible,
    bool drawHiddenSummary) const
{
    DrawGraphWithPositions(
        dc, m_positions, visible, drawHiddenSummary);
}

void GraphCanvas::OnPaint(wxPaintEvent&)
{
    wxAutoBufferedPaintDC dc(this);
    PrepareDC(dc);

    const double scale = (double)m_zoomPercent / 100.0;
    dc.SetUserScale(scale, scale);

    dc.SetBackground(wxBrush(GetBackgroundColour()));
    dc.Clear();

    int viewX = 0;
    int viewY = 0;
    int pixelsX = 1;
    int pixelsY = 1;

    GetViewStart(&viewX, &viewY);
    GetScrollPixelsPerUnit(&pixelsX, &pixelsY);

    if (pixelsX <= 0) pixelsX = 1;
    if (pixelsY <= 0) pixelsY = 1;

    const wxSize client = GetClientSize();

    const int logicalLeft =
        (int)((viewX * pixelsX) / scale);

    const int logicalTop =
        (int)((viewY * pixelsY) / scale);

    const int logicalWidth =
        (int)(client.x / scale) + 1;

    const int logicalHeight =
        (int)(client.y / scale) + 1;

    wxRect visible(
        logicalLeft, logicalTop, logicalWidth, logicalHeight);

    wxRect culling = visible;
    culling.Inflate(NODE_MAX_W, NODE_H);

    DrawGraph(dc, culling, true);
}

void GraphCanvas::OnEraseBackground(wxEraseEvent&)
{
    // OnPaint cubre toda la superficie mediante doble buffer.
}

void GraphCanvas::OnLeftDown(wxMouseEvent& event)
{
    int deviceWorldX = 0;
    int deviceWorldY = 0;

    CalcUnscrolledPosition(
        event.GetX(), event.GetY(),
        &deviceWorldX, &deviceWorldY);

    const double scale = (double)m_zoomPercent / 100.0;

    const int worldX = (int)(deviceWorldX / scale);
    const int worldY = (int)(deviceWorldY / scale);

    unsigned long selected = 0;

    for (std::map<unsigned long, NodePosition>::const_iterator it =
             m_positions.begin();
         it != m_positions.end();
         ++it)
    {
        const ExecutionNode* node = 0;

        for (std::vector<ExecutionNode>::size_type i = 0;
             i < m_snapshot.nodes.size(); ++i)
        {
            if (m_snapshot.nodes[i].id == it->first)
            {
                node = &m_snapshot.nodes[i];
                break;
            }
        }

        if (!node)
            continue;

        wxRect rect(
            it->second.x,
            it->second.y,
            NodeWidth(*node),
            NODE_H);

        if (rect.Contains(worldX, worldY))
        {
            selected = it->first;
            break;
        }
    }

    if (selected != m_selectedNodeId)
    {
        m_selectedNodeId = selected;
        Refresh(false);
    }

    event.Skip();
}

void GraphCanvas::OnMiddleDown(wxMouseEvent& event)
{
    m_panning = true;
    m_lastMouse = event.GetPosition();

    if (!HasCapture())
        CaptureMouse();
}

void GraphCanvas::OnMiddleUp(wxMouseEvent&)
{
    if (!m_panning)
        return;

    m_panning = false;

    if (HasCapture())
        ReleaseMouse();
}

void GraphCanvas::OnRightDown(wxMouseEvent& event)
{
    m_panning = true;
    m_lastMouse = event.GetPosition();

    if (!HasCapture())
        CaptureMouse();
}

void GraphCanvas::OnRightUp(wxMouseEvent&)
{
    if (!m_panning)
        return;

    m_panning = false;

    if (HasCapture())
        ReleaseMouse();
}

void GraphCanvas::OnMotion(wxMouseEvent& event)
{
    if (!m_panning || !event.Dragging())
        return;

    const wxPoint now = event.GetPosition();
    const int dx = now.x - m_lastMouse.x;
    const int dy = now.y - m_lastMouse.y;

    int viewX = 0;
    int viewY = 0;

    GetViewStart(&viewX, &viewY);

    Scroll(viewX - dx, viewY - dy);

    m_lastMouse = now;
}

bool GraphCanvas::ExportStudyImage(
    const wxString& path,
    const ProblemInstance& problem,
    const wxString& strategyLabel,
    wxString& error) const
{
    if (m_snapshot.nodes.empty())
    {
        error = wxT(
            "Todavia no existe una traza detallada para exportar. "
            "Ejecuta al menos un paso del caso.");
        return false;
    }

    const int margin = 30;
    const int exportGapX = 34;
    const int exportGapY = 28;
    const int bandGap = 55;

    // ---------------------------------------------------------------------
    // Layout de póster.
    //
    // El layout interactivo privilegia estabilidad temporal y puede ser
    // extremadamente ancho. Reducirlo entero a 3000 px hizo que las tarjetas
    // dejaran de ser legibles. Para exportación reagrupamos POR PROFUNDIDAD y
    // envolvemos cada capa en varias filas, conservando el tamaño de tarjetas.
    // ---------------------------------------------------------------------
    int maxNodeWidth = NODE_MIN_W;

    std::map<int, std::vector<const ExecutionNode*> > levels;

    for (std::vector<ExecutionNode>::size_type i = 0;
         i < m_snapshot.nodes.size(); ++i)
    {
        const ExecutionNode& node = m_snapshot.nodes[i];
        levels[node.depth].push_back(&node);
        maxNodeWidth = std::max(maxNodeWidth, NodeWidth(node));
    }

    // 12 columnas producen un póster grande pero todavía manejable. Para
    // trazas pequeñas se usan solamente las columnas necesarias.
    int columns = 12;
    if ((int)m_snapshot.nodes.size() < columns)
        columns = (int)m_snapshot.nodes.size();
    if (columns < 1)
        columns = 1;

    std::map<unsigned long, NodePosition> exportPositions;

    int graphW =
        60 + columns * maxNodeWidth + (columns - 1) * exportGapX;

    int bandY = 35;
    int graphH = 0;

    for (std::map<int, std::vector<const ExecutionNode*> >::const_iterator level =
             levels.begin();
         level != levels.end();
         ++level)
    {
        const std::vector<const ExecutionNode*>& nodes = level->second;

        const int rows =
            ((int)nodes.size() + columns - 1) / columns;

        for (size_t i = 0; i < nodes.size(); ++i)
        {
            const int row = (int)i / columns;
            const int col = (int)i % columns;

            const int x =
                30 + col * (maxNodeWidth + exportGapX);

            const int y =
                bandY + row * (NODE_H + exportGapY);

            exportPositions[nodes[i]->id] =
                NodePosition(x, y);
        }

        bandY +=
            rows * (NODE_H + exportGapY) + bandGap;

        graphH = std::max(graphH, bandY);
    }

    graphH += 20;

    // Entrada del problema: cinco movimientos por línea.
    std::vector<wxString> candidateLines;
    wxString current;

    for (size_t i = 0; i < problem.valuesCents.size(); ++i)
    {
        wxString item =
            wxString::Format(
                wxT("M%lu %s"),
                (unsigned long)(i + 1),
                Money(problem.valuesCents[i]).c_str());

        if (!current.IsEmpty())
            current += wxT("    ");

        current += item;

        if ((i + 1) % 5 == 0)
        {
            candidateLines.push_back(current);
            current.clear();
        }
    }

    if (!current.IsEmpty())
        candidateLines.push_back(current);

    const int headerHeight =
        225 + (int)candidateLines.size() * 18;

    // Mantenemos el póster a escala 1.0 siempre que sea razonable. Sólo
    // reducimos hasta 0.80 si el bitmap excedería ~48 millones de píxeles.
    double exportScale = 1.0;
    const double maxPixels = 48000000.0;

    double estimatedPixels =
        (double)(graphW + margin * 2)
        * (double)(headerHeight + graphH + margin * 2);

    if (estimatedPixels > maxPixels)
    {
        exportScale =
            std::sqrt(maxPixels / estimatedPixels);

        if (exportScale < 0.80)
            exportScale = 0.80;
    }

    const int graphPixelW =
        (int)(graphW * exportScale);

    const int graphPixelH =
        (int)(graphH * exportScale);

    const int imageW =
        std::max(1100, graphPixelW + margin * 2);

    const int imageH =
        headerHeight + graphPixelH + margin * 2;

    // Última defensa para una VM x86. A diferencia de 0.5.2, no seguimos
    // reduciendo hasta hacer ilegible el contenido: preferimos avisar.
    if ((double)imageW * (double)imageH > 70000000.0)
    {
        error = wxT(
            "La traza retenida produciria una imagen demasiado grande para "
            "la memoria disponible de Windows XP. Reinicia el caso y exporta "
            "una ejecucion con menos detalle.");
        return false;
    }

    wxBitmap bitmap(imageW, imageH);

    if (!bitmap.IsOk())
    {
        error = wxT(
            "Windows no pudo reservar memoria para la imagen de exportacion.");
        return false;
    }

    wxMemoryDC dc;
    dc.SelectObject(bitmap);

    dc.SetBackground(wxBrush(*wxWHITE));
    dc.Clear();

    // ---------------------------- cabecera -------------------------------
    dc.SetTextForeground(wxColour(30, 55, 90));
    dc.SetFont(wxFont(
        16, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_BOLD));

    dc.DrawText(
        wxT("ReconcileLab 2006 - Lamina de estudio"),
        margin, margin);

    dc.SetTextForeground(*wxBLACK);
    dc.SetFont(wxFont(
        10, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_BOLD));

    dc.DrawText(
        wxT("Caso: ")
        + wxString(problem.name.c_str(), wxConvLocal),
        margin, margin + 34);

    dc.SetFont(wxFont(
        9, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_NORMAL));

    dc.DrawText(
        wxT("Monto a conciliar: ") + Money(problem.targetCents),
        margin, margin + 55);

    dc.DrawText(
        wxT("Metodo: ") + strategyLabel,
        margin + 310, margin + 55);

    dc.DrawText(
        wxString::Format(
            wxT("Tick: %lu   Visitados: %lu   Descartados: %lu   "
                "Compatibles: %lu   Evitados: %lu"),
            m_snapshot.logicalTick,
            m_snapshot.visited,
            m_snapshot.pruned,
            m_snapshot.matches,
            m_snapshot.avoidedStates),
        margin, margin + 76);

    dc.SetFont(wxFont(
        9, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_BOLD));

    dc.DrawText(
        wxT("Entrada - movimientos candidatos:"),
        margin, margin + 101);

    dc.SetFont(wxFont(
        8, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_NORMAL));

    int y = margin + 122;

    for (size_t i = 0; i < candidateLines.size(); ++i)
    {
        dc.DrawText(candidateLines[i], margin, y);
        y += 18;
    }

    dc.SetFont(wxFont(
        9, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_BOLD));

    dc.DrawText(
        wxT("Que significa el diagrama:"),
        margin, y + 5);

    dc.SetFont(wxFont(
        8, wxFONTFAMILY_DEFAULT, wxFONTSTYLE_NORMAL, wxFONTWEIGHT_NORMAL));

    dc.DrawText(
        wxT("Cada tarjeta representa un estado de la busqueda: una pregunta "
            "parcial sobre que movimientos incluir u omitir."),
        margin, y + 24);

    dc.DrawText(
        wxT("Las ramas conectan estados relacionados. Una tarjeta DESCARTADO "
            "indica trabajo que puede evitarse sin perder una solucion valida."),
        margin, y + 40);

    dc.DrawText(
        wxT("Este poster reorganiza las tarjetas por profundidad para mantener "
            "el texto legible; no es una captura literal del viewport."),
        margin, y + 56);

    dc.SetPen(wxPen(wxColour(145, 155, 165)));
    dc.DrawLine(
        margin,
        headerHeight - 8,
        imageW - margin,
        headerHeight - 8);

    // ---------------------------- póster ---------------------------------
    dc.SetDeviceOrigin(margin, headerHeight + margin);
    dc.SetUserScale(exportScale, exportScale);

    dc.SetPen(*wxTRANSPARENT_PEN);
    dc.SetBrush(wxBrush(GetBackgroundColour()));
    dc.DrawRectangle(0, 0, graphW, graphH);

    DrawGraphWithPositions(
        dc,
        exportPositions,
        wxRect(0, 0, graphW, graphH),
        true);

    dc.SetUserScale(1.0, 1.0);
    dc.SetDeviceOrigin(0, 0);

    dc.SelectObject(wxNullBitmap);

    if (!bitmap.SaveFile(path, wxBITMAP_TYPE_PNG))
    {
        error = wxT(
            "No se pudo guardar el archivo PNG. "
            "Verifica la ruta y los permisos.");
        return false;
    }

    return true;
}
