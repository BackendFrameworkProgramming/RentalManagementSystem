package hanyang.RentalManagementSystem.common.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pagination {
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static Pagination of(org.springframework.data.domain.Page<?> page) {
        return Pagination.builder()
                .page(page.getNumber() + 1)
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
