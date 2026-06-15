package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.BranchManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BranchManagerRepository extends JpaRepository<BranchManager, Long> {
    Page<BranchManager> findAllByBranchIdAndIsDeletedFalse(Long branchId, Pageable pageable);
    List<BranchManager> findAllByBranchIdAndIsDeletedFalse(Long branchId);

    // 지점 목록 N+1 제거용: 여러 지점의 담당자를 한 번에 조회 후 메모리 그룹핑
    List<BranchManager> findAllByBranchIdInAndIsDeletedFalse(List<Long> branchIds);

    // 소프트삭제 일관성: 삭제된 담당자는 수정/삭제 대상에서 제외
    Optional<BranchManager> findByIdAndIsDeletedFalse(Long id);
}
