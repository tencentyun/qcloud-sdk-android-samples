package com.tencent.qcloud.cosxml.sample.common;

import android.content.Context;
import android.widget.Toast;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.common.Region;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 */
public class QServiceCfg {

    /** 腾讯云 cos 服务的 appid */
    private final String appid = "1252386093";

    /** appid 对应的 秘钥 */
    private final String secretId = "填写密钥secretId";

    /** appid 对应的 秘钥 */
    private final String secretKey = "填写密钥secretKey";

    /** bucketForObjectAPITest 所处在的地域 */
    private String region = Region.AP_Guangzhou.getRegion();


    /**
     * bucketForObjectAPITest api 操作测试
     *
     * bucketForBucketAPITest : 用于测试 bucket API 的 bucket
     *
     */
    private String bucketForBucketAPITest;

    /**
     * Object api 操作测试
     *
     * bucketForObjectAPITest: 用于测试object API 的 bucket
     * cosPath: 将文件上传到 cos 上的远端绝对路径，格式为： /dirName/fileName
     */
    private String bucketForObjectAPITest;

    /** 用于 put 上传文件的 cosPath: 小文件上传 */
    private String uploadCosPath;

    /** 用于 分片上传文件的 cosPath：大文件分片上传 */
    private String multiUploadCosPath;

    /** 用于 append 上传文件的 cosPath：追加形式上传文件 */
    private String appendCosPath;

    /** 用于 下载文件的 cosPath：cos 上文件的位置 */
    private String getCosPath;

   /** 下载文件到本地的路径*/
   private String downloadDir;

    /** 本地文件的路径: 小文件*/
    private String uploadFileUrl;

    /** 本地文件的路径: 追加文件*/
    private String appendUploadFileUrl;

    /** 本地文件的路径: 大文件*/
    private String multiUploadFileUrl;

    /** 用于分片上传中 保留的 分片号 和对应的 eTag */
    private Map<Integer, String> partNumberAndEtag;



    /**
     *  xml sdk 服务类: 通过 CosXmlService 调用各种API服务
     */
    public CosXmlService cosXmlService;


    private final int identity;
    private static volatile QServiceCfg instance;
    private Context context;

    public static QServiceCfg instance(Context context) {
        if (instance == null) {
            synchronized (QServiceCfg.class) {
                instance = new QServiceCfg(context);
            }
        }
        return instance;
    }

    private QServiceCfg(Context context){
        this.context = context;

        /** 初始化服务配置 CosXmlServiceConfig */
        CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .isHttps(false)
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .builder();

        /**
         * 设置密钥获取方式,此处使用 ShortTimeCredentialProvider 演示
         *
         * 可以使用以下两种密钥策略：
         *
         * 1. 通过 CAM 获取带token的临时会话密钥  可以参考{@link com.tencent.qcloud.core.auth.SessionCredentialProvider}
         * 2. 用永久密钥和一个有效期生成的临时密钥 可以参考{@link com.tencent.qcloud.core.auth.ShortTimeCredentialProvider}
         *
         * 此处只是示例，出于安全考虑客户端不应该缓存 secretKey，生成密钥的过程建议放到server端
         *
         */

        /** 初始化服务类 CosXmlService */
        cosXmlService = new CosXmlService(context,cosXmlServiceConfig,
                new ShortTimeCredentialProvider(secretId,secretKey,600));


        /**
         * 初始化参数值
         */
        identity = Identifier.getIdentifier(context);

        bucketForObjectAPITest = "rickenwang-guagnzhou";

        uploadCosPath = String.format("/upload_%d.txt", identity);
        multiUploadCosPath = String.format("/bigfile_%d", identity);
        appendCosPath = String.format("/append_%d", identity);
        getCosPath = uploadCosPath;

        uploadFileUrl = context.getExternalCacheDir().getPath() + File.separator + "upload.txt";
        multiUploadFileUrl = context.getExternalCacheDir().getPath() + File.separator + "bigfile.txt";
        appendUploadFileUrl = context.getExternalCacheDir().getPath() + File.separator + "append.txt";
        downloadDir = context.getExternalCacheDir().getPath() +  File.separator + "download";

        partNumberAndEtag = new LinkedHashMap<Integer, String>();
    }

    public String getBucketForBucketAPITest() {
        bucketForBucketAPITest = Identifier.getUserBucket();
        return bucketForBucketAPITest;
    }

    public void setBucketForBucketAPITest(String bucketForBucketAPITest) {
        Identifier.setUserBucket(bucketForBucketAPITest);
    }

    public String getBucketForObjectAPITest() {
        return bucketForObjectAPITest;
    }

    public String getUploadCosPath() {
        return uploadCosPath;
    }

    public String getMultiUploadCosPath() {
        return multiUploadCosPath;
    }

    public String getAppendCosPath() {
        return appendCosPath;
    }

    public String getGetCosPath() {
        return getCosPath;
    }

    public String getUploadFileUrl() {
        if (!new File(uploadFileUrl).exists()) {
            return writeLocalFile(uploadFileUrl, "construct a file for put object api test");
        }
        return uploadFileUrl;
    }

    private String writeLocalFile(String localUrl, String content) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(localUrl, "UTF-8");
            out.println(content);
            return localUrl;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return null;
    }

    public String getAppendUploadFileUrl() {
        if (!new File(appendUploadFileUrl).exists()) {
            return writeLocalFile(appendUploadFileUrl, 1024 * 1024 * 1);
        }
        return appendUploadFileUrl;
    }



    public void setCurrentUploadId(String currentUploadId) {
        Identifier.setUploadId(currentUploadId);
    }

    public String getCurrentUploadId() {
        return Identifier.getUploadId();
    }

    public String getMultiUploadFileUrl() {
       return writeLocalFile(multiUploadFileUrl, 1024 * 1024 * 2);
    }

    /** construct a large file for multi upload file */
    private String writeLocalFile(String fileName, long fileSize){
        File file = new File(fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
                RandomAccessFile randomAccessFile = new RandomAccessFile(file,"rw");
                randomAccessFile.setLength(fileSize);
                randomAccessFile.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
        return fileName;
    }

    public String getDownloadDir() {
        return downloadDir;
    }

    public void setPartNumberAndEtag(int partNumber, String eTag){
        partNumberAndEtag.put(partNumber, eTag);
    }

    public Map<Integer, String> getPartNumberAndEtag() {
        return partNumberAndEtag;
    }

    public void toastShow(String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
