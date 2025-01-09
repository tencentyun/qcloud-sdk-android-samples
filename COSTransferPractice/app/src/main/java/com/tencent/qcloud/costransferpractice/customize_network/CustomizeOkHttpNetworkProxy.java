package com.tencent.qcloud.costransferpractice.customize_network;

import com.tencent.qcloud.core.http.NetworkProxy;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 此处实现一个网络请求，例如okhttp的Call
 * <p>
 * Created by jordanqin on 2024/12/30 19:37.
 * Copyright 2010-2020 Tencent Cloud. All Rights Reserved.
 */
public class CustomizeOkHttpNetworkProxy<T> extends NetworkProxy<T> {
    private static final String TAG = "CustomizeOkHttpNetworkProxy";
    private Call httpCall;
    private OkHttpClient okHttpClient;

    public CustomizeOkHttpNetworkProxy(OkHttpClient okHttpClient){
        this.okHttpClient = okHttpClient;
    }

    @Override
    public void cancel(){
        if (httpCall != null) {
            httpCall.cancel();
        }
    }

    @Override
    public Response callHttpRequest(Request okHttpRequest) throws IOException {
        httpCall = okHttpClient.newCall(okHttpRequest);
        return httpCall.execute();
    }
}
