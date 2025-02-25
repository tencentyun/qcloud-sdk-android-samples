package com.tencent.qcloud.costransferpractice.customize_network;

import android.text.TextUtils;
import android.util.Log;

import com.tencent.qcloud.core.http.NetworkProxy;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;

/**
 * 此处实现一个网络请求
 * <p>
 * Created by jordanqin on 2024/12/30 19:37.
 * Copyright 2010-2020 Tencent Cloud. All Rights Reserved.
 */
public class CustomizeNetworkProxy<T> extends NetworkProxy<T> {
    private static final String TAG = "CustomizeNetworkProxy";
    private HttpURLConnection connection;
    private CustomizeNetworkClient.HttpURLConnectionManager httpURLConnectionManager;

    public CustomizeNetworkProxy(CustomizeNetworkClient.HttpURLConnectionManager httpURLConnectionManager) {
        this.httpURLConnectionManager = httpURLConnectionManager;
    }

    @Override
    protected void cancel() {
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Override
    public Response callHttpRequest(Request okHttpRequest) throws IOException {
        try {
            connection = httpURLConnectionManager.createConnection(okHttpRequest);
            int responseCode = connection.getResponseCode();
            Log.d("NetworkRequest", "Response code: " + responseCode);
            Response response = convertToOkHttpResponse(connection);
            Log.d("NetworkRequest", "Response headers: " + response.headers());
            return response;
        } catch (IOException e) {
            Log.e("NetworkRequest", "Failed to execute HTTP request", e);
            throw e;
        }
    }

    @Override
    protected void disconnect(){
        if (connection != null) {
            connection.disconnect();
        }
    }

    private Response convertToOkHttpResponse(HttpURLConnection connection) throws IOException {
        int code = connection.getResponseCode();
        String message = connection.getResponseMessage();
        String contentType = connection.getContentType();
        int contentLength = connection.getContentLength();

        // Convert headers
        Headers.Builder headersBuilder = new Headers.Builder();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String name = entry.getKey();
            if (TextUtils.isEmpty(name)) continue;
            for (String value : entry.getValue()) {
                headersBuilder.add(name, value);
            }
        }
        Headers headers = headersBuilder.build();

        // Convert body
        BufferedSource source = Okio.buffer(Okio.source(connection.getInputStream()));
        ResponseBody body = new ResponseBody() {
            @Override
            public MediaType contentType() {
                if(TextUtils.isEmpty(contentType)){
                    return null;
                } else {
                    return MediaType.parse(contentType);
                }
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public BufferedSource source() {
                return source;
            }
        };

        // Build OkHttp Response
        Request request = new Request.Builder()
                .url(connection.getURL().toString())
                .build();

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message(message)
                .headers(headers)
                .body(body)
                .build();
    }
}
