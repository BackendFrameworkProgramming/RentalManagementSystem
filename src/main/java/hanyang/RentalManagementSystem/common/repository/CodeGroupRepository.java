package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeGroupRepository extends JpaRepository<CodeGroup, Long> {
    boolean existsByGroupCode(String groupCode);
}
