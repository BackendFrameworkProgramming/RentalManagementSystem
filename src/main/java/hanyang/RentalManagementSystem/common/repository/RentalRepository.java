package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Rental;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {
    Page<Rental> findAllByIsDeletedFalse(Pageable pageable);
    Optional<Rental> findByIdAndIsDeletedFalse(Long id);
    List<Rental> findAllByUserIdAndIsDeletedFalse(Long userId);
    Page<Rental> findAllByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);
    List<Rental> findAllByDeviceIdAndIsDeletedFalse(Long deviceId);
    List<Rental> findAllByBranchIdAndIsDeletedFalse(Long branchId);
    boolean existsByDeviceIdAndReturnDateIsNullAndIsDeletedFalse(Long deviceId);
    Optional<Rental> findByDeviceIdAndStatusAndIsDeletedFalse(Long deviceId, String status);
}
