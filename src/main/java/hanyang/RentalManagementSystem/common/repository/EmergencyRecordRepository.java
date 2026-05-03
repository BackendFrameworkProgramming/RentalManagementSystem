package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.EmergencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmergencyRecordRepository extends JpaRepository<EmergencyRecord, Long> {
}
