package com.helpercodes.crudbenchmark.component;

import com.helpercodes.crudbenchmark.data.TicketRecord;

import java.util.HashMap;

public final class MySqlTicketStore extends AbstractMapTicketStore {
    public MySqlTicketStore() {
        super(new HashMap<String, TicketRecord>());
    }

    @Override
    public String name() {
        return "MySQL";
    }
}
