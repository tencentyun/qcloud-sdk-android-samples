package com.tencent.qcloud.csp.sample;

import android.text.TextUtils;

import com.tencent.qcloud.core.auth.QCloudCredentials;
import com.tencent.qcloud.core.auth.QCloudSigner;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.common.QCloudServiceException;
import com.tencent.qcloud.core.http.QCloudHttpClient;
import com.tencent.qcloud.core.http.QCloudHttpRequest;
import com.tencent.qcloud.core.http.RequestBodySerializer;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * Created by rickenwang on 2018/9/20.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class RemoteCOSSigner implements QCloudSigner {

    private URL requestSignUrl;

    public RemoteCOSSigner(URL url) {

        requestSignUrl = url;
    }

    /**
     * @param request     即为发送到 CSP 服务端的请求，您需要根据这个 HTTP 请求的参数来计算签名，并给其添加 Authorization header
     * @param credentials 空字段，请不要使用
     * @throws QCloudClientException 您可以在处理过程中抛出异常
     */
    @Override
    public void sign(QCloudHttpRequest request, QCloudCredentials credentials) throws QCloudClientException {

        /**
         * 获取计算签名所需字段
         */
        URL url = request.url();
        String method = request.method();
        String host = url.getHost();
        String schema = url.getProtocol();
        String path = url.getPath();
        Map<String, List<String>> headers = request.headers();

        String signFieldJson = null;
        try {
            signFieldJson = signField2Json(method, schema, host, path, headers);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new QCloudClientException("sign field transfer to json failed");
        }


        /**
         * 向您自己的服务端请求签名
         */
        QCloudHttpRequest<String> httpRequest = new QCloudHttpRequest.Builder<String>()
                .method("PUT")
                .url(requestSignUrl)
                .body(RequestBodySerializer.string(null, signFieldJson))
                .build();

        String response = null;
        try {
            response = QCloudHttpClient.getDefault().resolveRequest(httpRequest).executeNow().content();
        } catch (QCloudServiceException e) {
            e.printStackTrace();
            throw new QCloudClientException(e);
        }

        String sign = null;
        try {
            sign = getSignFromResponse(response);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new QCloudClientException("parse response failed");
        }

        /**
         * 给请求设置 Authorization Header
         */
        if (TextUtils.isEmpty(sign)) {
            throw new QCloudClientException("get sign from server failed!!!");
        }
        request.addHeader("Authorization", sign);
    }

    /**
     * 将签名需要的字段转化为 json 字符串
     *
     * @return
     */
    private String signField2Json(String method, String schema, String host, String path, Map<String, List<String>> headers) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("method", method);
        jsonObject.put("schema", schema);
        jsonObject.put("host", host);
        jsonObject.put("path", path);
        jsonObject.put("headers", headers);

        return jsonObject.toString();
    }

    private String getSignFromResponse(String response) throws JSONException {

        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.optString("sign");
    }
}
