package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ModelUpsertRequest {
    private String modelName;
    private String manufacturer;
    private String description;
}
