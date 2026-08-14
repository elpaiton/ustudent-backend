package co.edu.usta.ustudent.iam.application.service;

import co.edu.usta.ustudent.iam.domain.model.Role;
import co.edu.usta.ustudent.iam.domain.model.User;
import co.edu.usta.ustudent.iam.domain.model.UserStatus;
import co.edu.usta.ustudent.iam.domain.repository.RefreshTokenRepository;
import co.edu.usta.ustudent.iam.domain.repository.RoleRepository;
import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import co.edu.usta.ustudent.shared.exception.ApiException;
import co.edu.usta.ustudent.shared.exception.ErrorCode;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administracion de usuarios.
 *
 * <p>La autorizacion se declara por permiso atomico, nunca por rol: crear un
 * rol nuevo desde el panel no debe exigir tocar este codigo.
 */
@Service
public class UserAdminService {

    private static final Logger log = LoggerFactory.getLogger(UserAdminService.class);

    private final UserRepository users;
    private final RoleRepository roles;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;

    public UserAdminService(UserRepository users, RoleRepository roles,
                            RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.roles = roles;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
    }

    @PreAuthorize("hasAuthority('user:read')")
    @Transactional(readOnly = true)
    public Page<User> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return users.findAllBy(pageable);
        }
        return users.search(query.trim(), pageable);
    }

    @PreAuthorize("hasAuthority('user:read')")
    @Transactional(readOnly = true)
    public User byPublicId(UUID publicId) {
        return users.findByPublicIdWithRoles(publicId)
                .orElseThrow(() -> ApiException.notFound("ese usuario"));
    }

    @PreAuthorize("hasAuthority('user:manage')")
    @Transactional
    public User create(String email, String documentNumber, String fullName,
                       String rawPassword, Set<String> roleCodes) {

        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Ya existe una cuenta con ese correo.");
        }
        if (users.existsByDocumentNumber(documentNumber)) {
            throw ApiException.conflict("Ya existe una cuenta con ese numero de documento.");
        }

        User user = new User(email, documentNumber, fullName, passwordEncoder.encode(rawPassword));
        user.replaceRoles(resolveRoles(roleCodes));
        User saved = users.save(user);
        log.info("Usuario creado: {}", saved.getPublicId());
        return saved;
    }

    @PreAuthorize("hasAuthority('user:manage')")
    @Transactional
    public User rename(UUID publicId, String fullName) {
        User user = load(publicId);
        user.rename(fullName);
        return users.save(user);
    }

    /**
     * Cambia el estado de la cuenta.
     *
     * <p>Desactivar revoca las sesiones abiertas: si no, el usuario seguiria
     * entrando con el token que ya tenia hasta que venciera.
     */
    @PreAuthorize("hasAuthority('user:manage')")
    @Transactional
    public User changeStatus(UUID publicId, UserStatus status, UUID actorPublicId) {
        if (publicId.equals(actorPublicId) && status != UserStatus.ACTIVE) {
            // actorPublicId puede ser null si la peticion no vino de una sesion
            // normal; en ese caso la regla no aplica y equals() lo resuelve solo.
            // Sin esta regla, un administrador puede dejarse fuera de su propia
            // plataforma con un clic, y recuperarlo exige tocar la base de datos.
            throw ApiException.businessRule("No puedes desactivar tu propia cuenta.");
        }

        User user = load(publicId);
        switch (status) {
            case ACTIVE -> user.activate();
            case INACTIVE -> {
                user.deactivate();
                refreshTokens.revokeAllForUser(user.getId());
            }
            case LOCKED -> throw ApiException.businessRule(
                    "El bloqueo lo aplica el sistema tras varios intentos fallidos, no se asigna a mano.");
        }

        log.info("Estado del usuario {} cambiado a {}", publicId, status);
        return users.save(user);
    }

    /**
     * Reemplaza los roles del usuario.
     *
     * <p>Revoca sus sesiones: los permisos viajan dentro del token, asi que sin
     * revocar seguiria operando con los permisos viejos hasta que expirara.
     * Quitarle un permiso a alguien tiene que surtir efecto ya.
     */
    @PreAuthorize("hasAuthority('user:manage')")
    @Transactional
    public User assignRoles(UUID publicId, Set<String> roleCodes) {
        User user = load(publicId);
        user.replaceRoles(resolveRoles(roleCodes));
        refreshTokens.revokeAllForUser(user.getId());
        log.info("Roles del usuario {} actualizados a {}", publicId, roleCodes);
        return users.save(user);
    }

    private User load(UUID publicId) {
        return users.findByPublicIdWithRoles(publicId)
                .orElseThrow(() -> ApiException.notFound("ese usuario"));
    }

    private Set<Role> resolveRoles(Set<String> roleCodes) {
        Set<Role> resolved = new LinkedHashSet<>();
        for (String code : roleCodes) {
            resolved.add(roles.findByCode(code).orElseThrow(() ->
                    new ApiException(ErrorCode.VALIDATION_FAILED, "El rol " + code + " no existe.")));
        }
        return resolved;
    }
}
