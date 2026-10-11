package com.irctc.util;

import java.util.List;

/** Builds a small SVG line from numeric series. The markup contains no user text. */
public final class Sparkline {
    private Sparkline() {
    }

    public static String toSvg(List<Long> points) {
        List<Long> series = points == null || points.isEmpty()
                ? List.of(0L, 0L, 0L, 0L, 0L, 0L, 0L)
                : points;
        long max = 1L;
        for (Long point : series) {
            max = Math.max(max, point == null ? 0L : point);
        }
        StringBuilder coordinates = new StringBuilder();
        int count = series.size();
        for (int index = 0; index < count; index++) {
            long point = series.get(index) == null ? 0L : series.get(index);
            double x = count == 1 ? 60 : (120.0 * index) / (count - 1);
            double y = 32 - (24.0 * point / max);
            if (index > 0) {
                coordinates.append(' ');
            }
            coordinates.append(round(x)).append(',').append(round(y));
        }
        return "<svg class=\"sparkline\" viewBox=\"0 0 120 36\" aria-hidden=\"true\"><polyline points=\""
                + coordinates + "\"></polyline></svg>";
    }

    private static String round(double value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }
}
