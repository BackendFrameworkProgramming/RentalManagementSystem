package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.Center;
import hanyang.RentalManagementSystem.common.repository.CenterRepository;
import hanyang.RentalManagementSystem.taewoong.dto.CenterResponse;
import hanyang.RentalManagementSystem.taewoong.dto.CenterUpsertRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CenterService {
    private final CenterRepository centerRepository;

    public CommonResponse<CenterResponse> get() {
        Center c = centerRepository.findTopByOrderByIdAsc().orElse(null);
        return CommonResponse.success(c == null ? CenterResponse.builder().build() : CenterResponse.from(c));
    }

    @Transactional
    public CommonResponse<CenterResponse> save(CenterUpsertRequest req) {
        Center center = centerRepository.findTopByOrderByIdAsc().orElseGet(Center::new);
        if (req.getCenterName() != null) center.setCenterName(req.getCenterName());
        if (req.getCenterNameAbbr() != null) center.setCenterNameAbbr(req.getCenterNameAbbr());
        if (req.getBizRegNo() != null) center.setBizRegNo(req.getBizRegNo());
        if (req.getCorpRegNo() != null) center.setCorpRegNo(req.getCorpRegNo());
        if (req.getBizType() != null) center.setBizType(req.getBizType());
        if (req.getBizCategory() != null) center.setBizCategory(req.getBizCategory());
        if (req.getCeoName() != null) center.setCeoName(req.getCeoName());
        if (req.getPhone() != null) center.setPhone(req.getPhone());
        if (req.getFax() != null) center.setFax(req.getFax());
        if (req.getEmail() != null) center.setEmail(req.getEmail());
        if (req.getZipCode() != null) center.setZipCode(req.getZipCode());
        if (req.getAddress() != null) center.setAddress(req.getAddress());
        if (req.getAddressDetail() != null) center.setAddressDetail(req.getAddressDetail());
        if (req.getTaxManagerName() != null) center.setTaxManagerName(req.getTaxManagerName());
        if (req.getTaxManagerEmail() != null) center.setTaxManagerEmail(req.getTaxManagerEmail());
        if (req.getTaxManagerPhone() != null) center.setTaxManagerPhone(req.getTaxManagerPhone());
        if (req.getTaxManagerFax() != null) center.setTaxManagerFax(req.getTaxManagerFax());
        centerRepository.save(center);
        return CommonResponse.success(CenterResponse.from(center));
    }
}
