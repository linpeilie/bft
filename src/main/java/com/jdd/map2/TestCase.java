package com.jdd.map2;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class TestCase {

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

                //计算移动后的坐标
                
















                loop--;
            }
        }


    }


    private String[][] move(String[][] needMove){



        return needMove;
    }




    public static void main(String[] args) {
        func();
    }


}
