package com.marcosmoreiradev.reconcilelab.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseEvidenceTest {

    @TempDir
    Path tempDirectory;

    @Test
    void generaHashSha256YMetadata() throws Exception {
        Path artifact =
                tempDirectory.resolve("artifact.bin");

        Files.writeString(
                artifact,
                "ReconcileLab");

        Path output =
                tempDirectory.resolve("release-evidence.txt");

        ReleaseEvidence.write(
                output,
                List.of(artifact));

        String content =
                Files.readString(output);

        assertTrue(content.contains("product=ReconcileLab"));
        assertTrue(content.contains("vendor=Marcos Moreira Dev"));
        assertTrue(content.contains("sha256="));
        assertTrue(content.contains("bytes=12"));
    }
}
