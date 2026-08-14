package co.edu.usta.ustudent.iam.domain.repository;

import co.edu.usta.ustudent.iam.domain.model.Permission;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findByCodeIn(Set<String> codes);

    List<Permission> findAllByOrderByResourceAscActionAsc();
}
