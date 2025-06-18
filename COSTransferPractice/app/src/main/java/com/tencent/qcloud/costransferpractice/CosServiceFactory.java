package com.tencent.qcloud.costransferpractice;

import static com.tencent.qcloud.core.http.HttpConstants.Header.AUTHORIZATION;

import android.content.Context;
import android.text.TextUtils;

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

    public static CosXmlService getCosXmlService(Context context, String region, boolean refresh) {
        if (refresh) {
            cosXmlServiceMap.remove(region);
        }

        CosXmlService cosXmlService = cosXmlServiceMap.get(region);

        if (cosXmlService == null) {
            // 配置是否开启QUIC
            boolean enableQuic = false;
            // 配置是否开启 HTTPDNS
            boolean enableHttpDns = false;
            // 配置是否开启全球加速
            boolean enableGlobalAccelerate = false;
            // 配置加速域名或自定义域名
            String host = null;

            // 根据不同的bucket配置上面参数
//            if (region.equals("test-1250000000")) {
//                enableQuic = true;
//                enableHttpDns = true;
//                enableGlobalAccelerate = true;
//                host = "accelerate.domain.com";
//            }

            CosXmlServiceConfig cosXmlServiceConfig = getCosXmlServiceConfig(region, host, enableGlobalAccelerate, enableQuic);
            final QCloudCredentialProvider qCloudCredentialProvider = new MySessionCredentialProvider();
            // 永久秘钥仅用于测试
//            final QCloudCredentialProvider qCloudCredentialProvider = getCredentialProviderWithIdAndKey(secretId, secretKey);

            /* 获取默认签名CosXmlService实例 */
            cosXmlService = getCosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
            /* 获取自定义签名CosXmlService实例 */
//            cosXmlService = getCosXmlServiceByCustomSignature(context, cosXmlServiceConfig, qCloudCredentialProvider);

            // 开启 HTTPDNS 功能
            if (enableHttpDns) {
//                setHttpDns(context, cosXmlService);
            }

            cosXmlServiceMap.put(region, cosXmlService);
        }

        return cosXmlService;
    }

    /**
     * 获取桶列表的GetService不能走quic 因为quic是针对某个桶开启的
     */
    public static CosXmlService getCosXmlServiceByGetService(Context context, boolean refresh) {
        return getCosXmlService(context, defaultRegion, refresh);
    }

    /**
     * 获取默认签名CosXmlService实例
     */
    private static CosXmlService getCosXmlService(
            Context context,
            CosXmlServiceConfig cosXmlServiceConfig,
            QCloudCredentialProvider qCloudCredentialProvider
    ) {
        return new CosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
    }

    /**
     * 获取配置类
     */
    private static CosXmlServiceConfig getCosXmlServiceConfig(String region, String host, boolean enableGlobalAccelerate, boolean enableQuic) {
        CosXmlServiceConfig.Builder builder = new CosXmlServiceConfig.Builder()
                .setRegion(region)
                .setDebuggable(true)
                .isHttps(true)
                .setConnectionTimeout(10 * 1000)
                .setSocketTimeout(30 * 1000);
        // 配置加速域名或自定义域名
        if (!TextUtils.isEmpty(host)) {
            builder.setHostFormat(host);
        }
        // 配置是否开启全球加速
        builder.setAccelerate(enableGlobalAccelerate);
        // 配置是否开启QUIC
        builder.enableQuic(enableQuic);

        // .setCustomizeNetworkClient(new CustomizeNetworkClient())
        return builder.builder();
    }

    /**
     * 开启 HTTPDNS 功能
     */
//    private static void setHttpDns(Context context, CosXmlSimpleService service) {
//        // 初始化http dns sdk，该操作可以放到 Application 的 onCreate 方法中
//        tencentHttpDnsInit(context);
//        // cos sdk中接入http dns
//        service.addCustomerDNSFetch(hostname -> {
//            String ips = MSDKDnsResolver.getInstance().getAddrByName(hostname);
//            String[] ipArr = ips.split(";");
//            if (0 == ipArr.length) {
//                return Collections.emptyList();
//            }
//            List<InetAddress> inetAddressList = new ArrayList<>(ipArr.length);
//            for (String ip : ipArr) {
//                if ("0".equals(ip)) {
//                    continue;
//                }
//                try {
//                    InetAddress inetAddress = InetAddress.getByName(ip);
//                    inetAddressList.add(inetAddress);
//                } catch (UnknownHostException ignored) {
//                }
//            }
//            return inetAddressList;
//        });
//    }

    /**
     * 获取自定义签名CosXmlService实例
     */
    private static CosXmlService getCosXmlServiceByCustomSignature(
            Context context,
            CosXmlServiceConfig cosXmlServiceConfig,
            final QCloudCredentialProvider qCloudCredentialProvider
    ) {
        return new CosXmlService(context, cosXmlServiceConfig, new QCloudSelfSigner() {
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
     * 获取QCloudCredentialProvider对象，来给 SDK 提供临时密钥
     *
     * @param secretKey 永久密钥 secretKey
     * @parma secretId 永久密钥 secretId
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

//    private static void tencentHttpDnsInit(Context context) {
//        DnsConfig dnsConfigBuilder = new DnsConfig.Builder()
//                //（必填）dns 解析 id，即授权 id，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
//                .dnsId("UT_DNS_ID")
//                //（必填）dns 解析 key，即授权 id 对应的 key（加密密钥），在申请 SDK 后的邮箱里，腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于域名解析鉴权
//                .dnsKey("UT_DNS_KEY")
//                //（必填）Channel为desHttp()或aesHttp()时使用 119.29.29.98（默认填写这个就行），channel为https()时使用 119.29.29.99
//                .dnsIp("http dns ip")
//                //（可选）channel配置：基于 HTTP 请求的 DES 加密形式，默认为 desHttp()，另有 aesHttp()、https() 可选。（注意仅当选择 https 的 channel 需要选择 119.29.29.99 的dnsip并传入token，例如：.dnsIp('119.29.29.99').https().token('....') ）。
////                .desHttp()
//                //（可选，选择 https channel 时进行设置）腾讯云官网（https://console.cloud.tencent.com/httpdns）申请获得，用于 HTTPS 校验。仅当选用https()时进行填写
////                .token("xxx")
//                //（可选）日志粒度，如开启Debug打印则传入"Log.DEBUG"
//                .logLevel(Log.DEBUG)
//                //（可选）预解析域名，填写形式："baidu.com", "qq.com"，建议不要设置太多预解析域名，当前限制为最多 10 个域名。仅在初始化时触发。
////                .preLookupDomains("myqcloud.com")
//                //（可选）解析缓存自动刷新, 以域名形式进行配置，填写形式："baidu.com", "qq.com"。配置的域名会在 TTL * 75% 时自动发起解析请求更新缓存，实现配置域名解析时始终命中缓存。此项建议不要设置太多域名，当前限制为最多 10 个域名。与预解析分开独立配置。
////                .persistentCacheDomains("baidu.com", "qq.com")
//                // (可选) IP 优选，以 IpRankItem(hostname, port) 组成的 List 配置, port（可选）默认值为 8080。例如：IpRankItem("qq.com", 443)。sdk 会根据配置项进行 socket 连接测速情况对解析 IP 进行排序，IP 优选不阻塞当前解析，在下次解析时生效。当前限制为最多 10 项。
////                .ipRankItems(ipRankItemList)
//                //（可选）手动指定网络栈支持情况，仅进行 IPv4 解析传 1，仅进行 IPv6 解析传 2，进行 IPv4、IPv6 双栈解析传 3。默认为根据客户端本地网络栈支持情况发起对应的解析请求。
////                .setCustomNetStack(3)
//                //（可选）设置是否允许使用过期缓存，默认false，解析时先取未过期的缓存结果，不满足则等待解析请求完成后返回解析结果。
//                // 设置为true时，会直接返回缓存的解析结果，没有缓存则返回0;0，用户可使用localdns（InetAddress）进行兜底。且在无缓存结果或缓存已过期时，会异步发起解析请求更新缓存。因异步API（getAddrByNameAsync，getAddrsByNameAsync）逻辑在回调中始终返回未过期的解析结果，设置为true时，异步API不可使用。建议使用同步API （getAddrByName，getAddrsByName）。
////                .setUseExpiredIpEnable(true)
//                //（可选）设置是否启用本地缓存（Room），默认false
////                .setCachedIpEnable(true)
//                //（可选）设置域名解析请求超时时间，默认为1000ms
////                .timeoutMills(1000)
//                //（可选）是否开启解析异常上报，默认false，不上报
////                .enableReport(true)
//                // 以build()结束
//                .build();
//
//        MSDKDnsResolver.getInstance().init(context, dnsConfigBuilder);
//    }
}
