package com.tencent.cosxml.assistant.common;

import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
/**
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class CommonHelper {


    public static String size(long size) {

        float realSize = size;
        int index = 0;
        String [] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
//        while ((realSize = (float) (1.0 * size / 1024)) > 1000 && index < 5) {
//            index++;
//            size /= 1024;
//        }

        while (realSize > 1000 && index < 5) {

            index++;
            realSize /= 1024;
        }


        String capacityText =  new DecimalFormat("###,###,###.##").format(realSize);
        return String.format(Locale.ENGLISH, "%s%s", capacityText, units[index]);
    }

    private static String utfTransfer(String utc, String fromTimeZone, String toTimeZone) throws ParseException {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone(fromTimeZone));

        Date date = format.parse(utc);
        format.setTimeZone(TimeZone.getTimeZone(toTimeZone));
        return format.format(date);
    }

    public static String utc(long time) {

        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String time(String utc) {


        if (TextUtils.isEmpty(utc) || utc.length() < 21) {
            return "";
        }

        String subUtc = utc.substring(0, 21);
        Log.d("TAG", subUtc);
        if (!utc.endsWith("08:00")) { //

            try {
                subUtc = utfTransfer(utc, "GMT+0", "GMT+8");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Log.d("TAG", subUtc); // -2005

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(subUtc);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format2.format(date);

    }

    private static String getLocalTimeFromUTC(String UTCTime, DateFormat format){
        Date UTCDate = null ;
        String localTimeStr = null ;
        try {
            UTCDate = format.parse(UTCTime);
            format.setTimeZone(TimeZone.getTimeZone("GMT-8")) ;
            localTimeStr = format.format(UTCDate) ;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return localTimeStr ;
    }
}
