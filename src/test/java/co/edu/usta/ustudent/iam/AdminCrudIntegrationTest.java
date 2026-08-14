package co.edu.usta.ustudent.iam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usta.ustudent.TestcontainersConfiguration;
import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/** Administracion de usuarios y roles contra PostgreSQL real. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@WithMockUser(authorities = {"user:read", "user:manage", "role:read", "role:manage"})
class AdminCrudIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository users;

    private String unicoCorreo() {
        return "prueba-" + UUID.randomUUID().toString().substring(0, 8) + "@usta.edu.co";
    }

    private String unicoDocumento() {
        return String.valueOf(System.nanoTime()).substring(0, 12);
    }

    @Test
    @DisplayName("Crear usuario, consultarlo y desactivarlo")
    void ciclo_de_vida_de_un_usuario() throws Exception {
        String correo = unicoCorreo();

        String creado = mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "documentNumber": "%s",
                                  "fullName": "Persona De Prueba",
                                  "password": "unaContrasenaLarga",
                                  "roles": ["TEACHER"]
                                }
                                """.formatted(correo, unicoDocumento())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(correo))
                .andExpect(jsonPath("$.roles[0]").value("TEACHER"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(creado, "$.id");

        mockMvc.perform(get("/api/v1/admin/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Persona De Prueba"));

        mockMvc.perform(post("/api/v1/admin/users/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INACTIVE\", \"reason\": \"fin de contrato\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("La respuesta de un usuario nunca incluye su contrasena")
    void la_respuesta_no_filtra_la_contrasena() throws Exception {
        String cuerpo = mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "documentNumber": "%s",
                                  "fullName": "Sin Filtracion",
                                  "password": "unaContrasenaLarga",
                                  "roles": ["STUDENT"]
                                }
                                """.formatted(unicoCorreo(), unicoDocumento())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(cuerpo.toLowerCase())
                .doesNotContain("password")
                .doesNotContain("contrasena")
                .doesNotContain("$2a$");
    }

    @Test
    @DisplayName("Un correo repetido responde 409, sin distinguir mayusculas")
    void correo_duplicado() throws Exception {
        String correo = unicoCorreo();
        String cuerpo = """
                {
                  "email": "%s",
                  "documentNumber": "%s",
                  "fullName": "Primera Cuenta",
                  "password": "unaContrasenaLarga",
                  "roles": ["STUDENT"]
                }
                """;

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo.formatted(correo, unicoDocumento())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo.formatted(correo.toUpperCase(), unicoDocumento())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));
    }

    @Test
    @DisplayName("Asignar un rol inexistente se rechaza con 400")
    void rol_inexistente() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "documentNumber": "%s",
                                  "fullName": "Rol Inventado",
                                  "password": "unaContrasenaLarga",
                                  "roles": ["NO_EXISTE"]
                                }
                                """.formatted(unicoCorreo(), unicoDocumento())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Una contrasena corta se rechaza antes de tocar la base de datos")
    void contrasena_corta() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "documentNumber": "%s",
                                  "fullName": "Clave Corta",
                                  "password": "corta",
                                  "roles": ["STUDENT"]
                                }
                                """.formatted(unicoCorreo(), unicoDocumento())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("Crear un rol, cambiarle los permisos y eliminarlo")
    void ciclo_de_vida_de_un_rol() throws Exception {
        String codigo = "PRUEBA_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase().replace("-", "");

        String creado = mockMvc.perform(post("/api/v1/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "%s",
                                  "name": "Rol de prueba",
                                  "description": "Creado por una prueba automatica",
                                  "permissions": ["case:read:own"]
                                }
                                """.formatted(codigo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.system").value(false))
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(creado, "$.id");

        mockMvc.perform(put("/api/v1/admin/roles/" + id + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissions\": [\"case:read:own\", \"mood:create:self\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions.length()").value(2));

        mockMvc.perform(delete("/api/v1/admin/roles/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Los roles predefinidos no se pueden eliminar")
    void los_roles_del_sistema_no_se_eliminan() throws Exception {
        String roles = mockMvc.perform(get("/api/v1/admin/roles"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        java.util.List<String> encontrados = JsonPath.read(roles, "$[?(@.code == 'ADMIN')].id");
        assertThat(encontrados).as("la semilla debe haber creado el rol ADMIN").hasSize(1);
        String adminId = encontrados.get(0);

        mockMvc.perform(delete("/api/v1/admin/roles/" + adminId))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATED"));
    }

    @Test
    @DisplayName("Un permiso inexistente al crear un rol se rechaza y lo nombra")
    void permiso_inexistente() throws Exception {
        mockMvc.perform(post("/api/v1/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "ROL_CON_PERMISO_FALSO",
                                  "name": "Rol invalido",
                                  "permissions": ["case:read:own", "permiso:inventado"]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("permiso:inventado")));
    }

    @Test
    @DisplayName("Cambiar los roles de un usuario revoca sus sesiones abiertas")
    void reasignar_roles_revoca_sesiones() throws Exception {
        String correo = unicoCorreo();
        String creado = mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "documentNumber": "%s",
                                  "fullName": "Cambio De Rol",
                                  "password": "unaContrasenaLarga",
                                  "roles": ["STUDENT"]
                                }
                                """.formatted(correo, unicoDocumento())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(creado, "$.id");

        mockMvc.perform(put("/api/v1/admin/users/" + id + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"TEACHER\", \"WELLBEING\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles.length()").value(2));

        assertThat(users.findByEmailWithRoles(correo)).isPresent();
    }
}
