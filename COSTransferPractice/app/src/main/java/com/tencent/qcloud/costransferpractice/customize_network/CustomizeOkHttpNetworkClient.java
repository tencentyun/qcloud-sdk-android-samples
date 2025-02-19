package com.tencent.qcloud.costransferpractice.customize_network;

import com.tencent.qcloud.core.http.HttpLogger;
import com.tencent.qcloud.core.http.HttpLoggingInterceptor;
import com.tencent.qcloud.core.http.NetworkClient;
import com.tencent.qcloud.core.http.NetworkProxy;
import com.tencent.qcloud.core.http.QCloudHttpClient;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Dns;
import okhttp3.OkHttpClient;

/**
 * 此处实现自定义网络Client，例如OkHttpClient实例
 * <p>
 * Created by jordanqin on 2024/12/30 19:36.
 * Copyright 2010-2020 Tencent Cloud. All Rights Reserved.
 */
public class CustomizeOkHttpNetworkClient extends NetworkClient {
    private static final String TAG = "CustomizeOkHttpNetworkClient";

    private OkHttpClient okHttpClient;

    @Override
    public void init(QCloudHttpClient.Builder b, HostnameVerifier hostnameVerifier,
                     final Dns dns, HttpLogger httpLogger) {
        super.init(b, hostnameVerifier, dns, httpLogger);
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(httpLogger);
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                .writeTimeout(30 * 1000, TimeUnit.MILLISECONDS)
                        .addInterceptor(logInterceptor);
        okHttpClient = builder.build();
    }

    @Override
    public NetworkProxy getNetworkProxy() {
        CustomizeOkHttpNetworkProxy customizeOkHttpNetworkProxy = new CustomizeOkHttpNetworkProxy(okHttpClient);
        return customizeOkHttpNetworkProxy;
    }
}
