#ifndef RECONCILELAB_COVERAGE_GAUGE_H
#define RECONCILELAB_COVERAGE_GAUGE_H

#include <wx/panel.h>
#include <wx/timer.h>

/**
 * Indicador animado de progreso de ejecución.
 *
 * Importante: ya no representa "porcentaje del espacio potencial explorado".
 * Ese dato sigue existiendo como métrica separada porque una búsqueda con
 * objetivo "primera coincidencia" puede terminar correctamente tras examinar
 * sólo una fracción pequeña del espacio.
 *
 * La barra representa el estado de la EJECUCIÓN:
 * - mientras corre, usa como referencia el espacio contabilizado hasta ahora;
 * - cuando el objetivo del caso termina, converge visualmente a 100%.
 *
 * El control posee su propio wxTimer. Así puede terminar de animar un cambio
 * aunque el motor haya completado entre dos ticks de muestreo de MainFrame.
 */
class CoverageGauge : public wxPanel
{
public:
    explicit CoverageGauge(wxWindow* parent);

    /**
     * Publica un nuevo objetivo visual 0..1000.
     *
     * Si el objetivo sube, se interpola en varios frames.
     * Si baja (por ejemplo al cargar/reiniciar un caso), se aplica de inmediato
     * para no conservar relleno verde perteneciente a la ejecución anterior.
     */
    void SetTargetValue(int value);

    /** Devuelve el valor actualmente dibujado. */
    int GetDisplayedValue() const;

    /** Devuelve el objetivo al que se está animando. */
    int GetTargetValue() const;

private:
    enum
    {
        ID_ANIMATION_TIMER = wxID_HIGHEST + 930
    };

    /** Avanza la interpolación visual hacia m_targetValue. */
    void OnAnimationTimer(wxTimerEvent& event);

    /** Dibuja porcentaje y barra usando m_displayValue. */
    void OnPaint(wxPaintEvent& event);

    /** Evita borrado previo al doble buffer. */
    void OnEraseBackground(wxEraseEvent& event);

    /** Un resize cambia el ancho del relleno y exige repaint completo. */
    void OnSize(wxSizeEvent& event);

    /** Al reaparecer se reconstruye el frame desde el estado interno. */
    void OnShow(wxShowEvent& event);

private:
    int m_displayValue;
    int m_targetValue;
    wxTimer m_animationTimer;

    DECLARE_EVENT_TABLE()
};

#endif
