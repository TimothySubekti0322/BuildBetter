package com.buildbetter;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class BuildBetterBackendApplicationModularityTest {

    static ApplicationModules modules = ApplicationModules.of("com.buildbetter");

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }
}
