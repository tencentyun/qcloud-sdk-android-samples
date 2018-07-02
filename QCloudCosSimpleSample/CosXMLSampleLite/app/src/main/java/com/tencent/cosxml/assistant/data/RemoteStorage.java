package com.tencent.cosxml.assistant.data;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.PutBucketRequest;
import com.tencent.cos.xml.model.service.GetServiceRequest;
import com.tencent.cos.xml.transfer.UploadService;
import com.tencent.cosxml.assistant.R;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.SessionCredentialProvider;
import com.tencent.qcloud.core.http.HttpRequest;
import com.tencent.qcloud.core.logger.QCloudLogger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 这里给出了如下几点示例：
 *
 * 1、如何利用临时秘钥服务器来对请求进行签名（即初始化 {@link QCloudCredentialProvider} 对象）。
 * 2、如何利用 {@link UploadService} 对象来上传。
 * 3、封装了 {@link GetServiceRequest} 和 {@link PutBucketRequest} 两个简单请求。
 *
 * Created by rickenwang on 2018/6/29.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class RemoteStorage {

    // 可以在 COS 控制台上查看 appid
    private final String APPID;


    // 以下为临时秘钥服务配置项，您只需要修改 TEMP_HOST 即可。

    /**
     * 临时密钥服务器地址（您必须修改为您自己的临时秘钥服务器地址，如机器的IP地址）
     */
    private final String TEMP_HOST;

    /**
     *   临时密钥服务器端口
     */
    private final int TEMP_PORT = 5000;

    /**
     * 获取临时密钥的路径
     */
    private final String TEMP_PATH = "/sign";

    /**
     * 获取临时密钥的 method
     */
    private final String TEMP_METHOD = "GET";

    /**
     * 获取临时密钥的 protocol
     */
    private final String TEMP_PROTOCOL = "http";

    /**
     * 分片上传大小
     */
    private final int MULTIPART_UPLOAD_SIZE = 2 * 1024 * 1024;

    /**
     * 保存当前 {@link CosXmlService} 对应的 region 信息
     */
    private String region;

    /**
     * COS 服务类，提供了 COS 服务所有的基本接口操作
     */
    private CosXmlService cosXmlService;


    private Context context;


    /**
     * 用于保存上传实例
     */
    private Map<String, UploadService> uploads;

    public RemoteStorage(Context context) {

        this.context = context;
        uploads = new HashMap<>();
        APPID = context.getString(R.string.appid);
        TEMP_HOST = context.getString(R.string.host);
    }


    /**
     * 列出所有的 bucket
     * @param region region
     * @param resultListener 创建
     */
    public void listBucket(String region, CosXmlResultListener resultListener) {

        if (region != null && !region.equals(this.region)) {  // should refresh

            this.region = region;
            refreshCosService(region);
        }

        if (cosXmlService != null) {
            cosXmlService.getServiceAsync(new GetServiceRequest(), resultListener);
        }
    }


    /**
     * 创建 bucket
     *
     * @param region region
     * @param bucketName bucket 名称
     * @param resultListener 创建结果 Listener
     */
    public void createBucket(String region, String bucketName, CosXmlResultListener resultListener) {

        if (region != null && !region.equals(this.region)) {  // should refresh

            this.region = region;
            refreshCosService(region);
        }

        if (cosXmlService != null) {

            cosXmlService.putBucketAsync(new PutBucketRequest(bucketName), resultListener);
        }

    }

    /**
     * 上传文件
     *
     * @param region region
     * @param bucketName bucket 名称
     * @param cosPath 上传到 COS 的路径
     * @param localPath 需要上传文件的本地路径
     * @param resultListener 结果监听器
     * @param progressListener  进度监听器
     *
     * @return 本次上传的 id，可以通过这个 id 来取消上传
     */
    public String uploadFile(String region, String bucketName, String cosPath, String localPath,
                                    final CosXmlResultListener resultListener, final CosXmlProgressListener progressListener) {

        if (region != null && !region.equals(this.region)) {  // should refresh

            this.region = region;
            refreshCosService(region);
        }

        UploadService.ResumeData resumeData = new UploadService.ResumeData();
        resumeData.sliceSize = MULTIPART_UPLOAD_SIZE;
        resumeData.cosPath = cosPath;
        resumeData.bucket = bucketName;
        resumeData.srcPath = localPath;

        /**
         * 上传服务类，这个类封装了 {@link CosXmlService} 几个上传相关的接口，通过使用该接口，您可以更加方便的上传文件。
         * 注意，每次上传都要初始化一个新的 {@link CosXmlService} 对象。
         */
        final UploadService uploadService = new UploadService(cosXmlService, resumeData);
        uploadService.setProgressListener(progressListener);

        final String uploadId = getUUID();
        uploads.put(uploadId, uploadService);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    UploadService.UploadServiceResult uploadResult = uploadService.upload();
                    resultListener.onSuccess(null, uploadResult);
                } catch (CosXmlClientException e) {
                    e.printStackTrace();
                    resultListener.onFail(null, e, null);
                } catch (CosXmlServiceException e) {
                    e.printStackTrace();
                    resultListener.onFail(null, null, e);
                }
                uploads.remove(uploadId);
            }
        };
        new Thread(runnable).start();
        return uploadId;
    }


    /**
     * 取消本次上传
     *
     * @param uploadId 上传id
     * @param resultListener 取消结果监听
     */
    public void cancelTransfer(String uploadId, CosXmlResultListener resultListener) {

        if (uploadId != null) {

            UploadService uploadService = uploads.remove(uploadId);
            if (uploadService != null) {
                QCloudLogger.i("assistant", "cancel: upload id is " + uploadId);
                uploadService.abort(resultListener);
            }
        }
    }

    /**
     * 初始化 {@link CosXmlService}
     * <br>
     * 不同的 region 对应 {@link CosXmlService} 的是不同的，因此如果 region 发生了变化，应该重新初始化 {@link CosXmlService} 对象。
     *
     * @param region region
     */
    private void refreshCosService(String region) {

        if (TextUtils.isEmpty(APPID) || TextUtils.isEmpty(region)) {

            QCloudLogger.e("assistant", "refresh cos xml service failed, appid or region is null.");
            return;
        }

        /**
         * 需要释放之前的 {@link CosXmlService} 对象。
         */
        if (cosXmlService != null) {
            cosXmlService.release();
        }

        /**
         * 初始化配置
         */
        CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .isHttps(false)
                .setAppidAndRegion(APPID, region)
                .setDebuggable(true)
                .builder();

        URL url = null; // 临时密钥服务器的地址
        try {
            url = new URL(TEMP_PROTOCOL, TEMP_HOST, TEMP_PORT, TEMP_PATH);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /**
         * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
         */
        QCloudCredentialProvider qCloudCredentialProvider = new SessionCredentialProvider(new HttpRequest.Builder<String>()
                .url(url)
                .method(TEMP_METHOD)
                .build());

        cosXmlService = new CosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
    }

    /**
     * 返回全局唯一字符串
     * @return 传输的 id 号
     */
    private String getUUID() {

        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
