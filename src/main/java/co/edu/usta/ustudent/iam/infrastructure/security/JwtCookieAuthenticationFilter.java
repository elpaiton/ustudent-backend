package co.edu.usta.ustudent.iam.infrastructure.security;

import co.edu.usta.ustudent.iam.application.port.TokenIssuer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica cada peticion a partir del JWT de la cookie de acceso.
 *
 * <p>Los permisos del token se registran como {@code GrantedAuthority} tal
 * cual, con su formato {@code recurso:accion}. Por eso los servicios pueden
 * escribir {@code @PreAuthorize("hasAuthority('case:assign')")} sin traduccion
 * intermedia, y crear un rol nuevo no exige tocar codigo.
 *
 * <p>Tambien acepta {@code Authorization: Bearer} para clientes que no son
 * navegador, como las pruebas de integracion o herramientas de linea de
 * comandos.
 */
@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenIssuer tokens;
    private final AuthCookies cookies;

    public JwtCookieAuthenticationFilter(TokenIssuer tokens, AuthCookies cookies) {
        this.tokens = tokens;
        this.cookies = cookies;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        extractToken(request)
                .flatMap(tokens::verifyAccessToken)
                .ifPresent(claims -> {
                    List<SimpleGrantedAuthority> authorities = claims.permissions().stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
                    var authentication = new UsernamePasswordAuthenticationToken(
                            claims.userPublicId().toString(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    // Permite correlacionar en los logs quien hizo cada peticion.
                    MDC.put("userId", claims.userPublicId().toString());
                });

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("userId");
            SecurityContextHolder.clearContext();
        }
    }

    private java.util.Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return java.util.Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return cookies.readAccessToken(request);
    }
}
