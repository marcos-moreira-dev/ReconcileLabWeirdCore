package com.marcosmoreiradev.reconcilelab.ui;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.engine.SearchEngine;
import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;
import com.marcosmoreiradev.reconcilelab.io.CaseFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StudyImageExporterTest {

    @Test
    void exportaUnaLaminaLegibleIndependienteDelViewport(
            @TempDir Path tempDir) throws Exception {

        ProblemInstance problem;

        try (InputStream input =
                     StudyImageExporterTest.class.getResourceAsStream(
                             "/examples/07-horizontal-compact.case")) {

            assertNotNull(input);
            problem = CaseFile.load(input);
        }

        SearchEngine engine =
                new SearchEngine();

        engine.reset(problem);
        engine.setStrategy(SearchStrategy.PRUNING);

        while (!engine.isComplete()) {
            engine.runBatch(1_000);
        }

        ExecutionSnapshot snapshot =
                engine.snapshot();

        Path png =
                tempDir.resolve("study.png");

        StudyImageExporter.export(
                png,
                problem,
                SearchStrategy.PRUNING,
                snapshot);

        assertTrue(java.nio.file.Files.size(png) > 10_000);

        BufferedImage image =
                ImageIO.read(png.toFile());

        assertNotNull(image);
        assertTrue(image.getWidth() >= 1_150);
        assertTrue(image.getHeight() >= 520);
    }


    @Test
    void exportaCasoDeEstresConTrazaRetenidaCompleta(
            @TempDir Path tempDir) throws Exception {

        ProblemInstance problem;

        try (InputStream input =
                     StudyImageExporterTest.class.getResourceAsStream(
                             "/examples/06-stress.case")) {

            assertNotNull(input);
            problem = CaseFile.load(input);
        }

        SearchEngine engine =
                new SearchEngine();

        engine.reset(problem);
        engine.setStrategy(SearchStrategy.PRUNING);

        while (!engine.isComplete()) {
            engine.runBatch(50_000);
        }

        ExecutionSnapshot snapshot =
                engine.snapshot();

        assertEquals(
                2_500,
                snapshot.nodes().size());

        Path png =
                tempDir.resolve(
                        "study-stress.png");

        StudyImageExporter.export(
                png,
                problem,
                SearchStrategy.PRUNING,
                snapshot);

        BufferedImage image =
                ImageIO.read(
                        png.toFile());

        assertNotNull(image);

        long pixels =
                (long) image.getWidth()
                        * image.getHeight();

        assertTrue(
                pixels <= 72_000_000L);

        assertTrue(
                image.getWidth() >= 1_150);

        assertTrue(
                java.nio.file.Files.size(png) > 50_000);
    }

    @Test
    void rechazaExportarSinTraza() throws Exception {
        ProblemInstance problem =
                new ProblemInstance(
                        "sin traza",
                        "",
                        java.util.List.of(100, 200),
                        300,
                        com.marcosmoreiradev.reconcilelab.domain.SearchObjective.ALL);

        SearchEngine engine =
                new SearchEngine();

        engine.reset(problem);

        IllegalStateException error =
                assertThrows(
                        IllegalStateException.class,
                        () -> StudyImageExporter.export(
                                Path.of("never-created.png"),
                                problem,
                                SearchStrategy.PRUNING,
                                engine.snapshot()));

        assertTrue(
                error.getMessage().contains("traza"));
    }
}
