package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.DeptHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeptHistoryRepository extends JpaRepository<DeptHistory, Long> {
    Page<DeptHistory> findAllByDepartmentId(Long departmentId, Pageable pageable);
}
