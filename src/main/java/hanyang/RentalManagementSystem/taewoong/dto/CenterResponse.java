package hanyang.RentalManagementSystem.taewoong.dto;

import hanyang.RentalManagementSystem.common.entity.Center;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CenterResponse {
    private Long id;
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

    public static CenterResponse from(Center c) {
        return CenterResponse.builder()
                .id(c.getId())
                .centerName(c.getCenterName())
                .centerNameAbbr(c.getCenterNameAbbr())
                .bizRegNo(c.getBizRegNo())
                .corpRegNo(c.getCorpRegNo())
                .bizType(c.getBizType())
                .bizCategory(c.getBizCategory())
                .ceoName(c.getCeoName())
                .phone(c.getPhone())
                .fax(c.getFax())
                .email(c.getEmail())
                .zipCode(c.getZipCode())
                .address(c.getAddress())
                .addressDetail(c.getAddressDetail())
                .taxManagerName(c.getTaxManagerName())
                .taxManagerEmail(c.getTaxManagerEmail())
                .taxManagerPhone(c.getTaxManagerPhone())
                .taxManagerFax(c.getTaxManagerFax())
                .build();
    }
}
