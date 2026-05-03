package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.DeptHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeptHistoryRepository extends JpaRepository<DeptHistory, Long> {
}
