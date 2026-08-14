package co.edu.usta.ustudent.iam.infrastructure;

import co.edu.usta.ustudent.iam.domain.model.Role;
import co.edu.usta.ustudent.iam.domain.model.User;
import co.edu.usta.ustudent.iam.domain.repository.RoleRepository;
import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Crea las cuentas de prueba en desarrollo.
 *
 * <p>Solo se activa con los perfiles {@code local} y {@code dev}: en produccion
 * este bean no existe, asi que no hay forma de que una cuenta con contrasena
 * conocida llegue alli por descuido. Las cuentas reales se crean desde el panel
 * de administracion.
 */
@Configuration
@Profile({"local", "dev"})
public class DevUserInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevUserInitializer.class);

    @Bean
    ApplicationRunner seedDevUsers(
            UserRepository users,
            RoleRepository roles,
            PasswordEncoder passwordEncoder,
            @Value("${ustudent.dev.admin-password:Admin123*}") String adminPassword) {

        return args -> {
            create(users, roles, passwordEncoder, "admin@usta.edu.co", "1000000001",
                    "Administrador de prueba", "ADMIN", adminPassword);
            create(users, roles, passwordEncoder, "docente@usta.edu.co", "1000000002",
                    "Docente de prueba", "TEACHER", adminPassword);
            create(users, roles, passwordEncoder, "estudiante@usta.edu.co", "1000000003",
                    "Estudiante de prueba", "STUDENT", adminPassword);
            create(users, roles, passwordEncoder, "bienestar@usta.edu.co", "1000000004",
                    "Profesional de bienestar", "WELLBEING", adminPassword);

            log.warn("Cuentas de desarrollo listas. Contrasena comun: {}. "
                    + "Este inicializador no se activa en produccion.", adminPassword);
        };
    }

    private void create(UserRepository users, RoleRepository roles, PasswordEncoder encoder,
                        String email, String document, String fullName, String roleCode, String password) {
        if (users.existsByEmailIgnoreCase(email)) {
            return;
        }
        Optional<Role> role = roles.findByCode(roleCode);
        if (role.isEmpty()) {
            log.error("No existe el rol {}: revisa la semilla R__seed_roles_permissions.sql", roleCode);
            return;
        }
        User user = new User(email, document, fullName, encoder.encode(password));
        user.assignRole(role.get());
        users.save(user);
        log.info("Usuario de desarrollo creado: {} ({})", email, roleCode);
    }
}
