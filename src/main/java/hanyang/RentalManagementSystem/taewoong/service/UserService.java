package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.common.entity.User;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final PasswordEncoder passwordEncoder;

    public CommonResponse<List<Map<String, Object>>> findAll(CommonSearchRequest request) {
        Page<User> page = userRepository.findAllByIsDeletedFalse(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId()); m.put("userName", u.getUserName()); m.put("userLoginId", u.getUserLoginId());
            m.put("role", u.getRole()); m.put("contact", u.getContact()); m.put("email", u.getEmail());
            return m;
        }).collect(Collectors.toList());
        return CommonResponse.success(data, Pagination.of(page));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> create(Map<String, Object> body) {
        String loginId = (String) body.get("userLoginId");
        if (loginId == null || loginId.isBlank()) {
            throw new CustomException("INVALID_REQUEST", "로그인 ID는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByUserLoginIdAndIsDeletedFalse(loginId)) {
            throw new CustomException("DUPLICATE_LOGIN_ID", "이미 사용 중인 아이디입니다.", HttpStatus.CONFLICT);
        }
        String password = (String) body.get("password");
        String role = body.get("role") != null ? body.get("role").toString() : "USER";
        User user = User.builder()
                .userName((String) body.get("userName"))
                .userLoginId(loginId)
                .password(password != null && !password.isBlank() ? passwordEncoder.encode(password) : null)
                .role(role)
                .contact((String) body.get("contact"))
                .email((String) body.get("email"))
                .build();
        userRepository.save(user);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", user.getId()); m.put("userName", user.getUserName()); m.put("role", user.getRole());
        return CommonResponse.created(m);
    }

    @Transactional
    public CommonResponse<Map<String, Object>> update(Long id, Map<String, Object> body) {
        User u = userRepository.findById(id).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("userName")) u.setUserName((String) body.get("userName"));
        if (body.containsKey("contact")) u.setContact((String) body.get("contact"));
        if (body.containsKey("email")) u.setEmail((String) body.get("email"));
        Map<String, Object> m = new LinkedHashMap<>(); m.put("id", u.getId()); m.put("userName", u.getUserName());
        return CommonResponse.success(m);
    }

    @Transactional
    public void delete(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        u.setIsDeleted(true);
    }
}
