package com.helpercodes.crudbenchmark.component;

import com.helpercodes.crudbenchmark.data.CrudAction;
import com.helpercodes.crudbenchmark.data.TicketRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ClickHouseTicketStore extends AbstractMapTicketStore {
    private final List<CrudAction> actionLog = new ArrayList<>();

    public ClickHouseTicketStore() {
        super(new HashMap<String, TicketRecord>());
    }

    @Override
    public String name() {
        return "ClickHouse";
    }

    @Override
    public void execute(CrudAction action) {
        actionLog.add(action);
        super.execute(action);
    }
}
