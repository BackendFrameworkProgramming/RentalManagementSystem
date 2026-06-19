package hanyang.RentalManagementSystem.common.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommonSearchRequest {
    private String searchField;
    private String searchKeyword;
    private String searchStatus;
    @Builder.Default
    private String orderField = "id";
    @Builder.Default
    private String orderType = "DESC";
    @Builder.Default
    private Integer page = 1;
    @Builder.Default
    private Integer size = 20;
    private String startDate;
    private String endDate;
    @Builder.Default
    private Boolean includeDeleted = false;

    public Pageable toPageable() {
        Sort sort = "ASC".equalsIgnoreCase(orderType)
                ? Sort.by(Sort.Direction.ASC, orderField)
                : Sort.by(Sort.Direction.DESC, orderField);
        // 사용자 입력 page/size를 안전 범위로 제한(정수 오버플로·과도한 요청 방어)
        int safePage = (page == null || page < 1) ? 1 : Math.min(page, 100_000);
        int safeSize = (size == null || size < 1) ? 20 : Math.min(size, 200);
        return PageRequest.of(safePage - 1, safeSize, sort);
    }
}
