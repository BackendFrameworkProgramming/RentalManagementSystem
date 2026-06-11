package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileUploadResponse {
    private String savedFileName;
    private String originalFileName;
}
