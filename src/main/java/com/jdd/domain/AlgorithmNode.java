package com.jdd.domain;

import lombok.Data;

@Data
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
}
