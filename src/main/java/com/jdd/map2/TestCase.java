package com.jdd.map2;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class TestCase {
    int up = 1;
    int down = 2;
    int left = 3;
    int right = 4;


    int x = 141;
    int y = 72;

    int xMax = 200;
    int yMax = 500;

    /**
     * 140 70 140 71 140 72
     * 141 70 141 71 141 72
     * 142 70 142 71 142 72
     */
    public static void func() {
        String[][] init = {
                {"140,70", "140,71", "140,72"},
                {"141,70", "141,71", "141,72"},
                {"142,70", "142,71", "142,72"}
        };

        Set<String> blockSet = new HashSet<>();
        Set<String> hasClean = new HashSet<>();
        Stack<String> goBack = new Stack<>();
        blockSet.add("139,71");
        blockSet.add("151,71");
        blockSet.add("199,499");
        blockSet.add("99,299");
        int loop = 20000;
        boolean flag = true;
        while (flag) {
            //计算还能跑多少，已经跑了多少
            while (loop != 0) {
                //往右 检查 [0][2] [1][2] [2][2]
                //往左 检查 [0][0] [1][0] [2][0]
                //往上 检查 [0][0] [0][1] [0][2]
                //往下 检查 [2][0] [2][1] [2][2]

                //计算移动后的坐标 1上2下3左4右
                String[][] newPoint = move(init, 4);

                if(blockSet.contains(newPoint[0][2])
                    ||
                   blockSet.contains(newPoint[1][2])
                    ||
                   blockSet.contains(newPoint[2][2])
                ){
                    //change direct









                }else{
                    init = newPoint;
                    
                    //log 坐标


                }
                loop--;
            }
        }


    }


    private static String[][] move(String[][] needMovedata, int direct) {
        int index;
        int increment;
        if (direct == 1) {
            // 向上x-1
            index = 0;
            increment = -1;
        } else if (direct == 2) {
            // 向下x+1
            index = 0;
            increment = 1;
        } else if (direct == 3) {
            // 向左y-1
            index = 1;
            increment = -1;
        } else if (direct == 4) {
            // 向右y+1
            index = 1;
            increment = 1;
        } else {
            return null;
        }

        for (String[] needMovedatum : needMovedata) {
            needMovedatum[0] = getNewLocation(needMovedatum[0], index, increment);
            needMovedatum[1] = getNewLocation(needMovedatum[1], index, increment);
            needMovedatum[2] = getNewLocation(needMovedatum[2], index, increment);
        }

        return needMovedata;
    }

    private static String getNewLocation(String oldLocation, int index, int increment) {
        String[] split = oldLocation.split(",");
        split[index] = String.valueOf(Integer.parseInt(split[index]) + increment);
        return String.format("%s,%s", split[0], split[1]);
    }




    public static void main(String[] args) {
        func();
    }


}
