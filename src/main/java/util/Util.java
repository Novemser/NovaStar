package util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Project: HadoopDFS
 * Package: util
 * Author:  Novemser
 * 2017/1/4
 */
public class Util {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void printlnTime(String str) {
        System.out.println(simpleDateFormat.format(new Date()) + "|" + str);
    }

    public static void printerrTime(String str) {
        System.err.println(simpleDateFormat.format(new Date()) + "|" + str);
    }
}
