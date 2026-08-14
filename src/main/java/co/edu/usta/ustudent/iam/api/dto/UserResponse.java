package co.edu.usta.ustudent.iam.api.dto;

import co.edu.usta.ustudent.iam.domain.model.User;
import co.edu.usta.ustudent.iam.domain.model.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Usuario visto por la administracion.
 *
 * <p>No incluye el hash de la contrasena ni el contador de intentos fallidos:
 * lo primero no debe salir jamas del servidor, y lo segundo solo interesa
 * traducido a un estado ({@code LOCKED}) que si se muestra.
 */
public record UserResponse(
        UUID id,
        String email,
        String documentNumber,
        String fullName,
        UserStatus status,
        boolean locked,
        List<String> roles,
        Instant lastLoginAt) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getDocumentNumber(),
                user.getFullName(),
                user.getStatus(),
                user.isLocked(),
                List.copyOf(user.roleCodes()),
                user.getLastLoginAt());
    }
}
