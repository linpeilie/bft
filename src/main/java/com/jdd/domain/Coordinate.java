package com.jdd.domain;

import com.jdd.helper.CoordinateCache;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AGV 坐标内容
 */
@Data
public class Coordinate implements Serializable {

    private int x;

    private int y;

    public static Coordinate valueOf(int x, int y) {
        return CoordinateCache.get(x, y);
    }

}
