package com.walmart.aex.strategy.listener.enums;

public enum EventType {
    CREATE(1),
    UPDATE(2),
    DELETE(3),
    INITIAL_LOAD(4);

    private final int id;

    EventType(final int type) {
        this.id = type;
    }

    public int getValue() {
        return id;
    }
}
