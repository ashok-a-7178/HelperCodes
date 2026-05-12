package com.helpercodes.crudbenchmark.data;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public record TicketRecord(
        String id,
        String userId,
        Instant createdAt,
        Instant updatedAt,
        Map<String, String> fields) {

    public TicketRecord {
        fields = Map.copyOf(fields);
    }

    public TicketRecord withUpdates(String updatedByUserId, Instant updatedAt, Map<String, String> updates) {
        Map<String, String> merged = new LinkedHashMap<>(fields);
        merged.putAll(updates);
        return new TicketRecord(id, updatedByUserId, createdAt, updatedAt, merged);
    }

    public Map<String, String> selectedFields(Iterable<String> requestedFields) {
        Map<String, String> selected = new LinkedHashMap<>();
        for (String field : requestedFields) {
            if (fields.containsKey(field)) {
                selected.put(field, fields.get(field));
            }
        }
        return selected;
    }
}
