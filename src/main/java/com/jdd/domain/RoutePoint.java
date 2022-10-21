package com.jdd.domain;

import com.jdd.enums.TaskTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class RoutePoint {

    private int x;
    private int y;
    private TaskTypeEnum type;
    private int direction;
    private List<Coordinate> cleanPoints;

}
