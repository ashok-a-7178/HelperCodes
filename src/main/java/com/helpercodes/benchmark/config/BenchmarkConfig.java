package com.helpercodes.benchmark.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public record BenchmarkConfig(
        long seed,
        int vectorDimension,
        int topK,
        long documentCount,
        int queryCount,
        int warmupQueries,
        int hnswM,
        int hnswEfConstruction,
        int jvectorGraphM,
        int jvectorSearchEf,
        Path outputDirectory,
        Path luceneDirectory,
        Path jvectorDirectory,
        List<Integer> threadModes) {

    public static BenchmarkConfig defaults() {
        Path base = Paths.get("benchmark-output");
        return new BenchmarkConfig(
                42L,
                384,
                1000,
                20_000_000L,
                1_000,
                100,
                16,
                100,
                16,
                128,
                base,
                base.resolve("lucene-index"),
                base.resolve("jvector-index"),
                List.of(1, 10, 50));
    }

    public BenchmarkConfig withOverrides(String[] args) {
        long seedValue = seed;
        int vectorDim = vectorDimension;
        int topKValue = topK;
        long docs = documentCount;
        int queries = queryCount;
        int warmups = warmupQueries;

        for (String arg : args) {
            String[] kv = arg.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            switch (kv[0]) {
                case "seed" -> seedValue = Long.parseLong(kv[1]);
                case "vectorDim" -> vectorDim = Integer.parseInt(kv[1]);
                case "topK" -> topKValue = Integer.parseInt(kv[1]);
                case "docs" -> docs = Long.parseLong(kv[1]);
                case "queries" -> queries = Integer.parseInt(kv[1]);
                case "warmup" -> warmups = Integer.parseInt(kv[1]);
                default -> { }
            }
        }
        return new BenchmarkConfig(seedValue, vectorDim, topKValue, docs, queries, warmups, hnswM, hnswEfConstruction,
                jvectorGraphM, jvectorSearchEf, outputDirectory, luceneDirectory, jvectorDirectory, threadModes);
    }
}
