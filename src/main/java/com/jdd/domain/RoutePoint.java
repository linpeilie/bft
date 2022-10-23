package com.jdd.domain;

import java.util.Arrays;
import java.util.Objects;

public class RoutePoint {

    private int x;
    private int y;
    private int direction;
    private Coordinate[] cleanPoints;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Coordinate[] getCleanPoints() {
        return cleanPoints;
    }

    public void setCleanPoints(Coordinate[] cleanPoints) {
        this.cleanPoints = cleanPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutePoint that = (RoutePoint) o;
        return getX() == that.getX() && getY() == that.getY() && getDirection() == that.getDirection() && Arrays.equals(getCleanPoints(), that.getCleanPoints());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getX(), getY(), getDirection());
        result = 31 * result + Arrays.hashCode(getCleanPoints());
        return result;
    }

    @Override
    public String toString() {
        return "RoutePoint{" +
                "x=" + x +
                ", y=" + y +
                ", direction=" + direction +
                ", cleanPoints=" + Arrays.toString(cleanPoints) +
                '}';
    }
}
