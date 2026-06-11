package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.ModelVersion;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModelVersionResponse {
    private Long id;
    private String version;
    private String spec;
    private LocalDate releaseDate;
    private String manualFileName;

    public static ModelVersionResponse from(ModelVersion v) {
        return ModelVersionResponse.builder()
                .id(v.getId())
                .version(v.getVersion())
                .spec(v.getSpec())
                .releaseDate(v.getReleaseDate())
                .manualFileName(v.getManualFileName())
                .build();
    }
}
