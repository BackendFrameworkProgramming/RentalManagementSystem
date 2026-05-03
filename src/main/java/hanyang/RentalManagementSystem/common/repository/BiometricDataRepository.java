package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.BiometricData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BiometricDataRepository extends JpaRepository<BiometricData, Long> {
}
