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
    // 데이터 스코핑: 지점관리자 본인 지점만 (단건을 페이지로 반환)
    Page<Branch> findByIdAndIsDeletedFalse(Long id, Pageable pageable);
}
