package com.jdd;

import com.jdd.algo.AStarAlgoFibonacci;
import com.jdd.algo.CarContext;
import com.jdd.domain.AlgorithmMapInfo;
import com.jdd.domain.Coordinate;
import com.jdd.domain.RoutePoint;
import com.jdd.enums.PointDirectionEnum;
import com.jdd.enums.TaskTypeEnum;
import com.jdd.helper.FileHelper;
import com.jdd.helper.PointHelper;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CalcRouteApplication {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            execute();
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("耗时 : " + (endTime - startTime));
        }
        checkResult();
    }

    public static void execute() {
        // 加载地图障碍点信息
        List<Coordinate> barrierPoints = FileHelper.parseBarrier("block.txt");
        barrierPoints.forEach(AlgorithmMapInfo::setObstacle);
        // 删除之前的文件
        FileHelper.delOldResultFile();
        // 设置障碍物单元格周围点方向
        setBarrierSurroundingDir();
        // 设置障碍物
        setObstacleWeight();
        // 输出原地图
//        printMap();
        // 计算可到达的点
        Set<Coordinate> canArrivedPoints = getCanArrivedPoints();
//        System.out.println("可到达的点数量 : " + canArrivedPoints.size());
        AlgorithmMapInfo.setCanArrived(canArrivedPoints.size());
        // 重新设置障碍物
        reCalcObstacle(canArrivedPoints);
        // 重新绘制地图
//        printMap();
        // 清除边界方向
        cleanBorderDirection();
        // 生成任务
        matchTask();
    }

    private static void checkResult() {
        System.out.println("可清理数量 " + AlgorithmMapInfo.getCanArrived());
        System.out.println("扫过点数量 " + AlgorithmMapInfo.getCleanPointSize());
    }

    private static void matchTask() {
        // 先让车辆到达原点（最小可用点）
        Coordinate origin = getOrigin();
//        System.out.println("原点[ " + origin.getX() + "," + origin.getY() + " ]");
        List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(CarContext.getCurrentPoint(), origin);
        CarContext.addPath(path, TaskTypeEnum.CLEANING);
        for (int y = origin.getY(); y < AlgorithmMapInfo.LINE_NUM; y += 2) {
            cleanNextLine(y);
        }
        List<RoutePoint> routePoints = CarContext.getRoutePoints();
        FileHelper.appendResult(routePoints, TaskTypeEnum.CLEANING.getType());
        routePoints.clear();
        // 获取返回的路径
        List<Coordinate> backPath = AStarAlgoFibonacci.getShortestPath(CarContext.getCurrentPoint(), AlgorithmMapInfo.INIT_POINT);
        CarContext.addPath(backPath, TaskTypeEnum.CHARGE);
        FileHelper.appendResult(CarContext.getRoutePoints(), TaskTypeEnum.CHARGE.getType());
        routePoints.clear();
        FileHelper.appendResult(routePoints, TaskTypeEnum.BACK.getType());
    }

    private static void cleanNextLine(int y) {
        // 第一行
        int minX = -1;
        int maxX = -1;
        for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
            if (AlgorithmMapInfo.canArrived(x, y)) {
                minX = x;
                break;
            }
        }
        for (int x = AlgorithmMapInfo.COL_NUM - 1; x >= 0; x--) {
            if (AlgorithmMapInfo.canArrived(x, y)) {
                maxX = x;
                break;
            }
        }
//        System.out.println("行数 [ " + y + " ], 最左侧到达[ " + minX + " ]，最右侧到达[ " + maxX + " ] ");
        if (minX == maxX) {
            return;
        }
        // 先到达第一个点
        List<Coordinate> path1 = AStarAlgoFibonacci.getShortestPath(CarContext.getCurrentPoint(), Coordinate.valueOf(minX, y));
        if (path1.size() > 1) {
            CarContext.addPath(path1, TaskTypeEnum.CLEANING);
        }
        // 到达第二个点
        List<Coordinate> path2 = AStarAlgoFibonacci.getShortestPath(CarContext.getCurrentPoint(), Coordinate.valueOf(maxX, y));
        if (path2.size() > 1) {
            CarContext.addPath(path2, TaskTypeEnum.CLEANING);
        }
        // 第二行
        minX = -1;
        maxX = -1;
        for (int x = AlgorithmMapInfo.COL_NUM - 1; x >= 0; x--) {
            if (AlgorithmMapInfo.canArrived(x, y + 1)) {
                maxX = x;
                break;
            }
        }
        for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
            if (AlgorithmMapInfo.canArrived(x, y + 1)) {
                minX = x;
                break;
            }
        }
//        System.out.println("行数 [ " + (y+1) + " ]，最左侧可到达[ " + minX + " ]，最右侧可到达[ " + maxX + " ]");
        if (minX == maxX) {
            return;
        }
        // 先到达最右侧，再到达最左侧
        List<Coordinate> path3 = AStarAlgoFibonacci.getShortestPath(CarContext.getCurrentPoint(), Coordinate.valueOf(maxX, y + 1));
        if (path3.size() > 1) {
            CarContext.addPath(path3, TaskTypeEnum.CLEANING);
        }
        List<Coordinate> path4 = AStarAlgoFibonacci.getShortestPath(CarContext.getCurrentPoint(), Coordinate.valueOf(minX, y + 1));
        if (path4.size() > 1) {
            CarContext.addPath(path4, TaskTypeEnum.CLEANING);
        }
    }

    private static void printMap() {
        for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
            System.out.print((x + 1) + " ");
        }
        for (int y = AlgorithmMapInfo.LINE_NUM - 1; y >= 0; y--) {
            System.out.print((y + 1) + " ");
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (AlgorithmMapInfo.isObstacle(x, y)) {
                    System.out.print("●");
                } else if (x == AlgorithmMapInfo.INIT_POINT.getX() && y == AlgorithmMapInfo.INIT_POINT.getY()) {
                    System.out.print("★");
                } else {
                    System.out.print(" ");
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    private static Coordinate getOrigin() {
        for (int y = 0; y < AlgorithmMapInfo.LINE_NUM; y++) {
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (!AlgorithmMapInfo.canArrived(x, y)) {
                    continue;
                }
                return Coordinate.valueOf(x, y);
            }
        }
        return null;
    }

    private static void cleanBorderDirection() {
        for (int y = 0; y < AlgorithmMapInfo.LINE_NUM; y--) {
            AlgorithmMapInfo.setDir(0, y, PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.descDir(1, y, PointDirectionEnum.WEST.getDirection());
            AlgorithmMapInfo.descDir(AlgorithmMapInfo.COL_NUM - 2, y, PointDirectionEnum.EAST.getDirection());
            AlgorithmMapInfo.setDir(AlgorithmMapInfo.COL_NUM - 1, y, PointDirectionEnum.NONE.getDirection());
        }
        for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
            AlgorithmMapInfo.setDir(x, 0, PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.descDir(x, 1, PointDirectionEnum.NORTH.getDirection());
            AlgorithmMapInfo.descDir(x, AlgorithmMapInfo.LINE_NUM - 2, PointDirectionEnum.SOUTH.getDirection());
            AlgorithmMapInfo.setDir(x, AlgorithmMapInfo.LINE_NUM - 1, PointDirectionEnum.NONE.getDirection());
        }
    }

    public static void setBarrierSurroundingDir() {
        for (Coordinate barrier : AlgorithmMapInfo.getBarrierPoints()) {
            // 障碍物周围十二个点
            AlgorithmMapInfo.setDir(barrier.getX(), barrier.getY() - 1, PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.setDir(barrier.getX(), barrier.getY() + 1, PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.setDir(barrier.getX() - 1, barrier.getY(), PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.setDir(barrier.getX() + 1, barrier.getY(), PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.setDir(barrier.getX() + 1, barrier.getY() + 1, PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.setDir(barrier.getX() - 1, barrier.getY() + 1, PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.setDir(barrier.getX() + 1, barrier.getY() - 1, PointDirectionEnum.NONE.getDirection());
            AlgorithmMapInfo.setDir(barrier.getX() - 1, barrier.getY() - 1, PointDirectionEnum.NONE.getDirection());

            Coordinate[] surrounding = new Coordinate[4];
            surrounding[0] = Coordinate.valueOf(barrier.getX(), barrier.getY() - 2);
            surrounding[1] = Coordinate.valueOf(barrier.getX(), barrier.getY() + 2);
            surrounding[2] = Coordinate.valueOf(barrier.getX() + 2, barrier.getY());
            surrounding[3] = Coordinate.valueOf(barrier.getX() - 2, barrier.getY());
            for (Coordinate coordinate : surrounding) {
                AlgorithmMapInfo.descDir(coordinate.getX(), coordinate.getY(), PointHelper.getDirBetweenTwoPoints(coordinate, barrier));
            }
        }
    }

    public static void setObstacleWeight() {
        for (Coordinate barrier : AlgorithmMapInfo.getBarrierPoints()) {
            AlgorithmMapInfo.setObstacle(barrier.getX(), barrier.getY());
        }
    }

    private static Set<Coordinate> getCanArrivedPoints() {
        Coordinate initPoint = AlgorithmMapInfo.INIT_POINT;
        Set<Coordinate> canArchivePoints = new HashSet<>();
        canArchivePoints.add(initPoint);

        Set<Coordinate> calculedPoints = new HashSet<>();
        while (true) {
            Set<Coordinate> temp = new HashSet<>();
            for (Coordinate canArchivePoint : canArchivePoints) {
                if (calculedPoints.contains(canArchivePoint)) {
                    continue;
                }
                calculedPoints.add(canArchivePoint);
                // 计算周围四个点是否可到达
                Coordinate p1 = Coordinate.valueOf(canArchivePoint.getX() - 1, canArchivePoint.getY());
                Coordinate p2 = Coordinate.valueOf(canArchivePoint.getX() + 1, canArchivePoint.getY());
                Coordinate p3 = Coordinate.valueOf(canArchivePoint.getX(), canArchivePoint.getY() - 1);
                Coordinate p4 = Coordinate.valueOf(canArchivePoint.getX(), canArchivePoint.getY() + 1);
                if (!AlgorithmMapInfo.isObstacle(p1.getX(), p1.getY()) && !canArchivePoints.contains(p1)) {
                    temp.add(p1);
                }
                if (!AlgorithmMapInfo.isObstacle(p2.getX(), p2.getY()) && !canArchivePoints.contains(p2)) {
                    temp.add(p2);
                }
                if (!AlgorithmMapInfo.isObstacle(p3.getX(), p3.getY()) && !canArchivePoints.contains(p3)) {
                    temp.add(p3);
                }
                if (!AlgorithmMapInfo.isObstacle(p4.getX(), p4.getY()) && !canArchivePoints.contains(p4)) {
                    temp.add(p4);
                }
            }
            if (temp.size() == 0) {
                break;
            } else {
                canArchivePoints.addAll(temp);
            }
        }

        return canArchivePoints;
    }

    private static void reCalcObstacle(Set<Coordinate> points) {
        for (int y = 0; y < AlgorithmMapInfo.LINE_NUM; y++) {
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (AlgorithmMapInfo.isObstacle(x, y)) {
                    continue;
                }
                if (!points.contains(Coordinate.valueOf(x, y))) {
                    AlgorithmMapInfo.setObstacle(x, y);
                }
            }
        }
    }

}
