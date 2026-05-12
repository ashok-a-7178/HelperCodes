package com.helpercodes.crudbenchmark.data;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public record CrudAction(
        long sequence,
        CrudActionType actionType,
        String userId,
        Instant timestamp,
        String ticketId,
        Map<String, String> data,
        Set<String> requestedFields) {

    public CrudAction {
        data = Map.copyOf(data);
        requestedFields = Set.copyOf(requestedFields);
    }
}
