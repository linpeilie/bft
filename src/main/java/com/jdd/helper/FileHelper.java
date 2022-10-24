package com.jdd.helper;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.jdd.domain.Coordinate;
import com.jdd.domain.RoutePoint;
import com.jdd.enums.PointDirectionEnum;
import com.jdd.enums.TaskTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileHelper {

    public static List<Coordinate> parseBarrier(String fileName) {
        File barrierFile = new File(fileName);
        if (!barrierFile.exists()) {
            throw new RuntimeException("障碍物点不存在");
        }
        List<Coordinate> barriers = new ArrayList<>();
        List<String> lines = FileUtil.readLines(barrierFile, StandardCharsets.UTF_8);
        for (String line : lines) {
            int[] arr = StrUtil.splitToInt(line, ",");
            if (ArrayUtil.isNotEmpty(arr) && arr.length == 2) {
                barriers.add(Coordinate.valueOf(arr[0], arr[1]));
            }
        }
        return barriers;
    }

    public static void delOldResultFile() {
        File resultFile = new File("result.txt");
        if (FileUtil.exist(resultFile)) {
            FileUtil.del(resultFile);
        }
    }

    public static void appendResult(List<RoutePoint> routePoints, int type) {
        File resultFile = new File("result.txt");
        if (TaskTypeEnum.BACK.getType() != type) {
            FileUtil.appendString("\n" + type, resultFile, StandardCharsets.UTF_8);
        }
        if (CollectionUtil.isNotEmpty(routePoints)) {
            List<String> lines = routePoints.stream().map(FileHelper::transLine).collect(Collectors.toList());
            FileUtil.appendLines(lines, resultFile, StandardCharsets.UTF_8);
        }
        if (TaskTypeEnum.BACK.getType() == type) {
            FileUtil.appendString("\n" + type, resultFile, StandardCharsets.UTF_8);
        }
    }

    public static void appendLine(String line) {
        File resultFile = new File("result.txt");
        FileUtil.appendString("\n" + line, resultFile, StandardCharsets.UTF_8);
    }

    private static String transLine(RoutePoint routePoint) {
        StringBuilder result = new StringBuilder()
                .append(PointDirectionEnum.getEnumByDirection(routePoint.getDirection()).getOutputDirection())
                .append(":")
                .append(routePoint.getX()).append(",")
                .append(routePoint.getY());
        if (ArrayUtil.isNotEmpty(routePoint.getCleanPoints())) {
            int size = routePoint.getCleanPoints().length;
            result.append(":");
            for (int i = 0; i < size; i++) {
                result.append(routePoint.getCleanPoints()[i].getX())
                        .append(",")
                        .append(routePoint.getCleanPoints()[i].getY());
                if (i != size - 1) {
                    result.append(";");
                }
            }
        }
        return result.toString();
    }

}
