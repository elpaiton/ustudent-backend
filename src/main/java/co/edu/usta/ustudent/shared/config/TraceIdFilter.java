package co.edu.usta.ustudent.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Asigna un identificador de traza a cada peticion y lo publica en el MDC, de
 * modo que todas las lineas de log de esa peticion lo lleven. El mismo valor se
 * devuelve en el cuerpo de error RFC 7807 y en la cabecera {@code X-Trace-Id}:
 * un usuario puede reportar un problema citando ese codigo y el equipo encuentra
 * exactamente su peticion.
 */
// Antes que la cadena de Spring Security, que se registra en -100: si este
// filtro corriera despues, los errores de autenticacion y autorizacion saldrian
// sin traceId, que es justo cuando mas falta hace.
@Component
@Order(TraceIdFilter.ORDER)
public class TraceIdFilter extends OncePerRequestFilter {

    public static final int ORDER = -105;

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = UUID.randomUUID().toString().substring(0, 12);
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
