package co.edu.usta.ustudent.iam.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cuenta de una persona en la plataforma.
 *
 * <p>Las reglas de bloqueo por intentos fallidos viven aqui y no en el
 * servicio: son invariantes de la cuenta, no del caso de uso que la consulta.
 */
@Entity
@Table(name = "iam_users")
public class User {

    /** Tras este numero de fallos consecutivos la cuenta se bloquea (RNF-S2). */
    public static final int MAX_FAILED_ATTEMPTS = 5;

    /** Duracion del bloqueo. Vencido, el siguiente intento vuelve a contar desde cero. */
    public static final int LOCK_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "document_number", nullable = false, unique = true, length = 20)
    private String documentNumber;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(name = "password_hash", nullable = false, length = 72)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "failed_attempts", nullable = false)
    private short failedAttempts;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "iam_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();

    protected User() {
        // Requerido por JPA.
    }

    public User(String email, String documentNumber, String fullName, String passwordHash) {
        this.email = email;
        this.documentNumber = documentNumber;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.status = UserStatus.ACTIVE;
    }

    @PrePersist
    void onPersist() {
        Instant now = Instant.now();
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    /**
     * Permisos efectivos: la union de los de todos sus roles.
     *
     * <p>Se calculan una vez al iniciar sesion y viajan dentro del token, para
     * que autorizar no cueste una consulta por peticion (ADR-0005).
     */
    public Set<String> effectivePermissions() {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Set<String> roleCodes() {
        return roles.stream().map(Role::getCode).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /** Una cuenta bloqueada se libera sola: no hace falta que nadie la desbloquee. */
    public boolean isLocked() {
        if (status == UserStatus.LOCKED) {
            return lockedUntil != null && lockedUntil.isAfter(Instant.now());
        }
        return false;
    }

    public boolean canAuthenticate() {
        return status == UserStatus.ACTIVE || (status == UserStatus.LOCKED && !isLocked());
    }

    /** Suma un intento fallido y bloquea la cuenta si se alcanzo el limite. */
    public void registerFailedAttempt() {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            status = UserStatus.LOCKED;
            lockedUntil = Instant.now().plusSeconds(LOCK_MINUTES * 60L);
        }
        updatedAt = Instant.now();
    }

    public void registerSuccessfulLogin() {
        failedAttempts = 0;
        lockedUntil = null;
        if (status == UserStatus.LOCKED) {
            status = UserStatus.ACTIVE;
        }
        lastLoginAt = Instant.now();
        updatedAt = lastLoginAt;
    }

    public void assignRole(Role role) {
        roles.add(role);
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getEmail() {
        return email;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public short getFailedAttempts() {
        return failedAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User user)) {
            return false;
        }
        return publicId != null && publicId.equals(user.publicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(publicId);
    }
}
