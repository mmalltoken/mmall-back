package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

/**
 * 描述：时间转换工具类
 * 作者：NearJC
 * 时间：2019.4.26
 */
public class DateTimeUtil {

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 字符串对象转成日期对象
     *
     * @param dateStr
     * @param formatter 时间类型
     * @return
     */
    public static Date strToDate(String dateStr, String formatter) {
        DateTimeFormatter format = DateTimeFormat.forPattern(formatter);
        DateTime dateTime = format.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    public static Date strToDate(String dateStr) {
        DateTimeFormatter format = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = format.parseDateTime(dateStr);
        return dateTime.toDate();
    }

    /**
     * 时间对象转成字符串对象
     *
     * @param date
     * @param formatter 时间格式
     * @return
     */
    public static String dateToStr(Date date, String formatter) {
        if (date == null)
            return "";

        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatter);
    }

    public static String dateToStr(Date date) {
        if (date == null)
            return "";
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }
}
