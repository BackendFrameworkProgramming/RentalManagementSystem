package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.SystemErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemErrorLogRepository extends JpaRepository<SystemErrorLog, Long> {
}
