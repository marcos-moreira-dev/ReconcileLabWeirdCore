#include "WorkspacePanel.h"

#include <wx/dcbuffer.h>
#include <wx/settings.h>
#include <algorithm>

BEGIN_EVENT_TABLE(ZoomRail, wxPanel)
    EVT_PAINT(ZoomRail::OnPaint)
    EVT_ERASE_BACKGROUND(ZoomRail::OnEraseBackground)
    EVT_SIZE(ZoomRail::OnSize)
    EVT_LEFT_DOWN(ZoomRail::OnLeftDown)
    EVT_LEFT_UP(ZoomRail::OnLeftUp)
    EVT_MOTION(ZoomRail::OnMotion)
END_EVENT_TABLE()

ZoomRail::ZoomRail(wxWindow* parent, GraphCanvas* canvas)
    : wxPanel(parent, wxID_ANY,
              wxDefaultPosition, wxSize(RAIL_W, -1),
              wxBORDER_NONE),
      m_canvas(canvas),
      m_zoomPercent(100),
      m_dragging(false)
{
    SetBackgroundStyle(wxBG_STYLE_PAINT);

    // Fondo claro de panel XP, como pidió la revisión visual.
    SetBackgroundColour(
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNFACE));

    // Sólo fijamos el ancho. Una altura mínima fue precisamente lo que podía
    // dejar al rail con geometría vieja/clipeada al expandir la timeline.
    SetMinSize(wxSize(RAIL_W, 0));
    SetToolTip(wxT("Zoom del lienzo"));
}

wxRect ZoomRail::ControlRect() const
{
    const wxSize size = GetClientSize();

    // El control intenta conservar sus 190 px clásicos, pero si la timeline
    // deja menos espacio se compacta en vez de quedar cortado. Siempre se
    // ancla al borde inferior de la región REALMENTE visible.
    int available =
        size.y - 12;

    if (available < 86)
        available = std::max(40, size.y - 4);

    // CONTROL_H es miembro de un enum interno. GCC 9.2 no deduce un único
    // tipo para std::min(enum, int), así que expresamos ambos argumentos como
    // int de forma explícita para conservar compatibilidad C++98/TDM-GCC.
    const int height =
        std::min((int)CONTROL_H, available);

    int y =
        size.y - height - 6;

    if (y < 2)
        y = 2;

    return wxRect(
        (RAIL_W - CONTROL_W) / 2,
        y,
        CONTROL_W,
        height);
}

int ZoomRail::ButtonHeight(
    const wxRect& control) const
{
    return control.height >= 130 ? 24 : 18;
}

int ZoomRail::TrackTopY(
    const wxRect& control) const
{
    return control.y + ButtonHeight(control) + 12;
}

int ZoomRail::LabelY(
    const wxRect& control) const
{
    return control.GetBottom() - 17;
}

int ZoomRail::MinusButtonY(
    const wxRect& control) const
{
    return LabelY(control)
        - ButtonHeight(control)
        - 5;
}

int ZoomRail::TrackBottomY(
    const wxRect& control) const
{
    int bottom =
        MinusButtonY(control) - 9;

    const int top =
        TrackTopY(control);

    if (bottom < top + 12)
        bottom = top + 12;

    return bottom;
}

int ZoomRail::YFromPercent(
    int percent,
    const wxRect& control) const
{
    if (percent < 25) percent = 25;
    if (percent > 200) percent = 200;

    const int top =
        TrackTopY(control);

    const int bottom =
        TrackBottomY(control);

    const double ratio =
        (double)(percent - 25) / 175.0;

    return bottom
        - (int)(ratio * (bottom - top) + 0.5);
}

int ZoomRail::PercentFromY(
    int y,
    const wxRect& control) const
{
    const int top =
        TrackTopY(control);

    const int bottom =
        TrackBottomY(control);

    if (y < top) y = top;
    if (y > bottom) y = bottom;

    const double ratio =
        (double)(bottom - y)
        / (double)std::max(1, bottom - top);

    int percent =
        25 + (int)(ratio * 175.0 + 0.5);

    if (percent < 25) percent = 25;
    if (percent > 200) percent = 200;

    return percent;
}

void ZoomRail::ApplyZoom(int percent)
{
    if (percent < 25) percent = 25;
    if (percent > 200) percent = 200;

    if (m_zoomPercent == percent)
        return;

    m_zoomPercent = percent;

    if (m_canvas)
        m_canvas->SetZoomPercent(percent);

    Refresh(false);
    Update();
}

void ZoomRail::OnPaint(wxPaintEvent&)
{
    wxAutoBufferedPaintDC dc(this);

    dc.SetBackground(wxBrush(GetBackgroundColour()));
    dc.Clear();

    const wxRect r =
        ControlRect();

    if (r.height < 35)
        return;

    const wxColour face =
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNFACE);

    const wxColour border =
        wxSystemSettings::GetColour(wxSYS_COLOUR_WINDOWFRAME);

    const wxColour shadow =
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNSHADOW);

    const wxColour highlight =
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNHIGHLIGHT);

    const wxColour text =
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNTEXT);

    dc.SetPen(wxPen(border));
    dc.SetBrush(wxBrush(face));
    dc.DrawRectangle(r);

    const int centerX =
        r.x + r.width / 2;

    const int buttonH =
        ButtonHeight(r);

    // Botón +
    dc.SetPen(wxPen(border));
    dc.SetBrush(wxBrush(face));
    dc.DrawRectangle(
        r.x + 7, r.y + 4,
        r.width - 14, buttonH);

    const int plusY =
        r.y + 4 + buttonH / 2;

    dc.SetPen(wxPen(text, 2));
    dc.DrawLine(
        centerX - 5, plusY,
        centerX + 5, plusY);

    dc.DrawLine(
        centerX, plusY - 5,
        centerX, plusY + 5);

    // Pista.
    const int trackTop =
        TrackTopY(r);

    const int trackBottom =
        TrackBottomY(r);

    dc.SetPen(wxPen(shadow));
    dc.DrawLine(
        centerX, trackTop,
        centerX, trackBottom);

    dc.SetPen(wxPen(highlight));
    dc.DrawLine(
        centerX + 1, trackTop,
        centerX + 1, trackBottom);

    // Marcas repartidas proporcionalmente.
    for (int p = 25; p <= 200; p += 25)
    {
        const int y =
            YFromPercent(p, r);

        const int half =
            (p == 100) ? 6 : 4;

        dc.SetPen(wxPen(shadow));
        dc.DrawLine(
            centerX - half, y,
            centerX + half, y);
    }

    // Thumb.
    const int thumbY =
        YFromPercent(m_zoomPercent, r);

    dc.SetPen(wxPen(border));
    dc.SetBrush(wxBrush(wxColour(221, 235, 249)));
    dc.DrawRectangle(
        centerX - 9, thumbY - 5,
        19, 11);

    dc.SetPen(wxPen(highlight));
    dc.DrawLine(
        centerX - 7, thumbY - 3,
        centerX + 7, thumbY - 3);

    // Botón -
    const int minusY =
        MinusButtonY(r);

    dc.SetPen(wxPen(border));
    dc.SetBrush(wxBrush(face));
    dc.DrawRectangle(
        r.x + 7, minusY,
        r.width - 14, buttonH);

    dc.SetPen(wxPen(text, 2));
    dc.DrawLine(
        centerX - 5,
        minusY + buttonH / 2,
        centerX + 5,
        minusY + buttonH / 2);

    // Porcentaje.
    dc.SetTextForeground(text);
    dc.SetFont(wxFont(
        8,
        wxFONTFAMILY_DEFAULT,
        wxFONTSTYLE_NORMAL,
        wxFONTWEIGHT_NORMAL));

    const wxString label =
        wxString::Format(
            wxT("%d%%"), m_zoomPercent);

    int tw = 0;
    int th = 0;

    dc.GetTextExtent(
        label, &tw, &th);

    dc.DrawText(
        label,
        centerX - tw / 2,
        LabelY(r));
}

void ZoomRail::OnEraseBackground(wxEraseEvent&)
{
    // OnPaint cubre toda la región.
}

void ZoomRail::OnSize(wxSizeEvent& event)
{
    // Fundamental para XP: un cambio de altura debe recalcular ControlRect()
    // inmediatamente. No conservamos ninguna Y cacheada.
    Refresh(false);
    event.Skip();
}

void ZoomRail::OnLeftDown(wxMouseEvent& event)
{
    const wxRect r =
        ControlRect();

    if (!r.Contains(event.GetPosition()))
        return;

    const int buttonH =
        ButtonHeight(r);

    wxRect plusRect(
        r.x + 7,
        r.y + 4,
        r.width - 14,
        buttonH);

    wxRect minusRect(
        r.x + 7,
        MinusButtonY(r),
        r.width - 14,
        buttonH);

    if (plusRect.Contains(event.GetPosition()))
    {
        ApplyZoom(m_zoomPercent + 10);
        return;
    }

    if (minusRect.Contains(event.GetPosition()))
    {
        ApplyZoom(m_zoomPercent - 10);
        return;
    }

    const int trackTop =
        TrackTopY(r);

    const int trackBottom =
        TrackBottomY(r);

    if (event.GetY() >= trackTop - 8
        && event.GetY() <= trackBottom + 8)
    {
        m_dragging = true;

        if (!HasCapture())
            CaptureMouse();

        ApplyZoom(
            PercentFromY(
                event.GetY(), r));
    }
}

void ZoomRail::OnLeftUp(wxMouseEvent&)
{
    if (!m_dragging)
        return;

    m_dragging = false;

    if (HasCapture())
        ReleaseMouse();
}

void ZoomRail::OnMotion(wxMouseEvent& event)
{
    if (!m_dragging
        || !event.Dragging())
    {
        return;
    }

    ApplyZoom(
        PercentFromY(
            event.GetY(),
            ControlRect()));
}


BEGIN_EVENT_TABLE(WorkspacePanel, wxPanel)
    EVT_SIZE(WorkspacePanel::OnSize)
END_EVENT_TABLE()

WorkspacePanel::WorkspacePanel(wxWindow* parent)
    : wxPanel(parent, wxID_ANY),
      m_canvas(new GraphCanvas(this)),
      m_zoomRail(new ZoomRail(this, m_canvas))
{
    LayoutChildren();
}

GraphCanvas* WorkspacePanel::GetCanvas() const
{
    return m_canvas;
}

void WorkspacePanel::LayoutChildren()
{
    const wxSize size =
        GetClientSize();

    const int railWidth =
        58;

    int canvasWidth =
        size.x - railWidth;

    if (canvasWidth < 0)
        canvasWidth = 0;

    // Asignación manual deliberada: ambos hijos reciben SIEMPRE la altura real
    // del workspace. Si la timeline gana 250 px, el rail pierde exactamente los
    // mismos 250 px y su consola se vuelve a anclar arriba de ese nuevo fondo.
    m_canvas->SetSize(
        0, 0,
        canvasWidth, size.y);

    m_zoomRail->SetSize(
        canvasWidth, 0,
        railWidth, size.y);

    m_zoomRail->Refresh(false);
}

void WorkspacePanel::OnSize(wxSizeEvent& event)
{
    LayoutChildren();
    event.Skip();
}
