package co.edu.usta.ustudent.shared.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de sistema. Existen para verificar de punta a punta que el frontend
 * alcanza el backend: es el recorrido minimo que cierra la fase 0.
 */
@RestController
@RequestMapping("/api/v1/system")
@Tag(name = "Sistema", description = "Verificacion de conectividad y version")
public class SystemController {

    private final String applicationName;

    public SystemController(@Value("${spring.application.name}") String applicationName) {
        this.applicationName = applicationName;
    }

    @GetMapping("/ping")
    @Operation(summary = "Comprueba que la API responde",
            description = "Endpoint publico, sin autenticacion. No expone informacion del sistema.")
    public Map<String, String> ping() {
        return Map.of(
                "application", applicationName,
                "status", "ok",
                "timestamp", Instant.now().toString());
    }
}
