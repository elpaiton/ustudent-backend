package co.edu.usta.ustudent.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Fronteras del monolito modular (ADR-0001).
 *
 * <p>El aislamiento entre modulos no lo garantiza el compilador, asi que lo
 * garantiza este test: si falla, el build falla. Sin esta red, la separacion en
 * modulos se degrada a una convencion de nombres de carpeta.
 *
 * <p>Las reglas entre pares de modulos viven en {@link ModuleBoundaryTest}.
 */
@AnalyzeClasses(
        packages = "co.edu.usta.ustudent",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    /**
     * El dominio expresa reglas de negocio: no debe conocer el framework de
     * aplicacion.
     *
     * <p>Se permiten a proposito dos excepciones: {@code jakarta.persistence},
     * porque son metadatos declarativos sobre como se guarda una entidad, y
     * {@code org.springframework.data.repository}, que aporta las interfaces de
     * repositorio. Separar ambas cosas exigiria duplicar el modelo entero y
     * mapear entre las dos copias, coste que a esta escala no se paga solo.
     *
     * <p>Lo que sigue prohibido es lo que de verdad ata el dominio al
     * framework: web, contenedor de dependencias, seguridad y arranque. Con
     * eso, las reglas de negocio se prueban sin levantar contexto.
     */
    @ArchTest
    static final ArchRule el_dominio_no_depende_del_framework_de_aplicacion =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.context..",
                            "org.springframework.boot..",
                            "org.springframework.security..",
                            "org.springframework.http..")
                    .allowEmptyShould(true)
                    .because("las reglas de negocio deben poder probarse sin levantar el framework");

    /** La capa web orquesta, no consulta: pasa siempre por application. */
    @ArchTest
    static final ArchRule los_controladores_no_usan_repositorios =
            noClasses()
                    .that().resideInAPackage("..api..")
                    .should().dependOnClassesThat().resideInAPackage("..domain.repository..")
                    .allowEmptyShould(true)
                    .because("los controladores deben pasar por la capa de aplicacion");

    /** La regla de dependencia apunta hacia adentro. */
    @ArchTest
    static final ArchRule el_dominio_no_depende_de_la_infraestructura =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .allowEmptyShould(true)
                    .because("la infraestructura implementa el dominio, no al reves");

    @ArchTest
    static final ArchRule el_dominio_no_depende_de_la_capa_web =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..api..")
                    .allowEmptyShould(true);

    /** shared es transversal: puede usarlo todo el mundo, pero no conoce a nadie. */
    @ArchTest
    static final ArchRule shared_no_depende_de_los_modulos_de_dominio =
            noClasses()
                    .that().resideInAPackage("co.edu.usta.ustudent.shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "co.edu.usta.ustudent.iam..",
                            "co.edu.usta.ustudent.academic..",
                            "co.edu.usta.ustudent.cases..",
                            "co.edu.usta.ustudent.risk..",
                            "co.edu.usta.ustudent.ai..",
                            "co.edu.usta.ustudent.reporting..",
                            "co.edu.usta.ustudent.notification..")
                    .allowEmptyShould(true)
                    .because("shared es transversal: si conoce un modulo, deja de serlo");

    /**
     * Sin ciclos entre modulos. Los ciclos aparentes (cases pide clasificacion a
     * ai, ai devuelve el resultado a cases) se rompen con eventos de dominio.
     */
    @ArchTest
    static final ArchRule sin_ciclos_entre_modulos =
            slices()
                    .matching("co.edu.usta.ustudent.(*)..")
                    .should().beFreeOfCycles();
}
