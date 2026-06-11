package hanyang.RentalManagementSystem.gyumin.dto;

import hanyang.RentalManagementSystem.common.entity.AsRecord;
import hanyang.RentalManagementSystem.common.entity.Device;
import lombok.*;

import java.time.LocalDate;

/** A/S 응답. 기존 Map 키 유지(프론트 계약 보존). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsRecordResponse {
    private Long id;
    private String status;
    private LocalDate requestDate;
    private LocalDate completionDate;
    private String asDescription;
    private String deviceId;
    private String modelName;
    private Long branchId;
    private String branchName;
    private Long userId;
    private String userName;

    public static AsRecordResponse from(AsRecord a) {
        Device device = a.getDevice();
        String modelName = (device != null && device.getModelVersion() != null && device.getModelVersion().getModel() != null)
                ? device.getModelVersion().getModel().getModelName() : null;
        boolean hasUser = a.getRental() != null && a.getRental().getUser() != null;
        return AsRecordResponse.builder()
                .id(a.getId())
                .status(a.getStatus())
                .requestDate(a.getReceiptDate())
                .completionDate(a.getCompleteDate())
                .asDescription(a.getReceiptContent())
                .deviceId(device != null ? device.getDeviceId() : null)
                .modelName(modelName)
                .branchId(a.getBranch() != null ? a.getBranch().getId() : null)
                .branchName(a.getBranch() != null ? a.getBranch().getBranchName() : null)
                .userId(hasUser ? a.getRental().getUser().getId() : null)
                .userName(hasUser ? a.getRental().getUser().getUserName() : null)
                .build();
    }
}
