package co.edu.usta.ustudent.iam.domain.repository;

import co.edu.usta.ustudent.iam.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);

    @Query("""
            select distinct r from Role r
            left join fetch r.permissions
            where r.publicId = :publicId
            """)
    Optional<Role> findByPublicIdWithPermissions(UUID publicId);

    @Query("""
            select distinct r from Role r
            left join fetch r.permissions
            order by r.code
            """)
    List<Role> findAllWithPermissions();
}
