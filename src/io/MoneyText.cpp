/**
 * Implementación del parser monetario.
 *
 * La prioridad es convertir texto decimal humano a centavos enteros sin
 * introducir punto flotante en el dominio.
 */
#include "MoneyText.h"
#include <cstdlib>
#include <sstream>
#include <iomanip>
#include <climits>

static std::string trimMoney(const std::string& s)
{
    const std::string ws = " \t\r\n";
    std::string::size_type a = s.find_first_not_of(ws);
    if (a == std::string::npos) return "";
    std::string::size_type b = s.find_last_not_of(ws);
    return s.substr(a, b - a + 1);
}

bool MoneyText::ParseCents(const std::string& text, int& cents, std::string& error)
{
    std::string s = trimMoney(text);
    if (s.empty())
    {
        error = "El monto esta vacio.";
        return false;
    }

    if (s[0] == '$')
        s = trimMoney(s.substr(1));

    if (s.empty() || s[0] == '-')
    {
        error = "El monto debe ser cero o mayor.";
        return false;
    }

    // Se acepta coma decimal solamente cuando no existe un punto.
    if (s.find('.') == std::string::npos)
    {
        std::string::size_type comma = s.find(',');
        if (comma != std::string::npos)
        {
            if (s.find(',', comma + 1) != std::string::npos)
            {
                error = "El monto contiene mas de un separador decimal.";
                return false;
            }
            s[comma] = '.';
        }
    }
    else if (s.find(',') != std::string::npos)
    {
        error = "No mezcles punto y coma como separadores decimales.";
        return false;
    }

    std::string wholePart = s;
    std::string fractionPart;

    std::string::size_type dot = s.find('.');
    if (dot != std::string::npos)
    {
        if (s.find('.', dot + 1) != std::string::npos)
        {
            error = "El monto contiene mas de un separador decimal.";
            return false;
        }
        wholePart = s.substr(0, dot);
        fractionPart = s.substr(dot + 1);
    }

    if (wholePart.empty())
        wholePart = "0";

    if (fractionPart.size() > 2)
    {
        error = "Usa como maximo dos decimales.";
        return false;
    }

    for (std::string::size_type i = 0; i < wholePart.size(); ++i)
    {
        if (wholePart[i] < '0' || wholePart[i] > '9')
        {
            error = "El monto contiene un caracter no valido.";
            return false;
        }
    }

    for (std::string::size_type i = 0; i < fractionPart.size(); ++i)
    {
        if (fractionPart[i] < '0' || fractionPart[i] > '9')
        {
            error = "La parte decimal del monto no es valida.";
            return false;
        }
    }

    char* end = 0;
    unsigned long whole = std::strtoul(wholePart.c_str(), &end, 10);
    if (end == wholePart.c_str() || *end != '\0')
    {
        error = "No se pudo interpretar el monto.";
        return false;
    }

    unsigned long fraction = 0;
    if (!fractionPart.empty())
    {
        fraction = (unsigned long)(fractionPart[0] - '0') * 10UL;
        if (fractionPart.size() == 2)
            fraction += (unsigned long)(fractionPart[1] - '0');
    }

    if (whole > (unsigned long)INT_MAX / 100UL)
    {
        error = "El monto es demasiado grande para esta version.";
        return false;
    }

    unsigned long total = whole * 100UL + fraction;
    if (total > (unsigned long)INT_MAX)
    {
        error = "El monto es demasiado grande para esta version.";
        return false;
    }

    cents = (int)total;
    return true;
}

std::string MoneyText::FormatCents(int cents)
{
    std::ostringstream out;
    out << "$" << (cents / 100) << "."
        << std::setw(2) << std::setfill('0') << (cents % 100);
    return out.str();
}
