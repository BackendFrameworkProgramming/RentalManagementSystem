package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.AsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsRecordRepository extends JpaRepository<AsRecord, Long> {
    Page<AsRecord> findAllByIsDeletedFalse(Pageable pageable);
    Optional<AsRecord> findByIdAndIsDeletedFalse(Long id);

    // @EntityGraph: AS 이력 조회 시 연관(임대·사용자·업체·유형) 함께 페치하여 N+1 방어
    @EntityGraph(attributePaths = {"rental", "rental.user", "vendor", "asType"})
    List<AsRecord> findAllByDeviceIdAndIsDeletedFalse(Long deviceId);

    Page<AsRecord> findAllByDeviceIdAndIsDeletedFalse(Long deviceId, Pageable pageable);
    List<AsRecord> findAllByVendorIdAndIsDeletedFalse(Long vendorId);

    // 교수님 피드백 #1: 청크 전체스캔+메모리필터 제거 → 쿼리에서 스코핑·검색·페이징.
    // nullable 연관(branch/rental) 때문에 LEFT JOIN FETCH 사용(누락 방지 + N+1 방어).
    @Query(value = "select a from AsRecord a " +
            "left join fetch a.device d " +
            "left join fetch d.modelVersion mv " +
            "left join fetch mv.model m " +
            "left join fetch a.branch b " +
            "left join fetch a.rental r " +
            "left join fetch r.user u " +
            "where a.isDeleted = false " +
            "and (:branchId is null or b.id = :branchId) " +
            "and (:userId is null or u.id = :userId) " +
            "and (:kw is null " +
            "  or lower(d.deviceId) like lower(concat('%', :kw, '%')) " +
            "  or lower(b.branchName) like lower(concat('%', :kw, '%')) " +
            "  or lower(u.userName) like lower(concat('%', :kw, '%')) " +
            "  or lower(a.receiptContent) like lower(concat('%', :kw, '%')))",
            countQuery = "select count(a) from AsRecord a " +
            "left join a.device d " +
            "left join a.branch b " +
            "left join a.rental r " +
            "left join r.user u " +
            "where a.isDeleted = false " +
            "and (:branchId is null or b.id = :branchId) " +
            "and (:userId is null or u.id = :userId) " +
            "and (:kw is null " +
            "  or lower(d.deviceId) like lower(concat('%', :kw, '%')) " +
            "  or lower(b.branchName) like lower(concat('%', :kw, '%')) " +
            "  or lower(u.userName) like lower(concat('%', :kw, '%')) " +
            "  or lower(a.receiptContent) like lower(concat('%', :kw, '%')))")
    Page<AsRecord> searchAsRecords(@Param("branchId") Long branchId,
                                   @Param("userId") Long userId,
                                   @Param("kw") String kw,
                                   Pageable pageable);

    // 교수님 피드백 #3: 지점별 집계는 group by 쿼리 (status는 아직 String — gyumin이 AsStatus enum 전환 예정)
    @Query("select a.branch.id, a.branch.branchName, count(a), " +
           "sum(case when a.status in ('AS_RECEIVED', 'AS_PROGRESS') then 1 else 0 end) " +
           "from AsRecord a where a.isDeleted = false and a.branch is not null " +
           "group by a.branch.id, a.branch.branchName")
    List<Object[]> summaryByBranch();
}
