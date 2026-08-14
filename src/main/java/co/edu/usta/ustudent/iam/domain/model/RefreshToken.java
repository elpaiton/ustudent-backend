package co.edu.usta.ustudent.iam.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Token de refresco emitido a un usuario.
 *
 * <p>Rotan: cada uso emite uno nuevo y marca el anterior como consumido. Todos
 * los tokens nacidos de un mismo inicio de sesion comparten {@code family}, de
 * modo que reutilizar uno ya consumido —senal clasica de robo de token—
 * permite invalidar la cadena entera de una vez (ADR-0005).
 */
@Entity
@Table(name = "iam_refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID jti;

    @Column(nullable = false, updatable = false)
    private UUID family;

    @Column(nullable = false)
    private boolean revoked;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RefreshToken() {
        // Requerido por JPA.
    }

    public RefreshToken(User user, UUID jti, UUID family, Instant expiresAt) {
        this.user = user;
        this.jti = jti;
        this.family = family;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onPersist() {
        createdAt = Instant.now();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    /** Utilizable solo una vez, y solo mientras no este revocado ni vencido. */
    public boolean isUsable() {
        return !revoked && !isUsed() && !isExpired();
    }

    public void markUsed() {
        usedAt = Instant.now();
    }

    public void revoke() {
        revoked = true;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UUID getJti() {
        return jti;
    }

    public UUID getFamily() {
        return family;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
