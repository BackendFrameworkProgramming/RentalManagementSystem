package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeDetailUpsertRequest {
    private String codeValue;
    private String codeName;
    private Integer sortOrder;
    private Boolean useYn;
}
