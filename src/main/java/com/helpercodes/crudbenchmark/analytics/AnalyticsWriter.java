package com.helpercodes.crudbenchmark.analytics;

import com.helpercodes.crudbenchmark.engine.ActionStats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class AnalyticsWriter {
    private AnalyticsWriter() {
    }

    public static void writeCsv(Path file, List<ActionStats> rows) throws IOException {
        Files.createDirectories(file.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("component,action,count,avg_ms,min_ms,max_ms");
            writer.newLine();
            for (ActionStats row : rows) {
                writer.write(String.format("%s,%s,%d,%.6f,%.6f,%.6f",
                        row.component(), row.actionType(), row.count(), row.avgMs(), row.minMs(), row.maxMs()));
                writer.newLine();
            }
        }
    }

    public static void writeHtml(Path file, List<ActionStats> rows) throws IOException {
        Files.createDirectories(file.getParent());
        double maxAvg = rows.stream().mapToDouble(ActionStats::avgMs).max().orElse(1.0);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write("<!doctype html><html><head><meta charset=\"utf-8\"><title>CRUD Benchmark Analytics</title>");
            writer.write("<style>body{font-family:Arial,sans-serif;margin:24px}table{border-collapse:collapse}td,th{border:1px solid #ccc;padding:6px 10px}.bar{background:#4f81bd;height:18px}</style>");
            writer.write("</head><body><h1>CRUD Benchmark Analytics</h1><table><tr><th>Component</th><th>Action</th><th>Count</th><th>Avg ms</th><th>Min ms</th><th>Max ms</th><th>Avg chart</th></tr>");
            for (ActionStats row : rows) {
                int width = (int) Math.max(1, Math.round((row.avgMs() / maxAvg) * 240));
                writer.write(String.format("<tr><td>%s</td><td>%s</td><td>%d</td><td>%.6f</td><td>%.6f</td><td>%.6f</td><td><div class=\"bar\" style=\"width:%dpx\"></div></td></tr>",
                        escape(row.component()), row.actionType(), row.count(), row.avgMs(), row.minMs(), row.maxMs(), width));
            }
            writer.write("</table></body></html>");
        }
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
