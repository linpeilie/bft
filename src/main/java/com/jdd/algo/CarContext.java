package com.jdd.algo;

import cn.hutool.core.util.ArrayUtil;
import com.jdd.domain.AlgorithmMapInfo;
import com.jdd.domain.Coordinate;
import com.jdd.domain.RoutePoint;
import com.jdd.enums.PointDirectionEnum;
import com.jdd.enums.TaskTypeEnum;
import com.jdd.helper.FileHelper;
import com.jdd.helper.PointHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CarContext {

    private static Coordinate CURRENT_POINT = AlgorithmMapInfo.INIT_POINT;

    private static int CURRENT_DIRECTION = PointDirectionEnum.NORTH.getDirection();

    private static final List<RoutePoint> ROUTE_POINTS = new ArrayList<>();

    /**
     * 通过的点
     */
    private static final AtomicInteger PASSED = new AtomicInteger();

    public static void setCurrentPoint(Coordinate currentPoint) {
        CURRENT_POINT = currentPoint;
    }

    public static Coordinate getCurrentPoint() {
        return CURRENT_POINT;
    }

    public static void setCurrentDirection(int direction) {
        CURRENT_DIRECTION = direction;
    }

    public static int getCurrentDirection() {
        return CURRENT_DIRECTION;
    }

    public static void addPath(List<Coordinate> path, TaskTypeEnum taskType) {
        for (int i = 0; i < path.size() - 1; i++) {
            // 路径当前点
            Coordinate currentPoint = path.get(i);
            // 路径下一个点
            Coordinate nextPoint = path.get(i + 1);
            CarContext.addRoutePoint(initRoutePoint(currentPoint, taskType, CarContext.getCurrentDirection()), taskType);
            // 从当前点到下一个点的方向
            int nextDirection = PointHelper.getDirBetweenTwoPoints(currentPoint, nextPoint);
            // 如果需要转换，则添加一个当前点转弯的路径点
            if (nextDirection != CarContext.getCurrentDirection()) {
                CarContext.addRoutePoint(initRoutePoint(currentPoint, taskType, nextDirection), taskType);
            }
        }
    }

    public static void addRoutePoint(RoutePoint routePoint, TaskTypeEnum taskType) {
        ROUTE_POINTS.add(routePoint);
        CURRENT_POINT = Coordinate.valueOf(routePoint.getX(), routePoint.getY());
        CURRENT_DIRECTION = routePoint.getDirection();
        if (!TaskTypeEnum.CLEANING.equals(taskType)) {
            return;
        }
        // 修改扫过的点的权重
        if (ArrayUtil.isNotEmpty(routePoint.getCleanPoints())) {
            for (Coordinate cleanPoint : routePoint.getCleanPoints()) {
                if (AlgorithmMapInfo.addCleanPoint(cleanPoint)) {
                    AlgorithmMapInfo.setWeight(routePoint.getX(), routePoint.getY(), 50);
                }
            }
        }
        int passed = PASSED.incrementAndGet();
        if (passed % 20000 == 0) {
            FileHelper.appendResult(ROUTE_POINTS, taskType.getType());
            ROUTE_POINTS.clear();
            // 记录当前点
            Coordinate tempPoint = Coordinate.valueOf(routePoint.getX(), routePoint.getY());
            // 记录当前方向
            int tempDirection = CURRENT_DIRECTION;
            // 添加回去的任务
            List<Coordinate> chargePath = AStarAlgoFibonacci.getShortestPath(tempPoint, AlgorithmMapInfo.INIT_POINT);
            addPath(chargePath, TaskTypeEnum.CHARGE);
            FileHelper.appendResult(ROUTE_POINTS, TaskTypeEnum.CHARGE.getType());
            ROUTE_POINTS.clear();
            PASSED.set(0);
            // 回到原来的点
            List<Coordinate> backPath = AStarAlgoFibonacci.getShortestPath(AlgorithmMapInfo.INIT_POINT, tempPoint);
            addPath(backPath, TaskTypeEnum.CLEANING);
            // 获取上一次的方向
            if (tempDirection != CURRENT_DIRECTION) {
                addRoutePoint(ROUTE_POINTS.get(ROUTE_POINTS.size() - 1), TaskTypeEnum.CLEANING);
            }
        }
    }

    private static RoutePoint initRoutePoint(Coordinate coordinate, TaskTypeEnum taskType, int direction) {
        RoutePoint routePoint = new RoutePoint();
        routePoint.setX(coordinate.getX());
        routePoint.setY(coordinate.getY());
        routePoint.setDirection(direction);
        if (TaskTypeEnum.CLEANING.equals(taskType)) {
            routePoint.setCleanPoints(calcCleanPoint(coordinate, direction));
        }
        return routePoint;
    }

    private static Coordinate[] calcCleanPoint(Coordinate currentPoint, int currentDirection) {
        int currentX = currentPoint.getX();
        int currentY = currentPoint.getY();
        Coordinate[] cleanPointArr = new Coordinate[3];
        if (currentDirection == PointDirectionEnum.SOUTH.getDirection() || currentDirection == PointDirectionEnum.NORTH.getDirection()) {
            cleanPointArr[0] = Coordinate.valueOf(currentX - 1, currentY);
            cleanPointArr[1] = Coordinate.valueOf(currentX, currentY);
            cleanPointArr[2] = Coordinate.valueOf(currentX + 1, currentY);
        } else if (currentDirection == PointDirectionEnum.WEST.getDirection() || currentDirection == PointDirectionEnum.EAST.getDirection()) {
            cleanPointArr[0] = Coordinate.valueOf(currentX, currentY - 1);
            cleanPointArr[1] = Coordinate.valueOf(currentX, currentY);
            cleanPointArr[2] = Coordinate.valueOf(currentX, currentY + 1);
        }
        return cleanPointArr;
    }

    public static List<RoutePoint> getRoutePoints() {
        return ROUTE_POINTS;
    }

}
