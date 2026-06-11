package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Rental;
import hanyang.RentalManagementSystem.common.enums.RentalStatus;
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
public interface RentalRepository extends JpaRepository<Rental, Long> {

    @EntityGraph(attributePaths = {"device", "device.modelVersion", "device.modelVersion.model", "branch", "user"})
    Page<Rental> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Rental> findByIdAndIsDeletedFalse(Long id);
    List<Rental> findAllByUserIdAndIsDeletedFalse(Long userId);

    @EntityGraph(attributePaths = {"device", "device.modelVersion", "device.modelVersion.model", "branch", "user"})
    Page<Rental> findAllByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    List<Rental> findAllByDeviceIdAndIsDeletedFalse(Long deviceId);
    List<Rental> findAllByBranchIdAndIsDeletedFalse(Long branchId);
    boolean existsByDeviceIdAndReturnDateIsNullAndIsDeletedFalse(Long deviceId);
    Optional<Rental> findByDeviceIdAndStatusAndIsDeletedFalse(Long deviceId, RentalStatus status);

    // 교수님 피드백 #1: 스코핑·검색을 쿼리에서 처리(페이징 전에 필터) + @EntityGraph로 N+1 방어.
    // branchId=지점관리자 본인 지점, userId=일반 사용자 본인, kw=검색어(없으면 전체).
    @EntityGraph(attributePaths = {"device", "device.modelVersion", "device.modelVersion.model", "branch", "user"})
    @Query(value = "select r from Rental r where r.isDeleted = false " +
            "and (:branchId is null or r.branch.id = :branchId) " +
            "and (:userId is null or r.user.id = :userId) " +
            "and (:kw is null " +
            "  or lower(r.device.deviceId) like lower(concat('%', :kw, '%')) " +
            "  or lower(r.branch.branchName) like lower(concat('%', :kw, '%')) " +
            "  or lower(r.user.userName) like lower(concat('%', :kw, '%')) " +
            "  or lower(r.device.modelVersion.model.modelName) like lower(concat('%', :kw, '%')))",
            countQuery = "select count(r) from Rental r where r.isDeleted = false " +
            "and (:branchId is null or r.branch.id = :branchId) " +
            "and (:userId is null or r.user.id = :userId) " +
            "and (:kw is null " +
            "  or lower(r.device.deviceId) like lower(concat('%', :kw, '%')) " +
            "  or lower(r.branch.branchName) like lower(concat('%', :kw, '%')) " +
            "  or lower(r.user.userName) like lower(concat('%', :kw, '%')) " +
            "  or lower(r.device.modelVersion.model.modelName) like lower(concat('%', :kw, '%')))")
    Page<Rental> searchRentals(@Param("branchId") Long branchId,
                               @Param("userId") Long userId,
                               @Param("kw") String kw,
                               Pageable pageable);

    // 교수님 피드백 #3: 지점별 집계는 전체 로드 대신 group by 쿼리
    @Query("select r.branch.id, r.branch.branchName, count(r), " +
           "sum(case when r.status = :renting then 1 else 0 end) " +
           "from Rental r where r.isDeleted = false and r.branch is not null " +
           "group by r.branch.id, r.branch.branchName")
    List<Object[]> summaryByBranch(@Param("renting") RentalStatus renting);
}
