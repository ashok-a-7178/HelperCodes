package com.helpercodes.crudbenchmark.engine;

import com.helpercodes.crudbenchmark.component.TicketStore;
import com.helpercodes.crudbenchmark.data.CrudAction;

import java.util.ArrayList;
import java.util.List;

public final class PerformanceAnalyzer {
    private PerformanceAnalyzer() {
    }

    public static List<ActionLatency> run(TicketStore store, List<CrudAction> actions) {
        List<ActionLatency> latencies = new ArrayList<>(actions.size());
        for (CrudAction action : actions) {
            long start = System.nanoTime();
            store.execute(action);
            long elapsed = System.nanoTime() - start;
            latencies.add(new ActionLatency(store.name(), action.actionType(), elapsed));
        }
        return latencies;
    }
}
