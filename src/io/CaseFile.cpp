/**
 * Implementación del formato `.case`.
 *
 * Este archivo concentra parseo, validación y escritura para que GUI, tests y
 * ejemplos compartan exactamente las mismas reglas.
 */
#include "CaseFile.h"
#include <fstream>
#include <sstream>
#include <cstdlib>
#include <climits>

static std::string trimCase(const std::string& s)
{
    const std::string ws = " \t\r\n";
    std::string::size_type a = s.find_first_not_of(ws);
    if (a == std::string::npos) return "";
    std::string::size_type b = s.find_last_not_of(ws);
    return s.substr(a, b - a + 1);
}

static bool parseCaseInt(const std::string& s, int& value)
{
    char* end = 0;
    long v = std::strtol(s.c_str(), &end, 10);
    if (end == s.c_str() || *end != '\0') return false;
    if (v < 0 || v > INT_MAX) return false;
    value = (int)v;
    return true;
}

bool CaseFile::Validate(const ProblemInstance& problem, std::string& error)
{
    if (problem.targetCents <= 0)
    {
        error = "El monto a conciliar debe ser mayor que cero.";
        return false;
    }

    if (problem.valuesCents.empty())
    {
        error = "El caso no contiene movimientos candidatos.";
        return false;
    }

    // La mascara compacta de seleccion deja dos bits altos libres y mantiene
    // el modelo dentro de la escala pequena/mediana prevista para el laboratorio.
    if (problem.valuesCents.size() > 30)
    {
        error = "Esta version admite como maximo 30 movimientos candidatos por caso.";
        return false;
    }

    unsigned long total = 0;

    for (std::vector<int>::size_type i = 0; i < problem.valuesCents.size(); ++i)
    {
        if (problem.valuesCents[i] <= 0)
        {
            error = "Los movimientos candidatos deben ser mayores que cero.";
            return false;
        }

        total += (unsigned long)problem.valuesCents[i];
        if (total > (unsigned long)INT_MAX)
        {
            error = "La suma de los movimientos candidatos es demasiado grande para esta version.";
            return false;
        }
    }

    return true;
}

bool CaseFile::Load(const std::string& path, ProblemInstance& out, std::string& error)
{
    std::ifstream in(path.c_str());
    if (!in)
    {
        error = "No se pudo abrir el archivo de caso: " + path;
        return false;
    }

    ProblemInstance tmp;
    std::string line;
    int lineNumber = 0;

    while (std::getline(in, line))
    {
        ++lineNumber;
        line = trimCase(line);
        if (line.empty() || line[0] == '#')
            continue;

        std::string::size_type eq = line.find('=');
        if (eq == std::string::npos)
        {
            std::ostringstream msg;
            msg << "Linea " << lineNumber << " no contiene '='.";
            error = msg.str();
            return false;
        }

        std::string key = trimCase(line.substr(0, eq));
        std::string value = trimCase(line.substr(eq + 1));

        if (key == "name")
        {
            tmp.name = value;
        }
        else if (key == "description")
        {
            tmp.description = value;
        }
        else if (key == "target")
        {
            if (!parseCaseInt(value, tmp.targetCents))
            {
                std::ostringstream msg;
                msg << "Linea " << lineNumber << " contiene un monto objetivo no valido.";
                error = msg.str();
                return false;
            }
        }
        else if (key == "objective")
        {
            if (value == "first") tmp.objective = OBJECTIVE_FIRST;
            else if (value == "all") tmp.objective = OBJECTIVE_ALL;
            else if (value == "count") tmp.objective = OBJECTIVE_COUNT;
            else
            {
                std::ostringstream msg;
                msg << "Linea " << lineNumber << " contiene un objetivo desconocido: " << value;
                error = msg.str();
                return false;
            }
        }
        else if (key == "values")
        {
            std::stringstream ss(value);
            std::string token;
            while (std::getline(ss, token, ','))
            {
                token = trimCase(token);
                if (token.empty()) continue;

                int cents = 0;
                if (!parseCaseInt(token, cents))
                {
                    std::ostringstream msg;
                    msg << "Linea " << lineNumber
                        << " contiene un monto candidato no valido: " << token;
                    error = msg.str();
                    return false;
                }
                tmp.valuesCents.push_back(cents);
            }
        }
        else
        {
            std::ostringstream msg;
            msg << "Linea " << lineNumber << " contiene un campo desconocido: " << key;
            error = msg.str();
            return false;
        }
    }

    if (tmp.name.empty())
        tmp.name = "Caso de conciliacion sin titulo";

    if (!Validate(tmp, error))
        return false;

    out = tmp;
    return true;
}

bool CaseFile::Save(const std::string& path, const ProblemInstance& problem, std::string& error)
{
    if (!Validate(problem, error))
        return false;

    std::ofstream out(path.c_str(), std::ios::out | std::ios::trunc);
    if (!out)
    {
        error = "No se pudo crear el archivo de caso: " + path;
        return false;
    }

    out << "# Archivo de caso de ReconcileLab 2006\n";
    out << "# Los valores monetarios se almacenan como centavos enteros.\n";
    out << "name=" << problem.name << "\n";
    out << "description=" << problem.description << "\n";
    out << "target=" << problem.targetCents << "\n";

    if (problem.objective == OBJECTIVE_ALL) out << "objective=all\n";
    else if (problem.objective == OBJECTIVE_COUNT) out << "objective=count\n";
    else out << "objective=first\n";

    out << "values=";
    for (std::vector<int>::size_type i = 0; i < problem.valuesCents.size(); ++i)
    {
        if (i > 0) out << ",";
        out << problem.valuesCents[i];
    }
    out << "\n";

    if (!out.good())
    {
        error = "Ocurrio un error al escribir el archivo de caso.";
        return false;
    }

    return true;
}
