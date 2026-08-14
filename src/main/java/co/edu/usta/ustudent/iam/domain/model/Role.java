package co.edu.usta.ustudent.iam.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Conjunto nombrado de permisos, asignable a usuarios.
 *
 * <p>Los cinco roles predefinidos llevan {@code isSystem = true} y no pueden
 * eliminarse desde la aplicacion; el administrador si puede crear los suyos.
 */
@Entity
@Table(name = "iam_roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "iam_role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> permissions = new LinkedHashSet<>();

    protected Role() {
        // Requerido por JPA.
    }

    /**
     * Rol creado desde el panel de administracion.
     *
     * <p>Siempre con {@code system = false}: los cinco roles predefinidos se
     * siembran por migracion y no hay forma de que uno creado a mano se haga
     * pasar por uno de ellos para volverse indestructible.
     */
    public Role(String code, String name, String description, Set<Permission> permissions) {
        this.publicId = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.description = description;
        this.system = false;
        this.permissions = new LinkedHashSet<>(permissions);
    }

    public void rename(String newName, String newDescription) {
        this.name = newName;
        this.description = newDescription;
    }

    public void replacePermissions(Set<Permission> newPermissions) {
        permissions.clear();
        permissions.addAll(newPermissions);
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystem() {
        return system;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Role role)) {
            return false;
        }
        return code != null && code.equals(role.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
