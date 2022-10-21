package com.jdd.domain;

import com.jdd.enums.TaskTypeEnum;
import lombok.Data;

import java.util.List;

@Data
public class Task {

    private Coordinate src;
    private Coordinate des;

    private List<RoutePoint> routePoints;

}
