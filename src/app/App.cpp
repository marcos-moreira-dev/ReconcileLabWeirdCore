#include "../ui/MainFrame.h"

#include <wx/wx.h>
#include <wx/image.h>

/**
 * Objeto de aplicación requerido por wxWidgets.
 *
 * Se mantiene deliberadamente pequeño. Registra los formatos de imagen que
 * necesita el producto y crea MainFrame.
 */
class ReconcileLabApp : public wxApp
{
public:
    /**
     * Inicializa servicios globales mínimos y crea la ventana principal.
     *
     * `wxInitAllImageHandlers()` es importante en wxWidgets 3.0: sin él,
     * wxBitmap puede conocer el archivo PNG pero no tener un decoder registrado.
     * Esto fue detectado directamente en la VM de Windows XP.
     */
    virtual bool OnInit()
    {
        wxInitAllImageHandlers();

        MainFrame* frame = new MainFrame;
        frame->Show(true);
        SetTopWindow(frame);

        return true;
    }
};

IMPLEMENT_APP(ReconcileLabApp)
