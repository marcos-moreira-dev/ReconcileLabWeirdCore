#ifndef RECONCILELAB_WORKSPACE_PANEL_H
#define RECONCILELAB_WORKSPACE_PANEL_H

#include "GraphCanvas.h"

#include <wx/panel.h>

/**
 * Rail vertical del zoom.
 *
 * Es hermano de GraphCanvas, no hijo suyo. Su superficie nunca participa del
 * scroll del mundo lógico.
 *
 * En 0.5.5 el rail deja además de depender del layout implícito de un sizer:
 * WorkspacePanel le asigna explícitamente la misma altura visible que tiene el
 * workspace. Esto evita que conserve una altura vieja cuando la línea de
 * tiempo se expande en wxWidgets 3.0.5/Windows XP.
 */
class ZoomRail : public wxPanel
{
public:
    ZoomRail(wxWindow* parent, GraphCanvas* canvas);

private:
    /** Rectángulo de la consola; se adapta si el workspace se vuelve bajo. */
    wxRect ControlRect() const;

    /** Altura de los botones + / - para el alto disponible. */
    int ButtonHeight(const wxRect& control) const;

    /** Y absoluta donde comienza la pista. */
    int TrackTopY(const wxRect& control) const;

    /** Y absoluta donde termina la pista. */
    int TrackBottomY(const wxRect& control) const;

    /** Y absoluta del botón -. */
    int MinusButtonY(const wxRect& control) const;

    /** Y absoluta de la etiqueta porcentual. */
    int LabelY(const wxRect& control) const;

    /** Convierte porcentaje a Y del thumb. */
    int YFromPercent(int percent, const wxRect& control) const;

    /** Convierte una Y física a porcentaje. */
    int PercentFromY(int y, const wxRect& control) const;

    /** Sincroniza valor y canvas. */
    void ApplyZoom(int percent);

    /** Dibuja la consola completa. */
    void OnPaint(wxPaintEvent& event);

    /** Evita borrado previo al doble buffer. */
    void OnEraseBackground(wxEraseEvent& event);

    /** Redibuja al cambiar la altura disponible. */
    void OnSize(wxSizeEvent& event);

    /** Atiende +, -, pista y comienzo de arrastre. */
    void OnLeftDown(wxMouseEvent& event);

    /** Termina el arrastre. */
    void OnLeftUp(wxMouseEvent& event);

    /** Actualiza el thumb durante el arrastre. */
    void OnMotion(wxMouseEvent& event);

private:
    GraphCanvas* m_canvas;
    int m_zoomPercent;
    bool m_dragging;

    enum
    {
        RAIL_W = 58,
        CONTROL_W = 46,
        CONTROL_H = 190
    };

    DECLARE_EVENT_TABLE()
};

/**
 * Workspace compuesto por un canvas desplazable y un rail de zoom fijo.
 *
 * El tamaño de ambos hijos se asigna explícitamente en OnSize. Para este
 * contenedor de sólo dos elementos resulta más predecible en la versión antigua
 * de wxWidgets que depender de una relayout encadenada entre varios sizers.
 */
class WorkspacePanel : public wxPanel
{
public:
    explicit WorkspacePanel(wxWindow* parent);

    /** Devuelve el canvas lógico. */
    GraphCanvas* GetCanvas() const;

private:
    /** Ajusta inmediatamente canvas y rail a la altura real del workspace. */
    void LayoutChildren();

    /** Se dispara cuando la timeline cambia el espacio disponible. */
    void OnSize(wxSizeEvent& event);

private:
    GraphCanvas* m_canvas;
    ZoomRail* m_zoomRail;

    DECLARE_EVENT_TABLE()
};

#endif
