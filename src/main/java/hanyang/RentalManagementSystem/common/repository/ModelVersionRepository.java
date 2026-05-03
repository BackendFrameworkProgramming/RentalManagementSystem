package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.ModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelVersionRepository extends JpaRepository<ModelVersion, Long> {
}
