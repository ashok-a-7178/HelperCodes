package com.helpercodes.benchmark.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record LatencyStats(double avgMs, double p95Ms, double p99Ms) {
    public static LatencyStats fromNanos(List<Long> nanos) {
        if (nanos.isEmpty()) {
            return new LatencyStats(0, 0, 0);
        }
        List<Long> copy = new ArrayList<>(nanos);
        Collections.sort(copy);
        double avg = copy.stream().mapToDouble(v -> v / 1_000_000.0).average().orElse(0);
        double p95 = percentile(copy, 95);
        double p99 = percentile(copy, 99);
        return new LatencyStats(avg, p95, p99);
    }

    private static double percentile(List<Long> sortedNanos, int pct) {
        int index = (int) Math.ceil((pct / 100.0) * sortedNanos.size()) - 1;
        index = Math.max(0, Math.min(index, sortedNanos.size() - 1));
        return sortedNanos.get(index) / 1_000_000.0;
    }
}
