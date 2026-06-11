package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.AsRecord;
import lombok.*;

import java.time.LocalDate;

/** 디바이스 AS 이력 응답. 기존 Map 키 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceAsHistoryResponse {
    private Long id;
    private String status;
    private LocalDate receiptDate;
    private String receiptBy;
    private String receiptContent;
    private String confirmBy;
    private LocalDate confirmDate;
    private String confirmResult;
    private String repairContent;
    private LocalDate completeDate;
    private String assignedTo;
    private LocalDate collectDate;
    private LocalDate resendDate;
    private String userName;
    private LocalDate rentalDate;
    private String vendorName;
    private String asTypeName;

    public static DeviceAsHistoryResponse from(AsRecord r) {
        boolean hasUser = r.getRental() != null && r.getRental().getUser() != null;
        return DeviceAsHistoryResponse.builder()
                .id(r.getId())
                .status(r.getStatus())
                .receiptDate(r.getReceiptDate())
                .receiptBy(r.getReceiptBy())
                .receiptContent(r.getReceiptContent())
                .confirmBy(r.getConfirmBy())
                .confirmDate(r.getConfirmDate())
                .confirmResult(r.getConfirmResult())
                .repairContent(r.getRepairContent())
                .completeDate(r.getCompleteDate())
                .assignedTo(r.getAssignedTo())
                .collectDate(r.getCollectDate())
                .resendDate(r.getResendDate())
                .userName(hasUser ? r.getRental().getUser().getUserName() : null)
                .rentalDate(hasUser ? r.getRental().getApplyDate() : null)
                .vendorName(r.getVendor() != null ? r.getVendor().getVendorName() : null)
                .asTypeName(r.getAsType() != null ? r.getAsType().getTypeName() : null)
                .build();
    }
}
