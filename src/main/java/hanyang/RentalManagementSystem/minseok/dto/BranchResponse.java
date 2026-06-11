package hanyang.RentalManagementSystem.minseok.dto;

import hanyang.RentalManagementSystem.common.entity.Branch;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 지점 응답. 기존 Map 키 유지(주담당자 정보 포함). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BranchResponse {
    private Long id;
    private String branchName;
    private Boolean status;
    private String address;
    private String addressDetail;
    private String managerName;
    private String phone;
    private String fax;
    private LocalDate appliedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String mainManagerName;
    private String mainManagerContact;

    public static BranchResponse from(Branch b) {
        return BranchResponse.builder()
                .id(b.getId())
                .branchName(b.getBranchName())
                .status(b.getStatus())
                .address(b.getAddress())
                .addressDetail(b.getAddressDetail())
                .managerName(b.getManagerName())
                .phone(b.getPhone())
                .fax(b.getFax())
                .appliedDate(b.getAppliedDate())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
