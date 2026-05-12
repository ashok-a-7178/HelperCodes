package com.helpercodes.crudbenchmark.analytics;

import com.helpercodes.crudbenchmark.data.CrudActionType;
import com.helpercodes.crudbenchmark.engine.ActionLatency;
import com.helpercodes.crudbenchmark.engine.ActionStats;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class AnalyticsBuilder {
    private static final double NANOS_PER_MILLI = 1_000_000.0;

    private AnalyticsBuilder() {
    }

    public static List<ActionStats> summarize(List<ActionLatency> latencies) {
        Map<String, Map<CrudActionType, MutableStats>> grouped = new LinkedHashMap<>();
        for (ActionLatency latency : latencies) {
            grouped.computeIfAbsent(latency.component(), key -> new EnumMap<>(CrudActionType.class))
                    .computeIfAbsent(latency.actionType(), key -> new MutableStats())
                    .add(latency.latencyNanos());
        }

        List<ActionStats> rows = new ArrayList<>();
        for (Map.Entry<String, Map<CrudActionType, MutableStats>> componentEntry : grouped.entrySet()) {
            for (CrudActionType actionType : CrudActionType.values()) {
                MutableStats stats = componentEntry.getValue().get(actionType);
                if (stats != null) {
                    rows.add(stats.toRow(componentEntry.getKey(), actionType));
                }
            }
        }
        return rows;
    }

    private static final class MutableStats {
        private long count;
        private long total;
        private long min = Long.MAX_VALUE;
        private long max = Long.MIN_VALUE;

        private void add(long nanos) {
            count++;
            total += nanos;
            min = Math.min(min, nanos);
            max = Math.max(max, nanos);
        }

        private ActionStats toRow(String component, CrudActionType actionType) {
            return new ActionStats(component, actionType, count,
                    (total / (double) count) / NANOS_PER_MILLI,
                    min / NANOS_PER_MILLI,
                    max / NANOS_PER_MILLI);
        }
    }
}
