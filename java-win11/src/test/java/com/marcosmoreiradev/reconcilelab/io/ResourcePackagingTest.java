package com.marcosmoreiradev.reconcilelab.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResourcePackagingTest {

    @Test
    void recursosVisiblesDeLaAplicacionEstanEnElClasspath() {
        String[] resources = {
                "/css/reconcilelab.css",
                "/help/index.html",
                "/java-help/index.html",
                "/help/workspace.html",
                "/branding/reconcilelab-icon-32.png",
                "/branding/reconcilelab-icon-64.png",
                "/branding/reconcilelab-icon-128.png",
                "/examples/01-small-exact.case",
                "/examples/07-horizontal-compact.case"
        };

        for (String resource : resources) {
            assertNotNull(
                    ResourcePackagingTest.class.getResource(resource),
                    resource);
        }
    }
}
