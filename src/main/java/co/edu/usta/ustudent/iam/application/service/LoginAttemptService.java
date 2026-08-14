package co.edu.usta.ustudent.iam.application.service;

import co.edu.usta.ustudent.iam.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra los intentos fallidos de inicio de sesion.
 *
 * <p><strong>Esta clase existe por una razon concreta: no la fusiones con
 * {@link AuthService}.</strong> Un intento fallido termina lanzando una
 * excepcion, y una excepcion revierte la transaccion en curso — incluido el
 * incremento del contador. El resultado seria que el contador vuelve a cero en
 * cada intento y la cuenta no se bloquea nunca, que es exactamente el fallo que
 * detecto la prueba de integracion.
 *
 * <p>{@code REQUIRES_NEW} abre una transaccion propia que se confirma aunque la
 * de fuera se revierta. Y va en un bean aparte porque Spring aplica la
 * propagacion mediante un proxy: una llamada dentro de la misma clase no pasa
 * por el, y la anotacion no tendria efecto.
 */
@Service
public class LoginAttemptService {

    private final UserRepository users;

    public LoginAttemptService(UserRepository users) {
        this.users = users;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerFailedAttempt(Long userId) {
        users.findById(userId).ifPresent(user -> {
            user.registerFailedAttempt();
            users.save(user);
        });
    }
}
