package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.DesignHistory;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.DesignHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesignHistoryService {

    private final DesignHistoryRepository repo;

    public CommonResponse<List<Map<String, Object>>> findAll() {
        List<Map<String, Object>> data = repo.findAllByIsDeletedFalseOrderByRoundAsc()
                .stream().map(this::toMap).collect(Collectors.toList());
        return CommonResponse.success(data);
    }

    public CommonResponse<Map<String, Object>> findById(Long id) {
        DesignHistory h = repo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("HISTORY_NOT_FOUND", "설계이력을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        return CommonResponse.success(toMap(h));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> create(Map<String, Object> body) {
        DesignHistory h = DesignHistory.builder()
                .round(((Number) body.get("round")).intValue())
                .roundDate((String) body.get("roundDate"))
                .source((String) body.get("source"))
                .sourceType((String) body.get("sourceType"))
                .title((String) body.get("title"))
                .changes(body.get("changes") != null ? body.get("changes").toString() : "[]")
                .build();
        repo.save(h);
        return CommonResponse.created(toMap(h));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> update(Long id, Map<String, Object> body) {
        DesignHistory h = repo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("HISTORY_NOT_FOUND", "설계이력을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        if (body.containsKey("round")) h.setRound(((Number) body.get("round")).intValue());
        if (body.containsKey("roundDate")) h.setRoundDate((String) body.get("roundDate"));
        if (body.containsKey("source")) h.setSource((String) body.get("source"));
        if (body.containsKey("sourceType")) h.setSourceType((String) body.get("sourceType"));
        if (body.containsKey("title")) h.setTitle((String) body.get("title"));
        if (body.containsKey("changes")) h.setChanges(body.get("changes").toString());
        return CommonResponse.success(toMap(h));
    }

    @Transactional
    public void delete(Long id) {
        DesignHistory h = repo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("HISTORY_NOT_FOUND", "설계이력을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        h.setIsDeleted(true);
    }

    private Map<String, Object> toMap(DesignHistory h) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", h.getId());
        m.put("round", h.getRound());
        m.put("roundDate", h.getRoundDate());
        m.put("source", h.getSource());
        m.put("sourceType", h.getSourceType());
        m.put("title", h.getTitle());
        m.put("changes", h.getChanges());
        m.put("createdAt", h.getCreatedAt());
        return m;
    }
}
