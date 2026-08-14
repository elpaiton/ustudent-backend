package co.edu.usta.ustudent.iam.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Capacidad atomica, con formato {@code recurso:accion} (por ejemplo
 * {@code case:assign}).
 *
 * <p>Los endpoints exigen permisos, nunca roles: asi el administrador puede
 * crear un rol nuevo desde el panel sin que nadie despliegue codigo
 * (ADR-0005). El catalogo lo siembra {@code R__seed_roles_permissions.sql}.
 */
@Entity
@Table(name = "iam_permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 30)
    private String resource;

    @Column(nullable = false, length = 30)
    private String action;

    @Column(length = 255)
    private String description;

    protected Permission() {
        // Requerido por JPA.
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Permission permission)) {
            return false;
        }
        return code != null && code.equals(permission.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
