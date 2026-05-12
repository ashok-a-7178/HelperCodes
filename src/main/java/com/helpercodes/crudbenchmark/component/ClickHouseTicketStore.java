package com.helpercodes.crudbenchmark.component;

import com.helpercodes.crudbenchmark.data.TicketRecord;

import java.util.HashMap;

public final class ClickHouseTicketStore extends AbstractMapTicketStore {
    public ClickHouseTicketStore() {
        super(new HashMap<String, TicketRecord>());
    }

    @Override
    public String name() {
        return "ClickHouse";
    }
}
