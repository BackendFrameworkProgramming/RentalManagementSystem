package hanyang.RentalManagementSystem.gyumin.dto;

import hanyang.RentalManagementSystem.common.entity.Device;
import hanyang.RentalManagementSystem.common.entity.Rental;
import lombok.*;

import java.time.LocalDate;

/** 임대 응답. 기존 Map 키 유지(프론트 계약 보존). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RentalResponse {
    private Long id;
    private String status;
    private LocalDate applyDate;
    private LocalDate returnDate;
    private LocalDate expectedStartDate;
    private LocalDate expectedReturnDate;
    private LocalDate receiptDate;
    private String wornStatus;
    private String deviceId;
    private String battery;
    private String modelName;
    private Long branchId;
    private String branchName;
    private Long userId;
    private String userName;

    public static RentalResponse from(Rental r) {
        Device device = r.getDevice();
        String modelName = (device != null && device.getModelVersion() != null && device.getModelVersion().getModel() != null)
                ? device.getModelVersion().getModel().getModelName() : null;
        return RentalResponse.builder()
                .id(r.getId())
                .status(r.getStatus() == null ? null : r.getStatus().name())
                .applyDate(r.getApplyDate())
                .returnDate(r.getReturnDate())
                .expectedStartDate(r.getUseStartDate())
                .expectedReturnDate(r.getReturnDueDate())
                .receiptDate(r.getReceiveDate())
                .wornStatus(r.getWearYn() == null ? null : (r.getWearYn() ? "Y" : "N"))
                .deviceId(device != null ? device.getDeviceId() : null)
                .battery(device != null ? device.getBattery() : null)
                .modelName(modelName)
                .branchId(r.getBranch() != null ? r.getBranch().getId() : null)
                .branchName(r.getBranch() != null ? r.getBranch().getBranchName() : null)
                .userId(r.getUser() != null ? r.getUser().getId() : null)
                .userName(r.getUser() != null ? r.getUser().getUserName() : null)
                .build();
    }
}
