package co.edu.usta.ustudent.iam.domain.repository;

import co.edu.usta.ustudent.iam.domain.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(String code);
}
