package com.jdd.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    CLEANING(1),
    CHARGE(2),
    BACK(9);

    private final int type;


}
