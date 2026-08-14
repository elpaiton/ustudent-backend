package co.edu.usta.ustudent.iam.application.service;

import co.edu.usta.ustudent.iam.application.port.TokenIssuer;
import co.edu.usta.ustudent.iam.domain.model.RefreshToken;
import co.edu.usta.ustudent.iam.domain.model.User;
import co.edu.usta.ustudent.iam.domain.repository.RefreshTokenRepository;
import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import co.edu.usta.ustudent.shared.exception.ApiException;
import co.edu.usta.ustudent.shared.exception.ErrorCode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de autenticacion: entrar, renovar y salir.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * Mismo mensaje para usuario inexistente y contrasena incorrecta. Distinguir
     * ambos casos permitiria averiguar que correos existen en la plataforma.
     */
    private static final String INVALID_CREDENTIALS = "Correo o contrasena incorrectos.";

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokens;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder, TokenIssuer tokens) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    /** Par de tokens emitidos, listos para viajar en cookies httpOnly. */
    public record IssuedTokens(String accessToken, String refreshToken) {}

    @Transactional
    public IssuedTokens login(String email, String rawPassword) {
        Optional<User> found = users.findByEmailWithRoles(email);

        if (found.isEmpty()) {
            // Se comprueba igualmente el hash contra un valor ficticio para que
            // responder tarde lo mismo exista o no la cuenta: si no, el tiempo
            // de respuesta delata que correos estan registrados.
            passwordEncoder.matches(rawPassword, "$2a$12$0000000000000000000000000000000000000000000000000000");
            throw new ApiException(ErrorCode.UNAUTHENTICATED, INVALID_CREDENTIALS);
        }

        User user = found.get();

        if (user.isLocked()) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED,
                    "Tu cuenta esta bloqueada temporalmente por varios intentos fallidos. "
                            + "Intenta de nuevo en unos minutos.");
        }

        if (!user.canAuthenticate()) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED,
                    "Tu cuenta esta inactiva. Comunicate con la administracion de la plataforma.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            user.registerFailedAttempt();
            users.save(user);
            log.info("Intento fallido para el usuario {} ({} consecutivos)",
                    user.getPublicId(), user.getFailedAttempts());
            throw new ApiException(ErrorCode.UNAUTHENTICATED, INVALID_CREDENTIALS);
        }

        user.registerSuccessfulLogin();
        users.save(user);

        return issueFor(user, UUID.randomUUID());
    }

    /**
     * Rota el token de refresco.
     *
     * <p>Presentar uno ya consumido es la senal clasica de que alguien copio el
     * token: en ese caso no basta con rechazar la peticion, hay que invalidar
     * toda la cadena nacida de ese inicio de sesion, porque no se sabe quien de
     * los dos es el legitimo.
     */
    @Transactional
    public IssuedTokens refresh(String refreshTokenValue) {
        UUID jti = tokens.verifyRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED,
                        "Tu sesion expiro. Inicia sesion de nuevo."));

        RefreshToken stored = refreshTokens.findByJti(jti)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED,
                        "Tu sesion expiro. Inicia sesion de nuevo."));

        if (stored.isUsed() || stored.isRevoked()) {
            refreshTokens.revokeFamily(stored.getFamily());
            log.warn("Reutilizacion de token de refresco detectada para el usuario {}: "
                    + "se revoco la familia completa", stored.getUser().getPublicId());
            throw new ApiException(ErrorCode.UNAUTHENTICATED,
                    "Tu sesion se cerro por seguridad. Inicia sesion de nuevo.");
        }

        if (stored.isExpired()) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED,
                    "Tu sesion expiro. Inicia sesion de nuevo.");
        }

        stored.markUsed();
        refreshTokens.save(stored);

        User user = users.findByPublicIdWithRoles(stored.getUser().getPublicId())
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED, INVALID_CREDENTIALS));

        if (!user.canAuthenticate()) {
            refreshTokens.revokeAllForUser(user.getId());
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Tu cuenta ya no esta activa.");
        }

        // Se conserva la familia: sigue siendo la misma sesion.
        return issueFor(user, stored.getFamily());
    }

    /** Cierra la sesion actual revocando la cadena a la que pertenece el token. */
    @Transactional
    public void logout(String refreshTokenValue) {
        tokens.verifyRefreshToken(refreshTokenValue)
                .flatMap(refreshTokens::findByJti)
                .ifPresent(token -> refreshTokens.revokeFamily(token.getFamily()));
    }

    private IssuedTokens issueFor(User user, UUID family) {
        UUID jti = UUID.randomUUID();
        String refresh = tokens.issueRefreshToken(user, jti, family);
        Instant expiresAt = Instant.now().plus(tokens.refreshTokenTtl());
        refreshTokens.save(new RefreshToken(user, jti, family, expiresAt));
        return new IssuedTokens(tokens.issueAccessToken(user), refresh);
    }
}
