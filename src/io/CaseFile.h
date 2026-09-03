#ifndef RECONCILELAB_CASE_FILE_H
#define RECONCILELAB_CASE_FILE_H

#include "../model/ProblemInstance.h"
#include <string>

/**
 * Lector, escritor y validador del formato de casos `.case`.
 *
 * Los ejemplos del repositorio y los casos creados por el usuario comparten
 * exactamente el mismo formato. No existe un segundo sistema de presets
 * escondido dentro del ejecutable.
 */
class CaseFile
{
public:
    /**
     * Lee un archivo y aplica inmediatamente las restricciones de V1.
     *
     * @param path Ruta del archivo.
     * @param out Caso resultante cuando todo es válido.
     * @param error Mensaje explicativo en caso de fallo.
     */
    static bool Load(
        const std::string& path,
        ProblemInstance& out,
        std::string& error);

    /**
     * Guarda un caso usando centavos enteros y un formato determinista.
     *
     * El resultado está pensado para poder inspeccionarse cómodamente con
     * Notepad++ u otro editor de texto.
     */
    static bool Save(
        const std::string& path,
        const ProblemInstance& problem,
        std::string& error);

    /**
     * Verifica restricciones compartidas por archivos y por el diálogo Nuevo.
     *
     * Mantener la validación en un único punto evita que un caso admitido por
     * la GUI sea rechazado luego por el motor.
     */
    static bool Validate(
        const ProblemInstance& problem,
        std::string& error);
};

#endif
