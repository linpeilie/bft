package com.jdd.enums;

public enum PointDirectionEnum {

    NONE(0, 0),
    NORTH(1, 4),
    SOUTH(2, 3),
    WEST(4, 1),
    EAST(8, 2);

    private final int direction;
    private final int outputDirection;

    PointDirectionEnum(int direction, int outputDirection) {
        this.direction = direction;
        this.outputDirection = outputDirection;
    }

    public int getDirection() {
        return direction;
    }

    public int getOutputDirection() {
        return outputDirection;
    }

    public static PointDirectionEnum getEnumByDirection(int direction) {
        for (PointDirectionEnum pointDirectionEnum : PointDirectionEnum.values()) {
            if (pointDirectionEnum.getDirection() == direction) {
                return pointDirectionEnum;
            }
        }
        return null;
    }

}
