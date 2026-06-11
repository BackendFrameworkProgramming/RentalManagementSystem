package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModelVersionUpsertRequest {
    private String version;
    private String spec;
    private String releaseDate; // ISO yyyy-MM-dd
}
