package com.tencent.tac.tacpayment.order;

import android.util.Log;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.tencent.qcloud.core.util.IOUtils.closeQuietly;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */
public abstract class NetworkConnection {


    protected abstract String url();


    public void connect(PaymentCallback callback) {

        new Thread(new Runnable() {

            String content = "";

            @Override
            public void run() {

                InputStream is = null;

                try{

                    // 发起http连接
                    URL u = new URL(url());
                    HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    conn.setConnectTimeout(3000);
                    conn.setReadTimeout(3000);
                    conn.connect();
                    is = conn.getInputStream();
                    content = readInputStream(is);
                    Log.d("payment", "content is " + content);

                    JSONObject jsonObject = new JSONObject(content);
                    String payinfo = jsonObject.optString("pay_info");
                    callback.onResult(payinfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    // throw new RuntimeException(e);
                } finally {
                    closeQuietly(is);
                }
            }

        }).start();
    }

    private String readInputStream(InputStream is) throws IOException {

        byte[] bytes = new byte[256];
        StringBuilder result = new StringBuilder();
        int len = 0;
        while ((len = is.read(bytes)) > 0) {
            result.append(new String(bytes, 0, len));
        }

        return result.toString();
    }

}
