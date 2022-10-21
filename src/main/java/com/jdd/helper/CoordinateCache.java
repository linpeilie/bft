package com.jdd.helper;

import com.jdd.domain.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class CoordinateCache {

    public static final Map<Integer, Coordinate> CACHE_MAP = new HashMap<>();

    public static Coordinate get(int x, int y) {
        int key = x * 10000 + y;
        if (CACHE_MAP.containsKey(key)) {
            return CACHE_MAP.get(key);
        } else {
            Coordinate coordinate = new Coordinate();
            coordinate.setX(x);
            coordinate.setY(y);
            CACHE_MAP.put(key, coordinate);
            return coordinate;
        }
    }

}
