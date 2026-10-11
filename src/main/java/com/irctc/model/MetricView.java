package com.irctc.model;

import com.irctc.util.PeriodComparison;
import com.irctc.util.Sparkline;

import java.util.List;

public class MetricView {
    private final String label;
    private final String icon;
    private final String tone;
    private final String displayValue;
    private final String rawValue;
    private final boolean comparisonAvailable;
    private final boolean increase;
    private final String changeLabel;
    private final String sparklineSvg;

    public MetricView(String label, String icon, String tone, String displayValue, String rawValue,
                      PeriodComparison comparison, List<Long> sparkline) {
        this.label = label;
        this.icon = icon;
        this.tone = tone;
        this.displayValue = displayValue;
        this.rawValue = rawValue;
        this.comparisonAvailable = comparison.isAvailable();
        this.increase = comparison.isIncrease();
        this.changeLabel = comparison.getLabel();
        this.sparklineSvg = Sparkline.toSvg(sparkline);
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getTone() {
        return tone;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public String getRawValue() {
        return rawValue;
    }

    public boolean isComparisonAvailable() {
        return comparisonAvailable;
    }

    public boolean isIncrease() {
        return increase;
    }

    public String getChangeLabel() {
        return changeLabel;
    }

    public String getSparklineSvg() {
        return sparklineSvg;
    }
}
