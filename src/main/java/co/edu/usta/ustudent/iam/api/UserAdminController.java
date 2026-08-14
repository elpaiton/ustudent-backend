package co.edu.usta.ustudent.iam.api;

import co.edu.usta.ustudent.iam.api.dto.UserRequests;
import co.edu.usta.ustudent.iam.api.dto.UserResponse;
import co.edu.usta.ustudent.iam.application.service.UserAdminService;
import co.edu.usta.ustudent.shared.api.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administracion de usuarios.
 *
 * <p>Los permisos se exigen en el servicio de aplicacion, no aqui: asi la
 * regla se aplica venga la llamada de donde venga, y no solo por esta puerta.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Administracion · usuarios", description = "Alta, consulta, estado y roles")
public class UserAdminController {

    private final UserAdminService service;

    public UserAdminController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista usuarios",
            description = "Filtra por nombre, correo o documento con el parametro query.")
    public PageResponse<UserResponse> list(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
        return PageResponse.from(service.search(query, pageable), UserResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrado"),
            @ApiResponse(responseCode = "403", description = "Sin permiso user:read"),
            @ApiResponse(responseCode = "404", description = "No existe")
    })
    public UserResponse detail(@PathVariable UUID id) {
        return UserResponse.from(service.byPublicId(id));
    }

    @PostMapping
    @Operation(summary = "Crea un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "403", description = "Sin permiso user:manage"),
            @ApiResponse(responseCode = "409", description = "El correo o el documento ya existen")
    })
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequests.Create request) {
        UserResponse created = UserResponse.from(service.create(
                request.email(), request.documentNumber(), request.fullName(),
                request.password(), request.roles()));
        return ResponseEntity.created(URI.create("/api/v1/admin/users/" + created.id())).body(created);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Actualiza los datos basicos")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UserRequests.Update request) {
        return UserResponse.from(service.rename(id, request.fullName()));
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Activa o desactiva la cuenta",
            description = "Desactivar revoca las sesiones abiertas del usuario.")
    public UserResponse changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserRequests.ChangeStatus request,
            Authentication authentication) {
        // getName() y no @AuthenticationPrincipal String: el principal no
        // siempre es una cadena (depende de como se autentico la peticion), y
        // un cast fallido aqui daria un 500 donde deberia haber un 403.
        return UserResponse.from(service.changeStatus(id, request.status(), actorId(authentication)));
    }

    /** Vacio si el identificador no es un UUID: la regla de autoproteccion simplemente no aplica. */
    private UUID actorId(Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Reemplaza los roles del usuario",
            description = "Revoca sus sesiones para que el cambio surta efecto de inmediato.")
    public UserResponse assignRoles(@PathVariable UUID id, @Valid @RequestBody UserRequests.AssignRoles request) {
        return UserResponse.from(service.assignRoles(id, request.roles()));
    }
}
