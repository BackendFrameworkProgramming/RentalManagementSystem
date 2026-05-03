package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.AsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsRecordRepository extends JpaRepository<AsRecord, Long> {
    Page<AsRecord> findAllByIsDeletedFalse(Pageable pageable);
    Optional<AsRecord> findByIdAndIsDeletedFalse(Long id);
    List<AsRecord> findAllByDeviceIdAndIsDeletedFalse(Long deviceId);
    Page<AsRecord> findAllByDeviceIdAndIsDeletedFalse(Long deviceId, Pageable pageable);
    List<AsRecord> findAllByVendorIdAndIsDeletedFalse(Long vendorId);
}
