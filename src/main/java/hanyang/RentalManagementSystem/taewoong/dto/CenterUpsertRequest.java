package hanyang.RentalManagementSystem.taewoong.dto;

import lombok.*;

/** 센터정보 저장 요청. null 필드는 변경하지 않음. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CenterUpsertRequest {
    private String centerName;
    private String centerNameAbbr;
    private String bizRegNo;
    private String corpRegNo;
    private String bizType;
    private String bizCategory;
    private String ceoName;
    private String phone;
    private String fax;
    private String email;
    private String zipCode;
    private String address;
    private String addressDetail;
    private String taxManagerName;
    private String taxManagerEmail;
    private String taxManagerPhone;
    private String taxManagerFax;
}
