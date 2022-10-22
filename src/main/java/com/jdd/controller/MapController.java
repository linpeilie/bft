package com.jdd.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jdd.algo.AStarAlgoFibonacci;
import com.jdd.domain.AlgorithmMapInfo;
import com.jdd.domain.Coordinate;
import com.jdd.domain.RoutePoint;
import com.jdd.domain.Task;
import com.jdd.enums.PointDirectionEnum;
import com.jdd.enums.TaskTypeEnum;
import com.jdd.helper.PointHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping(value = "/map")
@Slf4j
public class MapController {

    private static final AtomicInteger PASSED = new AtomicInteger();

    private static final Set<Coordinate> CLEAN_POINTS = new HashSet<>();

    private static final Set<Coordinate> CAN_RECEIVED_POINTS = new HashSet<>();

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
                    barriers.add(Coordinate.valueOf(arr[0], arr[1]));
                }
            }
            // 重置地图
            AlgorithmMapInfo algorithmMap = AlgorithmMapInfo.getInstance();
            algorithmMap.reset();
            CLEAN_POINTS.clear();
            CAN_RECEIVED_POINTS.clear();
            PASSED.set(0);
            // 删除之前的文件
            String bashPath = getResourceBasePath();
            File resultFile = new File(bashPath, "result.txt");
            if (FileUtil.exist(resultFile)) {
                FileUtil.del(resultFile);
            }
            // 设置障碍物单元格周围点方向
            for (Coordinate barrier : barriers) {
                // 周围十二个点
                List<Coordinate> surrounding = new ArrayList<>();
                surrounding.add(Coordinate.valueOf(barrier.getX(), barrier.getY() - 1));
                surrounding.add(Coordinate.valueOf(barrier.getX(), barrier.getY() + 1));
                surrounding.add(Coordinate.valueOf(barrier.getX() - 1, barrier.getY()));
                surrounding.add(Coordinate.valueOf(barrier.getX() + 1, barrier.getY()));
                surrounding.add(Coordinate.valueOf(barrier.getX() + 1, barrier.getY() + 1));
                surrounding.add(Coordinate.valueOf(barrier.getX() - 1, barrier.getY() + 1));
                surrounding.add(Coordinate.valueOf(barrier.getX() + 1, barrier.getY() - 1));
                surrounding.add(Coordinate.valueOf(barrier.getX() - 1, barrier.getY() - 1));
                surrounding.forEach(point -> {
                    algorithmMap.setDir(point.getX(), point.getY(), PointDirectionEnum.NONE.getDirection());
                });
                surrounding.clear();

                surrounding.add(Coordinate.valueOf(barrier.getX(), barrier.getY() - 2));
                surrounding.add(Coordinate.valueOf(barrier.getX(), barrier.getY() + 2));
                surrounding.add(Coordinate.valueOf(barrier.getX() - 2, barrier.getY()));
                surrounding.add(Coordinate.valueOf(barrier.getX() + 2, barrier.getY()));
                surrounding.forEach(point -> {
                    algorithmMap.descDir(point.getX(), point.getY(), PointHelper.getDirBetweenTwoPoints(point, barrier));
                });

                surrounding.clear();
            }
            // 设置障碍物
            barriers.forEach(barrier -> {
                algorithmMap.setObstacle(barrier.getX(), barrier.getY());
            });
            // 设置初始点的方向
            algorithmMap.setDir(AlgorithmMapInfo.INIT_POINT.getX(), AlgorithmMapInfo.INIT_POINT.getY(), PointDirectionEnum.NORTH.getDirection());
            // 输出地图结构
//            printMap(algorithmMap);
            System.out.println("====================================================================================================================");
            // 重新计算障碍物
            calcObstacle(algorithmMap);
            // 重新输出地图结构
            printMap(algorithmMap);
            // 清除边界方向
            cleanBorderDirection(algorithmMap);

            matchTask(algorithmMap);

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
        StringBuilder result = new StringBuilder()
                .append(PointDirectionEnum.getEnumByDirection(routePoint.getDirection()).getOutputDirection())
                .append(":")
                .append(routePoint.getX()).append(",")
                .append(routePoint.getY());
        if (CollectionUtil.isNotEmpty(routePoint.getCleanPoints())) {
            int size = routePoint.getCleanPoints().size();
            result.append(":");
            for (int i = 0; i < size; i++) {
                result.append(routePoint.getCleanPoints().get(i).getX())
                        .append(",")
                        .append(routePoint.getCleanPoints().get(i).getY());
                if (i != size - 1) {
                    result.append(";");
                }
            }
        }
        return result.toString();
    }

    private void printMap(AlgorithmMapInfo algorithmMap) {
        for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
            System.out.print((x + 1)  + " ");
        }
        for (int y = AlgorithmMapInfo.LINE_NUM - 1; y >= 0; y--) {
            System.out.print((y + 1) + " ");
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
        CAN_RECEIVED_POINTS.addAll(noObstaclePoints);
        System.out.println("所有可以到达的点的 " + noObstaclePoints.size());
        for (int y = 0; y < AlgorithmMapInfo.LINE_NUM; y++) {
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (mapInfo.isObstacle(x, y)) {
                    continue;
                }
                if (!noObstaclePoints.contains(Coordinate.valueOf(x, y))) {
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

    private Set<Coordinate> getCanArchivePoints(AlgorithmMapInfo mapInfo, Coordinate currentPoint, Set<Coordinate> canArchivePoints) {
        Set<Coordinate> set = new HashSet<>();
        Coordinate p1 = Coordinate.valueOf(currentPoint.getX() - 1, currentPoint.getY());
        Coordinate p2 = Coordinate.valueOf(currentPoint.getX() + 1, currentPoint.getY());
        Coordinate p3 = Coordinate.valueOf(currentPoint.getX(), currentPoint.getY() - 1);
        Coordinate p4 = Coordinate.valueOf(currentPoint.getX(), currentPoint.getY() + 1);
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
        int currentDirection = PointDirectionEnum.NORTH.getDirection();
        // 当前点
        Coordinate currentCoordinate = Coordinate.valueOf(AlgorithmMapInfo.INIT_POINT.getX(), AlgorithmMapInfo.INIT_POINT.getY());
        List<RoutePoint> routePoints = new ArrayList<>();
        // 先让车辆前往最右边的点
        for (int x = AlgorithmMapInfo.COL_NUM; x > currentCoordinate.getX(); x--) {
            boolean obstacle = mapInfo.isObstacle(x, currentCoordinate.getY() + 1);
            if (obstacle) {
                continue;
            }
            Coordinate dest = Coordinate.valueOf(x, currentCoordinate.getY() + 1);
            List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
            if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                continue;
            }
            // 找到之后
            Task task = initTask(path, currentDirection, TaskTypeEnum.CLEANING);
            addRoute(routePoints, task, mapInfo);
            // 当前方向
            currentDirection = task.getRoutePoints().get(task.getRoutePoints().size() - 1).getDirection();
            // 当前点
            currentCoordinate = dest;
            break;
        }
        boolean isRight = true;
        // 初始化y轴上部分任务
        for (int y = AlgorithmMapInfo.INIT_POINT.getY(); y <= AlgorithmMapInfo.LINE_NUM; y += 2) {
            System.out.println("当前y" + y);
            List<Task> nextTasks = getNextTask(mapInfo, y, currentDirection, currentCoordinate, isRight);
            if (CollectionUtil.isEmpty(nextTasks)) {
                break;
            }
            isRight = !isRight;
            for (Task nextTask : nextTasks) {
                addRoute(routePoints, nextTask, mapInfo);
            }
            Task lastTask = nextTasks.get(nextTasks.size() - 1);
            currentCoordinate = lastTask.getDes();
        }
        // 初始化y轴下部分任务
        for (int y = AlgorithmMapInfo.INIT_POINT.getY(); y >= 0; y -= 2) {
            System.out.println("当前y" + y);
            List<Task> nextTasks = getNextTask(mapInfo, y, currentDirection, currentCoordinate, isRight);
            if (CollectionUtil.isEmpty(nextTasks)) {
                break;
            }
            isRight = !isRight;
            for (Task nextTask : nextTasks) {
                addRoute(routePoints, nextTask, mapInfo);
            }
            Task lastTask = nextTasks.get(nextTasks.size() - 1);
            currentCoordinate = lastTask.getDes();
        }
        // 当前点
        RoutePoint currentPoint = routePoints.get(routePoints.size() - 1);
        // 追加补偿
        Set<Coordinate> nonCleanPoints = new HashSet<>();
        for (Coordinate canReceivedPoint : CAN_RECEIVED_POINTS) {
            if (CLEAN_POINTS.contains(canReceivedPoint)) {
                continue;
            }
            nonCleanPoints.add(canReceivedPoint);
        }
        // 补偿点
        Set<Coordinate> compensationPoints = new HashSet<>();
        for (Coordinate nonCleanPoint : nonCleanPoints) {
            int x = nonCleanPoint.getX();
            int y = nonCleanPoint.getY();
            if (!nonCleanPoints.contains(Coordinate.valueOf(x - 1, y))) {
                continue;
            }
            if (!nonCleanPoints.contains(Coordinate.valueOf(x + 1, y))) {
                continue;
            }
            if (!nonCleanPoints.contains(Coordinate.valueOf(x, y - 1))) {
                continue;
            }
            if (!nonCleanPoints.contains(Coordinate.valueOf(x, y + 1))) {
                continue;
            }
            compensationPoints.add(nonCleanPoint);
        }
        System.out.println("需要补偿的点数量 : " + compensationPoints.size());

        System.out.println("可清理数量");
        System.out.println("扫过点数量 " + CLEAN_POINTS.size());
        appendResult(routePoints, TaskTypeEnum.CLEANING.getType());
        routePoints.clear();
        appendResult(routePoints, TaskTypeEnum.CHARGE.getType());
        List<Coordinate> backPath = AStarAlgoFibonacci.getShortestPath(Coordinate.valueOf(currentPoint.getX(), currentPoint.getY()), AlgorithmMapInfo.INIT_POINT, mapInfo);
        Task backTask = initTask(backPath, currentPoint.getDirection(), TaskTypeEnum.BACK);
        List<RoutePoint> backRoutePoints = backTask.getRoutePoints();
        routePoints.addAll(backRoutePoints);
        // 获取当前方向
        RoutePoint lastRoutePoint = backRoutePoints.get(backRoutePoints.size() - 1);
        routePoints.add(initRoutePoint(AlgorithmMapInfo.INIT_POINT.getX(), AlgorithmMapInfo.INIT_POINT.getY(),
                TaskTypeEnum.BACK, lastRoutePoint.getDirection()));
        appendResult(routePoints, TaskTypeEnum.BACK.getType());
        return routePoints;
    }

    private void appendResult(List<RoutePoint> routePoints, int type) {
        String bashPath = getResourceBasePath();
        File resultFile = new File(bashPath, "result.txt");
        System.out.println("写入任务类型 : " + type + "，写入数据数量 : " + routePoints.size());
        if (TaskTypeEnum.BACK.getType() != type) {
            FileUtil.appendString(type + "\n", resultFile, StandardCharsets.UTF_8);
        }
        for (RoutePoint routePoint : routePoints) {
            String line = transLine(routePoint) + "\n";
            FileUtil.appendString(line, resultFile, StandardCharsets.UTF_8);
        }
        if (TaskTypeEnum.BACK.getType() == type) {
            FileUtil.appendString(type + "", resultFile, StandardCharsets.UTF_8);
        }
    }


    private static String getResourceBasePath() {
        // 获取跟目录
        File path = null;
        try {
            path = new File(ResourceUtils.getURL("classpath:").getPath());
        } catch (FileNotFoundException e) {
            // nothing to do
        }
        if (path == null || !path.exists()) {
            path = new File("");
        }

        String pathStr = path.getAbsolutePath();
        // 如果是在eclipse中运行，则和target同级目录,如果是jar部署到服务器，则默认和jar包同级
        pathStr = pathStr.replace("\\target\\classes", "");

        return pathStr;
    }


    private void addRoute(List<RoutePoint> routePoints, Task task, AlgorithmMapInfo mapInfo) {
        for (RoutePoint routePoint : task.getRoutePoints()) {
            addRoute(routePoints, routePoint, mapInfo);
        }
    }

    private void addRoute(List<RoutePoint> routePoints, RoutePoint routePoint, AlgorithmMapInfo mapInfo) {
        routePoints.add(routePoint);
        List<Coordinate> cleanPoints = routePoint.getCleanPoints();
        if (CollectionUtil.isNotEmpty(cleanPoints)) {
            for (Coordinate cleanPoint : cleanPoints) {
                boolean add = CLEAN_POINTS.add(cleanPoint);
                if (add) {
                    // 设置扫过的点权重
                    mapInfo.setWeight(cleanPoint.getX(), cleanPoint.getY(), 200);
                }
            }
        }
        PASSED.incrementAndGet();
        // 两万
        if (PASSED.get() % 20000 == 0) {
            // 写入文件
            appendResult(routePoints, TaskTypeEnum.CLEANING.getType());
            routePoints.clear();
            // 添加回去的任务
            Coordinate tempPoint = Coordinate.valueOf(routePoint.getX(), routePoint.getY());
            Coordinate initPoint = AlgorithmMapInfo.INIT_POINT;
            List<Coordinate> chargePath = AStarAlgoFibonacci.getShortestPath(tempPoint, initPoint, mapInfo);
            Task chargeTask = initTask(chargePath, routePoint.getDirection(), TaskTypeEnum.CHARGE);
            List<RoutePoint> chargeTaskRoutePoints = chargeTask.getRoutePoints();
            routePoints.addAll(chargeTaskRoutePoints);
            // 获取回去后最后一个点的方向
            int initPointDirection = chargeTaskRoutePoints.get(chargeTaskRoutePoints.size() - 1).getDirection();
            // 如果不是西的话，则，添加一个向东的指令
            if (initPointDirection != PointDirectionEnum.NORTH.getDirection()) {
                RoutePoint initPointTurn = initRoutePoint(initPoint.getX(), initPoint.getY(), TaskTypeEnum.CLEANING, PointDirectionEnum.NORTH.getDirection());
                routePoints.add(initPointTurn);
            }
            appendResult(routePoints, TaskTypeEnum.CHARGE.getType());
            routePoints.clear();
            PASSED.set(0);
            // 再获取回来的路径
            List<Coordinate> backTaskPath = AStarAlgoFibonacci.getShortestPath(initPoint, tempPoint, mapInfo);
            Task backCleanTask = initTask(backTaskPath, PointDirectionEnum.NORTH.getDirection(), TaskTypeEnum.CLEANING);
            addRoute(routePoints, backCleanTask, mapInfo);
            // 获取最后到达的方向
            List<RoutePoint> backCleanPath = backCleanTask.getRoutePoints();
            // 获取当前点
            RoutePoint currentPoint = backCleanPath.get(backCleanPath.size() - 1);
            if (currentPoint.getDirection() != routePoint.getDirection()) {
                // 再添加一个
                addRoute(routePoints, routePoint, mapInfo);
            }
        }
    }

    private List<Task> getNextTask(AlgorithmMapInfo mapInfo, int currentY, int currentDirection, Coordinate currentCoordinate, boolean isRight) {
        List<Task> tasks = new ArrayList<>();
        if (isRight) {
            // 先到达当前点
            // 右边的，从右往左循环
            for (int x = AlgorithmMapInfo.COL_NUM - 1; x >= 0; x--) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                if (mapInfo.getDir(x, currentY) == 0) {
                    continue;
                }
                Coordinate dest = Coordinate.valueOf(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                currentCoordinate = path.get(path.size() - 1);
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
                break;
            }
            // 再找到最另外一侧坐标点
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                if (mapInfo.getDir(x, currentY) == 0) {
                    continue;
                }
                Coordinate dest = Coordinate.valueOf(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
                break;
            }
        } else {
            for (int x = 0; x < AlgorithmMapInfo.COL_NUM; x++) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                if (mapInfo.getDir(x, currentY) == 0) {
                    continue;
                }
                Coordinate dest = Coordinate.valueOf(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                currentCoordinate = path.get(path.size() - 1);
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
                break;
            }
            for (int x = AlgorithmMapInfo.COL_NUM - 1; x >= 0; x--) {
                if (mapInfo.isObstacle(x, currentY)) {
                    continue;
                }
                if (mapInfo.getDir(x, currentY) == 0) {
                    continue;
                }
                Coordinate dest = Coordinate.valueOf(x, currentY);
                List<Coordinate> path = AStarAlgoFibonacci.getShortestPath(currentCoordinate, dest, mapInfo);
                if (CollectionUtil.isEmpty(path) || path.size() == 1) {
                    continue;
                }
                tasks.add(initTask(path, currentDirection, TaskTypeEnum.CLEANING));
                break;
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
            // 减轻当前点权重
            AlgorithmMapInfo.getInstance().increWeight(currentPoint.getX(), currentPoint.getY(), 1);
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
        routePoint.setDirection(direction);
        if (TaskTypeEnum.CLEANING.equals(taskType)) {
            routePoint.setCleanPoints(calcCleanPoint(Coordinate.valueOf(x, y), direction));
        }
        return routePoint;
    }

    private List<Coordinate> calcCleanPoint(Coordinate currentPoint, int currentDirection) {
        int currentX = currentPoint.getX();
        int currentY = currentPoint.getY();
        List<Coordinate> list = new ArrayList<>();
        if (currentDirection == PointDirectionEnum.SOUTH.getDirection() || currentDirection == PointDirectionEnum.NORTH.getDirection()) {
            list.add(Coordinate.valueOf(currentX - 1, currentY));
            list.add(Coordinate.valueOf(currentX, currentY));
            list.add(Coordinate.valueOf(currentX + 1, currentY));
        } else if (currentDirection == PointDirectionEnum.WEST.getDirection() || currentDirection == PointDirectionEnum.EAST.getDirection()) {
            list.add(Coordinate.valueOf(currentX, currentY - 1));
            list.add(Coordinate.valueOf(currentX, currentY));
            list.add(Coordinate.valueOf(currentX, currentY + 1));
        }
        return list;
    }

}
