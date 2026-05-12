package com.helpercodes.crudbenchmark.component;

import com.helpercodes.crudbenchmark.data.CrudAction;

public interface TicketStore {
    String name();

    void execute(CrudAction action);
}
