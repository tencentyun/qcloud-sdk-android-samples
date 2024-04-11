package com.tencent.qcloud.costransferpractice;/*
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

import android.util.Log;

import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MySessionCredentialProvider
        extends BasicLifecycleCredentialProvider {

    @Override
    protected QCloudLifecycleCredentials fetchNewCredentials()
            throws QCloudClientException {

        // 首先从您的临时密钥服务器获取包含了密钥信息的响应
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                // 这里请替换为真实的临时秘钥服务的url
                // 如果是启动本项目的StsNodejsDemo本地服务，则IP地址为电脑本机IP，Android模拟器可以使用10.0.2.2这个特殊的ip连接
                .url("http://10.0.2.2:3000/sts")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null || !response.isSuccessful())
            throw new QCloudClientException(new IOException("Unexpected code " + response));

        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
        }

        try {
            String jsonStr = "";
            try {
                jsonStr = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("MySessionCredentialProvider", jsonStr);
            JSONTokener jsonParser = new JSONTokener(jsonStr);
            JSONObject data = (JSONObject) jsonParser.nextValue();
            JSONObject credentials = data.getJSONObject("credentials");
            return new SessionQCloudCredentials(credentials.getString("tmpSecretId"), credentials.getString("tmpSecretKey"),
                    credentials.getString("sessionToken"), data.getLong("startTime"), data.getLong("expiredTime"));
//            return new SessionQCloudCredentials(credentials.getString("tmpSecretId"), credentials.getString("tmpSecretKey"),
//                    credentials.getString("sessionToken"), data.getLong("expiredTime"));
        } catch (JSONException ex) {
            throw new QCloudClientException(ex);
        }
    }
}
