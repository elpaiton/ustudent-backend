package co.edu.usta.ustudent.iam.domain.model;

/**
 * Estado de una cuenta.
 *
 * <p>Las cuentas no se borran: se desactivan. Un usuario con casos radicados
 * debe seguir siendo identificable en el expediente del estudiante y en la
 * bitacora de auditoria.
 */
public enum UserStatus {

    /** Puede iniciar sesion. */
    ACTIVE,

    /** Desactivada por un administrador. No puede entrar y sus tokens se revocan. */
    INACTIVE,

    /** Bloqueada temporalmente por intentos fallidos. Se libera sola al vencer. */
    LOCKED
}
