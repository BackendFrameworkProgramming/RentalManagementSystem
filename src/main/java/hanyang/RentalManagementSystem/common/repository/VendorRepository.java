package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Page<Vendor> findAllByIsDeletedFalse(Pageable pageable);
    Optional<Vendor> findByIdAndIsDeletedFalse(Long id);
}
