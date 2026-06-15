package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CenterRepository extends JpaRepository<Center, Long> {
    // 센터는 단일 행: findAll 후 findFirst 대신 정렬 쿼리로 1건 조회
    Optional<Center> findTopByOrderByIdAsc();
}
