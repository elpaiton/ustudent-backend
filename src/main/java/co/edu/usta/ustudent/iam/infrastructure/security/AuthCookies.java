package co.edu.usta.ustudent.iam.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Construye y lee las cookies de sesion.
 *
 * <p>{@code httpOnly} deja el token fuera del alcance de JavaScript, lo que
 * neutraliza el robo por XSS y permite que los Server Components de Next.js lo
 * lean. {@code SameSite=Lax} corta el grueso de los ataques CSRF.
 *
 * <p>La cookie de refresco se acota a su propia ruta: no viaja en cada peticion
 * a la API, solo cuando de verdad hace falta renovar.
 */
@Component
public class AuthCookies {

    public static final String ACCESS_COOKIE = "ustudent_access";
    public static final String REFRESH_COOKIE = "ustudent_refresh";

    private static final String REFRESH_PATH = "/api/v1/auth";

    private final boolean secure;

    public AuthCookies(@Value("${ustudent.security.cookies.secure:false}") boolean secure) {
        this.secure = secure;
    }

    public ResponseCookie accessCookie(String token, Duration ttl) {
        return build(ACCESS_COOKIE, token, "/", ttl);
    }

    public ResponseCookie refreshCookie(String token, Duration ttl) {
        return build(REFRESH_COOKIE, token, REFRESH_PATH, ttl);
    }

    /** Cookies vacias con vida cero: el navegador las borra. */
    public ResponseCookie clearedAccessCookie() {
        return build(ACCESS_COOKIE, "", "/", Duration.ZERO);
    }

    public ResponseCookie clearedRefreshCookie() {
        return build(REFRESH_COOKIE, "", REFRESH_PATH, Duration.ZERO);
    }

    public Optional<String> readAccessToken(HttpServletRequest request) {
        return read(request, ACCESS_COOKIE);
    }

    public Optional<String> readRefreshToken(HttpServletRequest request) {
        return read(request, REFRESH_COOKIE);
    }

    private ResponseCookie build(String name, String value, String path, Duration ttl) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(ttl)
                .build();
    }

    private Optional<String> read(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
