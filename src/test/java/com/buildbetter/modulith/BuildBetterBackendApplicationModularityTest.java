package com.buildbetter.modulith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import com.buildbetter.BuildBetterBackendApplication;

/**
 * Verifies that the BuildBetter code base fulfils all Spring Modulith rules:
 * • no cyclic dependencies between modules
 * • only API packages are referenced from the outside
 * • no field injection, etc.
 *
 * If any rule is violated the test fails with an
 * {@code org.springframework.modulith.core.Violations} report.
 */
class BuildBetterBackendApplicationModularityTest {

    /** Scan is anchored at the main Spring Boot application class. */
    private static final ApplicationModules modules = ApplicationModules.of(BuildBetterBackendApplication.class);

    @Test
    @DisplayName("All modules satisfy Spring Modulith constraints")
    void allModulesSatisfyConstraints() {
        modules.verify(); // throws on the first violation
    }

    @Test
    @DisplayName("✍️ write PlantUML + canvas docs to target/spring-modulith-docs")
    void generateModuleDocumentation() {

        new Documenter(modules)
                .writeModulesAsPlantUml() // C4 overview diagram
                .writeIndividualModulesAsPlantUml()// per‑module diagrams
                .writeModuleCanvases(); // tabular canvases

        // output folder: target/spring-modulith-docs
    }
}
