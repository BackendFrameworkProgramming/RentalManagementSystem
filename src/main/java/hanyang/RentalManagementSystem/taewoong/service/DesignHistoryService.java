package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.DesignHistory;
import hanyang.RentalManagementSystem.common.exception.CustomException;
import hanyang.RentalManagementSystem.common.repository.DesignHistoryRepository;
import hanyang.RentalManagementSystem.taewoong.dto.DesignHistoryResponse;
import hanyang.RentalManagementSystem.taewoong.dto.DesignHistoryUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DesignHistoryService {

    private final DesignHistoryRepository repo;

    public CommonResponse<List<DesignHistoryResponse>> findAll() {
        List<DesignHistoryResponse> data = repo.findAllByIsDeletedFalseOrderByRoundAsc()
                .stream().map(DesignHistoryResponse::from).toList();
        return CommonResponse.success(data);
    }

    public CommonResponse<DesignHistoryResponse> findById(Long id) {
        return CommonResponse.success(DesignHistoryResponse.from(getHistory(id)));
    }

    @Transactional
    public CommonResponse<DesignHistoryResponse> create(DesignHistoryUpsertRequest req) {
        DesignHistory h = DesignHistory.builder()
                .round(req.getRound())
                .roundDate(req.getRoundDate())
                .source(req.getSource())
                .sourceType(req.getSourceType())
                .title(req.getTitle())
                .changes(req.getChanges() != null ? req.getChanges() : "[]")
                .build();
        repo.save(h);
        return CommonResponse.created(DesignHistoryResponse.from(h));
    }

    @Transactional
    public CommonResponse<DesignHistoryResponse> update(Long id, DesignHistoryUpsertRequest req) {
        DesignHistory h = getHistory(id);
        if (req.getRound() != null) h.setRound(req.getRound());
        if (req.getRoundDate() != null) h.setRoundDate(req.getRoundDate());
        if (req.getSource() != null) h.setSource(req.getSource());
        if (req.getSourceType() != null) h.setSourceType(req.getSourceType());
        if (req.getTitle() != null) h.setTitle(req.getTitle());
        if (req.getChanges() != null) h.setChanges(req.getChanges());
        return CommonResponse.success(DesignHistoryResponse.from(h));
    }

    @Transactional
    public void delete(Long id) {
        getHistory(id).setIsDeleted(true);
    }

    private DesignHistory getHistory(Long id) {
        return repo.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new CustomException("HISTORY_NOT_FOUND", "설계이력을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
}
