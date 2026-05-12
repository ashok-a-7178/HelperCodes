package com.helpercodes.crudbenchmark.engine;

import com.helpercodes.crudbenchmark.data.CrudActionType;

public record ActionLatency(String component, CrudActionType actionType, long latencyNanos) {
}
