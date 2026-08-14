package co.edu.usta.ustudent.iam.api.dto;

import co.edu.usta.ustudent.iam.domain.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

/** Cuerpos de peticion de la administracion de usuarios. */
public final class UserRequests {

    private UserRequests() {
    }

    public record Create(

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo no tiene un formato valido")
            @Size(max = 160)
            String email,

            @NotBlank(message = "El numero de documento es obligatorio")
            @Pattern(regexp = "\\d{5,20}", message = "El documento debe tener entre 5 y 20 digitos")
            String documentNumber,

            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 160)
            String fullName,

            /**
             * Minimo 10 caracteres. La longitud protege mucho mas que exigir
             * simbolos raros, que solo consigue que la gente apunte la
             * contrasena en un papel.
             */
            @NotBlank(message = "La contrasena es obligatoria")
            @Size(min = 10, max = 128, message = "La contrasena debe tener al menos 10 caracteres")
            String password,

            @NotEmpty(message = "Asigna al menos un rol")
            Set<String> roles) {
    }

    public record Update(

            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 160)
            String fullName) {
    }

    public record ChangeStatus(

            @NotNull(message = "Indica el nuevo estado")
            UserStatus status,

            @Size(max = 255)
            String reason) {
    }

    public record AssignRoles(

            @NotEmpty(message = "Asigna al menos un rol")
            Set<String> roles) {
    }
}
