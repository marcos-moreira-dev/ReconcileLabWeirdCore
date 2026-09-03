#ifndef RECONCILELAB_GRAPH_CANVAS_H
#define RECONCILELAB_GRAPH_CANVAS_H

#include "../model/ExecutionSnapshot.h"
#include "../model/ProblemInstance.h"

#include <wx/scrolwin.h>
#include <map>
#include <vector>

/**
 * Posición lógica de una tarjeta dentro del lienzo virtual.
 */
struct NodePosition
{
    int x;
    int y;

    NodePosition() : x(0), y(0) {}
    NodePosition(int px, int py) : x(px), y(py) {}
};

/**
 * Lienzo desplazable del espacio de búsqueda.
 *
 * Desde 0.5.3 este wxScrolledWindow dibuja únicamente el mundo desplazable.
 * El rail de zoom vive en WorkspacePanel. Esto importa en Windows XP porque
 * Scroll() puede reutilizar píxeles mediante blitting: cualquier overlay fijo
 * pintado dentro de esta misma superficie podía terminar copiado como
 * fragmentos fantasma.
 */
class GraphCanvas : public wxScrolledWindow
{
public:
    explicit GraphCanvas(wxWindow* parent);

    /** Sustituye la instantánea observable del motor. */
    void SetSnapshot(const ExecutionSnapshot& snapshot);

    /** Devuelve la tarjeta seleccionada si sigue retenida. */
    bool GetSelectedNode(ExecutionNode& out) const;

    /** Cambia el zoom entre 25% y 200%. */
    void SetZoomPercent(int percent);

    /** Devuelve el porcentaje actual. */
    int GetZoomPercent() const;

    /**
     * Selecciona un estado retenido y desplaza el viewport para mostrarlo.
     *
     * La operación no cambia el layout ni el algoritmo: sólo modifica
     * selección y scroll del lienzo.
     *
     * @return true si el estado sigue disponible en la traza detallada.
     */
    bool FocusNode(unsigned long nodeId);

    /**
     * Exporta una lámina PNG de estudio.
     *
     * La exportación NO reutiliza el layout extremadamente ancho del viewport.
     * Construye un layout de póster por capas, a tamaño legible, para que los
     * estados no terminen reducidos a puntos en una imagen grande.
     */
    bool ExportStudyImage(
        const wxString& path,
        const ProblemInstance& problem,
        const wxString& strategyLabel,
        wxString& error) const;

private:
    /** Limpia posiciones al cambiar/reiniciar un caso. */
    void ResetLayout();

    /** Añade coordenadas sólo a estados que todavía no las tienen. */
    void ExtendLayout();

    /** Ajusta el tamaño virtual al layout y al zoom. */
    void UpdateVirtualSize();

    /**
     * Calcula el ancho de una tarjeta según el texto más propenso a crecer.
     *
     * Los ids de 3/4 dígitos ya no desbordan el encabezado.
     */
    int NodeWidth(const ExecutionNode& node) const;

    /** Dibuja una tarjeta usando su ancho adaptativo. */
    void DrawNode(
        wxDC& dc,
        const ExecutionNode& node,
        const NodePosition& pos) const;

    /** Dibuja un grafo usando un mapa de posiciones dado. */
    void DrawGraphWithPositions(
        wxDC& dc,
        const std::map<unsigned long, NodePosition>& positions,
        const wxRect& visible,
        bool drawHiddenSummary) const;

    /** Dibuja el layout interactivo actual. */
    void DrawGraph(
        wxDC& dc,
        const wxRect& visible,
        bool drawHiddenSummary) const;

    /** Formatea centavos para interfaz/exportación. */
    wxString Money(int cents) const;

    /** Pinta el frame visible. */
    void OnPaint(wxPaintEvent& event);

    /** Evita doble borrado de fondo. */
    void OnEraseBackground(wxEraseEvent& event);

    /** Selecciona una tarjeta. */
    void OnLeftDown(wxMouseEvent& event);

    /** Inicia paneo con botón central. */
    void OnMiddleDown(wxMouseEvent& event);

    /** Termina paneo con botón central. */
    void OnMiddleUp(wxMouseEvent& event);

    /** Inicia paneo alternativo con botón derecho. */
    void OnRightDown(wxMouseEvent& event);

    /** Termina paneo alternativo con botón derecho. */
    void OnRightUp(wxMouseEvent& event);

    /** Convierte arrastre en desplazamiento 2D. */
    void OnMotion(wxMouseEvent& event);

private:
    ExecutionSnapshot m_snapshot;
    unsigned long m_selectedNodeId;

    std::map<unsigned long, NodePosition> m_positions;
    std::map<int, int> m_nextXByDepth;

    bool m_panning;
    wxPoint m_lastMouse;

    int m_zoomPercent;
    int m_logicalVirtualWidth;
    int m_logicalVirtualHeight;

    enum
    {
        NODE_MIN_W = 190,
        NODE_MAX_W = 300,
        NODE_H = 84,
        GAP_X = 34,
        GAP_Y = 46
    };

    DECLARE_EVENT_TABLE()
};

#endif
