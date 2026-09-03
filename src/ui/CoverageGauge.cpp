#include "CoverageGauge.h"

#include <wx/dcbuffer.h>
#include <wx/settings.h>
#include <algorithm>

BEGIN_EVENT_TABLE(CoverageGauge, wxPanel)
    EVT_TIMER(ID_ANIMATION_TIMER, CoverageGauge::OnAnimationTimer)
    EVT_PAINT(CoverageGauge::OnPaint)
    EVT_ERASE_BACKGROUND(CoverageGauge::OnEraseBackground)
    EVT_SIZE(CoverageGauge::OnSize)
    EVT_SHOW(CoverageGauge::OnShow)
END_EVENT_TABLE()

CoverageGauge::CoverageGauge(wxWindow* parent)
    : wxPanel(parent, wxID_ANY,
              wxDefaultPosition, wxSize(-1, 38),
              wxBORDER_NONE),
      m_displayValue(0),
      m_targetValue(0),
      m_animationTimer(this, ID_ANIMATION_TIMER)
{
    SetBackgroundStyle(wxBG_STYLE_PAINT);
    SetBackgroundColour(
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNFACE));

    SetMinSize(wxSize(90, 38));
}

void CoverageGauge::SetTargetValue(int value)
{
    if (value < 0) value = 0;
    if (value > 1000) value = 1000;

    if (value == m_targetValue)
        return;

    m_targetValue = value;

    if (m_targetValue <= m_displayValue)
    {
        // Reinicio, cambio de caso o corrección hacia abajo: no animamos hacia
        // atrás porque durante unos frames parecería quedar progreso del caso
        // anterior.
        m_displayValue = m_targetValue;
        m_animationTimer.Stop();
        Refresh(true);
        Update();
        return;
    }

    // 20 ms produce hasta 50 frames/s para un control diminuto y no obliga al
    // canvas ni a las tablas a repintarse a esa frecuencia.
    if (!m_animationTimer.IsRunning())
        m_animationTimer.Start(20);
}

int CoverageGauge::GetDisplayedValue() const
{
    return m_displayValue;
}

int CoverageGauge::GetTargetValue() const
{
    return m_targetValue;
}

void CoverageGauge::OnAnimationTimer(wxTimerEvent&)
{
    if (m_displayValue >= m_targetValue)
    {
        m_displayValue = m_targetValue;
        m_animationTimer.Stop();
        Refresh(true);
        Update();
        return;
    }

    const int remaining =
        m_targetValue - m_displayValue;

    // Avance rápido al principio y fino al final. El paso mínimo de 25
    // equivale a 2.5 puntos porcentuales, suficiente para que un caso pequeño
    // complete la animación en unas pocas décimas de segundo.
    const int step =
        std::max(25, remaining / 5);

    m_displayValue += step;

    if (m_displayValue >= m_targetValue)
    {
        m_displayValue = m_targetValue;
        m_animationTimer.Stop();
    }

    Refresh(true);
    Update();
}

void CoverageGauge::OnPaint(wxPaintEvent&)
{
    wxAutoBufferedPaintDC dc(this);

    const wxSize size =
        GetClientSize();

    const wxColour face =
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNFACE);

    const wxColour border =
        wxSystemSettings::GetColour(wxSYS_COLOUR_WINDOWFRAME);

    dc.SetBackground(wxBrush(face));
    dc.Clear();

    // Texto y barra nacen del MISMO m_displayValue. No puede existir un frame
    // con porcentaje nuevo y relleno viejo.
    const double percent =
        (double)m_displayValue / 10.0;

    const wxString caption =
        wxString::Format(
            wxT("Progreso de ejecucion: %.1f%%"),
            percent);

    dc.SetTextForeground(
        wxSystemSettings::GetColour(wxSYS_COLOUR_BTNTEXT));

    dc.SetFont(GetFont());
    dc.DrawText(caption, 0, 0);

    int textW = 0;
    int textH = 0;

    dc.GetTextExtent(
        wxT("Ag"),
        &textW,
        &textH);

    const int barY =
        textH + 5;

    const int barH =
        14;

    const int barW =
        size.x;

    if (barW <= 2
        || barY + barH > size.y)
    {
        return;
    }

    dc.SetPen(wxPen(border));
    dc.SetBrush(*wxWHITE_BRUSH);
    dc.DrawRectangle(
        0, barY,
        barW, barH);

    const int innerW =
        barW - 4;

    const int innerH =
        barH - 4;

    const int fillW =
        (innerW * m_displayValue) / 1000;

    if (fillW <= 0)
        return;

    dc.SetPen(*wxTRANSPARENT_PEN);
    dc.SetBrush(
        wxBrush(wxColour(74, 184, 74)));

    dc.DrawRectangle(
        2, barY + 2,
        fillW, innerH);

    // Segmentación ligera estilo XP.
    dc.SetPen(
        wxPen(wxColour(226, 255, 226)));

    for (int x = 10; x < fillW; x += 10)
    {
        dc.DrawLine(
            2 + x, barY + 3,
            2 + x, barY + barH - 3);
    }
}

void CoverageGauge::OnEraseBackground(wxEraseEvent&)
{
    // El doble buffer cubre todo.
}

void CoverageGauge::OnSize(wxSizeEvent& event)
{
    Refresh(true);
    event.Skip();
}

void CoverageGauge::OnShow(wxShowEvent& event)
{
    if (event.IsShown())
    {
        Refresh(true);
        Update();
    }

    event.Skip();
}
