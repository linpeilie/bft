package com.jdd.domain;

import com.jdd.helper.CoordinateCache;

import java.io.Serializable;
import java.util.Objects;

/**
 * AGV 坐标内容
 */
public class Coordinate implements Serializable {

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private int x;

    private int y;

    public static Coordinate valueOf(int x, int y) {
        return CoordinateCache.get(x, y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinate that = (Coordinate) o;
        return getX() == that.getX() && getY() == that.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
