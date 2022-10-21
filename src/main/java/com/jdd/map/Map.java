package com.jdd.map;

import com.jdd.domain.Point;
import lombok.Data;

import java.util.List;

@Data
public class Map {

    public static final int lineNum = 500;

    public static final int colNum = 200;

    /**
     * 障碍点
     */
    private List<Point> barriers;

}
