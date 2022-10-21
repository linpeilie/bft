package com.jdd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointDirectionEnum {

    NONE(0, 0),
    NORTH(1, 4),
    SOUTH(2, 3),
    WEST(4, 1),
    EAST(8, 2);

    private final int direction;
    private final int outputDirection;

    public static PointDirectionEnum getEnumByDirection(int direction) {
        for (PointDirectionEnum pointDirectionEnum : PointDirectionEnum.values()) {
            if (pointDirectionEnum.getDirection() == direction) {
                return pointDirectionEnum;
            }
        }
        return null;
    }

}
