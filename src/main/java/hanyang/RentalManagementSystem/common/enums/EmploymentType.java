package hanyang.RentalManagementSystem.common.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * 근무 형태. (교수님 피드백 #5: 문자열 하드코딩 대신 Enum)
 * 참고: PM이 한 Device.status(DeviceStatus) 적용 방식 그대로.
 * 기존 DB에 한글 값(정규직/계약직)이 남아 있어도 읽히도록 라벨 매핑을 둠.
 */
public enum EmploymentType {
    FULL_TIME("정규직"),
    CONTRACT("계약직");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** 영문 enum명 또는 기존 한글 라벨 모두 허용. */
    public static EmploymentType fromString(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        for (EmploymentType t : values()) {
            if (t.name().equalsIgnoreCase(v) || t.label.equals(v)) return t;
        }
        throw new IllegalArgumentException("알 수 없는 근무형태: " + value);
    }

    /**
     * DB에 기존 한글 값이 남아 있어도 읽히도록 양방향 변환.
     * 쓰기는 영문 name으로 저장 → 갱신되는 행부터 점진적으로 정규화(데이터 정리 SQL 불필요).
     */
    @Converter
    public static class JpaConverter implements AttributeConverter<EmploymentType, String> {
        @Override
        public String convertToDatabaseColumn(EmploymentType attribute) {
            return attribute == null ? null : attribute.name();
        }

        @Override
        public EmploymentType convertToEntityAttribute(String dbData) {
            return EmploymentType.fromString(dbData);
        }
    }
}
