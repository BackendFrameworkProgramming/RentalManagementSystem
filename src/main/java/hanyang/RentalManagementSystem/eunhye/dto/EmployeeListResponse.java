package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;
import java.util.List;

/** 직원 목록 응답. 기존 Map 키 {employees:[...]} 유지. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeListResponse {
    private List<EmployeeResponse> employees;
}
