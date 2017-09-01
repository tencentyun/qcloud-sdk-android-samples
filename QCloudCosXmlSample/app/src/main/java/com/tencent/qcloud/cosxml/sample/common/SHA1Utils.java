package com.tencent.qcloud.cosxml.sample.common;

import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by bradyxiao on 2017/2/28.
 * author bradyxiao
 * 借助java sha1算法
 * 简便
 */
public class SHA1Utils {
    public static String getSHA1(byte[] data, int offset,int len){
        if(data == null || len <= 0 || offset < 0){
            Log.e("SHA1Utils","data == null | len <= 0 |" +
                    " offset < 0 |offset >= len");
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(data,offset,len);
            return StringUtils.toHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e("SHA1Utils","no such algorithm");
            e.printStackTrace();
        }catch (OutOfMemoryError e){
            Log.e("SHA1Utils","OutOfMemoryError");
            e.printStackTrace();
        }
        return null;
    }

    public static String getSHA1FromString(String content){
        try {
            byte[] data = content.getBytes("utf-8");
            return getSHA1(data,0,data.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSHA1FromPath(String filePath){
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[64 * 1024];
            int len = 0;
            while((len = fileInputStream.read(buffer,0,buffer.length)) != -1){
                messageDigest.update(buffer,0,len);
            }
            return StringUtils.toHexString(messageDigest.digest());
        } catch (FileNotFoundException e) {
            Log.e("SHA1Utils","FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("SHA1Utils","IOException");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            Log.e("SHA1Utils","NoSuchAlgorithmException");
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
