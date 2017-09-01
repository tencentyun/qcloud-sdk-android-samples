package com.tencent.qcloud.cosxml.sample.common;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.qcloud.network.auth.BasicCredentialProvider;
import com.tencent.qcloud.network.auth.BasicLocalCredentialProvider;
import com.tencent.qcloud.network.auth.SampleSessionCredentialProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 */
public class QServiceCfg {
    private final String appid = "1253653367";
    private final String secretId = "AKIDPiqmW3qcgXVSKN8jngPzRhvxzYyDL5qP";
    private final String secretKey = "EH8oHoLgpmJmBQUM1Uoywjmv7EFzd5OJ";
    public final String accountId = "1278687956";
    public final String region = "cn-south";

    // demo演示用的bucket，用于object的操作
    public final String bucket = "androidsample";

    // demo演示用的sample文件
    public final String sampleCosPath = "/sample.txt";
    // demo演示用的大文件
    public final String bigfileCosPath = "/bigfile";
    // demo演示用的上传文件内容
    private final String uploadContent = "这是我上传的文件内容";

    // 上传文件的远程cos唯一地址
    public final String uploadCosPath;
    // 分片上传的远程cos唯一地址
    public final String multiUploadCosPath;
    // append文件的远程cos唯一地址
    private final String appendCosPath;
    // demo演示用的bucket，用于bucket的操作
    public final String userBucketName;

    // 本地下载文件和缓存文件路径
    public final String downloadDir;
    // 本地上传文件地址
    private final String uploadFileUrl;
    // 本地分片上传的大文件地址
    private final String multiUploadFileUrl;

    public CosXmlService cosXmlService;

    // demo使用的一个设备唯一标识
    private final int identity;

    private static QServiceCfg instance;

    public static QServiceCfg instance(Context context) {
        if (instance == null) {
            instance = new QServiceCfg(context);
        }

        return instance;
    }

    private QServiceCfg(Context context){
        CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig(appid,region);
        cosXmlServiceConfig.setSocketTimeout(450000);

        /**
         * 可以使用以下两种密钥策略：
         *
         * 1. 通过 CAM 获取带token的临时会话密钥  {@link SampleSessionCredentialProvider}
         * 2. 用永久密钥和一个有效期生成的临时密钥 {@link BasicLocalCredentialProvider}
         *
         * 此处只是示例，出于安全考虑客户端不应该缓存 secretKey，生成密钥的过程建议放到server端
         *
         */
        BasicCredentialProvider credentialProvider =
                new SampleSessionCredentialProvider(secretId, secretKey, appid);
//                new BasicLocalCredentialProvider(secretId,secretKey,600);

        cosXmlService = new CosXmlService(context,cosXmlServiceConfig, credentialProvider);

        identity = Identifier.getIdentifier(context);

        userBucketName = String.format("xmlbucket%d", identity);

        uploadCosPath = String.format("/upload_%d.txt", identity);
        multiUploadCosPath = String.format("/bigfile_%d", identity);
        appendCosPath = String.format("/append_%d", identity);

        uploadFileUrl = context.getExternalCacheDir() + File.separator + "upload.txt";
        multiUploadFileUrl = context.getExternalCacheDir() + File.separator + "bigfile";
        downloadDir = context.getExternalCacheDir().getPath();
    }

    public String getUserBucket() {
        return Identifier.getUserBucket();
    }

    public void setUserBucket(String userBucket) {
        Identifier.setUserBucket(userBucket);
    }

    public String getUserObject() {
        return Identifier.getUserObject();
    }

    public void setUserObject(String userObject) {
        Identifier.setUserObject(userObject);
    }

    public void setCurrentUploadId(String currentUploadId) {
        Identifier.setUploadId(currentUploadId);
    }

    public String getCurrentUploadId() {
        return Identifier.getUploadId();
    }

    public String getMultiUploadCosPath() {
        return multiUploadCosPath;
    }

    public String getAppendCosPath() {
        return appendCosPath;
    }

    public String getMultiUploadFileUrl() {
        return multiUploadFileUrl;
    }

    public boolean hasMultiUploadFile() {
        return new File(multiUploadFileUrl).exists();
    }

    public String getAppendFileUrl() {
        File sampleFile = new File(downloadDir + sampleCosPath);
        return sampleFile.exists() ? sampleFile.toString() : null;
    }

    public String getUploadFileUrl() {
        if (!new File(uploadFileUrl).exists()) {
            return writeLocalFile(uploadFileUrl, uploadContent);
        }

        return uploadFileUrl;
    }

    // 不允许多个上传任务
    AtomicBoolean canUpload = new AtomicBoolean(true);
    public void blockOtherUploadTask() {
        canUpload.set(false);
    }

    public void releaseUploadBarrier() {
        canUpload.set(true);
    }

    public boolean canUpload() {
        return canUpload.get();
    }

    private String writeLocalFile(String localUrl, String content) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(localUrl);
            out.println(content);

            return localUrl;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }

        return null;
    }

}
