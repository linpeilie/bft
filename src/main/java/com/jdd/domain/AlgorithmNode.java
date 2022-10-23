package com.jdd.domain;

import java.util.Objects;

public class AlgorithmNode implements Comparable<AlgorithmNode> {

    private Coordinate point;
    private int g;
    private int h;
    private int f;
    private int t;
    private boolean isInOpenList;
    private boolean isInCloseList;
    private AlgorithmNode parentNode;
    private int dir;

    public AlgorithmNode(Coordinate point) {
        this.point = point;
    }

    @Override
    public int compareTo(AlgorithmNode o) {
        return (int) (this.f - o.f);
    }

    public Coordinate getPoint() {
        return point;
    }

    public void setPoint(Coordinate point) {
        this.point = point;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public boolean isInOpenList() {
        return isInOpenList;
    }

    public void setInOpenList(boolean inOpenList) {
        isInOpenList = inOpenList;
    }

    public boolean isInCloseList() {
        return isInCloseList;
    }

    public void setInCloseList(boolean inCloseList) {
        isInCloseList = inCloseList;
    }

    public AlgorithmNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(AlgorithmNode parentNode) {
        this.parentNode = parentNode;
    }

    public int getDir() {
        return dir;
    }

    public void setDir(int dir) {
        this.dir = dir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlgorithmNode that = (AlgorithmNode) o;
        return getG() == that.getG() && getH() == that.getH() && getF() == that.getF() && getT() == that.getT() && isInOpenList() == that.isInOpenList() && isInCloseList() == that.isInCloseList() && getDir() == that.getDir() && Objects.equals(getPoint(), that.getPoint()) && Objects.equals(getParentNode(), that.getParentNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPoint(), getG(), getH(), getF(), getT(), isInOpenList(), isInCloseList(), getParentNode(), getDir());
    }

    @Override
    public String toString() {
        return "AlgorithmNode{" +
                "point=" + point +
                ", g=" + g +
                ", h=" + h +
                ", f=" + f +
                ", t=" + t +
                ", isInOpenList=" + isInOpenList +
                ", isInCloseList=" + isInCloseList +
                ", parentNode=" + parentNode +
                ", dir=" + dir +
                '}';
    }
}
