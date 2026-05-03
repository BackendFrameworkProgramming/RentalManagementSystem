package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Page<Device> findAllByIsDeletedFalse(Pageable pageable);
    Optional<Device> findByIdAndIsDeletedFalse(Long id);
    List<Device> findAllByBranchIdAndIsDeletedFalse(Long branchId);
    List<Device> findAllByBranchIsNullAndIsDeletedFalse();
    List<Device> findAllByIsDeletedFalse();
    List<Device> findAllByStatusAndIsDeletedFalse(String status);
    List<Device> findAllByModelVersionIdAndIsDeletedFalse(Long modelVersionId);
    boolean existsByDeviceIdAndIsDeletedFalse(String deviceId);
}
