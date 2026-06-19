package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 배치 처리 결과 응답 (교수님 #4: Map 대신 DTO). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BatchResultResponse {
    private int updatedCount;
}
