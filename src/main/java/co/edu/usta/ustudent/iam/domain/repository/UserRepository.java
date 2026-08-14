package co.edu.usta.ustudent.iam.domain.repository;

import co.edu.usta.ustudent.iam.domain.model.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDocumentNumber(String documentNumber);

    /** Cuantos usuarios tienen asignado un rol. Evita borrar un rol en uso. */
    @Query("select count(u) from User u join u.roles r where r.code = :roleCode")
    long countByRoleCode(String roleCode);

    /**
     * Listado sin filtro.
     *
     * <p>El {@code EntityGraph} trae los roles con el listado; los permisos de
     * cada rol no hacen falta aqui, que para eso el resumen solo muestra el
     * nombre del rol.
     */
    @EntityGraph(attributePaths = "roles")
    Page<User> findAllBy(Pageable pageable);

    /**
     * Listado filtrado por nombre, correo o documento.
     *
     * <p>Va aparte del listado sin filtro a proposito. La version con
     * {@code :query is null or ...} dentro de la consulta falla en PostgreSQL:
     * al no poder inferir el tipo del parametro nulo lo toma como binario y
     * revienta con "function lower(bytea) does not exist". Dos consultas
     * explicitas son mas claras y no dependen de la inferencia de tipos.
     */
    @EntityGraph(attributePaths = "roles")
    @Query("""
            select u from User u
            where lower(u.fullName) like lower(concat('%', :query, '%'))
               or lower(u.email) like lower(concat('%', :query, '%'))
               or u.documentNumber like concat('%', :query, '%')
            """)
    Page<User> search(String query, Pageable pageable);
}
