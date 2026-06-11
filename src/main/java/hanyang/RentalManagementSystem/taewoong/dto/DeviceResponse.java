package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.Device;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 디바이스 응답. 기존 Map 키와 동일하게 유지(프론트 계약 보존). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceResponse {
    private Long id;
    private String deviceId;
    private String status;
    private String battery;
    private Long branchId;
    private String branchName;
    private LocalDate branchSendDate;
    private Long modelVersionId;
    private String modelName;
    private String version;
    private LocalDate incomingDate;
    private LocalDate latestRentalDate;
    private LocalDate latestAsDate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DeviceResponse from(Device d) {
        return DeviceResponse.builder()
                .id(d.getId())
                .deviceId(d.getDeviceId())
                .status(d.getStatus() == null ? null : d.getStatus().name())
                .battery(d.getBattery())
                .branchId(d.getBranch() != null ? d.getBranch().getId() : null)
                .branchName(d.getBranch() != null ? d.getBranch().getBranchName() : null)
                .branchSendDate(d.getBranchSendDate())
                .modelVersionId(d.getModelVersion() != null ? d.getModelVersion().getId() : null)
                .modelName(d.getModelVersion() != null && d.getModelVersion().getModel() != null
                        ? d.getModelVersion().getModel().getModelName() : null)
                .version(d.getModelVersion() != null ? d.getModelVersion().getVersion() : null)
                .incomingDate(d.getIncomingDate())
                .latestRentalDate(d.getLatestRentalDate())
                .latestAsDate(d.getLatestAsDate())
                .remark(d.getRemark())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
