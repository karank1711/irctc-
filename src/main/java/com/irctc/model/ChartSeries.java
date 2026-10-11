package com.irctc.model;

import java.util.ArrayList;
import java.util.List;

public class ChartSeries {
    private final List<String> labels = new ArrayList<>();
    private final List<Long> counts = new ArrayList<>();

    public void add(String label, long count) {
        labels.add(label);
        counts.add(count);
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Long> getCounts() {
        return counts;
    }

    public boolean isWithoutRows() {
        for (Long count : counts) {
            if (count != null && count > 0) {
                return false;
            }
        }
        return true;
    }
}
