package com.jdd.domain;

import com.jdd.enums.PointDirectionEnum;

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
    public static final Coordinate INIT_POINT = new Coordinate(141,71);

    /**
     * 点数据，前8位点权重值，后4位是方向
     */
    private int[][] pointData;

    private static final AlgorithmMapInfo INSTANCE = new AlgorithmMapInfo();

    private AlgorithmMapInfo() {
        this.pointData = new int[LINE_NUM][COL_NUM];
    }

    public static AlgorithmMapInfo getInstance() {
        return INSTANCE;
    }

    public void reset() {
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
    private boolean validateCoordinate(int x, int y) {
        return x < 0 || x >= COL_NUM || y < 0 || y >= LINE_NUM;
    }

    /**
     * 设置障碍物点
     *
     * @param x      x坐标点
     * @param y      y坐标点
     * @param weight 权重值
     */
    public void setWeight(int x, int y, int weight) {
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
    public void increWeight(int x, int y, int weight) {
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
    public int getWeight(int x, int y) {
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
    public void setDir(int x, int y, int dir) {
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
    public int getDir(int x, int y) {
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
    public void descDir(int x, int y, int dir) {
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
     * 获取指定点的方向
     *
     * @param point 点
     * @return 方向
     */
    public int getDir(Coordinate point) {
        return this.getDir(point.getX(), point.getY());
    }

    /**
     * 设置障碍物
     *
     * @param x x坐标
     * @param y y坐标
     */
    public void setObstacle(int x, int y) {
        this.setWeight(x, y, OBSTACLE);
    }

    /**
     * 设置默认权重
     *
     * @param x x坐标
     * @param y y坐标
     */
    public void setDefaultWeight(int x, int y) {
        this.setWeight(x, y, DEFAULT_WEIGHT);
    }

    /**
     * 是否障碍物
     *
     * @param x x坐标
     * @param y y坐标
     * @return 如果是障碍物的话，返回true，否则返回false
     */
    public boolean isObstacle(int x, int y) {
        return getWeight(x, y) == OBSTACLE;
    }

}
