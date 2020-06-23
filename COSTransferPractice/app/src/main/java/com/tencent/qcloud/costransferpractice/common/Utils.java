package com.tencent.qcloud.costransferpractice.common;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by jordanqin on 2020/6/18.
 * 工具类
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class Utils {
    /**
     * 将byte转换为更加友好的单位
     * @param sizeInB byte
     * @return 更加友好的单位（KB、GB等）
     */
    public static String readableStorageSize(long sizeInB)  {
        float floatSize = sizeInB;
        int index = 0;
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};

        while (floatSize > 1000 && index < 5) {
            index++;
            floatSize /= 1024;
        }

        String capacityText =  new DecimalFormat("###,###,###.##").format(floatSize);
        return String.format(Locale.ENGLISH, "%s%s", capacityText, units[index]);
    }

    /**
     * 将ISO8601格式的时间转换为 yyyy-MM-dd HH:mm:ss格式的时间
     * @param utc ISO8601格式的时间
     * @return yyyy-MM-dd HH:mm:ss格式的时间
     * @throws ParseException
     */
    public static String utc2normalWithCOSPattern(String utc) throws ParseException {
        String cosPattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        return utc2normal(utc, cosPattern);
    }

    /**
     * 将 UTC 时间转换为本地时区时间。
     *
     * @param utc
     * @param pattern
     * @return
     * @throws ParseException
     */
    public static String utc2normal(String utc, String pattern) throws ParseException {
        int index = utc.lastIndexOf('.');
        if(index < 0){
            utc = utc.replace('Z', '.').concat("000Z");
        }else {
            int abs = utc.length() - index;
            if(abs < 5){
                String sub = "";
                for (int i = 0; i < 5 - abs; i ++){
                    sub +='0';
                }
                utc = utc.replace("Z", sub + "Z");
            }
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        /** 修改默认时区为 UTC 零时区 */
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ", Locale.getDefault()).format(sdf.parse(utc));
    }
}
