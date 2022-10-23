package com.jdd.helper;

import com.jdd.domain.Coordinate;
import com.jdd.enums.PointDirectionEnum;

public final class PointHelper {

    private PointHelper() {
    }

    /**
     * 获取北方向值
     */
    public static int getNorthDirection() {
        return PointDirectionEnum.NORTH.getDirection();
    }

    /**
     * 获取南方向
     */
    public static int getSouthDirection() {
        return PointDirectionEnum.SOUTH.getDirection();
    }

    /**
     * 获取西方向
     */
    public static int getWestDirection() {
        return PointDirectionEnum.WEST.getDirection();
    }

    /**
     * 获取东方向
     */
    public static int getEastDirection() {
        return PointDirectionEnum.EAST.getDirection();
    }

    /**
     * 判断方向是否可以通过
     *
     * @param supportDirection 支持的方向
     * @param direction        要判断的方向
     * @return 如果方向支持，返回 true，否则返回 false
     */
    public static boolean canPass(int supportDirection, int direction) {
        return (supportDirection & direction) != 0;
    }

    /**
     * 计算曼哈顿距离
     *
     * @param srcX 起点x坐标
     * @param srcY 起点y坐标
     * @param desX 终点x坐标
     * @param desY 终点y坐标
     * @return 曼哈顿距离
     */
    public static int getManhattanDistance(int srcX, int srcY, int desX, int desY) {
        return Math.abs(srcX - desX) + Math.abs(srcY - desY);
    }

    /**
     * 计算曼哈顿距离
     *
     * @param srcPoint 起点
     * @param desPoint 终点
     * @return 曼哈顿距离
     */
    public static int getManhattanDistance(Coordinate srcPoint, Coordinate desPoint) {
        return getManhattanDistance(srcPoint.getX(), srcPoint.getY(), desPoint.getX(), desPoint.getY());
    }

    /**
     * 获取从起点到终点的方向
     *
     * @param srcPoint 起点
     * @param dstPoint 终点
     * @return 方向值
     */
    public static int getDirBetweenTwoPoints(Coordinate srcPoint, Coordinate dstPoint) {
        if (srcPoint.getX() == dstPoint.getX()) {
            return dstPoint.getY() > srcPoint.getY() ? PointDirectionEnum.NORTH.getDirection() : PointDirectionEnum.SOUTH.getDirection();
        } else {
            return dstPoint.getX() > srcPoint.getX() ? PointDirectionEnum.EAST.getDirection() : PointDirectionEnum.WEST.getDirection();
        }
    }

}
