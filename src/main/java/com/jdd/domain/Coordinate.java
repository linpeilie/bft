package com.jdd.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AGV 坐标内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coordinate implements Serializable {
    private int x;

    private int y;
}
