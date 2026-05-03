package hanyang.RentalManagementSystem.taewoong.controller;

import hanyang.RentalManagementSystem.common.dto.*;
import hanyang.RentalManagementSystem.common.entity.SystemErrorLog;
import hanyang.RentalManagementSystem.common.repository.SystemErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
        List<Map<String, Object>> data = page.getContent().stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", l.getId()); m.put("errorCode", l.getErrorCode()); m.put("errorMessage", l.getErrorMessage());
            m.put("requestUrl", l.getRequestUrl()); m.put("requestMethod", l.getRequestMethod());
            m.put("clientIp", l.getClientIp()); m.put("createdAt", l.getCreatedAt());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(CommonResponse.success(data, Pagination.of(page)));
    }
}
