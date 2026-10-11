package com.irctc.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Compares a period with the one before it.
 * When the earlier period has no rows, there is no honest percentage to show.
 */
public final class PeriodComparison {
    private final boolean available;
    private final BigDecimal percent;
    private final boolean increase;

    private PeriodComparison(boolean available, BigDecimal percent, boolean increase) {
        this.available = available;
        this.percent = percent;
        this.increase = increase;
    }

    public static PeriodComparison of(long current, long previous) {
        return of(BigDecimal.valueOf(current), BigDecimal.valueOf(previous));
    }

    public static PeriodComparison of(BigDecimal current, BigDecimal previous) {
        BigDecimal safeCurrent = current == null ? BigDecimal.ZERO : current;
        if (previous == null || previous.compareTo(BigDecimal.ZERO) <= 0) {
            return new PeriodComparison(false, null, false);
        }
        BigDecimal change = safeCurrent.subtract(previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP);
        return new PeriodComparison(true, change, change.signum() >= 0);
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isIncrease() {
        return increase;
    }

    public String getLabel() {
        if (!available || percent == null) {
            return "Comparison unavailable";
        }
        return percent.abs().setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";
    }
}
