package com.helpercodes.crudbenchmark.data;

import com.helpercodes.crudbenchmark.config.CrudBenchmarkConfig;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class TicketWorkloadGenerator {
    private static final List<String> FIELD_NAMES = List.of(
            "Name", "Title", "Description", "Status", "Owner", "Priority", "Category", "Source");
    private static final List<String> STATUSES = List.of("open", "pending", "in_progress", "resolved", "closed");
    private static final List<String> PRIORITIES = List.of("low", "medium", "high", "critical");

    private TicketWorkloadGenerator() {
    }

    public static List<CrudAction> generate(CrudBenchmarkConfig config) {
        Random random = new Random(config.seed());
        List<CrudAction> actions = new ArrayList<>(config.actionCount());
        List<String> activeIds = new ArrayList<>();
        long nextId = 11_223_344L;
        Instant base = Instant.parse("2026-01-01T00:00:00Z");

        for (int sequence = 0; sequence < config.actionCount(); sequence++) {
            CrudActionType type = chooseActionType(random, activeIds.isEmpty());
            String userId = "user-" + (1 + random.nextInt(config.userCount()));
            Instant timestamp = base.plusMillis(sequence);

            switch (type) {
                case INSERT -> {
                    String id = String.valueOf(nextId++);
                    activeIds.add(id);
                    actions.add(new CrudAction(sequence, type, userId, timestamp, id, ticketData(id, sequence, random), Set.of()));
                }
                case READ -> {
                    String id = activeIds.get(random.nextInt(activeIds.size()));
                    actions.add(new CrudAction(sequence, type, userId, timestamp, id, Map.of(), requestedFields(random)));
                }
                case UPDATE -> {
                    String id = activeIds.get(random.nextInt(activeIds.size()));
                    actions.add(new CrudAction(sequence, type, userId, timestamp, id, updateData(sequence, random), Set.of()));
                }
                case DELETE -> {
                    int index = random.nextInt(activeIds.size());
                    String id = activeIds.remove(index);
                    actions.add(new CrudAction(sequence, type, userId, timestamp, id, Map.of(), Set.of()));
                }
            }
        }
        return actions;
    }

    private static CrudActionType chooseActionType(Random random, boolean forceInsert) {
        if (forceInsert) {
            return CrudActionType.INSERT;
        }
        int bucket = random.nextInt(100);
        if (bucket < 40) {
            return CrudActionType.INSERT;
        }
        if (bucket < 65) {
            return CrudActionType.READ;
        }
        if (bucket < 90) {
            return CrudActionType.UPDATE;
        }
        return CrudActionType.DELETE;
    }

    private static Map<String, String> ticketData(String id, int sequence, Random random) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("ID", id);
        data.put("Name", "ticket-" + sequence);
        data.put("Title", "Issue in PR review " + sequence);
        data.put("Description", "PR review is still pending for generated ticket " + sequence);
        data.put("Status", STATUSES.get(random.nextInt(STATUSES.size())));
        data.put("Owner", "owner-" + (1 + random.nextInt(500)));
        data.put("Priority", PRIORITIES.get(random.nextInt(PRIORITIES.size())));
        data.put("Category", "category-" + (1 + random.nextInt(25)));
        data.put("Source", "generator");
        return data;
    }

    private static Map<String, String> updateData(int sequence, Random random) {
        Map<String, String> updates = new LinkedHashMap<>();
        updates.put("Status", STATUSES.get(random.nextInt(STATUSES.size())));
        if (random.nextBoolean()) {
            updates.put("Owner", "owner-" + (1 + random.nextInt(500)));
        }
        if (random.nextBoolean()) {
            updates.put("Description", "Updated generated ticket details " + sequence);
        }
        return updates;
    }

    private static Set<String> requestedFields(Random random) {
        if (random.nextBoolean()) {
            return new LinkedHashSet<>(FIELD_NAMES);
        }
        Set<String> fields = new LinkedHashSet<>();
        int count = 1 + random.nextInt(FIELD_NAMES.size());
        while (fields.size() < count) {
            fields.add(FIELD_NAMES.get(random.nextInt(FIELD_NAMES.size())));
        }
        return fields;
    }
}
