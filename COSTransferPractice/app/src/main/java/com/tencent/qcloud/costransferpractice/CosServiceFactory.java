package com.tencent.qcloud.costransferpractice;

import static com.tencent.qcloud.core.http.HttpConstants.Header.AUTHORIZATION;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.qcloud.core.auth.AuthConstants;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.QCloudSelfSigner;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;
import com.tencent.qcloud.core.auth.Utils;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.http.QCloudHttpRequest;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link CosXmlService} 是您访问 COS 服务的核心类，它封装了所有 COS 服务的基础 API 方法。
 * <p>
 * 每一个{@link CosXmlService} 对象只能对应一个 region，如果您需要同时操作多个 region 的
 * Bucket，请初始化多个 {@link CosXmlService} 对象。
 * <p>
 * {@link TransferManager} 进一步封装了 {@link CosXmlService} 的上传和下载接口，当您需要
 * 上传文件到 COS 或者从 COS 下载文件时，请优先使用这个类。
 * <p>
 * Created by rickenwang on 2018/10/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class CosServiceFactory {
    private static final String defaultRegion = "ap-shanghai";

    private static Map<String, CosXmlService> cosXmlServiceMap = new HashMap<>();

    public static CosXmlService getCosXmlService(Context context, String region, final String secretId,
                                                 final String secretKey, boolean refresh) {
        if (refresh) {
            cosXmlServiceMap.remove(region);
        }

        CosXmlService cosXmlService = cosXmlServiceMap.get(region);

        if (cosXmlService == null) {
            CosXmlServiceConfig cosXmlServiceConfig = getCosXmlServiceConfig(region);
            final QCloudCredentialProvider qCloudCredentialProvider = getCredentialProviderWithIdAndKey(secretId, secretKey);

            /* 获取默认签名CosXmlService实例 */
            cosXmlService = getCosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
            /* 获取自定义签名CosXmlService实例 */
//            cosXmlService = getCosXmlServiceByCustomSignature(context, cosXmlServiceConfig, qCloudCredentialProvider);

            cosXmlServiceMap.put(region, cosXmlService);
        }

        return cosXmlService;
    }

    public static CosXmlService getCosXmlService(Context context, String secretId, String secretKey, boolean refresh) {
        return getCosXmlService(context, defaultRegion, secretId, secretKey, refresh);
    }

    /**
     * 获取默认签名CosXmlService实例
     */
    private static CosXmlService getCosXmlService(
            Context context,
            CosXmlServiceConfig cosXmlServiceConfig,
            QCloudCredentialProvider qCloudCredentialProvider
    ){
        return new CosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
    }

    /**
     * 获取自定义签名CosXmlService实例
     */
    private static CosXmlService getCosXmlServiceByCustomSignature(
            Context context,
            CosXmlServiceConfig cosXmlServiceConfig,
            final QCloudCredentialProvider qCloudCredentialProvider
    ){
        return new CosXmlService(context, cosXmlServiceConfig, new QCloudSelfSigner(){
                @Override
                public void sign(QCloudHttpRequest qCloudHttpRequest) throws QCloudClientException {
                    StringBuilder authorization = new StringBuilder();

                    QCloudLifecycleCredentials lifecycleCredentials = (QCloudLifecycleCredentials) qCloudCredentialProvider.getCredentials();

                    String keyTime = qCloudHttpRequest.getKeyTime();
                    if (keyTime == null) {
                        keyTime = lifecycleCredentials.getKeyTime();
                    }
//                    COSXmlSignSourceProvider sourceProvider = (COSXmlSignSourceProvider) qCloudHttpRequest.getSignProvider();
                    MyCOSXmlSignSourceProvider sourceProvider = new MyCOSXmlSignSourceProvider();
                    sourceProvider.setSignTime(keyTime);
                    String signature = signature(sourceProvider.source(qCloudHttpRequest), lifecycleCredentials.getSignKey());

                    authorization.append(AuthConstants.Q_SIGN_ALGORITHM).append("=").append(AuthConstants.SHA1).append("&")
                            .append(AuthConstants.Q_AK).append("=")
                            .append(lifecycleCredentials.getSecretId()).append("&")
                            .append(AuthConstants.Q_SIGN_TIME).append("=")
                            .append(keyTime).append("&")
                            .append(AuthConstants.Q_KEY_TIME).append("=")
                            .append(lifecycleCredentials.getKeyTime()).append("&")
                            .append(AuthConstants.Q_HEADER_LIST).append("=")
                            .append(sourceProvider.getRealHeaderList().toLowerCase(Locale.ROOT)).append("&")
                            .append(AuthConstants.Q_URL_PARAM_LIST).append("=")
                            .append(sourceProvider.getRealParameterList().toLowerCase(Locale.ROOT)).append("&")
                            .append(AuthConstants.Q_SIGNATURE).append("=").append(signature);
                    String auth = authorization.toString();

                    qCloudHttpRequest.removeHeader(AUTHORIZATION);
                    qCloudHttpRequest.addHeader(AUTHORIZATION, auth);
                }
            });
    }

    /**
     * 获取配置类
     */
    private static CosXmlServiceConfig getCosXmlServiceConfig(String region) {
        return new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .setDebuggable(true)
//                .setHostFormat("www.jordanqin.cn")
                .isHttps(true)
//                .enableQuic(true)
                .builder();
    }

    /**
     * 获取QCloudCredentialProvider对象，来给 SDK 提供临时密钥
     * @parma secretId 永久密钥 secretId
     * @param secretKey 永久密钥 secretKey
     */
    private static QCloudCredentialProvider getCredentialProviderWithIdAndKey(String secretId, String secretKey) {
        /**
         * 注意！注意！注意！
         * 由于该方式会存在泄漏密钥的风险，我们强烈不推荐您使用这种方式，建议您仅在安全的环境下临时测试时使用.
         * 建议采用"通过临时秘钥进行授权"，请参考：https://cloud.tencent.com/document/product/436/12159#.E5.88.9D.E5.A7.8B.E5.8C.96.E6.9C.8D.E5.8A.A1
         */
        return new ShortTimeCredentialProvider(secretId, secretKey, 300);
    }

    private static String signature(String source, String signKey) {
        byte[] sha1Bytes = Utils.hmacSha1(source, signKey);
        String signature = "";
        if (sha1Bytes != null) {
            signature = new String(Utils.encodeHex(sha1Bytes));
        }
        return signature;
    }
}
