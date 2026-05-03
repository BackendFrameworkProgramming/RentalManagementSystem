package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.EmergencyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyRecordRepository extends JpaRepository<EmergencyRecord, Long> {
    Page<EmergencyRecord> findAll(Pageable pageable);
    List<EmergencyRecord> findAllByBiometricDataId(Long biometricDataId);
}
