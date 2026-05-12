package com.helpercodes.crudbenchmark.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public record CrudBenchmarkConfig(
        long seed,
        int userCount,
        int actionCount,
        Path outputDirectory) {

    public static CrudBenchmarkConfig defaults() {
        return new CrudBenchmarkConfig(42L, 10_000, 100_000, Paths.get("crud-benchmark-output"));
    }

    public CrudBenchmarkConfig withOverrides(String[] args) {
        long seedValue = seed;
        int users = userCount;
        int actions = actionCount;
        Path output = outputDirectory;

        for (String arg : args) {
            String[] kv = arg.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            switch (kv[0]) {
                case "seed" -> seedValue = Long.parseLong(kv[1]);
                case "users" -> users = Integer.parseInt(kv[1]);
                case "actions" -> actions = Integer.parseInt(kv[1]);
                case "output" -> output = Paths.get(kv[1]);
                default -> { }
            }
        }
        return new CrudBenchmarkConfig(seedValue, users, actions, output);
    }
}
