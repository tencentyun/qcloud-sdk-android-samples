package com.tencent.qcloud.csp.sample;

/**
 * Created by rickenwang on 2018/9/18.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
import android.content.Context;
import android.support.annotation.Nullable;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.bucket.PutBucketRequest;
import com.tencent.cos.xml.model.bucket.PutBucketResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.cos.xml.model.service.GetServiceRequest;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.cos.xml.transfer.UploadService;
import com.tencent.qcloud.core.auth.COSXmlSigner;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudCredentials;
import com.tencent.qcloud.core.auth.QCloudSignSourceProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.http.HttpConstants;
import com.tencent.qcloud.core.http.QCloudHttpRequest;
import com.tencent.qcloud.core.http.RequestBodySerializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * 这里给出了如下几点示例：
 *
 * 1、如何利用 {@link UploadService} 对象来上传。
 * 2、封装了 {@link GetServiceRequest} 和 {@link PutBucketRequest} 两个简单请求。
 *
 * Created by rickenwang on 2018/6/29.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class RemoteStorage {

    private int MULTIPART_UPLOAD_SIZE = 1024 * 2;

    private CosXmlService cosXmlService;
    private boolean isHttps;
    private String appid;
    private String region;

    /**
     * 您的服务器对应的主域名，默认为 myqcloud.com，设置后访问地址为：
     *
     * {bucket-name}-{appid}.cos.{cos-region}.{domainSuffix}
     */
    private String domainSuffix;

    public RemoteStorage(Context context, String appid, String region, String domainSuffix) {

        isHttps = false;
        this.appid = appid;
        this.region = region;
        this. domainSuffix = domainSuffix;

        /**
         * 初始化配置
         */
        CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .isHttps(isHttps)
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .setDomainSuffix(domainSuffix)  // 私有云需要设置主域名
                .builder();

        /**
         * 私有云暂时不支持临时密钥进行签名，如果直接在客户端直接使用永久密钥会有安全性问题，因此这里采用
         * 服务端直接下发签名的方式来进行鉴权。
         */
        QCloudCredentialProvider credentialProvider = null;

        cosXmlService = new CosXmlService(context, cosXmlServiceConfig, credentialProvider);
    }


    /**
     * 列出所有的 bucket
     */
    public GetServiceResult getService() throws CosXmlServiceException, CosXmlClientException {

        GetServiceRequest getServiceRequest = new GetServiceRequest();

        /**
         * 从远程服务端获取签名来授权请求
         */
        String sign = getSignFromRemoteService(getServiceRequest);
        getServiceRequest.setSign(sign);

        return cosXmlService.getService(getServiceRequest);
    }


    /**
     * 创建 bucket
     *
     * @param bucketName bucket 名称
     */
    public PutBucketResult putBucket(String bucketName) throws CosXmlServiceException, CosXmlClientException {

        PutBucketRequest putBucketRequest = new PutBucketRequest(bucketName);

        /**
         * 从远程服务端获取签名来授权请求
         */
        String sign = getSignFromRemoteService(putBucketRequest);
        putBucketRequest.setSign(sign);

        return cosXmlService.putBucket(putBucketRequest);
    }

    /**
     * 上传文件
     *
     * @param bucketName bucket 名称
     * @param cosPath 上传到 COS 的路径
     * @param localPath 需要上传文件的本地路径
     * @param progressListener  进度监听器
     *
     * @return 本次上传的 id，可以通过这个 id 来取消上传
     */
    public UploadService.UploadServiceResult uploadFile(String bucketName, String cosPath, String localPath, CosXmlProgressListener progressListener)
            throws CosXmlServiceException, CosXmlClientException {

        UploadService.ResumeData resumeData = new UploadService.ResumeData();
        resumeData.sliceSize = MULTIPART_UPLOAD_SIZE; // 分片上传的大小
        resumeData.cosPath = cosPath;
        resumeData.bucket = bucketName;
        resumeData.srcPath = localPath;

        /**
         * 上传服务类，这个类封装了 {@link CosXmlService} 几个上传相关的接口，通过使用该接口，您可以更加方便的上传文件。
         * 注意，每次上传都要初始化一个新的 {@link CosXmlService} 对象。
         */
        final UploadService uploadService = new UploadService(cosXmlService, resumeData);
        uploadService.setProgressListener(progressListener);
        return uploadService.upload();
    }

    public PutObjectResult simpleUploadFile(String bucketName, String cosPath, String localPath, CosXmlProgressListener progressListener)
            throws CosXmlServiceException, CosXmlClientException {

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, cosPath, localPath);
        putObjectRequest.setProgressListener(progressListener);

        /**
         * 从远程服务端获取签名来授权请求
         */
        String sign = getSignFromRemoteService(putObjectRequest);
        putObjectRequest.setSign(sign);

        return cosXmlService.putObject(putObjectRequest);
    }


    /**
     * 您需要向您的服务器去请求签名；
     *
     * @param request
     * @return
     */
    private String getSignFromRemoteService(CosXmlRequest request) throws CosXmlClientException {

        /**
         * 获取 {@link CosXmlRequest} 的 HTTP 参数
         */
        String method = request.getMethod();
        String scheme = isHttps ? "https" : "http";
        String host = request.getHost(appid, region, domainSuffix, false);
        String path = request.getPath();
        Map<String, List<String>> headers = request.getRequestHeaders();

        /**
         * 根据 HTTP 参数计算签名，这里测试是在本地计算签名，请您在服务端生成签名
         */
        return exampleLocalSignerService(method, scheme, host, path, headers, request.getSignSourceProvider());
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
                                             Map<String, List<String>> headers, QCloudSignSourceProvider sourceProvider) {

        String secretId = "AKIDZuxhBMAbeOovjDtI42h3mCJ7dsnQwkSq";
        String secretKey = "MUKs73g01j8DzTdU2HDqBDzpLbYBSOzF";

        ShortTimeCredentialProvider credentialProvider = new ShortTimeCredentialProvider(secretId, secretKey, 600);
        QCloudHttpRequest.Builder httpRequestBuilder = null;

        httpRequestBuilder = new QCloudHttpRequest.Builder()
                .method(method)
                .scheme(schema)
                .host(host)
                .path(path)
                .addHeader(HttpConstants.Header.HOST, host)
                .userAgent(CosXmlServiceConfig.DEFAULT_USER_AGENT)
                .signer("CosXmlSigner", sourceProvider);

        /**
         * PUT HTTP 请求 body 不能为空
         */
        if (method.equalsIgnoreCase("put")) {
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
