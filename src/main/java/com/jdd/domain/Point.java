package com.jdd.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Point implements Serializable {

    private int x;
    private int y;
    private int direction;

}
