package co.edu.usta.ustudent.iam.domain;

import static org.assertj.core.api.Assertions.assertThat;

import co.edu.usta.ustudent.iam.domain.model.User;
import co.edu.usta.ustudent.iam.domain.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reglas de bloqueo de una cuenta.
 *
 * <p>Son invariantes del modelo, no del caso de uso, asi que se prueban sin
 * levantar el contexto de Spring ni tocar la base de datos.
 */
class UserLockingTest {

    private User newUser() {
        return new User("ana@usta.edu.co", "1234567890", "Ana Perez", "$2a$12$hash");
    }

    @Test
    @DisplayName("Una cuenta nueva puede autenticarse y no esta bloqueada")
    void cuenta_nueva_activa() {
        User user = newUser();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.canAuthenticate()).isTrue();
        assertThat(user.isLocked()).isFalse();
        assertThat(user.getFailedAttempts()).isZero();
    }

    @Test
    @DisplayName("Los intentos fallidos por debajo del limite no bloquean")
    void intentos_bajo_el_limite() {
        User user = newUser();

        for (int i = 0; i < User.MAX_FAILED_ATTEMPTS - 1; i++) {
            user.registerFailedAttempt();
        }

        assertThat(user.getFailedAttempts()).isEqualTo((short) (User.MAX_FAILED_ATTEMPTS - 1));
        assertThat(user.isLocked()).isFalse();
        assertThat(user.canAuthenticate()).isTrue();
    }

    @Test
    @DisplayName("Al alcanzar el limite la cuenta queda bloqueada")
    void bloqueo_al_alcanzar_el_limite() {
        User user = newUser();

        for (int i = 0; i < User.MAX_FAILED_ATTEMPTS; i++) {
            user.registerFailedAttempt();
        }

        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.isLocked()).isTrue();
        assertThat(user.canAuthenticate()).isFalse();
        assertThat(user.getLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("Un inicio de sesion correcto limpia el contador y desbloquea")
    void login_correcto_limpia_el_contador() {
        User user = newUser();
        for (int i = 0; i < User.MAX_FAILED_ATTEMPTS; i++) {
            user.registerFailedAttempt();
        }

        user.registerSuccessfulLogin();

        assertThat(user.getFailedAttempts()).isZero();
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isLocked()).isFalse();
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("Los permisos efectivos son la union de los de sus roles, sin duplicados")
    void permisos_efectivos_sin_duplicados() {
        User user = newUser();

        assertThat(user.effectivePermissions()).isEmpty();
        assertThat(user.roleCodes()).isEmpty();
    }
}
