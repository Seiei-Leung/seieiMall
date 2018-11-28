package top.seiei.mall.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具
 * 主要功能有：
 * Date 转化为 String
 * String 转化为 Date
 */
public class DateTimeUtil {

    final static String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String dateToStr(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
        return simpleDateFormat.format(date);
    }

    public static String dateToStr(Date date, String formatStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);
        return simpleDateFormat.format(date);
    }

    public static Date strToDate(String dateString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FORMAT);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  date;
    }

    public static Date strToDate(String dateString, String formatStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  date;
    }

}
