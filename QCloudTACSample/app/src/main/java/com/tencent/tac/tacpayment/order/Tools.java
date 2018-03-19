package com.tencent.tac.tacpayment.order;

import android.util.Log;

import com.tencent.midas.comm.APLog;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class Tools {

    public static String flatParams(Map<String, String> params) {

        Object[] keys = params.keySet().toArray();

        Arrays.sort(keys);

        StringBuilder buffer = new StringBuilder(128);

        // buffer.append(method.toUpperCase()).append("&").append(encodeUrl(url_path)).append("&");

        StringBuilder buffer2= new StringBuilder();

        for(int i=0; i<keys.length; i++)
        {
            buffer2.append(keys[i]).append("=").append(params.get(keys[i]));

            if (i!=keys.length-1)
            {
                buffer2.append("&");
            }
        }

        //buffer.append(encodeUrl(buffer2.toString()));
        buffer.append(buffer2.toString());

        return buffer.toString();

    }


    public static String getGMTime() {

        TimeZone timeZone = TimeZone.getTimeZone("GMT+8:00");
        // dateTime是格林威治时间
        long chineseMills = (System.currentTimeMillis() - timeZone.getRawOffset()) / 1000;

        APLog.d("chineseMills:", "" + chineseMills);

        return chineseMills+"";
    }


    public static String RASEncode(String privateKey, String contentToEncode) {
        privateKey = privateKey.replaceAll("-----BEGIN ENCRYPTED PRIVATE KEY-----", "").
                replaceAll("-----END ENCRYPTED PRIVATE KEY-----", "").
                replaceAll("\n", "");

        Log.d("payment", "private key is " + privateKey);

        // 私钥需要进行Base64解密
        //byte[] b1 = Base64.getDecoder().decode(privateKey);
        byte[] b1 = Base64Coder.decode(privateKey);

        Log.d("payment", "private key decode key is " + new String(b1));

        try {
            // 将字节数组转换成PrivateKey对象
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey pk = kf.generatePrivate(spec);


            java.security.Signature privateSignature = java.security.Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(pk);
            // 输入需要签名的内容
            privateSignature.update(contentToEncode.getBytes("UTF-8"));
            // 拿到签名后的字节数组
            byte[] s = privateSignature.sign();

            // 将签名后拿到的字节数组做一个Base64编码，以便以字符串的形式保存
            //return Base64.getEncoder().encodeToString(s);
            return new String(Base64Coder.encode(s));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return "";


    }
}
