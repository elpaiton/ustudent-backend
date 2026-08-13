package co.edu.usta.ustudent.shared.exception;

/**
 * Excepcion de negocio traducida a RFC 7807 por {@link GlobalExceptionHandler}.
 * El {@code detail} se muestra al usuario: debe explicar que paso y que hacer,
 * sin exponer informacion tecnica ni datos sensibles.
 */
public class ApiException extends RuntimeException {

    private final ErrorCode code;
    private final String detail;

    public ApiException(ErrorCode code, String detail) {
        super(detail);
        this.code = code;
        this.detail = detail;
    }

    public ApiException(ErrorCode code, String detail, Throwable cause) {
        super(detail, cause);
        this.code = code;
        this.detail = detail;
    }

    public static ApiException notFound(String resource) {
        return new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No encontramos " + resource + ".");
    }

    public static ApiException conflict(String detail) {
        return new ApiException(ErrorCode.RESOURCE_CONFLICT, detail);
    }

    public static ApiException businessRule(String detail) {
        return new ApiException(ErrorCode.BUSINESS_RULE_VIOLATED, detail);
    }

    public ErrorCode code() {
        return code;
    }

    public String detail() {
        return detail;
    }
}
