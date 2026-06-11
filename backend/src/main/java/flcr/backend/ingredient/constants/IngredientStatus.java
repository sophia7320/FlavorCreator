package flcr.backend.ingredient.constants;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public enum IngredientStatus {

    EXPIRED(0, "已过期"),
    URGENT(1, "红灯"),
    WARNING(2, "黄灯"),
    NORMAL(3, "绿灯");

    private final int code;
    private final String label;

    IngredientStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isExpired() {
        return this == EXPIRED;
    }

    public boolean isUrgent() {
        return this == URGENT;
    }

    public boolean isWarning() {
        return this == WARNING;
    }

    public boolean isNormal() {
        return this == NORMAL;
    }

    public static IngredientStatus fromCode(int code) {
        for (IngredientStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    public static IngredientStatus compute(LocalDate expireDate) {
        if (expireDate == null) {
            return NORMAL;
        }
        long daysLeft = LocalDate.now().until(expireDate, ChronoUnit.DAYS);
        if (daysLeft < 0) {
            return EXPIRED;
        } else if (daysLeft <= 3) {
            return URGENT;
        } else if (daysLeft <= 15) {
            return WARNING;
        } else {
            return NORMAL;
        }
    }
}
