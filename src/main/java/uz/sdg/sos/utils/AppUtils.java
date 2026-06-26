package uz.sdg.sos.utils;

import java.util.Random;

public class AppUtils {

    public static String random() {
        StringBuilder num = new StringBuilder();
        new Random().ints(6, 0, 10).forEach(num::append);
        return num.toString();
    }

    public static String getLotNumber(Long id) {
        return "0".repeat(Math.max(0, 7 - Long.toString(id).length())) + id;
    }
}
