package com.jdd.enums;

public enum TaskTypeEnum {

    CLEANING(1),
    CHARGE(2),
    BACK(9);

    private final int type;

    TaskTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
