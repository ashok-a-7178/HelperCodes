package com.helpercodes.benchmark.jvector;

import java.io.Serializable;

public record JVectorDocument(int docId, String keyword0, int int0, float[] vector) implements Serializable {
}
