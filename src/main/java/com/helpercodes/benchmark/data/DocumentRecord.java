package com.helpercodes.benchmark.data;

public record DocumentRecord(
        int docId,
        int[] intFields,
        float[] floatFields,
        String[] textFields,
        String[] keywordFields,
        String comment,
        float[] vectorA,
        float[] vectorB) {
}
