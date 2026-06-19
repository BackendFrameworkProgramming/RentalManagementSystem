package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    Page<Model> findAllByIsDeletedFalse(Pageable pageable);
    Optional<Model> findByIdAndIsDeletedFalse(Long id);
}
