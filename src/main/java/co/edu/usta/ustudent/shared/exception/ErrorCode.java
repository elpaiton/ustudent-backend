package co.edu.usta.ustudent.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Catalogo de errores de negocio. Cada codigo se expone tal cual en el campo
 * {@code code} del cuerpo RFC 7807, de modo que el frontend pueda reaccionar a
 * un identificador estable en lugar de al texto del mensaje.
 */
public enum ErrorCode {

    VALIDATION_FAILED("Los datos enviados no son validos", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("Necesitas iniciar sesion", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("No tienes permiso para realizar esta accion", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND("El recurso solicitado no existe", HttpStatus.NOT_FOUND),
    RESOURCE_CONFLICT("El recurso ya existe o esta en un estado incompatible", HttpStatus.CONFLICT),
    CASE_INVALID_TRANSITION("Transicion de estado no permitida", HttpStatus.CONFLICT),
    BUSINESS_RULE_VIOLATED("La operacion incumple una regla de negocio", HttpStatus.UNPROCESSABLE_ENTITY),
    PAYLOAD_TOO_LARGE("El archivo supera el tamano permitido", HttpStatus.PAYLOAD_TOO_LARGE),
    RATE_LIMITED("Demasiadas peticiones, intenta de nuevo en un momento", HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR("Ocurrio un error inesperado", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String defaultTitle;
    private final HttpStatus status;

    ErrorCode(String defaultTitle, HttpStatus status) {
        this.defaultTitle = defaultTitle;
        this.status = status;
    }

    public String defaultTitle() {
        return defaultTitle;
    }

    public HttpStatus status() {
        return status;
    }
}
