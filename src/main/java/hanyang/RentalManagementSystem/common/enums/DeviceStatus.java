package hanyang.RentalManagementSystem.common.enums;

/**
 * 디바이스 상태. (교수님 피드백 #5: 코드상 문자열 하드코딩 대신 Enum 타입)
 * 기존 DB 값(INCOMING / RENTAL_READY / AS_RECEIVED 등)과 정확히 일치하도록 정의.
 */
public enum DeviceStatus {
    INCOMING,       // 입고
    RENTAL_READY,   // 임대 가능(지점 출고됨)
    RENTING,        // 임대중
    AS_RECEIVED,    // AS 접수
    AS_PROGRESS,    // AS 진행
    RETURNED,       // 반납
    DISPOSED;       // 폐기

    public static DeviceStatus fromString(String value) {
        if (value == null || value.isBlank()) return null;
        return DeviceStatus.valueOf(value.trim().toUpperCase());
    }
}
