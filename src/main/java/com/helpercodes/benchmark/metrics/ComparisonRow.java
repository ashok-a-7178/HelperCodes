package com.helpercodes.benchmark.metrics;

import com.helpercodes.benchmark.data.QueryType;

public record ComparisonRow(
        int queryId,
        QueryType queryType,
        int threads,
        int intersection,
        double jaccard,
        double recallAtK,
        double spearman,
        double top1Overlap,
        double top1To5Overlap,
        double top5To10Overlap) {
}
