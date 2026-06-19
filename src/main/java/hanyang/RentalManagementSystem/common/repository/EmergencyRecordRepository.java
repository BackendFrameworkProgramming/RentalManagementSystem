package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.EmergencyRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyRecordRepository extends JpaRepository<EmergencyRecord, Long> {
    @Override
    Page<EmergencyRecord> findAll(Pageable pageable);
    List<EmergencyRecord> findAllByBiometricDataId(Long biometricDataId);

    // 목록 N+1 제거용: 여러 생체정보의 응급기록을 한 번에 조회
    List<EmergencyRecord> findAllByBiometricDataIdIn(List<Long> biometricDataIds);

    // 응급기록 목록: 최신순 정렬 + 생체정보 함께 페치(N+1 방어)
    @EntityGraph(attributePaths = "biometricData")
    Page<EmergencyRecord> findAllByOrderByEmergencyRecordTimeDesc(Pageable pageable);
}
