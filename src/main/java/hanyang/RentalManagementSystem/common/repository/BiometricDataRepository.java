package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.BiometricData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BiometricDataRepository extends JpaRepository<BiometricData, Long> {
    Page<BiometricData> findAllByIsDeletedFalse(Pageable pageable);
    List<BiometricData> findAllByDeviceIdAndIsDeletedFalse(Long deviceId);

    /**
     * 모델명별 생체정보 건수를 DB에서 직접 집계한다.
     * device → modelVersion → model 을 LEFT JOIN 으로 한 번에 조회하므로
     * 전체 행을 메모리에 적재하며 발생하던 3N+1 지연 로딩을 제거한다.
     * 모델 정보가 없는 경우 '-' 로 집계한다.
     */
    @Query("SELECT COALESCE(m.modelName, '-'), COUNT(b) " +
            "FROM BiometricData b " +
            "LEFT JOIN b.device d " +
            "LEFT JOIN d.modelVersion mv " +
            "LEFT JOIN mv.model m " +
            "WHERE b.isDeleted = false " +
            "GROUP BY m.modelName " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> countByModelName();
}
