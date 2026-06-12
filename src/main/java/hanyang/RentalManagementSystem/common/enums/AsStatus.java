package hanyang.RentalManagementSystem.common.enums;

public enum AsStatus {
    AS_RECEIVED,   // 이상접수
    AS_PROGRESS,   // 처리중
    AS_COMPLETED;  // 완료

    public static AsStatus fromString(String value) {
        if (value == null || value.isBlank()) return null;
        return AsStatus.valueOf(value.trim().toUpperCase());
    }
}