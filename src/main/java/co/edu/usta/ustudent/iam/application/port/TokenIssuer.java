package co.edu.usta.ustudent.iam.application.port;

import co.edu.usta.ustudent.iam.domain.model.User;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Emision y verificacion de los tokens de sesion.
 *
 * <p>La aplicacion depende de esta interfaz y no de la libreria concreta: si
 * algun dia la universidad ofrece SSO institucional, cambia el adaptador y el
 * resto del sistema no se entera, porque depende de permisos y no de como se
 * autentico la persona (ADR-0005).
 */
public interface TokenIssuer {

    /** Datos que el token transporta ya resueltos, para no consultar la base en cada peticion. */
    record AccessTokenClaims(UUID userPublicId, String email, Set<String> roles, Set<String> permissions) {}

    String issueAccessToken(User user);

    /** El {@code jti} identifica al token; el {@code family} agrupa la cadena de rotacion. */
    String issueRefreshToken(User user, UUID jti, UUID family);

    /** Vacio si el token es invalido, esta vencido o su firma no cuadra. */
    Optional<AccessTokenClaims> verifyAccessToken(String token);

    /** Devuelve el {@code jti} del token de refresco si es valido. */
    Optional<UUID> verifyRefreshToken(String token);

    Duration accessTokenTtl();

    Duration refreshTokenTtl();
}
