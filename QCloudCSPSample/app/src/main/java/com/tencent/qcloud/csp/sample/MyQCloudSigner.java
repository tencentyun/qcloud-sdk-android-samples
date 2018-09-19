package com.tencent.qcloud.csp.sample;

import android.support.annotation.Nullable;

import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.qcloud.core.auth.COSXmlSignSourceProvider;
import com.tencent.qcloud.core.auth.COSXmlSigner;
import com.tencent.qcloud.core.auth.QCloudCredentials;
import com.tencent.qcloud.core.auth.QCloudSigner;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.http.HttpConstants;
import com.tencent.qcloud.core.http.QCloudHttpRequest;
import com.tencent.qcloud.core.http.RequestBodySerializer;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * Created by rickenwang on 2018/9/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class MyQCloudSigner implements QCloudSigner {

    /**
     * @param request 即为发送到 CSP 服务端的请求，您需要根据这个 HTTP 请求的参数来计算签名，并给其添加 Authorization header
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

        /**
         * 向您自己的服务端请求签名
         */
        String sign = exampleLocalSignerService(method, schema, host, path, headers);

        /**
         * 给请求设置 Authorization Header
         */
        request.addHeader("Authorization", sign);
    }

    /**
     * 这里仅仅是为了方便测试，在本地生成了签名串，生产环境下不能使用这个方法。
     *
     * 正式使用时请在服务端根据 COS 签名文档来计算 COS 请求对应的签名串，然后返回给终端用于身份校验。
     *
     * @return
     */
    @Deprecated
    private String exampleLocalSignerService(String method, String schema, String host, String path,
                                             Map<String, List<String>> headers) {

        String secretId = "xxx";
        String secretKey = "xxx";

        ShortTimeCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId, secretKey, 600);
        QCloudHttpRequest.Builder httpRequestBuilder = null;

        httpRequestBuilder = new QCloudHttpRequest.Builder()
                .method(method)
                .scheme(schema)
                .host(host)
                .path(path)
                .addHeader(HttpConstants.Header.HOST, host)
                .userAgent(CosXmlServiceConfig.DEFAULT_USER_AGENT)
                .signer("CosXmlSigner", (new COSXmlSignSourceProvider()).setDuration(600L));

        /**
         * PUT HTTP 请求 body 不能为空
         */
        if (method.equalsIgnoreCase("put") || method.equalsIgnoreCase("post")) {
            httpRequestBuilder.body(new RequestBodySerializer() {
                @Override
                public RequestBody body() {
                    return new RequestBody() {
                        @Nullable
                        @Override
                        public MediaType contentType() {
                            return MediaType.parse("plaint/text");
                        }

                        @Override
                        public void writeTo(BufferedSink bufferedSink) throws IOException {
                            bufferedSink.write(new byte[3]);
                        }
                    };
                }
            });
        }

        QCloudHttpRequest httpRequest = httpRequestBuilder.build();

        try {
            COSXmlSigner cosXmlSigner =  new COSXmlSigner();
            cosXmlSigner.sign(httpRequest, credentialProvider.getCredentials());
        } catch (QCloudClientException e) {
            e.printStackTrace();
        }
        Object auth = httpRequest.headers().get("Authorization");
        return (String) ((List)auth).get(0);
    }


}
