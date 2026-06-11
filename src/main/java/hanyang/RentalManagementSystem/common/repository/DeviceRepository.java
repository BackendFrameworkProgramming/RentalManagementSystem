package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.enums.DeviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    // @EntityGraph: 목록/상세에서 모델·지점을 함께 페치해 N+1 방어 (다른 조 우수사례 반영)
    @EntityGraph(attributePaths = {"modelVersion", "modelVersion.model", "branch"})
    Page<Device> findAllByIsDeletedFalse(Pageable pageable);

    @EntityGraph(attributePaths = {"modelVersion", "modelVersion.model", "branch"})
    Optional<Device> findByIdAndIsDeletedFalse(Long id);

    List<Device> findAllByBranchIdAndIsDeletedFalse(Long branchId);
    List<Device> findAllByBranchIsNullAndIsDeletedFalse();
    List<Device> findAllByIsDeletedFalse();
    List<Device> findAllByStatusAndIsDeletedFalse(DeviceStatus status);
    List<Device> findAllByModelVersionIdAndIsDeletedFalse(Long modelVersionId);
    boolean existsByDeviceIdAndIsDeletedFalse(String deviceId);
    Optional<Device> findByDeviceIdAndIsDeletedFalse(String deviceId);

    // 교수님 피드백 #3: 카운트는 엔티티 전체 로드 대신 count 쿼리
    long countByIsDeletedFalse();
    long countByBranchIsNullAndIsDeletedFalse();
    long countByStatusAndIsDeletedFalse(DeviceStatus status);
    long countByStatusInAndIsDeletedFalse(Collection<DeviceStatus> statuses);
    long countByModelVersionIdAndIsDeletedFalse(Long modelVersionId);

    @Query("select d.branch.id, d.branch.branchName, count(d) from Device d " +
           "where d.isDeleted = false and d.branch is not null " +
           "group by d.branch.id, d.branch.branchName")
    List<Object[]> countGroupByBranch();
}
