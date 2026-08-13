package co.edu.usta.ustudent.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion OpenAPI. Es la fuente desde la que el frontend genera sus tipos
 * ({@code npm run generate:api}), asi que mantenerla fiel no es cosmetico: si el
 * contrato miente, el cliente compila contra algo que no existe.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ustudentOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("uStudent API")
                        .version("v1")
                        .description("""
                                Plataforma de promocion y permanencia estudiantil.

                                Gestiona solicitudes y reportes de bienestar, su clasificacion
                                asistida por IA y el indice de riesgo de desercion por estudiante.

                                La autorizacion se expresa por permisos atomicos con formato
                                `recurso:accion` (por ejemplo `case:assign`), nunca por rol.
                                """)
                        .contact(new Contact().name("Equipo uStudent"))
                        .license(new License().name("Uso institucional")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://ustudent.usta.edu.co").description("Produccion")))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("ustudent_access")
                                .description("JWT de acceso en cookie httpOnly (ver ADR-0005)")));
    }
}
