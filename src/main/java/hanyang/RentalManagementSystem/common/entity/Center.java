package hanyang.RentalManagementSystem.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "center")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Center extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "center_name", length = 100, nullable = false)
    private String centerName;

    @Column(name = "center_name_abbr", length = 50)
    private String centerNameAbbr;

    @Column(name = "center_name_en", length = 100)
    private String centerNameEn;

    @Column(name = "center_name_en_abbr", length = 50)
    private String centerNameEnAbbr;

    @Column(name = "biz_reg_no", length = 20)
    private String bizRegNo;

    @Column(name = "corp_reg_no", length = 20)
    private String corpRegNo;

    @Column(name = "biz_type", length = 50)
    private String bizType;

    @Column(name = "biz_category", length = 50)
    private String bizCategory;

    @Column(name = "ceo_name", length = 50)
    private String ceoName;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String fax;

    @Column(length = 100)
    private String email;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(length = 200)
    private String address;

    @Column(name = "address_detail", length = 200)
    private String addressDetail;

    @Column(name = "tax_manager_name", length = 50)
    private String taxManagerName;

    @Column(name = "tax_manager_email", length = 100)
    private String taxManagerEmail;

    @Column(name = "tax_manager_phone", length = 20)
    private String taxManagerPhone;

    @Column(name = "tax_manager_fax", length = 20)
    private String taxManagerFax;

    @Column(name = "seal_image_path", columnDefinition = "TEXT")
    private String sealImagePath;

    @Column(name = "logo_light_path", columnDefinition = "TEXT")
    private String logoLightPath;

    @Column(name = "logo_dark_path", columnDefinition = "TEXT")
    private String logoDarkPath;
}
