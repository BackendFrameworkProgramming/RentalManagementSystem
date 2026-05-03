package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Page<Department> findAllByIsDeletedFalse(Pageable pageable);
    Optional<Department> findByIdAndIsDeletedFalse(Long id);
}
