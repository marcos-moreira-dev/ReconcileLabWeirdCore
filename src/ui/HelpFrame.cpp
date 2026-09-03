#include "HelpFrame.h"

HelpFrame::HelpFrame(wxWindow* parent, const wxString& startPage)
    : wxFrame(parent, wxID_ANY, wxT("Ayuda de ReconcileLab 2006"),
              wxDefaultPosition, wxSize(780, 590))
{
    m_html = new wxHtmlWindow(this, wxID_ANY,
                              wxDefaultPosition, wxDefaultSize,
                              wxHW_SCROLLBAR_AUTO);

    if (!m_html->LoadPage(startPage))
    {
        m_html->SetPage(
            wxT("<html><body><h2>La ayuda no esta disponible</h2>")
            wxT("<p>No se pudieron abrir los archivos locales de ayuda. ")
            wxT("Verifica que la carpeta <b>help</b> permanezca junto a la ")
            wxT("estructura del programa.</p></body></html>"));
    }

    CentreOnParent();
}
