package hanyang.RentalManagementSystem.taewoong.service;

import hanyang.RentalManagementSystem.common.dto.CommonResponse;
import hanyang.RentalManagementSystem.common.entity.Center;
import hanyang.RentalManagementSystem.common.repository.CenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CenterService {
    private final CenterRepository centerRepository;

    public CommonResponse<Map<String, Object>> get() {
        Optional<Center> opt = centerRepository.findAll().stream().findFirst();
        return CommonResponse.success(opt.map(this::toMap).orElse(new LinkedHashMap<>()));
    }

    @Transactional
    public CommonResponse<Map<String, Object>> save(Map<String, Object> body) {
        Center center = centerRepository.findAll().stream().findFirst().orElse(new Center());
        if (body.containsKey("centerName")) center.setCenterName((String) body.get("centerName"));
        if (body.containsKey("centerNameAbbr")) center.setCenterNameAbbr((String) body.get("centerNameAbbr"));
        if (body.containsKey("bizRegNo")) center.setBizRegNo((String) body.get("bizRegNo"));
        if (body.containsKey("corpRegNo")) center.setCorpRegNo((String) body.get("corpRegNo"));
        if (body.containsKey("bizType")) center.setBizType((String) body.get("bizType"));
        if (body.containsKey("bizCategory")) center.setBizCategory((String) body.get("bizCategory"));
        if (body.containsKey("ceoName")) center.setCeoName((String) body.get("ceoName"));
        if (body.containsKey("phone")) center.setPhone((String) body.get("phone"));
        if (body.containsKey("fax")) center.setFax((String) body.get("fax"));
        if (body.containsKey("email")) center.setEmail((String) body.get("email"));
        if (body.containsKey("zipCode")) center.setZipCode((String) body.get("zipCode"));
        if (body.containsKey("address")) center.setAddress((String) body.get("address"));
        if (body.containsKey("addressDetail")) center.setAddressDetail((String) body.get("addressDetail"));
        if (body.containsKey("taxManagerName")) center.setTaxManagerName((String) body.get("taxManagerName"));
        if (body.containsKey("taxManagerEmail")) center.setTaxManagerEmail((String) body.get("taxManagerEmail"));
        if (body.containsKey("taxManagerPhone")) center.setTaxManagerPhone((String) body.get("taxManagerPhone"));
        if (body.containsKey("taxManagerFax")) center.setTaxManagerFax((String) body.get("taxManagerFax"));
        centerRepository.save(center);
        return CommonResponse.success(toMap(center));
    }

    private Map<String, Object> toMap(Center c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId()); m.put("centerName", c.getCenterName());
        m.put("bizRegNo", c.getBizRegNo()); m.put("ceoName", c.getCeoName());
        m.put("phone", c.getPhone()); m.put("address", c.getAddress());
        return m;
    }
}
