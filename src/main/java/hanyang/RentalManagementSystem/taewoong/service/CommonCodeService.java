package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.*;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonCodeService {
    private final CodeGroupRepository groupRepo;
    private final CodeDetailRepository detailRepo;

    // === 코드그룹 ===

    // 그룹 목록 조회 (groupCode 파라미터 있으면 해당 그룹의 상세 코드 반환)
    public CommonResponse<List<Map<String, Object>>> findAll(String groupCode) {
        if (groupCode != null) {
            List<Map<String, Object>> details = detailRepo.findAll().stream()
                    .filter(d -> d.getCodeGroup().getGroupCode().equals(groupCode))
                    .map(this::detailToMap)
                    .collect(Collectors.toList());
            return CommonResponse.success(details);
        }
        List<Map<String, Object>> groups = groupRepo.findAll().stream()
                .map(this::groupToMap)
                .collect(Collectors.toList());
        return CommonResponse.success(groups);
    }

    // 그룹 등록
    @Transactional
    public CommonResponse<Map<String, Object>> createGroup(Map<String, Object> body) {
        CodeGroup group = CodeGroup.builder()
                .groupCode((String) body.get("groupCode"))
                .groupName((String) body.get("groupName"))
                .description((String) body.get("description"))
                .useYn(body.get("useYn") != null ? (Boolean) body.get("useYn") : true)
                .build();
        groupRepo.save(group);
        return CommonResponse.created(groupToMap(group));
    }

    // 그룹 수정
    @Transactional
    public CommonResponse<Map<String, Object>> updateGroup(Long id, Map<String, Object> body) {
        CodeGroup g = groupRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("groupName")) g.setGroupName((String) body.get("groupName"));
        if (body.containsKey("description")) g.setDescription((String) body.get("description"));
        if (body.containsKey("useYn")) g.setUseYn((Boolean) body.get("useYn"));
        return CommonResponse.success(groupToMap(g));
    }

    // 그룹 삭제
    @Transactional
    public void deleteGroup(Long id) {
        CodeGroup g = groupRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        groupRepo.delete(g);
    }

    // === 코드상세 ===

    // 그룹별 상세 목록
    public CommonResponse<List<Map<String, Object>>> findDetailsByGroupId(Long groupId) {
        List<Map<String, Object>> details = detailRepo.findAll().stream()
                .filter(d -> d.getCodeGroup().getId().equals(groupId))
                .map(this::detailToMap)
                .collect(Collectors.toList());
        return CommonResponse.success(details);
    }

    // 상세 등록
    @Transactional
    public CommonResponse<Map<String, Object>> createDetail(Long groupId, Map<String, Object> body) {
        CodeGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new CustomException("CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        CodeDetail detail = CodeDetail.builder()
                .codeGroup(group)
                .codeValue((String) body.get("codeValue"))
                .codeName((String) body.get("codeName"))
                .sortOrder(body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null)
                .useYn(body.get("useYn") != null ? (Boolean) body.get("useYn") : true)
                .build();
        detailRepo.save(detail);
        return CommonResponse.created(detailToMap(detail));
    }

    // 상세 수정
    @Transactional
    public CommonResponse<Map<String, Object>> updateDetail(Long id, Map<String, Object> body) {
        CodeDetail d = detailRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("codeName")) d.setCodeName((String) body.get("codeName"));
        if (body.containsKey("codeValue")) d.setCodeValue((String) body.get("codeValue"));
        if (body.containsKey("sortOrder")) d.setSortOrder(((Number) body.get("sortOrder")).intValue());
        if (body.containsKey("useYn")) d.setUseYn((Boolean) body.get("useYn"));
        return CommonResponse.success(detailToMap(d));
    }

    // 상세 삭제
    @Transactional
    public void deleteDetail(Long id) {
        CodeDetail d = detailRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        detailRepo.delete(d);
    }

    // === 헬퍼 ===

    private Map<String, Object> groupToMap(CodeGroup g) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", g.getId());
        m.put("groupCode", g.getGroupCode());
        m.put("groupName", g.getGroupName());
        m.put("description", g.getDescription());
        m.put("useYn", g.getUseYn());
        return m;
    }

    private Map<String, Object> detailToMap(CodeDetail d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("groupCode", d.getCodeGroup().getGroupCode());
        m.put("codeValue", d.getCodeValue());
        m.put("codeName", d.getCodeName());
        m.put("sortOrder", d.getSortOrder());
        m.put("useYn", d.getUseYn());
        return m;
    }
}