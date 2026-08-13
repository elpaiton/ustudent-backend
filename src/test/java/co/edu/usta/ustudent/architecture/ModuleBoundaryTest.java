package co.edu.usta.ustudent.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprueba, para cada par de modulos, que ninguno alcanza las tripas del otro.
 *
 * <p>Va aparte de {@link ArchitectureTest} porque necesita generar las reglas en
 * tiempo de ejecucion (una por par de modulos), y eso no cabe en un campo
 * estatico anotado con {@code @ArchTest}.
 */
class ModuleBoundaryTest {

    private static final String BASE = "co.edu.usta.ustudent";

    private static final String[] MODULES = {
            "iam", "academic", "cases", "risk", "ai", "reporting", "notification"
    };

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    @DisplayName("Ningun modulo accede a la infraestructura de otro")
    void ningun_modulo_accede_a_la_infraestructura_ajena() {
        forEachPair((from, to) -> noClasses()
                .that().resideInAPackage(BASE + "." + from + "..")
                .should().dependOnClassesThat().resideInAPackage(BASE + "." + to + ".infrastructure..")
                .allowEmptyShould(true)
                .because("la comunicacion entre modulos es por interfaz de aplicacion o por evento, "
                        + "nunca alcanzando la persistencia ajena"));
    }

    @Test
    @DisplayName("Ningun modulo accede al dominio interno de otro")
    void ningun_modulo_accede_al_dominio_ajeno() {
        forEachPair((from, to) -> noClasses()
                .that().resideInAPackage(BASE + "." + from + "..")
                .should().dependOnClassesThat().resideInAPackage(BASE + "." + to + ".domain..")
                .allowEmptyShould(true)
                .because("las entidades de un modulo son suyas: fuera se usan los DTO de su capa "
                        + "de aplicacion"));
    }

    private void forEachPair(RuleFactory factory) {
        for (String from : MODULES) {
            for (String to : MODULES) {
                if (from.equals(to)) {
                    continue;
                }
                factory.create(from, to).check(classes);
            }
        }
    }

    @FunctionalInterface
    private interface RuleFactory {
        ArchRule create(String fromModule, String toModule);
    }
}
