package co.edu.usta.ustudent.iam.api;

import co.edu.usta.ustudent.iam.api.dto.CurrentUserResponse;
import co.edu.usta.ustudent.iam.api.dto.LoginRequest;
import co.edu.usta.ustudent.iam.application.port.TokenIssuer;
import co.edu.usta.ustudent.iam.application.service.AuthService;
import co.edu.usta.ustudent.iam.application.service.CurrentUserService;
import co.edu.usta.ustudent.iam.infrastructure.security.AuthCookies;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autenticacion.
 *
 * <p>Los tokens no aparecen en el cuerpo de ninguna respuesta: viajan en
 * cookies {@code httpOnly}, fuera del alcance de JavaScript.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Inicio de sesion, renovacion y cierre")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final AuthCookies cookies;
    private final TokenIssuer tokens;

    public AuthController(AuthService authService, CurrentUserService currentUserService,
                          AuthCookies cookies, TokenIssuer tokens) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.cookies = cookies;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesion",
            description = "Devuelve el perfil y deja los tokens en cookies httpOnly.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesion iniciada"),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas o cuenta bloqueada"),
            @ApiResponse(responseCode = "400", description = "Datos incompletos")
    })
    public ResponseEntity<CurrentUserResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.IssuedTokens issued = authService.login(request.email(), request.password());
        return withCookies(issued, CurrentUserResponse.from(currentUserService.byEmail(request.email())));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renueva la sesion",
            description = "Rota el token de refresco. Reutilizar uno ya consumido cierra la sesion.")
    public ResponseEntity<CurrentUserResponse> refresh(HttpServletRequest request) {
        String refreshToken = cookies.readRefreshToken(request).orElse(null);
        AuthService.IssuedTokens issued = authService.refresh(refreshToken);
        return withCookies(issued, CurrentUserResponse.from(currentUserService.byAccessToken(issued.accessToken())));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cierra la sesion actual")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        cookies.readRefreshToken(request).ifPresent(authService::logout);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clearedAccessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookies.clearedRefreshCookie().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Perfil del usuario autenticado",
            description = "Incluye los permisos efectivos con los que el cliente arma su menu.")
    public CurrentUserResponse me(@AuthenticationPrincipal String userPublicId) {
        return CurrentUserResponse.from(currentUserService.byPublicId(userPublicId));
    }

    private ResponseEntity<CurrentUserResponse> withCookies(
            AuthService.IssuedTokens issued, CurrentUserResponse body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookies.accessCookie(issued.accessToken(), tokens.accessTokenTtl()).toString())
                .header(HttpHeaders.SET_COOKIE,
                        cookies.refreshCookie(issued.refreshToken(), tokens.refreshTokenTtl()).toString())
                .body(body);
    }
}
