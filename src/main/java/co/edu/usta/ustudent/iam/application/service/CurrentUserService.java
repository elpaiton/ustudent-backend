package co.edu.usta.ustudent.iam.application.service;

import co.edu.usta.ustudent.iam.application.port.TokenIssuer;
import co.edu.usta.ustudent.iam.domain.model.User;
import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import co.edu.usta.ustudent.shared.exception.ApiException;
import co.edu.usta.ustudent.shared.exception.ErrorCode;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lecturas del perfil del usuario autenticado.
 *
 * <p>Devuelve el modelo, no un DTO: los DTO pertenecen a la capa web y la capa
 * de aplicacion no debe conocerla. El mapeo lo hace el controlador.
 */
@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository users;
    private final TokenIssuer tokens;

    public CurrentUserService(UserRepository users, TokenIssuer tokens) {
        this.users = users;
        this.tokens = tokens;
    }

    public User byPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHENTICATED, "Necesitas iniciar sesion.");
        }
        return users.findByPublicIdWithRoles(UUID.fromString(publicId))
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED, "Necesitas iniciar sesion."));
    }

    public User byEmail(String email) {
        return users.findByEmailWithRoles(email)
                .orElseThrow(() -> ApiException.notFound("el usuario"));
    }

    /** Usado tras renovar: el token recien emitido ya trae la identidad resuelta. */
    public User byAccessToken(String accessToken) {
        return tokens.verifyAccessToken(accessToken)
                .map(claims -> byPublicId(claims.userPublicId().toString()))
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHENTICATED, "Necesitas iniciar sesion."));
    }
}
