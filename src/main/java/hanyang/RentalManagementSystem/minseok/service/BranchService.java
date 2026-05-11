package hanyang.RentalManagementSystem.minseok.service;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.common.entity.Branch;
import hanyang.RentalManagementSystem.common.entity.BranchManager;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BranchManagerRepository;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branchRepository;
    private final BranchManagerRepository branchManagerRepository;

    // 5-1 지점 목록
    public CommonResponse<List<Map<String, Object>>> findAll(CommonSearchRequest request) {
        Page<Branch> page = branchRepository.findAllByIsDeletedFalse(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::toMap).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    // 5-2 지점 등록
    @Transactional
    public CommonResponse<Map<String, Object>> create(Map<String, Object> body) {
        Branch branch = Branch.builder()
                .branchName((String) body.get("branchName"))
                .status(body.get("status") != null ? (Boolean) body.get("status") : true)
                .address((String) body.get("address"))
                .addressDetail((String) body.get("addressDetail"))
                .managerName((String) body.get("managerName"))
                .phone((String) body.get("phone"))
                .fax((String) body.get("fax"))
                .appliedDate(body.get("appliedDate") != null ? LocalDate.parse((String) body.get("appliedDate")) : LocalDate.now())
                .isDeleted(false)
                .build();
        branchRepository.save(branch);
        return CommonResponse.created(toMap(branch));
    }

    // 5-3 지점 수정
    @Transactional
    public CommonResponse<Map<String, Object>> update(Long id, Map<String, Object> body) {
        Branch b = getBranch(id);
        if (body.containsKey("branchName")) b.setBranchName((String) body.get("branchName"));
        if (body.containsKey("status")) b.setStatus((Boolean) body.get("status"));
        if (body.containsKey("address")) b.setAddress((String) body.get("address"));
        if (body.containsKey("addressDetail")) b.setAddressDetail((String) body.get("addressDetail"));
        if (body.containsKey("managerName")) b.setManagerName((String) body.get("managerName"));
        if (body.containsKey("phone")) b.setPhone((String) body.get("phone"));
        if (body.containsKey("fax")) b.setFax((String) body.get("fax"));
        return CommonResponse.success(toMap(b));
    }

    // 5-4 지점 삭제 (cascade soft delete → managers)
    @Transactional
    public void delete(Long id) {
        Branch b = getBranch(id);
        // cascade soft delete: 소속 담당자도 함께
        List<BranchManager> managers = branchManagerRepository.findAllByBranchIdAndIsDeletedFalse(id);
        managers.forEach(m -> m.setIsDeleted(true));
        b.setIsDeleted(true);
    }

    // 5-5 담당자 목록
    public CommonResponse<List<Map<String, Object>>> findManagers(Long branchId, CommonSearchRequest request) {
        Page<BranchManager> page = branchManagerRepository.findAllByBranchIdAndIsDeletedFalse(branchId, request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::managerToMap).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    // 5-6 담당자 등록
    @Transactional
    public CommonResponse<Map<String, Object>> createManager(Map<String, Object> body) {
        Long branchId = ((Number) body.get("branchId")).longValue();
        Branch branch = getBranch(branchId);
        BranchManager mgr = BranchManager.builder()
                .branch(branch)
                .managerName((String) body.get("managerName"))
                .contact((String) body.get("contact"))
                .email((String) body.get("email"))
                .managerType((String) body.getOrDefault("managerType", "주"))
                .status(body.get("status") != null ? (Boolean) body.get("status") : true)
                .isDeleted(false)
                .build();
        branchManagerRepository.save(mgr);
        return CommonResponse.created(managerToMap(mgr));
    }

    // 5-7 담당자 수정
    @Transactional
    public CommonResponse<Map<String, Object>> updateManager(Long id, Map<String, Object> body) {
        BranchManager m = branchManagerRepository.findById(id)
                .orElseThrow(() -> new CustomException("BRANCH_MANAGER_NOT_FOUND", "담당자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("managerName")) m.setManagerName((String) body.get("managerName"));
        if (body.containsKey("contact")) m.setContact((String) body.get("contact"));
        if (body.containsKey("email")) m.setEmail((String) body.get("email"));
        if (body.containsKey("managerType")) m.setManagerType((String) body.get("managerType"));
        if (body.containsKey("status")) m.setStatus((Boolean) body.get("status"));
        return CommonResponse.success(managerToMap(m));
    }

    // 5-8 담당자 삭제
    @Transactional
    public void deleteManager(Long id) {
        BranchManager m = branchManagerRepository.findById(id)
                .orElseThrow(() -> new CustomException("BRANCH_MANAGER_NOT_FOUND", "담당자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        m.setIsDeleted(true);
    }

    // === 헬퍼 ===
    private Branch getBranch(Long id) {
        return branchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "ID " + id + "에 해당하는 지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private Map<String, Object> toMap(Branch b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("branchName", b.getBranchName());
        m.put("status", b.getStatus());
        m.put("address", b.getAddress());
        m.put("addressDetail", b.getAddressDetail());
        m.put("managerName", b.getManagerName());
        m.put("phone", b.getPhone());
        m.put("fax", b.getFax());
        m.put("appliedDate", b.getAppliedDate());
        m.put("createdAt", b.getCreatedAt());
        m.put("updatedAt", b.getUpdatedAt());
        return m;
    }

    private Map<String, Object> managerToMap(BranchManager m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("branchId", m.getBranch().getId());
        map.put("branchName", m.getBranch().getBranchName());
        map.put("managerName", m.getManagerName());
        map.put("contact", m.getContact());
        map.put("email", m.getEmail());
        map.put("managerType", m.getManagerType());
        map.put("status", m.getStatus());
        return map;
    }
}
