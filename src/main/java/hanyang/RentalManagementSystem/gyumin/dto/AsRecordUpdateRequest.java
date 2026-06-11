package hanyang.RentalManagementSystem.gyumin.dto;

import lombok.*;

/** A/S 수정 요청(상태 변경). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AsRecordUpdateRequest {
    private String status;
}
