package com.helpercodes.benchmark.util;

import com.helpercodes.benchmark.metrics.ComparisonRow;
import com.helpercodes.benchmark.metrics.QueryMetricRow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CsvWriter {
    private CsvWriter() {}

    public static void writeQueryMetrics(Path file, List<QueryMetricRow> rows) throws IOException {
        Files.createDirectories(file.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("queryId,queryType,threads,system,latencyNanos,resultCount\n");
        for (QueryMetricRow r : rows) {
            sb.append(r.queryId()).append(',')
                    .append(r.queryType()).append(',')
                    .append(r.threads()).append(',')
                    .append(r.system()).append(',')
                    .append(r.latencyNanos()).append(',')
                    .append(r.resultCount()).append('\n');
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }

    public static void writeComparisons(Path file, List<ComparisonRow> rows) throws IOException {
        Files.createDirectories(file.getParent());
        StringBuilder sb = new StringBuilder();
        sb.append("queryId,queryType,threads,intersection,jaccard,recallAtK,spearman,top1Overlap,top1To5Overlap,top5To10Overlap\n");
        for (ComparisonRow r : rows) {
            sb.append(r.queryId()).append(',')
                    .append(r.queryType()).append(',')
                    .append(r.threads()).append(',')
                    .append(r.intersection()).append(',')
                    .append(r.jaccard()).append(',')
                    .append(r.recallAtK()).append(',')
                    .append(r.spearman()).append(',')
                    .append(r.top1Overlap()).append(',')
                    .append(r.top1To5Overlap()).append(',')
                    .append(r.top5To10Overlap()).append('\n');
        }
        Files.writeString(file, sb.toString(), StandardCharsets.UTF_8);
    }
}
