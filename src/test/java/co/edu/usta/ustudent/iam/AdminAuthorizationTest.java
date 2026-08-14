package co.edu.usta.ustudent.iam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usta.ustudent.TestcontainersConfiguration;
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

/**
 * Autorizacion de la administracion, endpoint por endpoint.
 *
 * <p>Cada endpoint tiene aqui su caso de permiso insuficiente. Sin esta prueba
 * un endpoint no se considera terminado: es barata de escribir y es la unica
 * que detecta el fallo mas caro del sistema, que alguien vea lo que no debe.
 *
 * <p>{@code @WithMockUser(authorities = ...)} inyecta directamente los permisos,
 * de modo que se prueba la regla de autorizacion sin depender del inicio de
 * sesion ni de que exista tal usuario.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AdminAuthorizationTest {

    private static final UUID CUALQUIER_ID = UUID.fromString("018f2c00-0000-7000-8000-000000000000");

    @Autowired
    private MockMvc mockMvc;

    // ── Sin autenticar: 401, no 403 ──────────────────────────────────

    @Test
    @DisplayName("Sin sesion, la administracion responde 401")
    void sin_sesion() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    // ── Autenticado sin el permiso: 403 ──────────────────────────────

    @Test
    @WithMockUser(authorities = {"case:create:self", "mood:create:self"})
    @DisplayName("Un estudiante no puede listar usuarios")
    void estudiante_no_lista_usuarios() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("Leer usuarios no habilita a crearlos")
    void leer_no_es_gestionar() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nuevo@usta.edu.co",
                                  "documentNumber": "1234509876",
                                  "fullName": "Persona Nueva",
                                  "password": "unaContrasenaLarga",
                                  "roles": ["STUDENT"]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("Leer usuarios no habilita a cambiar su estado")
    void leer_no_habilita_cambiar_estado() throws Exception {
        mockMvc.perform(post("/api/v1/admin/users/" + CUALQUIER_ID + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INACTIVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("Leer usuarios no habilita a reasignar sus roles")
    void leer_no_habilita_asignar_roles() throws Exception {
        mockMvc.perform(put("/api/v1/admin/users/" + CUALQUIER_ID + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\": [\"ADMIN\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:manage")
    @DisplayName("Gestionar usuarios no habilita a tocar los roles del sistema")
    void gestionar_usuarios_no_es_gestionar_roles() throws Exception {
        mockMvc.perform(post("/api/v1/admin/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "ROL_INVENTADO",
                                  "name": "Rol inventado",
                                  "permissions": ["user:read"]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    @DisplayName("Leer roles no habilita a cambiar sus permisos")
    void leer_roles_no_habilita_asignar_permisos() throws Exception {
        mockMvc.perform(put("/api/v1/admin/roles/" + CUALQUIER_ID + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissions\": [\"case:read:any\"]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    @DisplayName("Leer roles no habilita a eliminarlos")
    void leer_roles_no_habilita_eliminar() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/roles/" + CUALQUIER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "user:manage")
    @DisplayName("Gestionar usuarios no habilita a ver el catalogo de permisos")
    void gestionar_usuarios_no_habilita_ver_permisos() throws Exception {
        mockMvc.perform(get("/api/v1/admin/permissions"))
                .andExpect(status().isForbidden());
    }

    // ── Con el permiso correcto: pasa la autorizacion ────────────────

    @Test
    @WithMockUser(authorities = "user:read")
    @DisplayName("Con user:read se puede listar")
    void con_permiso_lista() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "role:read")
    @DisplayName("Con role:read se ven roles y catalogo de permisos")
    void con_permiso_ve_roles() throws Exception {
        mockMvc.perform(get("/api/v1/admin/roles")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/permissions")).andExpect(status().isOk());
    }
}
