package com.marcosmoreiradev.reconcilelab.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * Genera evidencia reproducible de una ronda de release.
 *
 * <p>El archivo resultante no se versiona. Conserva versión, plataforma y
 * hashes SHA-256 de los artefactos que fueron realmente verificados.</p>
 */
public final class ReleaseEvidence {

    private ReleaseEvidence() {
    }

    /**
     * Entrada utilizada por {@code release-evidence.bat}.
     *
     * @param args primer argumento: salida; restantes: archivos a hash
     * @throws IOException si una ruta no puede leerse o escribirse
     */
    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 2) {
            throw new IllegalArgumentException(
                    "Uso: ReleaseEvidence <salida> <archivo> [archivo...]");
        }

        Path output =
                Path.of(args[0])
                        .toAbsolutePath()
                        .normalize();

        List<Path> files =
                java.util.Arrays.stream(args)
                        .skip(1)
                        .map(Path::of)
                        .map(Path::toAbsolutePath)
                        .map(Path::normalize)
                        .toList();

        write(
                output,
                files);
    }

    /**
     * Escribe metadata y hashes.
     *
     * @param output destino
     * @param files artefactos a identificar
     * @throws IOException ante errores de E/S
     */
    static void write(
            Path output,
            List<Path> files) throws IOException {

        StringBuilder text =
                new StringBuilder();

        text.append("ReconcileLab Java - release evidence\n")
                .append("====================================\n")
                .append("generated=")
                .append(OffsetDateTime.now())
                .append('\n')
                .append("product=")
                .append(AppMetadata.PRODUCT_NAME)
                .append('\n')
                .append("vendor=")
                .append(AppMetadata.VENDOR)
                .append('\n')
                .append("package=")
                .append(AppMetadata.PACKAGE_BASE)
                .append('\n')
                .append("version=")
                .append(BuildInfo.version())
                .append('\n')
                .append("java=")
                .append(System.getProperty("java.version"))
                .append('\n')
                .append("os=")
                .append(System.getProperty("os.name"))
                .append('\n')
                .append('\n');

        for (Path file : files) {
            if (!Files.isRegularFile(file)) {
                throw new IOException(
                        "No existe el artefacto: "
                                + file);
            }

            text.append("file=")
                    .append(file)
                    .append('\n')
                    .append("sha256=")
                    .append(sha256(file))
                    .append('\n')
                    .append("bytes=")
                    .append(Files.size(file))
                    .append("\n\n");
        }

        Path parent = output.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                output,
                text.toString(),
                StandardCharsets.UTF_8);
    }

    private static String sha256(Path file) throws IOException {
        MessageDigest digest;

        try {
            digest =
                    MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 no está disponible.",
                    exception);
        }

        try (InputStream input =
                     Files.newInputStream(file)) {

            byte[] buffer =
                    new byte[16 * 1024];

            int read;

            while ((read = input.read(buffer)) >= 0) {
                digest.update(
                        buffer,
                        0,
                        read);
            }
        }

        return HexFormat.of()
                .formatHex(
                        digest.digest());
    }
}
