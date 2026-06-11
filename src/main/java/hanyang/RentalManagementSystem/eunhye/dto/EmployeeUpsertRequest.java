package hanyang.RentalManagementSystem.eunhye.dto;

import lombok.*;

/** 직원 등록/수정 요청. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmployeeUpsertRequest {
    private Long teamId;
    private String empName;
    private String empNo;
    private String jobTitle;
    private String employmentType;
    private String workStatus;
    private String hireDate; // ISO yyyy-MM-dd
    private String remark;
}
