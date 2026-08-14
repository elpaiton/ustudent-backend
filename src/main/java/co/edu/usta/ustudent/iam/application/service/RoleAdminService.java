package co.edu.usta.ustudent.iam.application.service;

import co.edu.usta.ustudent.iam.domain.model.Permission;
import co.edu.usta.ustudent.iam.domain.model.Role;
import co.edu.usta.ustudent.iam.domain.repository.PermissionRepository;
import co.edu.usta.ustudent.iam.domain.repository.RoleRepository;
import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import co.edu.usta.ustudent.shared.exception.ApiException;
import co.edu.usta.ustudent.shared.exception.ErrorCode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Administracion de roles y de su matriz de permisos.
 *
 * <p>Es lo que permite que el administrador cree un rol nuevo —un coordinador
 * de facultad, por ejemplo— sin que nadie despliegue codigo: los endpoints
 * exigen permisos, y los permisos son datos.
 */
@Service
public class RoleAdminService {

    private static final Logger log = LoggerFactory.getLogger(RoleAdminService.class);

    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final UserRepository users;

    public RoleAdminService(RoleRepository roles, PermissionRepository permissions, UserRepository users) {
        this.roles = roles;
        this.permissions = permissions;
        this.users = users;
    }

    @PreAuthorize("hasAuthority('role:read')")
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roles.findAllWithPermissions();
    }

    @PreAuthorize("hasAuthority('role:read')")
    @Transactional(readOnly = true)
    public Role byPublicId(UUID publicId) {
        return roles.findByPublicIdWithPermissions(publicId)
                .orElseThrow(() -> ApiException.notFound("ese rol"));
    }

    @PreAuthorize("hasAuthority('role:read')")
    @Transactional(readOnly = true)
    public List<Permission> catalog() {
        return permissions.findAllByOrderByResourceAscActionAsc();
    }

    @PreAuthorize("hasAuthority('role:manage')")
    @Transactional
    public Role create(String code, String name, String description, Set<String> permissionCodes) {
        if (roles.findByCode(code).isPresent()) {
            throw ApiException.conflict("Ya existe un rol con el codigo " + code + ".");
        }
        Role role = new Role(code, name, description, resolvePermissions(permissionCodes));
        Role saved = roles.save(role);
        log.info("Rol creado: {} con {} permisos", code, permissionCodes.size());
        return saved;
    }

    @PreAuthorize("hasAuthority('role:manage')")
    @Transactional
    public Role rename(UUID publicId, String name, String description) {
        Role role = load(publicId);
        role.rename(name, description);
        return roles.save(role);
    }

    /**
     * Reemplaza los permisos del rol.
     *
     * <p>Se permite incluso en los roles del sistema: la institucion puede
     * decidir que su personal de bienestar necesita un permiso mas. Lo que no
     * se permite es borrarlos.
     *
     * <p>El cambio no alcanza a quien ya tiene sesion abierta hasta que su
     * token de acceso expire, porque los permisos viajan dentro. Con 30
     * minutos de vida, la ventana es acotada y conocida.
     */
    @PreAuthorize("hasAuthority('role:manage')")
    @Transactional
    public Role assignPermissions(UUID publicId, Set<String> permissionCodes) {
        Role role = load(publicId);
        role.replacePermissions(resolvePermissions(permissionCodes));
        log.info("Permisos del rol {} actualizados: {}", role.getCode(), permissionCodes.size());
        return roles.save(role);
    }

    @PreAuthorize("hasAuthority('role:manage')")
    @Transactional
    public void delete(UUID publicId) {
        Role role = load(publicId);

        if (role.isSystem()) {
            throw ApiException.businessRule(
                    "Los roles predefinidos no se pueden eliminar. Si no los usas, deja de asignarlos.");
        }

        long assigned = users.countByRoleCode(role.getCode());
        if (assigned > 0) {
            throw ApiException.conflict(
                    "No puedes eliminar este rol: lo tienen asignado " + assigned + " usuario(s). "
                            + "Reasignalos primero.");
        }

        roles.delete(role);
        log.info("Rol eliminado: {}", role.getCode());
    }

    private Role load(UUID publicId) {
        return roles.findByPublicIdWithPermissions(publicId)
                .orElseThrow(() -> ApiException.notFound("ese rol"));
    }

    /**
     * Traduce codigos a permisos, fallando si alguno no existe.
     *
     * <p>Aceptar en silencio un permiso inventado dejaria un rol que parece
     * conceder algo y no concede nada, y eso solo se descubre cuando alguien no
     * puede hacer su trabajo.
     */
    private Set<Permission> resolvePermissions(Set<String> codes) {
        if (codes.isEmpty()) {
            return Set.of();
        }
        List<Permission> found = permissions.findByCodeIn(codes);
        if (found.size() != codes.size()) {
            Set<String> encontrados = found.stream().map(Permission::getCode)
                    .collect(java.util.stream.Collectors.toSet());
            Set<String> faltantes = new LinkedHashSet<>(codes);
            faltantes.removeAll(encontrados);
            throw new ApiException(ErrorCode.VALIDATION_FAILED,
                    "Estos permisos no existen: " + String.join(", ", faltantes));
        }
        return new LinkedHashSet<>(found);
    }
}
