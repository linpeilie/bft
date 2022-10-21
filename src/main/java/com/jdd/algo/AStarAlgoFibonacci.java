package com.jdd.algo;

import com.google.common.collect.Lists;
import com.jdd.domain.*;
import com.jdd.enums.PointDirectionEnum;
import com.jdd.helper.PointHelper;

import java.util.*;

public class AStarAlgoFibonacci {

    private static final int TURN_COST_VALUE = 5;

    /**
     * 获取最短路径
     *
     * @param src           起点
     * @param des           终点
     * @param mapInfoEntity 地图信息
     */
    public static List<Coordinate> getShortestPath(Coordinate src, Coordinate des, AlgorithmMapInfo mapInfoEntity) {
        FibonacciHeap<AlgorithmNode> openList = new FibonacciHeap<>();
        Set<Coordinate> closeList = new HashSet<>();
        // 路径
        List<AlgorithmNode> path = new ArrayList<>();

        Map<Coordinate, AlgorithmNode> vertexToNodeMap = new HashMap<>();
        Map<Coordinate, FibonacciHeapNode<AlgorithmNode>> vertexToHeapNodeMap = new HashMap<>();

        int src2DesDis = PointHelper.getManhattanDistance(src, des);

        AlgorithmNode node = new AlgorithmNode(src);
        node.setT(0);
        node.setG(0);
        // 将距离的权重调高，防止提前转弯
        node.setH(PointHelper.getManhattanDistance(src, des) * 3);

        vertexToNodeMap.put(node.getPoint(), null);

        int f = node.getG() + node.getH();
        FibonacciHeapNode<AlgorithmNode> fibNode = new FibonacciHeapNode<>(node);

        openList.insert(fibNode, f);

        vertexToHeapNodeMap.put(src, fibNode);

        while (!openList.isEmpty()) {
            fibNode = openList.removeMin();
            node = fibNode.getData();
            path.add(node);
            Coordinate nodePoint = node.getPoint();
            // 找到目的点
            if (nodePoint.equals(des)) {
                List<Coordinate> pathReverse = new ArrayList<>();
                AlgorithmNode n = path.get(path.size() - 1);
                while (n != null && n.getParentNode() != null && n.getParentNode().getPoint() != null) {
                    pathReverse.add(n.getPoint());
                    n = vertexToNodeMap.get(n.getParentNode().getPoint());
                }
                pathReverse.add(src);
                return Lists.reverse(pathReverse);
            }
            // 如果没有找到目的点，则寻找周围四个点
            for (int i = 3; i >= 0; i--) {
                // 判断该方向是否可行
                int newDir = 1 << i;
                if (!PointHelper.canPass(mapInfoEntity.getDir(nodePoint), newDir)) {
                    continue;
                }
                PointDirectionEnum pointDirection = PointDirectionEnum.getEnumByDirection(newDir);
                int dx = 0;
                int dy = 0;
                switch (Objects.requireNonNull(pointDirection)) {
                    case NORTH:
                        dy += 1;
                        break;
                    case SOUTH:
                        dy -= 1;
                        break;
                    case EAST:
                        dx += 1;
                        break;
                    case WEST:
                        dx -= 1;
                        break;
                    default:
                        continue;
                }
                // 方向可行后，获取新的点
                Coordinate newPoint = Coordinate.valueOf(nodePoint.getX() + dx, nodePoint.getY() + dy);
                // 障碍物不可行
                if (mapInfoEntity.isObstacle(newPoint.getX(), newPoint.getY())) {
                    continue;
                }
                // 计算该点的代价值
                AlgorithmNode newNode = new AlgorithmNode(newPoint);
                newNode.setDir(newDir);

                int weight = mapInfoEntity.getWeight(newPoint.getX(), newPoint.getY());
//                int newT = newNode.getDir() == node.getDir() ? node.getT() : node.getT() + TURN_COST_VALUE;
                // 起点的第二个点不增加权重
//                if (nodePoint.equals(src)) {
//                    newT = 0;
//                }
                int newT = 0;
                int newG = node.getG() + weight + newT * 3;
                int newH = PointHelper.getManhattanDistance(newPoint, des);

                newNode.setT(newT);
                newNode.setG(newG);
                newNode.setH(newH);
                newNode.setParentNode(node);

                if (!vertexToNodeMap.containsKey(newPoint)) {
                    vertexToNodeMap.put(newPoint, newNode);
                }

                // F
                int newF = newG + newH;

                FibonacciHeapNode<AlgorithmNode> newFib = new FibonacciHeapNode<>(newNode);

                if (vertexToHeapNodeMap.containsKey(newPoint)) {
                    // 获取之前处理的该结点
                    FibonacciHeapNode<AlgorithmNode> oldFib = vertexToHeapNodeMap.get(newPoint);
                    AlgorithmNode oldNode = oldFib.getData();
                    if (src2DesDis != 1) {
                        // 如果新计算的g(x)比之前计算的g(x)大，则取消操作
                        if (newG > oldNode.getG() || newF >= oldFib.getKey()) {
                            continue;
                        }
                        // 如果新计算的g(x)比之前计算的g(x)小，且该点在closeList中
                        if (!closeList.contains(oldNode.getPoint())) {
                            oldNode.setG(newNode.getG());
                            oldNode.setH(newNode.getH());
                            oldNode.setT(newNode.getT());
                            oldNode.setDir(newNode.getDir());
                            oldNode.setParentNode(newNode.getParentNode());
                            vertexToNodeMap.put(oldNode.getPoint(), oldNode);
                            openList.decreaseKey(oldFib, newF);
                        }
                    }
                } else {
                    openList.insert(newFib, newF);
                    vertexToHeapNodeMap.put(newPoint, newFib);
                }

            }
            closeList.add(node.getPoint());
        }

        return Collections.emptyList();
    }

}
