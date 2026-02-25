package com.helpercodes.benchmark.engine;

import com.helpercodes.benchmark.api.SearchResultItem;
import com.helpercodes.benchmark.metrics.ComparisonRow;
import com.helpercodes.benchmark.metrics.RankingMetrics;

import java.util.ArrayList;
import java.util.List;

public final class ComparisonEngine {
    private ComparisonEngine() {}

    public static List<ComparisonRow> compare(BenchmarkWorkloadResult lucene, BenchmarkWorkloadResult jvector, int topK) {
        List<ComparisonRow> rows = new ArrayList<>();
        for (int i = 0; i < lucene.rows().size(); i++) {
            BenchmarkQueryResult a = lucene.rows().get(i);
            BenchmarkQueryResult b = jvector.rows().get(i);
            RankingMetrics m = RankingMetrics.compare(a.results(), b.results(), topK);
            rows.add(new ComparisonRow(a.queryId(), a.queryType(), a.threads(), m.intersection(), m.jaccard(), m.recallAtK(),
                    m.spearman(), m.overlapTop1Pct(), m.overlapTop1To5Pct(), m.overlapTop5To10Pct()));
        }
        return rows;
    }

    public static void printSummary(List<ComparisonRow> rows, double luceneQps, double jvectorQps) {
        double top1 = rows.stream().mapToDouble(ComparisonRow::top1Overlap).average().orElse(0);
        double top1To5 = rows.stream().mapToDouble(ComparisonRow::top1To5Overlap).average().orElse(0);
        double top5To10 = rows.stream().mapToDouble(ComparisonRow::top5To10Overlap).average().orElse(0);

        double perfDiff = jvectorQps == 0 ? 0 : ((luceneQps - jvectorQps) / jvectorQps) * 100.0;

        System.out.printf("Top 1%% overlap: %.4f%n", top1);
        System.out.printf("Top 1-5%% overlap: %.4f%n", top1To5);
        System.out.printf("Top 5-10%% overlap: %.4f%n", top5To10);
        System.out.printf("Performance difference %% (Lucene vs JVector): %.2f%%%n", perfDiff);
    }

    public record BenchmarkQueryResult(int queryId, com.helpercodes.benchmark.data.QueryType queryType, int threads,
                                       long latencyNanos, List<SearchResultItem> results) {
    }

    public record BenchmarkWorkloadResult(String system, int threads, List<BenchmarkQueryResult> rows, double qps) {
    }
}
