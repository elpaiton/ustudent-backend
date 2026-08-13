package co.edu.usta.ustudent.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduce toda excepcion a una respuesta RFC 7807 ({@code application/problem+json}).
 *
 * <p>Dos reglas que no se negocian: al cliente nunca le llega una traza ni el
 * mensaje de una excepcion no controlada, y toda respuesta lleva el
 * {@code traceId} con el que el equipo puede localizar la peticion en los logs.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String ERROR_BASE_URI = "https://ustudent.usta.edu.co/errors/";

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException ex, HttpServletRequest request) {
        log.debug("Error de negocio [{}]: {}", ex.code(), ex.detail());
        return build(ex.code(), ex.detail(), request, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<Map<String, String>> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.add(Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage() == null ? "Valor no valido" : error.getDefaultMessage())));

        return build(ErrorCode.VALIDATION_FAILED,
                "Revisa los campos marcados.", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(ErrorCode.VALIDATION_FAILED,
                "El cuerpo de la peticion no tiene el formato esperado.", request, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(ErrorCode.UNAUTHENTICATED, ErrorCode.UNAUTHENTICATED.defaultTitle(), request, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(ErrorCode.ACCESS_DENIED, ErrorCode.ACCESS_DENIED.defaultTitle(), request, List.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return build(ErrorCode.PAYLOAD_TOO_LARGE,
                "El archivo supera el tamano permitido.", request, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {
        return build(ErrorCode.RESOURCE_NOT_FOUND, "La ruta solicitada no existe.", request, List.of());
    }

    /**
     * Red de seguridad. El detalle tecnico va al log con su traceId; al cliente
     * solo le llega un mensaje generico.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        String traceId = currentTraceId();
        log.error("Error no controlado [traceId={}] en {} {}",
                traceId, request.getMethod(), request.getRequestURI(), ex);
        return build(ErrorCode.INTERNAL_ERROR,
                "Ocurrio un error inesperado. Si vuelve a pasar, reporta el codigo " + traceId + ".",
                request, List.of());
    }

    private ProblemDetail build(ErrorCode code, String detail,
                                HttpServletRequest request, List<Map<String, String>> errors) {
        HttpStatus status = code.status();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(ERROR_BASE_URI + code.name().toLowerCase().replace('_', '-')));
        problem.setTitle(code.defaultTitle());
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code.name());
        problem.setProperty("traceId", currentTraceId());
        problem.setProperty("timestamp", Instant.now().toString());
        problem.setProperty("errors", errors);
        return problem;
    }

    private String currentTraceId() {
        String traceId = MDC.get("traceId");
        return traceId == null ? "sin-traza" : traceId;
    }
}
