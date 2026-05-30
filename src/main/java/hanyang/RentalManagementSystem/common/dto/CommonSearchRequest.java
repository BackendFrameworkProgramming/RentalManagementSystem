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
        return PageRequest.of(Math.max(0, page - 1), size, sort);
    }
}
