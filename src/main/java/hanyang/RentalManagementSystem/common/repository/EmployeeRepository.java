package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // @EntityGraph: 팀·부서 함께 페치하여 N+1 방어
    @EntityGraph(attributePaths = {"team", "team.department", "department"})
    Page<Employee> findAllByTeamIdAndIsDeletedFalse(Long teamId, Pageable pageable);
    List<Employee> findAllByTeamIdAndIsDeletedFalse(Long teamId);
    List<Employee> findAllByDepartmentIdAndIsDeletedFalse(Long departmentId);
    boolean existsByTeamIdAndIsDeletedFalse(Long teamId);
}
