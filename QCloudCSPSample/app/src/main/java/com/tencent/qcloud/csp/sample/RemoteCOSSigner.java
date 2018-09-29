package com.tencent.qcloud.csp.sample;

import android.text.TextUtils;

import com.tencent.qcloud.core.auth.QCloudCredentials;
import com.tencent.qcloud.core.auth.QCloudSigner;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.common.QCloudServiceException;
import com.tencent.qcloud.core.http.QCloudHttpClient;
import com.tencent.qcloud.core.http.QCloudHttpRequest;
import com.tencent.qcloud.core.http.RequestBodySerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.net.URL;
import java.util.HashMap;
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
        Map<String, String> headers = getHeaderMap(request.headers());
        Map<String, String> params = getQueryMap(url.getQuery());


        String signFieldJson = null;
        try {
            signFieldJson = signField2Json(method, schema, host, path, headers, params);
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

    private Map<String, String> getHeaderMap(Map<String, List<String>> multiValuesHeaders) {

        Map<String, String> header = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : multiValuesHeaders.entrySet()) {

            if (entry.getValue().size() > 0) {
                header.put(entry.getKey(), entry.getValue().get(0));
            }
        }

        return header;
    }

    private Map<String, String> getQueryMap(String query)
    {

        Map<String, String> map = new HashMap<>();
        if (TextUtils.isEmpty(query)) {
            return map;
        }

        String[] params = query.split("&");
        for (String param : params)
        {
            String[] paramKeyValue = param.split("=");
            if (paramKeyValue.length >= 2) {
                String name = paramKeyValue[0];
                String value = paramKeyValue[1];
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * 将签名需要的字段转化为 json 字符串
     *
     * @return
     */
    private String signField2Json(String method, String schema, String host, String path,
                                 Map<String, String> headers, Map<String, String> params) throws JSONException {

        JSONObject signJson = new JSONObject();
        signJson.put("method", method);
        signJson.put("schema", schema);
        signJson.put("host", host);
        signJson.put("path", path);

        JSONObject headersJSON = new JSONObject(headers);
        signJson.put("headers", headersJSON);

        JSONObject paramsJSON = new JSONObject(params);
        signJson.put("params", paramsJSON);

        return signJson.toString();
    }

    private String getSignFromResponse(String response) throws JSONException {

        JSONObject jsonObject = new JSONObject(response);
        return jsonObject.optString("sign");
    }
}
