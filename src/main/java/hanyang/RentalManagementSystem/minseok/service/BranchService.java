package hanyang.RentalManagementSystem.minseok.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.config.SecurityUtil;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.Branch;
import hanyang.RentalManagementSystem.common.entity.BranchManager;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BranchManagerRepository;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.minseok.dto.BranchManagerResponse;
import hanyang.RentalManagementSystem.minseok.dto.BranchManagerUpsertRequest;
import hanyang.RentalManagementSystem.minseok.dto.BranchResponse;
import hanyang.RentalManagementSystem.minseok.dto.BranchUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branchRepository;
    private final BranchManagerRepository branchManagerRepository;

    // 5-1 지점 목록 (주담당자 정보 포함, 교수님 #4: DTO)
    public CommonResponse<List<BranchResponse>> findAll(CommonSearchRequest request) {
        // 데이터 스코핑: 지점관리자는 본인 지점만, ADMIN/STAFF는 전체
        Long scopeBranchId = SecurityUtil.isBranchManager() ? SecurityUtil.currentBranchId() : null;
        Page<Branch> page = (scopeBranchId != null)
                ? branchRepository.findByIdAndIsDeletedFalse(scopeBranchId, request.toPageable())
                : branchRepository.findAllByIsDeletedFalse(request.toPageable());

        // N+1 제거: 페이지의 지점ID들로 담당자를 한 번에 조회 후 branchId별 '주' 담당자로 그룹핑
        List<Branch> branches = page.getContent();
        List<Long> branchIds = branches.stream().map(Branch::getId).toList();
        Map<Long, BranchManager> mainByBranch = branchIds.isEmpty() ? Map.of()
                : branchManagerRepository.findAllByBranchIdInAndIsDeletedFalse(branchIds).stream()
                        .filter(mgr -> "주".equals(mgr.getManagerType()) && Boolean.TRUE.equals(mgr.getStatus()))
                        .collect(Collectors.toMap(mgr -> mgr.getBranch().getId(), mgr -> mgr, (a, b) -> a));

        List<BranchResponse> data = branches.stream().map(b -> {
            BranchResponse r = BranchResponse.from(b);
            BranchManager main = mainByBranch.get(b.getId());
            if (main != null) {
                r.setMainManagerName(main.getManagerName());
                r.setMainManagerContact(main.getContact());
            }
            return r;
        }).toList();
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<BranchResponse> create(BranchUpsertRequest req) {
        if (req.getBranchName() == null || req.getBranchName().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "지점명은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        Branch branch = Branch.builder()
                .branchName(req.getBranchName())
                .status(req.getStatus() != null ? req.getStatus() : true)
                .address(req.getAddress())
                .addressDetail(req.getAddressDetail())
                .managerName(req.getManagerName())
                .phone(req.getPhone())
                .fax(req.getFax())
                .appliedDate(req.getAppliedDate() != null ? LocalDate.parse(req.getAppliedDate()) : LocalDate.now())
                .isDeleted(false)
                .build();
        branchRepository.save(branch);
        return CommonResponse.created(BranchResponse.from(branch));
    }

    @Transactional
    public CommonResponse<BranchResponse> update(Long id, BranchUpsertRequest req) {
        Branch b = getBranch(id);
        if (req.getBranchName() != null) b.setBranchName(req.getBranchName());
        if (req.getStatus() != null) b.setStatus(req.getStatus());
        if (req.getAddress() != null) b.setAddress(req.getAddress());
        if (req.getAddressDetail() != null) b.setAddressDetail(req.getAddressDetail());
        if (req.getManagerName() != null) b.setManagerName(req.getManagerName());
        if (req.getPhone() != null) b.setPhone(req.getPhone());
        if (req.getFax() != null) b.setFax(req.getFax());
        if (req.getAppliedDate() != null) b.setAppliedDate(LocalDate.parse(req.getAppliedDate()));
        return CommonResponse.success(BranchResponse.from(b));
    }

    @Transactional
    public void delete(Long id) {
        Branch b = getBranch(id);
        branchManagerRepository.findAllByBranchIdAndIsDeletedFalse(id).forEach(m -> m.setIsDeleted(true));
        b.setIsDeleted(true);
    }

    public CommonResponse<List<BranchManagerResponse>> findManagers(Long branchId, CommonSearchRequest request) {
        Branch branch = getBranch(branchId);
        Page<BranchManager> page = branchManagerRepository.findAllByBranchIdAndIsDeletedFalse(branchId, request.toPageable());
        List<BranchManagerResponse> data = page.getContent().stream()
                .map(m -> BranchManagerResponse.from(m, branch)).toList();
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<BranchManagerResponse> createManager(BranchManagerUpsertRequest req) {
        if (req.getBranchId() == null) {
            throw new CustomException("BRANCH_ID_REQUIRED", "branchId는 필수입니다.");
        }
        if (req.getManagerName() == null || req.getManagerName().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "담당자명은 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (req.getContact() == null || req.getContact().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "담당자 연락처는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        Branch branch = getBranch(req.getBranchId());
        BranchManager mgr = BranchManager.builder()
                .branch(branch)
                .managerName(req.getManagerName())
                .contact(req.getContact())
                .email(req.getEmail())
                .managerType(req.getManagerType() != null ? req.getManagerType() : "주")
                .status(req.getStatus() != null ? req.getStatus() : true)
                .isDeleted(false)
                .build();
        branchManagerRepository.save(mgr);
        return CommonResponse.created(BranchManagerResponse.from(mgr, branch));
    }

    @Transactional
    public CommonResponse<BranchManagerResponse> updateManager(Long id, BranchManagerUpsertRequest req) {
        BranchManager m = branchManagerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("BRANCH_MANAGER_NOT_FOUND", "담당자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (req.getManagerName() != null) m.setManagerName(req.getManagerName());
        if (req.getContact() != null) m.setContact(req.getContact());
        if (req.getEmail() != null) m.setEmail(req.getEmail());
        if (req.getManagerType() != null) m.setManagerType(req.getManagerType());
        if (req.getStatus() != null) m.setStatus(req.getStatus());
        return CommonResponse.success(BranchManagerResponse.from(m, m.getBranch()));
    }

    @Transactional
    public void deleteManager(Long id) {
        BranchManager m = branchManagerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("BRANCH_MANAGER_NOT_FOUND", "담당자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        m.setIsDeleted(true);
    }

    private Branch getBranch(Long id) {
        return branchRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "ID " + id + "에 해당하는 지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
