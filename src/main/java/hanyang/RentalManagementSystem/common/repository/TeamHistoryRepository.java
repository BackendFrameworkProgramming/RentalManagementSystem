package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.TeamHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamHistoryRepository extends JpaRepository<TeamHistory, Long> {
}
