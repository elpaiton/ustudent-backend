package co.edu.usta.ustudent.iam.api.dto;

import co.edu.usta.ustudent.iam.domain.model.Permission;
import co.edu.usta.ustudent.iam.domain.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Cuerpos de peticion y respuesta de la administracion de roles. */
public final class RoleDtos {

    private RoleDtos() {
    }

    public record Create(

            @NotBlank(message = "El codigo es obligatorio")
            @Pattern(regexp = "[A-Z][A-Z0-9_]{2,39}",
                    message = "El codigo debe ir en mayusculas, sin espacios (por ejemplo COORDINADOR_FACULTAD)")
            String code,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80)
            String name,

            @Size(max = 255)
            String description,

            @NotNull(message = "Indica los permisos del rol")
            Set<String> permissions) {
    }

    public record Update(

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 80)
            String name,

            @Size(max = 255)
            String description) {
    }

    public record AssignPermissions(

            @NotNull(message = "Indica los permisos del rol")
            Set<String> permissions) {
    }

    public record Response(
            UUID id,
            String code,
            String name,
            String description,
            boolean system,
            List<String> permissions) {

        public static Response from(Role role) {
            return new Response(
                    role.getPublicId(),
                    role.getCode(),
                    role.getName(),
                    role.getDescription(),
                    role.isSystem(),
                    role.getPermissions().stream().map(Permission::getCode).sorted().toList());
        }
    }

    /** Catalogo de permisos, agrupable por recurso en la interfaz. */
    public record PermissionResponse(String code, String resource, String action, String description) {

        public static PermissionResponse from(Permission permission) {
            return new PermissionResponse(
                    permission.getCode(),
                    permission.getResource(),
                    permission.getAction(),
                    permission.getDescription());
        }
    }
}
