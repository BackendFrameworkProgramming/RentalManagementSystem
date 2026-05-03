package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.AsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsRecordRepository extends JpaRepository<AsRecord, Long> {
}
