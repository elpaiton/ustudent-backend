package co.edu.usta.ustudent.iam.domain.repository;

import co.edu.usta.ustudent.iam.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Carga el usuario con sus roles y permisos en una sola consulta.
     *
     * <p>Sin el {@code join fetch}, calcular los permisos efectivos al iniciar
     * sesion dispara una consulta por rol.
     */
    @Query("""
            select distinct u from User u
            left join fetch u.roles r
            left join fetch r.permissions
            where lower(u.email) = lower(:email)
            """)
    Optional<User> findByEmailWithRoles(String email);

    @Query("""
            select distinct u from User u
            left join fetch u.roles r
            left join fetch r.permissions
            where u.publicId = :publicId
            """)
    Optional<User> findByPublicIdWithRoles(UUID publicId);

    boolean existsByEmailIgnoreCase(String email);
}
