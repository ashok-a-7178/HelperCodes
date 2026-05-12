package com.helpercodes.crudbenchmark.engine;

import com.helpercodes.crudbenchmark.data.CrudActionType;

public record ActionStats(
        String component,
        CrudActionType actionType,
        long count,
        double avgMs,
        double minMs,
        double maxMs) {
}
