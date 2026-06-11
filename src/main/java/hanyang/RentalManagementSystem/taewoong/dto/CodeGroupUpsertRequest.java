package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeGroupUpsertRequest {
    private String groupCode;
    private String groupName;
    private String description;
    private Boolean useYn;
}
