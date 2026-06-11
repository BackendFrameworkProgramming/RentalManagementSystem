package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.CodeDetail;
import hanyang.RentalManagementSystem.common.entity.CodeGroup;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.CodeDetailRepository;
import hanyang.RentalManagementSystem.common.repository.CodeGroupRepository;
import hanyang.RentalManagementSystem.taewoong.dto.CodeDetailResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CodeDetailUpsertRequest;
import hanyang.RentalManagementSystem.taewoong.dto.CodeGroupResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CodeGroupUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {
    private final CodeGroupRepository groupRepo;
    private final CodeDetailRepository detailRepo;

    // === 코드그룹 ===

    /** groupCode가 있으면 해당 그룹의 상세 코드, 없으면 그룹 목록 반환. */
    public CommonResponse<List<Object>> findAll(String groupCode) {
        if (groupCode != null) {
            // 교수님 피드백 #2: findAll 후 메모리 필터 대신 쿼리 직접 조회
            List<Object> details = detailRepo.findAllByCodeGroupGroupCode(groupCode).stream()
                    .map(d -> (Object) CodeDetailResponse.from(d)).toList();
            return CommonResponse.success(details);
        }
        List<Object> groups = groupRepo.findAll().stream()
                .map(g -> (Object) CodeGroupResponse.from(g)).toList();
        return CommonResponse.success(groups);
    }

    @Transactional
    public CommonResponse<CodeGroupResponse> createGroup(CodeGroupUpsertRequest req) {
        CodeGroup group = CodeGroup.builder()
                .groupCode(req.getGroupCode())
                .groupName(req.getGroupName())
                .description(req.getDescription())
                .useYn(req.getUseYn() != null ? req.getUseYn() : true)
                .build();
        groupRepo.save(group);
        return CommonResponse.created(CodeGroupResponse.from(group));
    }

    @Transactional
    public CommonResponse<CodeGroupResponse> updateGroup(Long id, CodeGroupUpsertRequest req) {
        CodeGroup g = groupRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (req.getGroupName() != null) g.setGroupName(req.getGroupName());
        if (req.getDescription() != null) g.setDescription(req.getDescription());
        if (req.getUseYn() != null) g.setUseYn(req.getUseYn());
        return CommonResponse.success(CodeGroupResponse.from(g));
    }

    @Transactional
    public void deleteGroup(Long id) {
        CodeGroup g = groupRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        groupRepo.delete(g);
    }

    // === 코드상세 ===

    public CommonResponse<List<CodeDetailResponse>> findDetailsByGroupId(Long groupId) {
        List<CodeDetailResponse> details = detailRepo.findAllByCodeGroupId(groupId).stream()
                .map(CodeDetailResponse::from).toList();
        return CommonResponse.success(details);
    }

    @Transactional
    public CommonResponse<CodeDetailResponse> createDetail(Long groupId, CodeDetailUpsertRequest req) {
        CodeGroup group = groupRepo.findById(groupId)
                .orElseThrow(() -> new CustomException("CODE_GROUP_NOT_FOUND", "코드그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        CodeDetail detail = CodeDetail.builder()
                .codeGroup(group)
                .codeValue(req.getCodeValue())
                .codeName(req.getCodeName())
                .sortOrder(req.getSortOrder())
                .useYn(req.getUseYn() != null ? req.getUseYn() : true)
                .build();
        detailRepo.save(detail);
        return CommonResponse.created(CodeDetailResponse.from(detail));
    }

    @Transactional
    public CommonResponse<CodeDetailResponse> updateDetail(Long id, CodeDetailUpsertRequest req) {
        CodeDetail d = detailRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (req.getCodeName() != null) d.setCodeName(req.getCodeName());
        if (req.getCodeValue() != null) d.setCodeValue(req.getCodeValue());
        if (req.getSortOrder() != null) d.setSortOrder(req.getSortOrder());
        if (req.getUseYn() != null) d.setUseYn(req.getUseYn());
        return CommonResponse.success(CodeDetailResponse.from(d));
    }

    @Transactional
    public void deleteDetail(Long id) {
        CodeDetail d = detailRepo.findById(id)
                .orElseThrow(() -> new CustomException("CODE_NOT_FOUND", "코드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        detailRepo.delete(d);
    }
}
