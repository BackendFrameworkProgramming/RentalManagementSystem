package hanyang.RentalManagementSystem.common.repository;

import hanyang.RentalManagementSystem.common.entity.CodeDetail;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeDetailRepository extends JpaRepository<CodeDetail, Long> {
    // 교수님 피드백 #2: 전체 findAll 후 메모리 필터 대신 쿼리로 직접 조회 (@EntityGraph로 N+1 방어)
    @EntityGraph(attributePaths = "codeGroup")
    List<CodeDetail> findAllByCodeGroupGroupCode(String groupCode);

    @EntityGraph(attributePaths = "codeGroup")
    List<CodeDetail> findAllByCodeGroupId(Long codeGroupId);
}
