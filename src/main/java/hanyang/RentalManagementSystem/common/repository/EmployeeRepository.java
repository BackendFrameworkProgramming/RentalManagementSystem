package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Page<Employee> findAllByTeamIdAndIsDeletedFalse(Long teamId, Pageable pageable);
    List<Employee> findAllByTeamIdAndIsDeletedFalse(Long teamId);
    List<Employee> findAllByDepartmentIdAndIsDeletedFalse(Long departmentId);
    boolean existsByTeamIdAndIsDeletedFalse(Long teamId);
}
