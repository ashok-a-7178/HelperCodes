package com.helpercodes.benchmark;

import com.helpercodes.benchmark.api.IndexBuilder;
import com.helpercodes.benchmark.api.SearchRunner;
import com.helpercodes.benchmark.config.BenchmarkConfig;
import com.helpercodes.benchmark.data.DatasetGenerator;
import com.helpercodes.benchmark.data.QueryGenerator;
import com.helpercodes.benchmark.data.QuerySpec;
import com.helpercodes.benchmark.engine.ComparisonEngine;
import com.helpercodes.benchmark.engine.ComparisonEngine.BenchmarkQueryResult;
import com.helpercodes.benchmark.engine.ComparisonEngine.BenchmarkWorkloadResult;
import com.helpercodes.benchmark.jvector.JVectorIndexBuilder;
import com.helpercodes.benchmark.jvector.JVectorSearchRunner;
import com.helpercodes.benchmark.lucene.LuceneIndexBuilder;
import com.helpercodes.benchmark.lucene.LuceneSearchRunner;
import com.helpercodes.benchmark.metrics.ComparisonRow;
import com.helpercodes.benchmark.metrics.LatencyStats;
import com.helpercodes.benchmark.metrics.QueryMetricRow;
import com.helpercodes.benchmark.util.CsvWriter;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BenchmarkRunner {
    public static void main(String[] args) throws Exception {
        BenchmarkConfig config = BenchmarkConfig.defaults().withOverrides(args);
        Files.createDirectories(config.outputDirectory());

        buildIndexes(config);

        List<QuerySpec> queries = QueryGenerator.generate(config);

        List<QueryMetricRow> metricRows = new ArrayList<>();
        List<ComparisonRow> comparisonRows = new ArrayList<>();

        try (SearchRunner lucene = new LuceneSearchRunner(config); SearchRunner jvector = new JVectorSearchRunner(config)) {
            warmUp(lucene, queries, config.warmupQueries());
            warmUp(jvector, queries, config.warmupQueries());

            for (int threads : config.threadModes()) {
                BenchmarkWorkloadResult luceneResult = runBenchmark(lucene, queries, threads);
                BenchmarkWorkloadResult jvectorResult = runBenchmark(jvector, queries, threads);

                metricRows.addAll(toMetricRows(luceneResult));
                metricRows.addAll(toMetricRows(jvectorResult));

                List<ComparisonRow> compared = ComparisonEngine.compare(luceneResult, jvectorResult, config.topK());
                comparisonRows.addAll(compared);

                System.out.println("==== Summary (threads=" + threads + ") ====");
                printLatency(luceneResult, jvectorResult);
                printRecall(compared);
                ComparisonEngine.printSummary(compared, luceneResult.qps(), jvectorResult.qps());
            }
        }

        CsvWriter.writeQueryMetrics(config.outputDirectory().resolve("per-query-metrics.csv"), metricRows);
        CsvWriter.writeComparisons(config.outputDirectory().resolve("ranking-comparison-metrics.csv"), comparisonRows);
    }

    private static void buildIndexes(BenchmarkConfig config) throws Exception {
        try (IndexBuilder lucene = new LuceneIndexBuilder(config); IndexBuilder jvector = new JVectorIndexBuilder(config)) {
            for (long i = 0; i < config.documentCount(); i++) {
                var doc = DatasetGenerator.generate(i, config);
                lucene.add(doc);
                jvector.add(doc);
                if (i > 0 && i % 100_000 == 0) {
                    System.out.println("Indexed docs: " + i);
                }
            }
        }
    }

    private static void warmUp(SearchRunner runner, List<QuerySpec> queries, int warmupCount) throws Exception {
        for (int i = 0; i < Math.min(warmupCount, queries.size()); i++) {
            runner.search(queries.get(i));
        }
    }

    private static BenchmarkWorkloadResult runBenchmark(SearchRunner runner, List<QuerySpec> queries, int threads) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        long start = System.nanoTime();

        List<Callable<BenchmarkQueryResult>> tasks = new ArrayList<>();
        for (QuerySpec q : queries) {
            tasks.add(() -> {
                long qStart = System.nanoTime();
                var result = runner.search(q);
                long latency = System.nanoTime() - qStart;
                return new BenchmarkQueryResult(q.queryId(), q.queryType(), threads, latency, result);
            });
        }

        List<Future<BenchmarkQueryResult>> futures = pool.invokeAll(tasks);
        List<BenchmarkQueryResult> rows = new ArrayList<>(futures.size());
        for (Future<BenchmarkQueryResult> f : futures) {
            rows.add(f.get());
        }

        pool.shutdown();
        long elapsed = System.nanoTime() - start;
        double qps = queries.isEmpty() ? 0 : queries.size() / (elapsed / 1_000_000_000.0);
        return new BenchmarkWorkloadResult(runner.name(), threads, rows, qps);
    }

    private static List<QueryMetricRow> toMetricRows(BenchmarkWorkloadResult result) {
        List<QueryMetricRow> rows = new ArrayList<>(result.rows().size());
        for (BenchmarkQueryResult row : result.rows()) {
            rows.add(new QueryMetricRow(row.queryId(), row.queryType(), row.threads(), result.system(), row.latencyNanos(), row.results().size()));
        }
        return rows;
    }

    private static void printLatency(BenchmarkWorkloadResult lucene, BenchmarkWorkloadResult jvector) {
        LatencyStats luceneStats = LatencyStats.fromNanos(lucene.rows().stream().map(BenchmarkQueryResult::latencyNanos).toList());
        LatencyStats jvectorStats = LatencyStats.fromNanos(jvector.rows().stream().map(BenchmarkQueryResult::latencyNanos).toList());
        System.out.printf("Latency avg/p95/p99 ms - Lucene: %.2f/%.2f/%.2f, JVector: %.2f/%.2f/%.2f%n",
                luceneStats.avgMs(), luceneStats.p95Ms(), luceneStats.p99Ms(),
                jvectorStats.avgMs(), jvectorStats.p95Ms(), jvectorStats.p99Ms());
    }

    private static void printRecall(List<ComparisonRow> comparisons) {
        double recall = comparisons.stream().mapToDouble(ComparisonRow::recallAtK).average().orElse(0);
        System.out.printf("Average Recall@K: %.4f%n", recall);
    }
}
