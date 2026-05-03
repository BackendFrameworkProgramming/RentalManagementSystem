package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.AsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsTypeRepository extends JpaRepository<AsType, Long> {
    Page<AsType> findAllByUseYnTrue(Pageable pageable);
}
