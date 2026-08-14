package co.edu.usta.ustudent.iam.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "Escribe tu correo institucional")
        @Email(message = "El correo no tiene un formato valido")
        String email,

        @NotBlank(message = "Escribe tu contrasena")
        @Size(max = 128, message = "La contrasena es demasiado larga")
        String password) {
}
