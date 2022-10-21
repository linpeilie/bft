package com.jdd.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.date.TemporalAccessorUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jdd.algo.AStarAlgoFibonacci;
import com.jdd.domain.AlgorithmMapInfo;
import com.jdd.domain.Coordinate;
import com.jdd.domain.RoutePoint;
import com.jdd.domain.Task;
import com.jdd.enums.PointDirectionEnum;
import com.jdd.enums.TaskTypeEnum;
import com.jdd.helper.PointHelper;
import com.sun.org.apache.bcel.internal.generic.IFLE;
import com.sun.xml.internal.org.jvnet.fastinfoset.sax.EncodingAlgorithmContentHandler;
import javafx.scene.layout.CornerRadii;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.websocket.PongMessage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(value = "/map")
@Slf4j
public class MapController {

    @PostMapping(value = "downloadResult")
    public void downloadResult() {

    }

    @PostMapping(value = "parseMap")
    public String parseMap(MultipartFile file) {
        BufferedReader br = null;
        try {
            InputStream is = file.getInputStream();
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            br = new BufferedReader(reader);
            // 解析单元格
            List<Coordinate> barriers = new ArrayList<>();
            while (br.ready()) {
                int[] arr = StrUtil.splitToInt(br.readLine(), ",");
                if (ArrayUtil.isNotEmpty(arr) && arr.length == 2) {
                    barriers.add(new Coordinate(arr[0], arr[1]));
                }
            }
            // 重置地图
            AlgorithmMapInfo algorithmMap = AlgorithmMapInfo.getInstance();
            algorithmMap.reset();
            // 设置障碍物单元格周围点方向
            for (Coordinate barrier : barriers) {
                // 周围八个点
                List<Coordinate> surrounding = new ArrayList<>();
                surrounding.add(new Coordinate(barrier.getX(), barrier.getY() - 1));
                surrounding.add(new Coordinate(barrier.getX(), barrier.getY() + 1));
                surrounding.add(new Coordinate(barrier.getX() - 1, barrier.getY()));
                surrounding.add(new Coordinate(barrier.getX() + 1, barrier.getY()));
                surrounding.forEach(point -> {
                    algorithmMap.setDir(point.getX(), point.getY(), PointDirectionEnum.NONE.getDirection());
                });
                surrounding.clear();


                surrounding.add(new Coordinate(barrier.getX(), barrier.getY() - 2));
                surrounding.add(new Coordinate(barrier.getX(), barrier.getY() + 2));
                surrounding.add(new Coordinate(barrier.getX() - 2, barrier.getY()));
                surrounding.add(new Coordinate(barrier.getX() + 2, barrier.getY()));
                surrounding.forEach(point -> {
                    algorithmMap.descDir(point.getX(), point.getY(), PointHelper.getDirBetweenTwoPoints(point, barrier));
                });
            }
            // 设置障碍物
            barriers.forEach(barrier -> {
                algorithmMap.setObstacle(barrier.getX(), barrier.getY());
                // 设置障碍物周围单元格方向

            });
            // 设置初始点的方向
            algorithmMap.setDir(AlgorithmMapInfo.INIT_POINT.getX(), AlgorithmMapInfo.INIT_POINT.getY(), PointDirectionEnum.WEST.getDirection());
            // 输出地图结构
            printMap(algorithmMap);
            System.out.println("====================================================================================================================");
            // 重新计算障碍物
            calcObstacle(algorithmMap);
            // 重新输出地图结构
            printMap(algorithmMap);
            // 清除边界方向
            cleanBorderDirection(algorithmMap);

            List<RoutePoint> routePoints = matchTask(algorithmMap);

            File tempFile = FileUtil.createTempFile(new File("result.txt"), true);
            routePoints.forEach(routePoint -> {
                String line = transLine(routePoint);
                FileUtil.appendString(line, tempFile , StandardCharsets.UTF_8);
            });

        } catch (Exception e) {
            log.error("解析文件异常", e);
            return "解析文件异常" + e.getMessage();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "解析文件成功";
    }

    private void cleanBorderDirection(AlgorithmMapInfo algorithmMap) {
        for (int y = 0; y < AlgorithmMapInfo.LINE_NUM; y--) {
            algorithmMap.setDir(0, y, PointDirectionEnum.NONE.getDirection());
            algorithmMap.descDir(1, y, PointDirectionEnum.WEST.getDirection());
            algorithmMap.descDir(AlgorithmMapInfo.COL_NUM - 2, y, PointDirectionEnum.EAST.getDirection());
            algorithmMap.setDir(AlgorithmMapInfo.COL_NUM - 1, y, PointDirectionEnum.NONE.getDirection());
        }
        for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
            algorithmMap.setDir(x, 0, PointDirectionEnum.NONE.getDirection());
            algorithmMap.descDir(x, 1, PointDirectionEnum.NORTH.getDirection());
            algorithmMap.descDir(x, AlgorithmMapInfo.LINE_NUM - 2, PointDirectionEnum.SOUTH.getDirection());
            algorithmMap.setDir(x, AlgorithmMapInfo.LINE_NUM - 1, PointDirectionEnum.NONE.getDirection());
        }
    }

    private String transLine(RoutePoint routePoint) {
        StringBuilder result = new StringBuilder(PointDirectionEnum.getEnumByDirection(routePoint.getDirection()).getOutputDirection())
                .append(":")
                .append(routePoint.getX())
                .append(",")
                .append(routePoint.getY())
                .append(":");
        int size = routePoint.getCleanPoints().size();
        for (int i = 0; i < size; i++) {
            result.append(routePoint.getX())
                    .append(",")
                    .append(routePoint.getY());
            if (i != size - 1) {
                result.append(";");
            }
        }
        return result.toString();
    }

    private void printMap(AlgorithmMapInfo algorithmMap) {
        for (int y = AlgorithmMapInfo.LINE_NUM - 1; y >= 0; y--) {
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (algorithmMap.isObstacle(x, y)) {
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

    private void calcObstacle(AlgorithmMapInfo mapInfo) throws InterruptedException {

        Set<Coordinate> noObstaclePoints = getCanArchivePoints(mapInfo);
        System.out.println("所有可以到达的点的 " + noObstaclePoints.size());
        for (int y = 0; y < AlgorithmMapInfo.LINE_NUM; y++) {
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (mapInfo.isObstacle(x, y)) {
                    continue;
                }
                if (!noObstaclePoints.contains(new Coordinate(x, y))) {
                    mapInfo.setObstacle(x, y);
                }
            }
        }
    }

    private Set<Coordinate> getCanArchivePoints(AlgorithmMapInfo mapInfo) {
        Coordinate initPoint = AlgorithmMapInfo.INIT_POINT;
        Set<Coordinate> canArchivePoints = new HashSet<>();
        canArchivePoints.add(initPoint);

        Set<Coordinate> calculedPoints = new HashSet<>();

        while (true) {
            Set<Coordinate> temp = new HashSet<>();
            for (Coordinate point : canArchivePoints) {
                if (calculedPoints.contains(point)) {
                    continue;
                }
                calculedPoints.add(point);
                Set<Coordinate> canArchivePoints1 = getCanArchivePoints(mapInfo, point, canArchivePoints);
                temp.addAll(canArchivePoints1);
            }
            if (temp.size() == 0) {
                break;
            } else {
                canArchivePoints.addAll(temp);
            }
        }
        return canArchivePoints;
    }

    private Set<Coordinate> getCanArchivePoints(AlgorithmMapInfo mapInfo, Coordinate currentPoint,
                                                Set<Coordinate> canArchivePoints) {
        Set<Coordinate> set = new HashSet<>();
        Coordinate p1 = new Coordinate(currentPoint.getX() - 1, currentPoint.getY());
        Coordinate p2 = new Coordinate(currentPoint.getX() + 1, currentPoint.getY());
        Coordinate p3 = new Coordinate(currentPoint.getX(), currentPoint.getY() - 1);
        Coordinate p4 = new Coordinate(currentPoint.getX(), currentPoint.getY() + 1);
        if (!mapInfo.isObstacle(p1.getX(), p1.getY()) && !canArchivePoints.contains(p1)) {
            set.add(p1);
        }
        if (!mapInfo.isObstacle(p2.getX(), p2.getY()) && !canArchivePoints.contains(p2)) {
            set.add(p2);
        }
        if (!mapInfo.isObstacle(p3.getX(), p3.getY()) && !canArchivePoints.contains(p3)) {
            set.add(p3);
        }
        if (!mapInfo.isObstacle(p4.getX(), p4.getY()) && !canArchivePoints.contains(p4)) {
            set.add(p4);
        }
        return set;
    }

    private List<RoutePoint> matchTask(AlgorithmMapInfo mapInfo) {
        int passed = 0;
        int currentDirection = PointDirectionEnum.WEST.getDirection();
        // 当前点
        Coordinate currentCoordinate = new Coordinate(AlgorithmMapInfo.INIT_POINT.getX(), AlgorithmMapInfo.INIT_POINT.getY());
        List<RoutePoint> routePoints = new ArrayList<>();
        // 先让车辆前往最右边的点
        for (int x = AlgorithmMapInfo.COL_NUM - 1; x > currentCoordinate.getX(); x--) {
            boolean obstacle = mapInfo.isObstacle(x, currentCoordinate.getY());
            if (obstacle) {
                continue;
            }
            Coordinate dest = new Coordinate(x, currentCoordinate.getY());
            List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
            if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                continue;
            }
            // 找到之后
            Task task = initTask(path, currentDirection, TaskTypeEnum.CLEANING);
            // 当前方向
            currentDirection = task.getRoutePoints().get(task.getRoutePoints().size() - 1).getDirection();
            // 当前点
            currentCoordinate = dest;
            break;
        }

        // 初始化y轴上部分任务
        for (int y = AlgorithmMapInfo.INIT_POINT.getY() - 3; y >= 0; y -= 3) {
            List<Task> nextTasks = getNextTask(mapInfo, y, currentDirection, currentCoordinate);
            if (CollectionUtil.isEmpty(nextTasks)) {
                break;
            }
            for (Task nextTask : nextTasks) {
                passed = addRoute(routePoints, passed, nextTask, mapInfo);
            }
        }
        // 初始化y轴下部分任务
        for (int y = AlgorithmMapInfo.INIT_POINT.getY(); y <= AlgorithmMapInfo.LINE_NUM; y += 3) {
            List<Task> nextTasks = getNextTask(mapInfo, y, currentDirection, currentCoordinate);
            if (CollectionUtil.isEmpty(nextTasks)) {
                break;
            }
            for (Task nextTask : nextTasks) {
                passed = addRoute(routePoints, passed, nextTask, mapInfo);
            }
        }
        // 添加回程任务
        RoutePoint currentPoint = routePoints.get(routePoints.size() - 1);
        List<Coordinate> backPath = AStarAlgoFibonacci.getShortestPath(new Coordinate(currentPoint.getX(), currentPoint.getY()), AlgorithmMapInfo.INIT_POINT, mapInfo);
        Task backTask = initTask(backPath, currentPoint.getDirection(), TaskTypeEnum.BACK);
        routePoints.addAll(backTask.getRoutePoints());
        return routePoints;
    }

    private int addRoute(List<RoutePoint> routePoints, int passedPoints, Task task, AlgorithmMapInfo mapInfo) {
        int passed = passedPoints;
        for (RoutePoint routePoint : task.getRoutePoints()) {
            routePoints.add(routePoint);
            passed++;
            // 两万
            if (routePoints.size() % 20000 == 0) {
                // 添加回去的任务
                Coordinate tempPoint = new Coordinate(routePoint.getX(), routePoint.getY());
                Coordinate initPoint = AlgorithmMapInfo.INIT_POINT;
                List<Coordinate> chargePath = AStarAlgoFibonacci.getShortestPath(tempPoint, initPoint, mapInfo);
                Task chargeTask = initTask(chargePath, routePoint.getDirection(), TaskTypeEnum.CHARGE);
                List<RoutePoint> chargeTaskRoutePoints = chargeTask.getRoutePoints();
                routePoints.addAll(chargeTaskRoutePoints);
                passed = 0;
                // 获取回去后最后一个点的方向
                int initPointDirection = chargeTaskRoutePoints.get(chargeTaskRoutePoints.size() - 1).getDirection();
                // 如果不是西的话，则，添加一个向西的指令
                if (initPointDirection != PointDirectionEnum.WEST.getDirection()) {
                    RoutePoint initPointTurn = initRoutePoint(initPoint.getX(), initPoint.getY(), TaskTypeEnum.CLEANING, PointDirectionEnum.WEST.getDirection());
                    routePoints.add(initPointTurn);
                    passed++;
                }
                // 再获取回来的路径
                List<Coordinate> backTaskPath = AStarAlgoFibonacci.getShortestPath(initPoint, tempPoint, mapInfo);
                Task backCleanTask = initTask(backTaskPath, PointDirectionEnum.WEST.getDirection(), TaskTypeEnum.CLEANING);
                passed = addRoute(routePoints, passed, backCleanTask, mapInfo);
                // 获取最后到达的方向
                List<RoutePoint> backCleanPath = backCleanTask.getRoutePoints();
                // 获取当前点
                RoutePoint currentPoint = backCleanPath.get(backCleanPath.size() - 1);
                if (currentPoint.getDirection() != routePoint.getDirection()) {
                    // 再添加一个
                    routePoints.add(routePoint);
                    passed++;
                }
            }
        }
        return passed;
    }

    private List<Task> getNextTask(AlgorithmMapInfo mapInfo, int currentY, int currentDirection, Coordinate currentCoordinate) {
        boolean isRight = currentDirection == PointDirectionEnum.WEST.getDirection();
        List<Task> tasks = new ArrayList<>();
        if (isRight) {
            // 先到达当前点
            // 右边的，从右往左循环
            for (int x = AlgorithmMapInfo.COL_NUM - 1; x >= 0; x--) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                Coordinate dest = new Coordinate(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
            }
            // 再找到最另外一侧坐标点
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                Coordinate dest = new Coordinate(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
            }
        } else {
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                Coordinate dest = new Coordinate(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
            }
            for (int x = AlgorithmMapInfo.COL_NUM - 1; x >= 0; x--) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                Coordinate dest = new Coordinate(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
            }
        }
        return tasks;
    }

    private Task initTask(List<Coordinate> path, int currentPointDirection, TaskTypeEnum taskTypeEnum) {
        Coordinate src = path.get(0);
        Coordinate des = path.get(path.size() - 1);
        List<RoutePoint> routePoints = new ArrayList<>();
        int direction = currentPointDirection;
        for (int i = 0; i < path.size() - 1; i++) {
            // 如果是第一个点，需要判断是否需要转弯
            Coordinate currentPoint = path.get(i);
            Coordinate nextPoint = path.get(i + 1);
            int nextDirection = PointHelper.getDirBetweenTwoPoints(currentPoint, nextPoint);
            // 如果需要转弯
            if (nextDirection != direction) {
                routePoints.add(initRoutePoint(currentPoint.getX(), currentPoint.getY(), taskTypeEnum, nextDirection));
            }
            // 添加任务
            routePoints.add(initRoutePoint(currentPoint.getX(), currentPoint.getY(), taskTypeEnum, direction));
            // 重新设置车辆方向
            direction = nextDirection;
        }

        Task task = new Task();
        task.setSrc(src);
        task.setDes(des);
        task.setRoutePoints(routePoints);

        return task;
    }

    private RoutePoint initRoutePoint(int x, int y, TaskTypeEnum taskType, int direction) {
        RoutePoint routePoint = new RoutePoint();
        routePoint.setX(x);
        routePoint.setY(y);
        routePoint.setType(taskType);
        routePoint.setDirection(direction);
        if (TaskTypeEnum.CLEANING.equals(taskType)) {
            routePoint.setCleanPoints(calcCleanPoint(new Coordinate(x, y), direction));
        }
        return routePoint;
    }

    private List<Coordinate> calcCleanPoint(Coordinate currentPoint, int currentDirection) {
        int currentX = currentPoint.getX();
        int currentY = currentPoint.getY();
        List<Coordinate> list = new ArrayList<>();
        if (currentDirection == PointDirectionEnum.SOUTH.getDirection()
                || currentDirection == PointDirectionEnum.NORTH.getDirection()) {
            list.add(new Coordinate(currentX - 1, currentY));
            list.add(new Coordinate(currentX, currentY));
            list.add(new Coordinate(currentX + 1, currentY));
        } else if (currentDirection == PointDirectionEnum.WEST.getDirection()
                || currentDirection == PointDirectionEnum.EAST.getDirection()) {
            list.add(new Coordinate(currentX, currentY - 1));
            list.add(new Coordinate(currentX, currentY));
            list.add(new Coordinate(currentX, currentY + 1));
        }
        return list;
    }

}
