package com.jdd.domain;

import com.jdd.enums.PointDirectionEnum;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class AlgorithmMapInfo {

    /**
     * 障碍物权重值
     */
    private static final int OBSTACLE = 255;

    /**
     * 默认权重值
     */
    private static final int DEFAULT_WEIGHT = 1;

    /**
     * 行数
     */
    public static final int LINE_NUM = 500;

    /**
     * 列数
     */
    public static final int COL_NUM = 200;

    /**
     * 初始点
     */
    public static final Coordinate INIT_POINT = Coordinate.valueOf(141,71);

    /**
     * 原障碍物点
     */
    private static final Set<Coordinate> BARRIER_POINTS = new HashSet<>();

    /**
     * 清掉的点
     */
    private static final Set<Coordinate> CLEAN_POINTS = new HashSet<>();

    /**
     * 可以到达的点
     */
    private static final Set<Coordinate> CAN_ARRIVED_POINTS = new HashSet<>();

    private static Integer CAN_ARRIVED = 0;

    /**
     * 点数据，前8位点权重值，后4位是方向
     */
    private static final int[][] pointData;

    static {
        pointData = new int[LINE_NUM][COL_NUM];
        reset();
    }

    public static void reset() {
        // 设置默认权重值
        for (int i = 0; i < LINE_NUM; i++) {
            for (int j = 0; j < COL_NUM; j++) {
                setWeight(j, i, DEFAULT_WEIGHT);
                setDir(j, i, 15);
            }
        }
    }

    /**
     * 校验障碍物
     *
     * @param x x坐标
     * @param y y坐标
     */
    private static boolean validateCoordinate(int x, int y) {
        return x < 0 || x >= COL_NUM || y < 0 || y >= LINE_NUM;
    }

    /**
     * 设置障碍物点
     *
     * @param x      x坐标点
     * @param y      y坐标点
     * @param weight 权重值
     */
    public static void setWeight(int x, int y, int weight) {
        if (validateCoordinate(x, y)) {
            return;
        }
        int data = pointData[y][x];
        data = (weight << 4) + (data & 0xf);
        pointData[y][x] = data;
    }

    /**
     * 增加权重
     *
     * @param x      x坐标点
     * @param y      y坐标点
     * @param weight 权重值
     */
    public static void increWeight(int x, int y, int weight) {
        if (validateCoordinate(x, y)) {
            return;
        }
        int oldWeight = getWeight(x, y);
        int newWeight = weight + oldWeight;
        newWeight = Math.min(newWeight, OBSTACLE);
        setWeight(x, y, newWeight);
    }

    /**
     * 设置权重值
     *
     * @param x x坐标值
     * @param y y坐标值
     */
    public static int getWeight(int x, int y) {
        if (validateCoordinate(x, y)) {
            return OBSTACLE;
        }
        return pointData[y][x] >> 4;
    }

    /**
     * 设置方向
     *
     * @param x   x坐标值
     * @param y   y坐标值
     * @param dir 方向
     */
    public static void setDir(int x, int y, int dir) {
        if (validateCoordinate(x, y)) {
            return;
        }
        int data = pointData[y][x];
        data = data >> 4;
        data = data << 4;
        data = data + (dir & 0xf);
        pointData[y][x] = data;
    }

    /**
     * 获取方向
     *
     * @param x x坐标值
     * @param y y坐标值
     * @return 方向值
     */
    public static int getDir(int x, int y) {
        if (validateCoordinate(x, y)) {
            return PointDirectionEnum.NONE.getDirection();
        }
        return pointData[y][x] & 0xf;
    }

    /**
     * 删掉指定点指定方向
     *
     * @param x   x坐标值
     * @param y   y坐标值
     * @param dir 方向
     */
    public static void descDir(int x, int y, int dir) {
        if (validateCoordinate(x, y)) {
            return;
        }
        int oldDir = getDir(x, y);
        // 如果之前不支持这个方向，则取消修改
        if ((oldDir & dir) == 0) {
            return;
        }
        int newDir = oldDir - dir;
        setDir(x, y, newDir);
    }

    /**
     * 增加指定点指定方向
     */
    public static void incrDir(int x, int y, int dir) {
        if (validateCoordinate(x, y)) {
            return;
        }
        int oldDir = getDir(x, y);
        // 如果之前支持这个方向，则取消修改
        if ((oldDir & dir) != 0) {
            return;
        }
        int newDir = oldDir + dir;
        setDir(x, y, newDir);
    }

    /**
     * 获取指定点的方向
     *
     * @param point 点
     * @return 方向
     */
    public static int getDir(Coordinate point) {
        return getDir(point.getX(), point.getY());
    }

    public static void setObstacle(Coordinate coordinate) {
        setObstacle(coordinate.getX(), coordinate.getY());
    }

    /**
     * 设置障碍物
     *
     * @param x x坐标
     * @param y y坐标
     */
    public static void setObstacle(int x, int y) {
        setWeight(x, y, OBSTACLE);
        BARRIER_POINTS.add(Coordinate.valueOf(x, y));
    }

    /**
     * 设置默认权重
     *
     * @param x x坐标
     * @param y y坐标
     */
    public static void setDefaultWeight(int x, int y) {
        setWeight(x, y, DEFAULT_WEIGHT);
    }

    /**
     * 是否障碍物
     *
     * @param x x坐标
     * @param y y坐标
     * @return 如果是障碍物的话，返回true，否则返回false
     */
    public static boolean isObstacle(int x, int y) {
        return getWeight(x, y) == OBSTACLE;
    }

    public static boolean canArrived(Coordinate point) {
        return canArrived(point.getX(), point.getY());
    }

    public static boolean canArrived(int x, int y) {
        return !isObstacle(x, y) && getDir(x, y) != PointDirectionEnum.NONE.getDirection();
    }

    public static Set<Coordinate> getBarrierPoints() {
        return BARRIER_POINTS;
    }

    public static void setCanArrived(Integer canArrived) {
        CAN_ARRIVED = canArrived;
    }

    public static Integer getCanArrived() {
        return CAN_ARRIVED;
    }

    public static boolean addCleanPoint(Coordinate point) {
        return CLEAN_POINTS.add(point);
    }

    public static Set<Coordinate> getCleanPoints() {
        return CLEAN_POINTS;
    }
    public static int getCleanPointSize() {
        return CLEAN_POINTS.size();
    }

}
