#include "NewCaseDialog.h"
#include "../io/MoneyText.h"
#include "../io/CaseFile.h"

#include <wx/sizer.h>
#include <wx/stattext.h>
#include <wx/statbox.h>
#include <wx/msgdlg.h>
#include <wx/tokenzr.h>
#include <sstream>

BEGIN_EVENT_TABLE(NewCaseDialog, wxDialog)
    EVT_BUTTON(wxID_OK, NewCaseDialog::OnOk)
END_EVENT_TABLE()

NewCaseDialog::NewCaseDialog(wxWindow* parent)
    : wxDialog(parent, wxID_ANY, wxT("Nuevo caso de conciliacion"),
               wxDefaultPosition, wxSize(520, 560),
               wxDEFAULT_DIALOG_STYLE | wxRESIZE_BORDER),
      m_name(0), m_target(0), m_values(0), m_objective(0)
{
    wxBoxSizer* root = new wxBoxSizer(wxVERTICAL);

    root->Add(new wxStaticText(this, wxID_ANY, wxT("Nombre del caso:")),
              0, wxLEFT | wxRIGHT | wxTOP, 10);
    m_name = new wxTextCtrl(this, wxID_ANY,
                            wxT("Caso de conciliacion personalizado"));
    root->Add(m_name, 0, wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 10);

    root->Add(new wxStaticText(this, wxID_ANY, wxT("Monto a conciliar:")),
              0, wxLEFT | wxRIGHT | wxTOP, 10);
    m_target = new wxTextCtrl(this, wxID_ANY, wxT("0.00"));
    root->Add(m_target, 0, wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 10);

    wxStaticBoxSizer* valuesBox =
        new wxStaticBoxSizer(wxVERTICAL, this, wxT("Movimientos candidatos"));

    valuesBox->Add(
        new wxStaticText(this, wxID_ANY,
                         wxT("Ingresa un monto por linea. Ejemplo: 32.00")),
        0, wxEXPAND | wxALL, 5);

    m_values = new wxTextCtrl(this, wxID_ANY, wxEmptyString,
                              wxDefaultPosition, wxDefaultSize,
                              wxTE_MULTILINE | wxTE_DONTWRAP);
    valuesBox->Add(m_values, 1, wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 5);
    root->Add(valuesBox, 1, wxEXPAND | wxLEFT | wxRIGHT, 10);

    root->Add(new wxStaticText(this, wxID_ANY, wxT("Objetivo de la busqueda:")),
              0, wxLEFT | wxRIGHT | wxTOP, 10);

    wxArrayString objectives;
    objectives.Add(wxT("Detenerse en la primera combinacion compatible"));
    objectives.Add(wxT("Encontrar todas las combinaciones compatibles"));
    objectives.Add(wxT("Contar las combinaciones compatibles"));

    m_objective = new wxChoice(this, wxID_ANY, wxDefaultPosition,
                               wxDefaultSize, objectives);
    m_objective->SetSelection(0);
    root->Add(m_objective, 0, wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 10);

    wxStaticText* note = new wxStaticText(
        this, wxID_ANY,
        wxT("Una combinacion compatible explica el monto numericamente. ")
        wxT("Por si sola no demuestra que esos movimientos formaron ")
        wxT("historicamente el deposito o abono original."));
    note->Wrap(470);
    root->Add(note, 0, wxEXPAND | wxLEFT | wxRIGHT | wxBOTTOM, 10);

    wxSizer* buttons = CreateStdDialogButtonSizer(wxOK | wxCANCEL);
    root->Add(buttons, 0, wxEXPAND | wxALL, 10);

    SetSizer(root);
    CentreOnParent();
}

void NewCaseDialog::OnOk(wxCommandEvent&)
{
    ProblemInstance candidate;
    candidate.name = std::string(m_name->GetValue().mb_str());
    if (candidate.name.empty())
        candidate.name = "Caso de conciliacion personalizado";

    candidate.description =
        "Creado manualmente en ReconcileLab 2006.";

    std::string error;
    if (!MoneyText::ParseCents(std::string(m_target->GetValue().mb_str()),
                               candidate.targetCents, error))
    {
        wxMessageBox(wxString(error.c_str(), wxConvLocal),
                     wxT("Monto a conciliar invalido"),
                     wxOK | wxICON_WARNING, this);
        m_target->SetFocus();
        return;
    }

    wxString raw = m_values->GetValue();
    wxStringTokenizer lines(raw, wxT("\n"), wxTOKEN_STRTOK);

    int sourceLine = 0;
    while (lines.HasMoreTokens())
    {
        ++sourceLine;
        wxString line = lines.GetNextToken();
        line.Trim(true);
        line.Trim(false);
        if (line.IsEmpty())
            continue;

        int cents = 0;
        std::string parseError;
        if (!MoneyText::ParseCents(std::string(line.mb_str()),
                                   cents, parseError))
        {
            wxString message;
            message.Printf(
                wxT("El movimiento candidato de la linea %d no es valido.\n\n%s"),
                sourceLine,
                wxString(parseError.c_str(), wxConvLocal).c_str());

            wxMessageBox(message, wxT("Movimiento candidato invalido"),
                         wxOK | wxICON_WARNING, this);
            m_values->SetFocus();
            return;
        }

        candidate.valuesCents.push_back(cents);
    }

    int objective = m_objective->GetSelection();
    if (objective == 1) candidate.objective = OBJECTIVE_ALL;
    else if (objective == 2) candidate.objective = OBJECTIVE_COUNT;
    else candidate.objective = OBJECTIVE_FIRST;

    if (!CaseFile::Validate(candidate, error))
    {
        wxMessageBox(wxString(error.c_str(), wxConvLocal),
                     wxT("No se puede crear el caso"),
                     wxOK | wxICON_WARNING, this);
        return;
    }

    m_problem = candidate;
    EndModal(wxID_OK);
}

ProblemInstance NewCaseDialog::GetProblem() const
{
    return m_problem;
}
