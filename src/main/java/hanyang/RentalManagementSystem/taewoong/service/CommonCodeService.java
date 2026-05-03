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

    public CommonResponse<List<Map<String, Object>>> findAll(String groupCode) {
        if (groupCode != null) {
            List<Map<String, Object>> details = detailRepo.findAll().stream()
                    .filter(d -> d.getCodeGroup().getGroupCode().equals(groupCode))
                    .map(d -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("id", d.getId()); m.put("codeValue", d.getCodeValue()); m.put("codeName", d.getCodeName()); m.put("sortOrder", d.getSortOrder()); m.put("useYn", d.getUseYn()); return m; })
                    .collect(Collectors.toList());
            return CommonResponse.success(details);
        }
        List<Map<String, Object>> groups = groupRepo.findAll().stream()
                .map(g -> { Map<String, Object> m = new LinkedHashMap<>(); m.put("id", g.getId()); m.put("groupCode", g.getGroupCode()); m.put("groupName", g.getGroupName()); m.put("useYn", g.getUseYn()); return m; })
                .collect(Collectors.toList());
        return CommonResponse.success(groups);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> create(Map<String, Object> body) {
        String groupCode = (String) body.get("groupCode");
        CodeGroup group = groupRepo.findAll().stream().filter(g -> g.getGroupCode().equals(groupCode)).findFirst()
                .orElseGet(() -> groupRepo.save(CodeGroup.builder().groupCode(groupCode).groupName(groupCode).useYn(true).build()));
        CodeDetail detail = CodeDetail.builder()
                .codeGroup(group)
                .codeValue((String) body.get("codeValue"))
                .codeName((String) body.get("codeName"))
                .sortOrder(body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null)
                .useYn(body.get("useYn") != null ? (Boolean) body.get("useYn") : true)
                .build();
        detailRepo.save(detail);
        Map<String, Object> m = new LinkedHashMap<>(); m.put("id", detail.getId()); m.put("codeValue", detail.getCodeValue());
        return CommonResponse.created(m);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> update(Long id, Map<String, Object> body) {
        CodeDetail d = detailRepo.findById(id).orElseThrow(() -> new CustomException("CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("codeName")) d.setCodeName((String) body.get("codeName"));
        if (body.containsKey("sortOrder")) d.setSortOrder(((Number) body.get("sortOrder")).intValue());
        if (body.containsKey("useYn")) d.setUseYn((Boolean) body.get("useYn"));
        Map<String, Object> m = new LinkedHashMap<>(); m.put("id", d.getId()); m.put("codeName", d.getCodeName());
        return CommonResponse.success(m);
    }

    @Transactional
    public void delete(Long id) {
        CodeDetail d = detailRepo.findById(id).orElseThrow(() -> new CustomException("CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        detailRepo.delete(d);
    }
}
