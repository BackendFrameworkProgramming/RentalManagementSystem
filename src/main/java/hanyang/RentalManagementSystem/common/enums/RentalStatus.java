package hanyang.RentalManagementSystem.common.enums;

/**
 * 임대 상태. (교수님 피드백 #5: 문자열 하드코딩 대신 Enum)
 * 기존 DB 값(APPLIED / RENTING / RETURNED 등)과 일치.
 */
public enum RentalStatus {
    APPLIED,         // 신청
    RECEIPT_WAITING, // 수령 대기
    RENTING,         // 임대중
    RETURNED,        // 반납
    RENTAL_READY,    // (디바이스) 임대 가능 복귀
    AS_RECEIVED,     // AS 접수로 전환
    REPLACED;        // 교체

    public static RentalStatus fromString(String value) {
        if (value == null || value.isBlank()) return null;
        return RentalStatus.valueOf(value.trim().toUpperCase());
    }
}
