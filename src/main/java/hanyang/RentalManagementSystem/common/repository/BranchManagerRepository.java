package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.BranchManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BranchManagerRepository extends JpaRepository<BranchManager, Long> {
    Page<BranchManager> findAllByBranchIdAndIsDeletedFalse(Long branchId, Pageable pageable);
    List<BranchManager> findAllByBranchIdAndIsDeletedFalse(Long branchId);
}
