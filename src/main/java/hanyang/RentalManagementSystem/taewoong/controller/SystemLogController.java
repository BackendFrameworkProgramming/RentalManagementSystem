package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.common.entity.SystemErrorLog;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.SystemErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system-logs")
@RequiredArgsConstructor
public class SystemLogController {
    private final SystemErrorLogRepository repo;

    @GetMapping
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> list(CommonSearchRequest request) {
        Page<SystemErrorLog> page = repo.findAll(request.toPageable());
        List<Map<String, Object>> data = page.getContent().stream().map(this::toMap).collect(Collectors.toList());
        return ResponseEntity.ok(CommonResponse.success(data, Pagination.of(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<Map<String, Object>>> detail(@PathVariable Long id) {
        SystemErrorLog log = repo.findById(id)
                .orElseThrow(() -> new CustomException("LOG_NOT_FOUND", "에러 로그를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        Map<String, Object> m = toMap(log);
        m.put("requestBody", log.getRequestBody());
        m.put("stackTrace", log.getStackTrace());
        m.put("pageName", resolvePageName(log.getRequestUrl()));
        return ResponseEntity.ok(CommonResponse.success(m));
    }

    private Map<String, Object> toMap(SystemErrorLog l) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("errorCode", l.getErrorCode());
        m.put("errorMessage", l.getErrorMessage());
        m.put("requestUrl", l.getRequestUrl());
        m.put("requestMethod", l.getRequestMethod());
        m.put("clientIp", l.getClientIp());
        m.put("pageName", resolvePageName(l.getRequestUrl()));
        m.put("createdAt", l.getCreatedAt());
        return m;
    }

    private String resolvePageName(String url) {
        if (url == null) return "알 수 없음";
        Map<String, String> map = new LinkedHashMap<>();
        map.put("/api/devices", "디바이스 현황");
        map.put("/api/rentals", "디바이스 임대 현황");
        map.put("/api/biometric", "생체정보/응급");
        map.put("/api/as-records", "AS 관리");
        map.put("/api/branches", "지점 관리");
        map.put("/api/center", "센터정보");
        map.put("/api/departments", "부서/팀");
        map.put("/api/employees", "센터 담당직원");
        map.put("/api/models", "모델 관리");
        map.put("/api/common-codes", "공통코드");
        map.put("/api/users", "사용자 관리");
        map.put("/api/system-logs", "에러로그");
        map.put("/api/auth", "인증");
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (url.startsWith(e.getKey())) return e.getValue();
        }
        return "기타 (" + url + ")";
    }
}
