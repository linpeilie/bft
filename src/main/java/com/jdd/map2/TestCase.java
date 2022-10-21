package com.jdd.map2;


import java.util.*;

public class TestCase {
    int up = 1;
    int down = 2;
    int left = 3;
    int right = 4;


    String x = "141";
    String y = "72";

    String xMax = "199";
    String yMax = "499";

    final static int xMaxIndex = 199;
    final static int yMaxIndex = 499;

    /**
     * 140 70(0,0) 140 71(0,1) 140 72(0,2)
     * 141 70(1,0) 141 71(1,1) 141 72(1,2)
     * 142 70(2,1) 142 71(2,1) 142 72(2,2)
     */
    public static void func() {
        String[][] init = {
                {"140,70", "140,71", "140,72"},
                {"141,70", "141,71", "141,72"},
                {"142,70", "142,71", "142,72"}
        };

        Set<String> blockSet = new HashSet<>();
        Set<String> hasClean = new HashSet<>();
        Stack<Map<String,String>> goBack = new Stack<>();
        blockSet.add("139,71");
        blockSet.add("151,71");
        blockSet.add("199,499");
        blockSet.add("99,299");

        for (int i = 0; i < 199 ; i++) {
            blockSet.add((i + 1) +"," + 130);
        }

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

                if(checkPoint(newPoint,4,blockSet)){
                    //change direct

                    //new move
                    newPoint = move(init,3);
                    if(!checkPoint(newPoint,3,blockSet)){
                        //change direct
                        newPoint = move(init,2);
                        if(!checkPoint(newPoint,2,blockSet)){
                            newPoint = move(init,1);
                            if(!checkPoint(newPoint,1,blockSet)){
                                break;
                            }else{
                                String[][] oldPoint = init;
                                init = newPoint;
                                logFile(1 + ":" + init[1][1] + ":" + init[1][0]+ ";" + init[1][1] + ";"+init[1][2]);
                                //log 坐标
                                hasClean.add(newPoint[1][0]);
                                hasClean.add(newPoint[1][1]);
                                hasClean.add(newPoint[1][2]);
                                //坐标放入stack
                                String[][] finalNewPoint = newPoint;
                                goBack.push(new HashMap<String,String>(){{
                                    put("dir","1");
                                    put("back", finalNewPoint[1][2]);
                                }});
                            }
                        }else{
                            String[][] oldPoint = init;
                            init = newPoint;
                            logFile(2 + ":" + init[1][1] +":" + init[1][0] + ";" + init[1][1]+";"+init[1][2]);
                            //log 坐标
                            hasClean.add(newPoint[1][0]);
                            hasClean.add(newPoint[1][1]);
                            hasClean.add(newPoint[1][2]);
                            //坐标放入stack
                            String[][] finalNewPoint = newPoint;
                            goBack.push(new HashMap<String,String>(){{
                                put("dir","2");
                                put("back", finalNewPoint[1][2]);
                            }});
                        }
                    }else{
                        String[][] oldPoint = init;
                        init = newPoint;
                        //log 坐标
                        logFile(3 + ":" + init[1][1] + ":" + init[0][1]+";"+init[1][1]+";"+init[2][1]);
                        hasClean.add(newPoint[0][1]);
                        hasClean.add(newPoint[1][1]);
                        hasClean.add(newPoint[2][1]);
                        //坐标放入stack
                        String[][] finalNewPoint = newPoint;
                        goBack.push(new HashMap<String,String>(){{
                            put("dir","3");
                            put("back", finalNewPoint[1][2]);
                        }});
                    }
                }else{
                    String[][] oldPoint = init;
                    init = newPoint;
                    logFile(4 + ":" + init[1][1] + ":" +init[0][1]+";"+init[1][1]+";"+init[2][1]);
                    //log 坐标
                    hasClean.add(newPoint[0][1]);
                    hasClean.add(newPoint[1][1]);
                    hasClean.add(newPoint[2][1]);
                    //坐标放入stack
                    String[][] finalNewPoint = newPoint;
                    goBack.push(new HashMap<String,String>(){{
                        put("dir","4");
                        put("back", finalNewPoint[1][2]);··
                    }});
                }
                loop--;
            }
        }


    }

    //false 表示转向
    private static boolean checkPoint(String[][] newPoint,int dir,Set<String> blockSet){
        if(newPoint == null){
            return false;
        }
        switch (dir){
            case 4:
                if(blockSet.contains(newPoint[0][2])
                        ||
                        blockSet.contains(newPoint[1][2])
                        ||
                        blockSet.contains(newPoint[2][2])
                        ||
                        newPoint[0][2].split(",")[1].equals("499")
                ){
                    return false;
                }
            case 3:
                if(blockSet.contains(newPoint[0][0])
                        ||
                        blockSet.contains(newPoint[1][0])
                        ||
                        blockSet.contains(newPoint[2][0])
                        ||
                        newPoint[0][0].split(",")[1].equals("0")
                ){
                    return false;
                }
            case 2:
                if(blockSet.contains(newPoint[2][0])
                        ||
                        blockSet.contains(newPoint[2][1])
                        ||
                        blockSet.contains(newPoint[2][2])
                        ||
                        newPoint[2][0].split(",")[1].equals("199")
                ){
                    return false;
                }
            case 1:
                if(blockSet.contains(newPoint[0][0])
                        ||
                        blockSet.contains(newPoint[0][1])
                        ||
                        blockSet.contains(newPoint[0][2])
                        ||
                        newPoint[0][0].split(",")[1].equals("0")
                ){
                    return false;
                }
        }
        return true;
    }


    private static void  logFile(String file){
        System.out.println(file);
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
            return needMovedata;
        }
        String[][] newLocation = new String[3][3];

        int firstIndex = 0;
        for (String[] needMovedatum : needMovedata) {
            String num = getNewLocation(needMovedatum[0], index, increment);
            if (num == null) {
                return null;
            }
            newLocation[firstIndex][0] = num;

            num = getNewLocation(needMovedatum[1], index, increment);
            if (num == null) {
                return null;
            }
            newLocation[firstIndex][1] = num;

            num = getNewLocation(needMovedatum[2], index, increment);
            if (num == null) {
                return null;
            }
            newLocation[firstIndex][2] = num;
            firstIndex++;
        }

        return newLocation;
    }

    private static String getNewLocation(String oldLocation, int index, int increment) {
        String[] split = oldLocation.split(",");
        int newLocation = Integer.parseInt(split[index]) + increment;
        if (index == 0) {
            if (newLocation < 0 || newLocation > xMaxIndex) {
                return null;
            }
        } else if (index == 1) {
            if (newLocation < 0 || newLocation > yMaxIndex) {
                return null;
            }
        }

        split[index] = String.valueOf(newLocation);
        return String.format("%s,%s", split[0], split[1]);
    }





    public static void main(String[] args) {
        func();
//        for (int i = 0; i < 199 ; i++) {
//            System.out.println((i + 1) +"," + 130);
//        }
    }


}
