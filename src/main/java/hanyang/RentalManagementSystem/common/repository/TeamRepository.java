package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Page<Team> findAllByDepartmentIdAndIsDeletedFalse(Long departmentId, Pageable pageable);
    List<Team> findAllByDepartmentIdAndIsDeletedFalse(Long departmentId);
    Optional<Team> findByIdAndIsDeletedFalse(Long id);
    List<Team> findAllByIsDeletedFalse();
}
