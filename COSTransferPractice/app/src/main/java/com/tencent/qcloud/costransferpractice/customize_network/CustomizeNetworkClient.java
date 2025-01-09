package com.tencent.qcloud.costransferpractice.customize_network;

import android.util.Log;

import com.tencent.qcloud.core.http.HttpLogger;
import com.tencent.qcloud.core.http.NetworkClient;
import com.tencent.qcloud.core.http.NetworkProxy;
import com.tencent.qcloud.core.http.QCloudHttpClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;

import okhttp3.Dns;
import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;

/**
 * 此处实现自定义网络Client，例如OkHttpClient实例
 * <p>
 * Created by jordanqin on 2024/12/30 19:36.
 * Copyright 2010-2020 Tencent Cloud. All Rights Reserved.
 */
public class CustomizeNetworkClient extends NetworkClient {
    private static final String TAG = "CustomizeNetworkClient";

    private HttpURLConnectionManager httpURLConnectionManager;
    @Override
    public void init(QCloudHttpClient.Builder b, HostnameVerifier hostnameVerifier, Dns dns, HttpLogger httpLogger) {
        super.init(b, hostnameVerifier, dns, httpLogger);
        httpURLConnectionManager = new HttpURLConnectionManager();
    }

    @Override
    public NetworkProxy getNetworkProxy() {
        return new CustomizeNetworkProxy(httpURLConnectionManager);
    }

    public static class HttpURLConnectionManager {
        private static final int CONNECT_TIMEOUT = 5000;
        private static final int READ_TIMEOUT = 5000;

        public HttpURLConnection createConnection(Request request) throws IOException {
            Log.d("NetworkRequest", "Request URL: " + request.url());
            Log.d("NetworkRequest", "Request method: " + request.method());

            URL url = new URL(request.url().toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            connection.setRequestMethod(request.method());

            // Set headers
            for (String name : request.headers().names()) {
                connection.setRequestProperty(name, request.header(name));
                Log.d("NetworkRequest", "Header: " + name + " = " + request.header(name));
            }

            // Set body
            if (request.body() != null) {
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream();
                     BufferedSink bufferedSink = Okio.buffer(Okio.sink(outputStream))) {
                    request.body().writeTo(bufferedSink);
                }
                Log.d("NetworkRequest", "Request body set");
            }

            return connection;
        }
    }
}
