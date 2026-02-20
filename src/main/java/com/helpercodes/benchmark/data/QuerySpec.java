package com.helpercodes.benchmark.data;

public record QuerySpec(
        int queryId,
        QueryType queryType,
        String keyword,
        float[] vector,
        int rangeMin,
        int rangeMax,
        int topK) {
}
