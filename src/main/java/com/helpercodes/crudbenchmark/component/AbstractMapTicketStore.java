package com.helpercodes.crudbenchmark.component;

import com.helpercodes.crudbenchmark.data.CrudAction;
import com.helpercodes.crudbenchmark.data.TicketRecord;

import java.util.LinkedHashMap;
import java.util.Map;

abstract class AbstractMapTicketStore implements TicketStore {
    private final Map<String, TicketRecord> tickets;

    AbstractMapTicketStore(Map<String, TicketRecord> tickets) {
        this.tickets = tickets;
    }

    @Override
    public void execute(CrudAction action) {
        switch (action.actionType()) {
            case INSERT -> tickets.put(action.ticketId(), new TicketRecord(
                    action.ticketId(), action.userId(), action.timestamp(), action.timestamp(), action.data()));
            case READ -> read(action);
            case UPDATE -> update(action);
            case DELETE -> tickets.remove(action.ticketId());
        }
    }

    protected TicketRecord find(String id) {
        return tickets.get(id);
    }

    protected void put(String id, TicketRecord record) {
        tickets.put(id, record);
    }

    protected Map<String, TicketRecord> tickets() {
        return tickets;
    }

    private void read(CrudAction action) {
        TicketRecord record = find(action.ticketId());
        if (record == null) {
            return;
        }
        if (action.requestedFields().isEmpty()) {
            new LinkedHashMap<>(record.fields());
        } else {
            record.selectedFields(action.requestedFields());
        }
    }

    private void update(CrudAction action) {
        TicketRecord current = find(action.ticketId());
        if (current != null) {
            put(action.ticketId(), current.withUpdates(action.userId(), action.timestamp(), action.data()));
        }
    }
}
