package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.CodeGroup;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeGroupResponse {
    private Long id;
    private String groupCode;
    private String groupName;
    private String description;
    private Boolean useYn;

    public static CodeGroupResponse from(CodeGroup g) {
        return CodeGroupResponse.builder()
                .id(g.getId())
                .groupCode(g.getGroupCode())
                .groupName(g.getGroupName())
                .description(g.getDescription())
                .useYn(g.getUseYn())
                .build();
    }
}
