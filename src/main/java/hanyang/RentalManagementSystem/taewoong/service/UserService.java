package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.dto.CommonSearchRequest;
import hanyang.RentalManagementSystem.common.dto.Pagination;
import hanyang.RentalManagementSystem.common.entity.User;
import hanyang.RentalManagementSystem.common.enums.Role;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.BranchRepository;
import hanyang.RentalManagementSystem.common.repository.EmployeeRepository;
import hanyang.RentalManagementSystem.common.repository.UserRepository;
import hanyang.RentalManagementSystem.taewoong.dto.UserResponse;
import hanyang.RentalManagementSystem.taewoong.dto.UserUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    // 교수님 피드백 #4: Map 대신 DTO 응답
    public CommonResponse<List<UserResponse>> findAll(CommonSearchRequest request) {
        Page<User> page = userRepository.findAllByIsDeletedFalse(request.toPageable());
        List<UserResponse> data = page.getContent().stream().map(UserResponse::from).toList();
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<UserResponse> create(UserUpsertRequest req) {
        if (req.getUserLoginId() == null || req.getUserLoginId().isBlank()) {
            throw new CustomException("INVALID_REQUEST", "로그인 ID는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUserLoginIdAndIsDeletedFalse(req.getUserLoginId())) {
            throw new CustomException("DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT);
        }
        String password = req.getPassword();
        User user = User.builder()
                .userName(req.getUserName())
                .userLoginId(req.getUserLoginId())
                .password(password != null && !password.isBlank() ? passwordEncoder.encode(password) : null)
                .role(Role.fromString(req.getRole()))
                .contact(req.getContact())
                .email(req.getEmail())
                .build();
        applyLinks(user, req);
        userRepository.save(user);
        return CommonResponse.created(UserResponse.from(user));
    }

    @Transactional
    public CommonResponse<UserResponse> update(Long id, UserUpsertRequest req) {
        User u = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (req.getUserName() != null) u.setUserName(req.getUserName());
        if (req.getContact() != null) u.setContact(req.getContact());
        if (req.getEmail() != null) u.setEmail(req.getEmail());
        if (req.getRole() != null) u.setRole(Role.fromString(req.getRole()));
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        applyLinks(u, req);
        return CommonResponse.success(UserResponse.from(u));
    }

    @Transactional
    public void delete(Long id) {
        User u = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        u.setIsDeleted(true);
    }

    /** 역할 모델: STAFF→직원(employee), BRANCH_MANAGER→지점(branch) 연결 (선택적). */
    private void applyLinks(User user, UserUpsertRequest req) {
        if (req.getEmployeeId() != null) {
            user.setEmployee(employeeRepository.findById(req.getEmployeeId())
                    .orElseThrow(() -> new CustomException("EMPLOYEE_NOT_FOUND", "직원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)));
        }
        if (req.getBranchId() != null) {
            user.setBranch(branchRepository.findById(req.getBranchId())
                    .orElseThrow(() -> new CustomException("BRANCH_NOT_FOUND", "지점을 찾을 수 없습니다.", HttpStatus.NOT_FOUND)));
        }
    }
}
