package com.medimitra.identityaccess;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/** Enforces the hexagonal package boundaries README section 3.1/9.2 require of every service. */
class LayeredArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.medimitra.identityaccess");

    @Test
    void domainHasNoFrameworkImports() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta.persistence..", "org.apache.kafka..");
        rule.check(CLASSES);
    }

    @Test
    void domainDoesNotDependOnOuterLayers() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..application..", "..infrastructure..", "..api..", "..config..");
        rule.check(CLASSES);
    }

    @Test
    void applicationDoesNotDependOnInfrastructureOrApi() {
        ArchRule rule = noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..", "..api..");
        rule.check(CLASSES);
    }

    @Test
    void onlyInfrastructureDependsOnJpa() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage("..infrastructure..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage("jakarta.persistence..");
        rule.check(CLASSES);
    }

    @Test
    void onlyInfrastructureImplementsApplicationPorts() {
        ArchRule rule = classes()
                .that()
                .resideInAPackage("..application.port..")
                .and()
                .areInterfaces()
                .should()
                .onlyHaveDependentClassesThat()
                .resideInAnyPackage("..application..", "..infrastructure..", "..config..");
        rule.check(CLASSES);
    }
}
