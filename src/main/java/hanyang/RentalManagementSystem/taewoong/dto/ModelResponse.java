package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.Model;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModelResponse {
    private Long id;
    private String modelName;
    private String manufacturer;
    private String description;
    private List<ModelVersionResponse> versions;

    public static ModelResponse from(Model m) {
        return ModelResponse.builder()
                .id(m.getId())
                .modelName(m.getModelName())
                .manufacturer(m.getManufacturer())
                .description(m.getDescription())
                .versions(m.getVersions().stream()
                        .filter(v -> !Boolean.TRUE.equals(v.getIsDeleted()))
                        .map(ModelVersionResponse::from)
                        .toList())
                .build();
    }
}
