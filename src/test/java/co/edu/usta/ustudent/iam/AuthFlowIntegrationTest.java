package co.edu.usta.ustudent.iam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.usta.ustudent.TestcontainersConfiguration;
import co.edu.usta.ustudent.iam.domain.model.Role;
import co.edu.usta.ustudent.iam.domain.model.User;
import co.edu.usta.ustudent.iam.domain.repository.RoleRepository;
import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import co.edu.usta.ustudent.iam.infrastructure.security.AuthCookies;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Flujo de autenticacion contra PostgreSQL real.
 *
 * <p>Comprueba el recorrido completo: iniciar sesion deja las cookies, la
 * cookie autentica las siguientes peticiones, y sin ella la API responde 401.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

    private static final String EMAIL = "prueba.auth@usta.edu.co";
    private static final String PASSWORD = "Secreta123*";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository users;

    @Autowired
    private RoleRepository roles;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity())
                .build();

        users.findByEmailWithRoles(EMAIL).ifPresent(users::delete);

        Role admin = roles.findByCode("ADMIN").orElseThrow(
                () -> new IllegalStateException("La semilla de roles no se aplico"));
        User user = new User(EMAIL, "9999999999", "Usuario de prueba", passwordEncoder.encode(PASSWORD));
        user.assignRole(admin);
        users.save(user);
    }

    @Test
    @DisplayName("Login correcto devuelve el perfil con sus permisos y deja las cookies")
    void login_correcto() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.permissions").isNotEmpty())
                .andReturn();

        Cookie access = result.getResponse().getCookie(AuthCookies.ACCESS_COOKIE);
        Cookie refresh = result.getResponse().getCookie(AuthCookies.REFRESH_COOKIE);

        assertThat(access).isNotNull();
        assertThat(access.isHttpOnly()).as("el token no debe ser accesible desde JavaScript").isTrue();
        assertThat(refresh).isNotNull();
        assertThat(refresh.isHttpOnly()).isTrue();
        assertThat(refresh.getPath()).as("la cookie de refresco no viaja en cada peticion")
                .isEqualTo("/api/v1/auth");
    }

    @Test
    @DisplayName("El token del cuerpo nunca se expone en la respuesta")
    void el_token_no_viaja_en_el_cuerpo() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        String payload = result.getResponse().getContentAsString();
        assertThat(payload).doesNotContain("token").doesNotContain("eyJ");
    }

    @Test
    @DisplayName("La cookie de acceso autentica las siguientes peticiones")
    void me_con_cookie() throws Exception {
        Cookie access = login();

        mockMvc.perform(get("/api/v1/auth/me").cookie(access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.permissions[?(@ == 'user:manage')]").exists());
    }

    @Test
    @DisplayName("Sin cookie, la API responde 401 en formato RFC 7807")
    void me_sin_cookie() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    @DisplayName("La contrasena incorrecta responde 401 sin revelar si el correo existe")
    void password_incorrecta() throws Exception {
        String conocido = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, "otraCosa123*")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String desconocido = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("noexiste@usta.edu.co", "otraCosa123*")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat(conocido).contains("Correo o contrasena incorrectos");
        assertThat(desconocido)
                .as("la respuesta no debe permitir deducir que correos existen")
                .contains("Correo o contrasena incorrectos");
    }

    @Test
    @DisplayName("Tras cinco intentos fallidos la cuenta queda bloqueada")
    void bloqueo_por_intentos() throws Exception {
        for (int i = 0; i < User.MAX_FAILED_ATTEMPTS; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body(EMAIL, "incorrecta" + i)))
                    .andExpect(status().isUnauthorized());
        }

        // Incluso con la contrasena correcta: la cuenta esta bloqueada.
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.containsString("bloqueada")));
    }

    @Test
    @DisplayName("El correo sin formato valido se rechaza con 400 y detalle por campo")
    void validacion_de_entrada() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("no-es-un-correo", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    private Cookie login() throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(EMAIL, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getCookie(AuthCookies.ACCESS_COOKIE);
    }

    private String body(String email, String password) {
        return """
                {"email": "%s", "password": "%s"}
                """.formatted(email, password);
    }
}
