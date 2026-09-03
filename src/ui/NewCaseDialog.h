#ifndef RECONCILELAB_NEW_CASE_DIALOG_H
#define RECONCILELAB_NEW_CASE_DIALOG_H

#include "../model/ProblemInstance.h"

#include <wx/dialog.h>
#include <wx/textctrl.h>
#include <wx/choice.h>

/**
 * Dialogo para crear un caso de conciliacion sin editar archivos a mano.
 *
 * La validacion reutiliza las mismas reglas de los archivos `.case`, de modo
 * que un caso creado desde la GUI y uno abierto desde el repositorio tienen la
 * misma semantica.
 */
class NewCaseDialog : public wxDialog
{
public:
    explicit NewCaseDialog(wxWindow* parent);

    /** Devuelve el problema validado cuando el dialogo termina con OK. */
    ProblemInstance GetProblem() const;

private:
    /** Valida los campos y cierra el dialogo solamente si el caso es valido. */
    void OnOk(wxCommandEvent& event);

private:
    wxTextCtrl* m_name;
    wxTextCtrl* m_target;
    wxTextCtrl* m_values;
    wxChoice* m_objective;

    ProblemInstance m_problem;

    DECLARE_EVENT_TABLE()
};

#endif
