package com.marcosmoreiradev.reconcilelab.app;

import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.engine.SearchEngine;
import com.marcosmoreiradev.reconcilelab.io.CaseFile;
import com.marcosmoreiradev.reconcilelab.runtime.RuntimeController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamespaceContractTest {

    @Test
    void namespaceCanonicoEsMarcosMoreiraDev() {
        String expected = "com.marcosmoreiradev.reconcilelab";

        assertEquals(expected, AppMetadata.PACKAGE_BASE);
        assertEquals("com.marcosmoreiradev", AppMetadata.MAVEN_GROUP_ID);
        assertEquals("Marcos Moreira Dev", AppMetadata.VENDOR);

        assertTrue(ProblemInstance.class.getPackageName().startsWith(expected));
        assertTrue(SearchEngine.class.getPackageName().startsWith(expected));
        assertTrue(CaseFile.class.getPackageName().startsWith(expected));
        assertTrue(RuntimeController.class.getPackageName().startsWith(expected));
    }
}
