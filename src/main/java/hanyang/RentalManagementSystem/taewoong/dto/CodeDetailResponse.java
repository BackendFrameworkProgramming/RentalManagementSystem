package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.CodeDetail;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeDetailResponse {
    private Long id;
    private String groupCode;
    private String codeValue;
    private String codeName;
    private Integer sortOrder;
    private Boolean useYn;

    public static CodeDetailResponse from(CodeDetail d) {
        return CodeDetailResponse.builder()
                .id(d.getId())
                .groupCode(d.getCodeGroup() != null ? d.getCodeGroup().getGroupCode() : null)
                .codeValue(d.getCodeValue())
                .codeName(d.getCodeName())
                .sortOrder(d.getSortOrder())
                .useYn(d.getUseYn())
                .build();
    }
}
