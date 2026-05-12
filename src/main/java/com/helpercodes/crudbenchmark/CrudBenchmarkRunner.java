package com.helpercodes.crudbenchmark;

import com.helpercodes.crudbenchmark.analytics.AnalyticsBuilder;
import com.helpercodes.crudbenchmark.analytics.AnalyticsWriter;
import com.helpercodes.crudbenchmark.component.ClickHouseTicketStore;
import com.helpercodes.crudbenchmark.component.HBaseTicketStore;
import com.helpercodes.crudbenchmark.component.MySqlTicketStore;
import com.helpercodes.crudbenchmark.component.TicketStore;
import com.helpercodes.crudbenchmark.config.CrudBenchmarkConfig;
import com.helpercodes.crudbenchmark.data.CrudAction;
import com.helpercodes.crudbenchmark.data.TicketWorkloadGenerator;
import com.helpercodes.crudbenchmark.engine.ActionLatency;
import com.helpercodes.crudbenchmark.engine.ActionStats;
import com.helpercodes.crudbenchmark.engine.PerformanceAnalyzer;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CrudBenchmarkRunner {
    public static void main(String[] args) throws Exception {
        CrudBenchmarkConfig config = CrudBenchmarkConfig.defaults().withOverrides(args);
        Files.createDirectories(config.outputDirectory());

        List<CrudAction> actions = TicketWorkloadGenerator.generate(config);
        List<Supplier<TicketStore>> stores = List.of(MySqlTicketStore::new, HBaseTicketStore::new, ClickHouseTicketStore::new);
        List<ActionLatency> latencies = new ArrayList<>(actions.size() * stores.size());

        System.out.printf("Running %,d CRUD actions across %,d users for %,d components%n",
                config.actionCount(), config.userCount(), stores.size());
        for (Supplier<TicketStore> storeFactory : stores) {
            TicketStore store = storeFactory.get();
            List<ActionLatency> componentLatencies = PerformanceAnalyzer.run(store, actions);
            latencies.addAll(componentLatencies);
            System.out.println("Completed component: " + store.name());
        }

        List<ActionStats> stats = AnalyticsBuilder.summarize(latencies);
        AnalyticsWriter.writeCsv(config.outputDirectory().resolve("crud-action-stats.csv"), stats);
        AnalyticsWriter.writeHtml(config.outputDirectory().resolve("crud-action-stats.html"), stats);

        System.out.println("Wrote analytics to " + config.outputDirectory().toAbsolutePath());
        for (ActionStats row : stats) {
            System.out.printf("%s %-6s count=%d avg=%.6fms min=%.6fms max=%.6fms%n",
                    row.component(), row.actionType(), row.count(), row.avgMs(), row.minMs(), row.maxMs());
        }
    }
}
