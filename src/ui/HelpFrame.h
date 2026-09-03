#ifndef RECONCILELAB_HELP_FRAME_H
#define RECONCILELAB_HELP_FRAME_H

#include <wx/frame.h>
#include <wx/html/htmlwin.h>

/**
 * Ventana de ayuda HTML integrada, inspirada en la ayuda compacta de las
 * aplicaciones de escritorio de la epoca de Windows XP.
 *
 * Estas paginas hablan como producto: explican conciliacion, vocabulario
 * bancario, uso e interpretacion de resultados, no la arquitectura interna.
 */
class HelpFrame : public wxFrame
{
public:
    /** Abre la ayuda y carga la pagina HTML local solicitada. */
    HelpFrame(wxWindow* parent, const wxString& startPage);

private:
    wxHtmlWindow* m_html;
};

#endif
