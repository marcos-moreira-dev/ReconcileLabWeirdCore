package com.marcosmoreiradev.reconcilelab.io;

import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.domain.SearchObjective;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CaseFile {

    private CaseFile() {
    }

    public static ProblemInstance load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return load(reader);
        }
    }

    public static ProblemInstance load(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return load(reader);
        }
    }

    public static ProblemInstance load(Reader source) throws IOException {
        BufferedReader reader = source instanceof BufferedReader br
                ? br
                : new BufferedReader(source);

        String name = "";
        String description = "";
        int target = 0;
        SearchObjective objective = SearchObjective.FIRST;
        List<Integer> values = new ArrayList<>();

        String line;
        int lineNumber = 0;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int eq = line.indexOf('=');
            if (eq < 0) {
                throw new IllegalArgumentException(
                        "Línea " + lineNumber + " no contiene '='.");
            }

            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();

            switch (key) {
                case "name" -> name = value;
                case "description" -> description = value;
                case "target" -> target = parseNonNegativeInt(value, lineNumber, "monto objetivo");
                case "objective" -> objective = switch (value) {
                    case "first" -> SearchObjective.FIRST;
                    case "all" -> SearchObjective.ALL;
                    case "count" -> SearchObjective.COUNT;
                    default -> throw new IllegalArgumentException(
                            "Línea " + lineNumber + " contiene un objetivo desconocido: " + value);
                };
                case "values" -> {
                    for (String token : value.split(",")) {
                        token = token.trim();
                        if (!token.isEmpty()) {
                            values.add(parseNonNegativeInt(
                                    token, lineNumber, "monto candidato"));
                        }
                    }
                }
                default -> throw new IllegalArgumentException(
                        "Línea " + lineNumber + " contiene un campo desconocido: " + key);
            }
        }

        if (name.isBlank()) {
            name = "Caso de conciliación sin título";
        }

        ProblemInstance result = new ProblemInstance(
                name,
                description,
                values,
                target,
                objective);

        validate(result);
        return result;
    }

    public static void save(Path path, ProblemInstance problem) throws IOException {
        validate(problem);

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("# Archivo de caso de ReconcileLab\n");
            writer.write("# Los valores monetarios se almacenan como centavos enteros.\n");
            writer.write("name=" + problem.name() + "\n");
            writer.write("description=" + problem.description() + "\n");
            writer.write("target=" + problem.targetCents() + "\n");
            writer.write("objective=" + switch (problem.objective()) {
                case FIRST -> "first";
                case ALL -> "all";
                case COUNT -> "count";
            });
            writer.write("\nvalues=");

            for (int i = 0; i < problem.valuesCents().size(); i++) {
                if (i > 0) {
                    writer.write(",");
                }
                writer.write(Integer.toString(problem.valuesCents().get(i)));
            }

            writer.write("\n");
        }
    }

    public static void validate(ProblemInstance problem) {
        if (problem.targetCents() <= 0) {
            throw new IllegalArgumentException(
                    "El monto a conciliar debe ser mayor que cero.");
        }

        if (problem.valuesCents().isEmpty()) {
            throw new IllegalArgumentException(
                    "El caso no contiene movimientos candidatos.");
        }

        if (problem.valuesCents().size() > ProblemInstance.MAX_CANDIDATES) {
            throw new IllegalArgumentException(
                    "Esta versión admite como máximo "
                            + ProblemInstance.MAX_CANDIDATES
                            + " movimientos candidatos por caso.");
        }

        long total = 0;

        for (int value : problem.valuesCents()) {
            if (value <= 0) {
                throw new IllegalArgumentException(
                        "Los movimientos candidatos deben ser mayores que cero.");
            }

            total += value;
            if (total > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "La suma de los movimientos candidatos es demasiado grande.");
            }
        }
    }

    private static int parseNonNegativeInt(
            String text,
            int lineNumber,
            String field) {

        try {
            long value = Long.parseLong(text);
            if (value < 0 || value > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }
            return (int) value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Línea " + lineNumber + " contiene un " + field + " no válido: " + text);
        }
    }
}
