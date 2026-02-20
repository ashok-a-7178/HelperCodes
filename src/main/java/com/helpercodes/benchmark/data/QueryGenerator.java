package com.helpercodes.benchmark.data;

import com.helpercodes.benchmark.config.BenchmarkConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public final class QueryGenerator {
    private QueryGenerator() {}

    public static List<QuerySpec> generate(BenchmarkConfig config) {
        SplittableRandom random = new SplittableRandom(config.seed() ^ 0xABCDEF1234L);
        List<QuerySpec> queries = new ArrayList<>(config.queryCount());

        QueryType[] types = QueryType.values();
        for (int i = 0; i < config.queryCount(); i++) {
            QueryType type = types[i % types.length];
            long sampledId = random.nextLong(config.documentCount());
            DocumentRecord source = DatasetGenerator.generate(sampledId, config);
            int min = Math.max(0, source.intFields()[0] - 5_000);
            int max = Math.min(1_000_000, source.intFields()[0] + 5_000);
            queries.add(new QuerySpec(i, type, source.keywordFields()[0], source.vectorA(), min, max, config.topK()));
        }
        return queries;
    }
}
