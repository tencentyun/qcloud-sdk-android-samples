package com.tencent.qcloud.costransferpractice;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.SessionCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;
import com.tencent.qcloud.core.http.HttpRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * {@link CosXmlService} 是您访问 COS 服务的核心类，它封装了所有 COS 服务的基础 API 方法。
 *
 * 每一个{@link CosXmlService} 对象只能对应一个 region，如果您需要同时操作多个 region 的
 * Bucket，请初始化多个 {@link CosXmlService} 对象。
 *
 * {@link TransferManager} 进一步封装了 {@link CosXmlService} 的上传和下载接口，当您需要
 * 上传文件到 COS 或者从 COS 下载文件时，请优先使用这个类。
 *
 * Created by rickenwang on 2018/10/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class CosServiceFactory {

    private static final String defaultRegion = "ap-shanghai";

    private static Map<String, CosXmlService> cosXmlServiceMap = new HashMap<>();

    public static CosXmlService getCosXmlServiceWithProperWay(Context context, String region) {

        COSConfigManager cosConfigManager = COSConfigManager.getInstance();

        if (cosConfigManager.isForeverSignComplete()) {

            return getCosXmlServiceWithForeverKey(context, cosConfigManager.getAppid(), region,
                    cosConfigManager.getSecretId(), cosConfigManager.getSecretKey(), false);
        } else {

            return getCosXmlServiceWithTemporaryKey(context, cosConfigManager.getAppid(),
                    region, cosConfigManager.getSignUrl(), false);
        }
    }

    public static CosXmlService getCosXmlServiceWithForeverKey(Context context, String appid, String region, String secretId,
                                                               String secretKey, boolean refresh) {

        if (refresh) {
            cosXmlServiceMap.remove(region);
        }

        CosXmlService cosXmlService = cosXmlServiceMap.get(region);

        if (cosXmlService == null) {

            CosXmlServiceConfig cosXmlServiceConfig = getCosXmlServiceConfig(appid, region);
            QCloudCredentialProvider qCloudCredentialProvider = getCredentialProviderWithIdAndKey(secretId, secretKey);
            cosXmlService = new CosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
            cosXmlServiceMap.put(region, cosXmlService);
        }

        return cosXmlService;
    }

    public static CosXmlService getCosXmlServiceWithForeverKey(Context context, String appid, String secretId,
                                                               String secretKey, boolean refresh) {

        return getCosXmlServiceWithForeverKey(context, appid, defaultRegion, secretId, secretKey, refresh);
    }

    public static CosXmlService getCosXmlServiceWithTemporaryKey(Context context, String appid, String region, String signUrl,
                                                                 boolean refresh) {
        if (refresh) {
            cosXmlServiceMap.remove(region);
        }

        CosXmlService cosXmlService = cosXmlServiceMap.get(region);

        if (cosXmlService == null) {

            CosXmlServiceConfig cosXmlServiceConfig = getCosXmlServiceConfig(appid, region);
            QCloudCredentialProvider qCloudCredentialProvider = getCredentialProviderWithUrl(signUrl);

            cosXmlService = new CosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
            cosXmlServiceMap.put(region, cosXmlService);
        }

        return cosXmlService;
    }

    public static CosXmlService getCosXmlServiceWithTemporaryKey(Context context, String appid, String signUrl, boolean refresh) {

        return getCosXmlServiceWithTemporaryKey(context, appid, defaultRegion, signUrl, refresh);
    }



    public static TransferManager getTransferManager(CosXmlService cosXmlService) {

        TransferConfig transferConfig = new TransferConfig.Builder()
                .build();

        return new TransferManager(cosXmlService, transferConfig);
    }

    /**
     * 获取配置类
     */
    private static CosXmlServiceConfig getCosXmlServiceConfig(String appid, String region) {

        return new CosXmlServiceConfig.Builder()
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .isHttps(true)
                .builder();
    }

    /**
     * 获取临时密钥授权类
     */
    private static QCloudCredentialProvider getCredentialProviderWithUrl(String signUrl) {

        URL url = null;

        try {
            url = new URL(signUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /**
         * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
         */
        return new SessionCredentialProvider(new HttpRequest.Builder<String>()
                .url(url)
                /**
                 * 注意这里的 HTTP method 为 GET，请根据您自己密钥服务的发布方式进行修改
                 */
                .method("GET")
                .build());
    }

    private static QCloudCredentialProvider getCredentialProviderWithIdAndKey(String secretId, String secretKey) {

        return  new ShortTimeCredentialProvider(secretId, secretKey, 300);
    }

}
