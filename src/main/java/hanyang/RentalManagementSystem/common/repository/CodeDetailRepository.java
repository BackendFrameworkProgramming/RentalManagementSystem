package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.CodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeDetailRepository extends JpaRepository<CodeDetail, Long> {
}
