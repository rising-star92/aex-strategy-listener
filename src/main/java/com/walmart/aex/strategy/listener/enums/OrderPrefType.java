package com.walmart.aex.strategy.listener.enums;

public enum OrderPrefType {
    ORDER_PREF_1(1),
    ORDER_PREF_2(2),
    ORDER_PREF_3(3),
    ORDER_PREF_4(4);

    private final int id;

    OrderPrefType(final int type) {
        this.id = type;
    }

    public int getValue() {
        return id;
    }
}
