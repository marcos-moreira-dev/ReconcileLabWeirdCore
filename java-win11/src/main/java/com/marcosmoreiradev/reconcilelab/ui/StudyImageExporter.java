package com.marcosmoreiradev.reconcilelab.ui;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;
import com.marcosmoreiradev.reconcilelab.ui.export.StudyPosterRenderer;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Fachada pública de exportación de imágenes de estudio.
 *
 * <p>Se conserva en el paquete {@code ui} para mantener estable el contrato de
 * llamadas y tests; la implementación vive en {@code ui.export}.</p>
 */
public final class StudyImageExporter {

    private StudyImageExporter() {
    }

    /**
     * Exporta una lámina PNG legible de la traza retenida.
     *
     * @param path destino
     * @param problem caso actual
     * @param strategy estrategia de búsqueda
     * @param snapshot snapshot a representar
     * @throws IOException si el archivo no puede escribirse
     */
    public static void export(
            Path path,
            ProblemInstance problem,
            SearchStrategy strategy,
            ExecutionSnapshot snapshot) throws IOException {

        new StudyPosterRenderer().export(path, problem, strategy, snapshot);
    }
}
