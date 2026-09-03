#ifndef RECONCILELAB_MONEY_TEXT_H
#define RECONCILELAB_MONEY_TEXT_H

#include <string>

/**
 * Conversión entre texto monetario visible y centavos enteros.
 *
 * El parser acepta `287.40`, `287,40` y `287`, además de espacios al inicio o
 * final. V1 rechaza montos negativos porque las reglas de poda del motor se
 * apoyan explícitamente en que todos los movimientos son positivos.
 */
class MoneyText
{
public:
    /**
     * Convierte un monto decimal a centavos exactos.
     *
     * @param text Texto introducido por el usuario.
     * @param cents Salida en centavos cuando el parseo es válido.
     * @param error Mensaje legible cuando el parseo falla.
     * @return true si la conversión fue válida.
     */
    static bool ParseCents(
        const std::string& text,
        int& cents,
        std::string& error);

    /**
     * Convierte centavos a una representación simple `$0.00`.
     *
     * No aplica reglas fiscales ni formatos regionales complejos; sirve para
     * la representación compacta de este laboratorio.
     */
    static std::string FormatCents(int cents);
};

#endif
