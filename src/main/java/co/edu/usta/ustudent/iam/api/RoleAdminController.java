package co.edu.usta.ustudent.iam.api;

import co.edu.usta.ustudent.iam.api.dto.RoleDtos;
import co.edu.usta.ustudent.iam.application.service.RoleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administracion de roles y de la matriz de permisos.
 */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Administracion · roles", description = "Roles, permisos y su asignacion")
public class RoleAdminController {

    private final RoleAdminService service;

    public RoleAdminController(RoleAdminService service) {
        this.service = service;
    }

    @GetMapping("/roles")
    @Operation(summary = "Lista los roles con sus permisos")
    public List<RoleDtos.Response> list() {
        return service.findAll().stream().map(RoleDtos.Response::from).toList();
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "Consulta un rol")
    public RoleDtos.Response detail(@PathVariable UUID id) {
        return RoleDtos.Response.from(service.byPublicId(id));
    }

    @PostMapping("/roles")
    @Operation(summary = "Crea un rol",
            description = "El rol creado nunca es de sistema, asi que se puede eliminar despues.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Creado"),
            @ApiResponse(responseCode = "403", description = "Sin permiso role:manage"),
            @ApiResponse(responseCode = "409", description = "El codigo ya existe")
    })
    public ResponseEntity<RoleDtos.Response> create(@Valid @RequestBody RoleDtos.Create request) {
        RoleDtos.Response created = RoleDtos.Response.from(service.create(
                request.code(), request.name(), request.description(), request.permissions()));
        return ResponseEntity.created(URI.create("/api/v1/admin/roles/" + created.id())).body(created);
    }

    @PatchMapping("/roles/{id}")
    @Operation(summary = "Actualiza nombre y descripcion")
    public RoleDtos.Response update(@PathVariable UUID id, @Valid @RequestBody RoleDtos.Update request) {
        return RoleDtos.Response.from(service.rename(id, request.name(), request.description()));
    }

    @PutMapping("/roles/{id}/permissions")
    @Operation(summary = "Reemplaza los permisos del rol",
            description = "Quien ya tenga sesion conserva sus permisos hasta que expire su token de acceso.")
    public RoleDtos.Response assignPermissions(
            @PathVariable UUID id, @Valid @RequestBody RoleDtos.AssignPermissions request) {
        return RoleDtos.Response.from(service.assignPermissions(id, request.permissions()));
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "Elimina un rol",
            description = "No se permite en roles predefinidos ni en roles con usuarios asignados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "409", description = "Tiene usuarios asignados"),
            @ApiResponse(responseCode = "422", description = "Es un rol del sistema")
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    @Operation(summary = "Catalogo de permisos",
            description = "Todos los permisos disponibles, para construir la matriz de la interfaz.")
    public List<RoleDtos.PermissionResponse> permissions() {
        return service.catalog().stream().map(RoleDtos.PermissionResponse::from).toList();
    }
}
