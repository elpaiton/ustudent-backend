package co.edu.usta.ustudent.iam.domain.repository;

import co.edu.usta.ustudent.iam.domain.model.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByJti(UUID jti);

    /** Corta la cadena completa cuando se detecta reutilizacion de un token. */
    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.family = :family and t.revoked = false")
    int revokeFamily(UUID family);

    /** Revoca todo lo vigente de un usuario: cierre de sesion, desactivacion o cambio de roles. */
    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user.id = :userId and t.revoked = false")
    int revokeAllForUser(Long userId);

    @Modifying
    @Query("delete from RefreshToken t where t.expiresAt < :before")
    int deleteExpiredBefore(Instant before);
}
