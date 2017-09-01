package com.tencent.qcloud.cosxml.sample.common;

import android.util.Log;

import com.tencent.cos.xml.utils.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bradyxiao on 2017/7/18.
 * author bradyxiao
 */
public class MD5Utils {
    public static String getMD5FromBytes(byte[] data, int offset, int len){
        if(data == null || len <= 0 || offset < 0){
            Log.e("MD5Utils","data == null | len <= 0 |" +
                    " offset < 0 |offset >= len");
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(data,offset,len);
            return com.tencent.cos.xml.utils.StringUtils.toHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5Utils","no such algorithm");
            e.printStackTrace();
        }catch (OutOfMemoryError e){
            Log.e("MD5Utils","OutOfMemoryError");
            e.printStackTrace();
        }
        return null;
    }

    public static String getMD5FromString(String content){
        if(content == null){
            return null;
        }
        try {
            byte[] data = content.getBytes("utf-8");
            return getMD5FromBytes(data,0,data.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMD5FromPath(String filePath){
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[64 * 1024];
            int len = 0;
            while((len = fileInputStream.read(buffer,0,buffer.length)) != -1){
                messageDigest.update(buffer,0,len);
            }
            return StringUtils.toHexString(messageDigest.digest());
        } catch (FileNotFoundException e) {
            Log.e("MD5Utils","FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("MD5Utils","IOException");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5Utils","NoSuchAlgorithmException");
            e.printStackTrace();
        }finally {
            if(fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
