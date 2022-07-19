package com.henry.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    private static final SimpleDateFormat YYYY_MM_DD_FORMAT = new SimpleDateFormat("yyyy-MM");

    public static String format(Date date) {
        return YYYY_MM_DD_FORMAT.format(date);
    }
}
