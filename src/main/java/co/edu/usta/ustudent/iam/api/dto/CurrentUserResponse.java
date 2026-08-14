package co.edu.usta.ustudent.iam.api.dto;

import co.edu.usta.ustudent.iam.domain.model.User;
import java.util.List;
import java.util.UUID;

/**
 * Perfil del usuario autenticado.
 *
 * <p>El frontend arma su menu con {@code permissions}. Ocultar no es autorizar:
 * el servidor vuelve a comprobar el permiso en cada endpoint.
 */
public record CurrentUserResponse(
        UUID id,
        String email,
        String fullName,
        List<String> roles,
        List<String> permissions) {

    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(
                user.getPublicId(),
                user.getEmail(),
                user.getFullName(),
                List.copyOf(user.roleCodes()),
                List.copyOf(user.effectivePermissions()));
    }
}
