package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Branch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    Page<Branch> findAllByIsDeletedFalse(Pageable pageable);
    Optional<Branch> findByIdAndIsDeletedFalse(Long id);
    List<Branch> findAllByIsDeletedFalse();
}
