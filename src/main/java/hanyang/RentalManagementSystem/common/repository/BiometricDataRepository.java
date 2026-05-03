package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.BiometricData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BiometricDataRepository extends JpaRepository<BiometricData, Long> {
    Page<BiometricData> findAllByIsDeletedFalse(Pageable pageable);
    List<BiometricData> findAllByDeviceIdAndIsDeletedFalse(Long deviceId);
}
