package com.helpercodes.crudbenchmark.component;

import com.helpercodes.crudbenchmark.data.TicketRecord;

import java.util.TreeMap;

public final class HBaseTicketStore extends AbstractMapTicketStore {
    public HBaseTicketStore() {
        super(new TreeMap<String, TicketRecord>());
    }

    @Override
    public String name() {
        return "HBase";
    }
}
