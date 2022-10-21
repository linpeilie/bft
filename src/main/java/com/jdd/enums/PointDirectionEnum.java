package com.jdd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointDirectionEnum {

    NONE(0),
    NORTH(1),
    SOUTH(2),
    WEST(4),
    EAST(8);

    private final int direction;

    public static PointDirectionEnum getEnumByDirection(int direction) {
        for (PointDirectionEnum pointDirectionEnum : PointDirectionEnum.values()) {
            if (pointDirectionEnum.getDirection() == direction) {
                return pointDirectionEnum;
            }
        }
        return null;
    }

}
