package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.DesignHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DesignHistoryRepository extends JpaRepository<DesignHistory, Long> {
    List<DesignHistory> findAllByIsDeletedFalseOrderByRoundAsc();
    Optional<DesignHistory> findByIdAndIsDeletedFalse(Long id);
    boolean existsByRoundAndIsDeletedFalse(Integer round);
}
