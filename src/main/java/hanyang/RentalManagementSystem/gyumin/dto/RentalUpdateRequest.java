package hanyang.RentalManagementSystem.gyumin.dto;

import lombok.*;

/** 임대 수정 요청(상태 변경/반납 처리/착용 여부). null 필드는 변경하지 않음. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RentalUpdateRequest {
    private String status;
    private String returnDate; // ISO yyyy-MM-dd
    private Boolean wearYn;
}
