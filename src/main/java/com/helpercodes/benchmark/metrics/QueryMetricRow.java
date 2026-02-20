package com.helpercodes.benchmark.metrics;

import com.helpercodes.benchmark.data.QueryType;

public record QueryMetricRow(
        int queryId,
        QueryType queryType,
        int threads,
        String system,
        long latencyNanos,
        int resultCount) {
}
