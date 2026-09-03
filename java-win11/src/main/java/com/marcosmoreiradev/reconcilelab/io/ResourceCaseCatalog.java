package com.marcosmoreiradev.reconcilelab.io;

import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ResourceCaseCatalog {

    public static final List<String> PRESET_FILES = List.of(
            "01-small-exact.case",
            "02-multiple-solutions.case",
            "03-no-solution.case",
            "04-pruning-showcase.case",
            "05-medium-reconciliation.case",
            "06-stress.case",
            "07-horizontal-compact.case");

    public List<PresetCase> loadAll() throws IOException {
        List<PresetCase> result = new ArrayList<>();

        for (String file : PRESET_FILES) {
            String resource = "/examples/" + file;

            try (InputStream input = ResourceCaseCatalog.class.getResourceAsStream(resource)) {
                if (input == null) {
                    throw new IOException("No se encontró el recurso " + resource);
                }

                ProblemInstance problem = CaseFile.load(input);
                result.add(new PresetCase(file, problem));
            }
        }

        return List.copyOf(result);
    }

    public record PresetCase(String fileName, ProblemInstance problem) {
        @Override
        public String toString() {
            return problem.name();
        }
    }
}
