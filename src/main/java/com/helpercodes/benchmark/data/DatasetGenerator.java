package com.helpercodes.benchmark.data;

import com.helpercodes.benchmark.config.BenchmarkConfig;

import java.util.SplittableRandom;

public final class DatasetGenerator {
    private static final double EPSILON = 1e-12;
    private static final String[] TOKENS = {
            "alpha", "beta", "gamma", "delta", "sigma", "omega", "search", "vector", "index", "benchmark",
            "quality", "latency", "throughput", "ranking", "comment", "hybrid", "keyword", "range", "lucene", "jvector"
    };

    private DatasetGenerator() {}

    public static DocumentRecord generate(long id, BenchmarkConfig config) {
        int docId = Math.toIntExact(id);
        SplittableRandom random = new SplittableRandom(config.seed() + id * 31L);

        int[] ints = new int[10];
        float[] floats = new float[10];
        String[] texts = new String[20];
        String[] keywords = new String[8];

        for (int i = 0; i < ints.length; i++) {
            ints[i] = random.nextInt(1_000_000);
        }
        for (int i = 0; i < floats.length; i++) {
            floats[i] = (float) random.nextDouble();
        }
        for (int i = 0; i < texts.length; i++) {
            texts[i] = sentence(random, 10);
        }
        for (int i = 0; i < keywords.length; i++) {
            keywords[i] = "kw_" + random.nextInt(5_000);
        }

        String comment = sentence(random, 40);
        float[] vectorA = embedding(comment, config.vectorDimension(), config.seed() ^ 0x9E3779B97F4A7C15L);
        float[] vectorB = embedding(comment, config.vectorDimension(), config.seed() ^ 0xD1B54A32D192ED03L);

        return new DocumentRecord(docId, ints, floats, texts, keywords, comment, vectorA, vectorB);
    }

    private static String sentence(SplittableRandom random, int tokenCount) {
        StringBuilder sb = new StringBuilder(tokenCount * 8);
        for (int i = 0; i < tokenCount; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(TOKENS[random.nextInt(TOKENS.length)]);
        }
        return sb.toString();
    }

    private static float[] embedding(String text, int dims, long seed) {
        long hash = 1125899906842597L;
        for (int i = 0; i < text.length(); i++) {
            hash = 31 * hash + text.charAt(i);
        }
        SplittableRandom random = new SplittableRandom(seed ^ hash);
        float[] vector = new float[dims];
        double norm = 0.0;
        for (int i = 0; i < dims; i++) {
            float v = (float) (random.nextDouble(-1.0, 1.0));
            vector[i] = v;
            norm += v * v;
        }
        float invNorm = (float) (1.0 / Math.sqrt(norm + EPSILON));
        for (int i = 0; i < dims; i++) {
            vector[i] *= invNorm;
        }
        return vector;
    }
}
