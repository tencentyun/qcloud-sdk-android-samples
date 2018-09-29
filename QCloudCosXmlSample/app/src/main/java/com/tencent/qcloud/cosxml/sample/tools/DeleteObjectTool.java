package com.tencent.qcloud.cosxml.sample.tools;

import android.content.Context;
import android.util.Log;

import com.tencent.cos.xml.CosXml;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.common.Region;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.object.DeleteObjectRequest;
import com.tencent.cos.xml.model.tag.Delete;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

/**
 * Created by bradyxiao on 2018/3/6.
 */

public class DeleteObjectTool {

    private static final String TAG = DeleteObjectTool.class.getName();

    /** 腾讯云 cos 服务的 appid */
    private final String appid = "1253653367";

    /** appid 对应的 秘钥 */
    private final String secretId = "";

    /** appid 对应的 秘钥 */
    private final String secretKey = "";

    /** bucketForObjectAPITest 所处在的地域 */
    private String region = Region.AP_Chengdu.getRegion();

    private String bucket = "errr4rr";

    CosXml cosXml;

    Context context;

    public DeleteObjectTool(Context context){
        this.context = context;
    }

    public void deleteObject(String cosPath){

        if(cosXml == null){
            CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .isHttps(true)
                    .setAppidAndRegion(appid, region)
                    .setDebuggable(true)
                    .builder();
            cosXml = new CosXmlService(context, cosXmlServiceConfig,
                    new ShortTimeCredentialProvider(secretId,secretKey,600) );
        }

        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, cosPath);
        try {
            cosXml.deleteObject(deleteObjectRequest);
            Log.d(TAG, cosPath + " delete success !");

        } catch (CosXmlClientException e) {
            Log.d(TAG, cosPath + " delete failed caused by " + e.getMessage());
        } catch (CosXmlServiceException e) {
            Log.d(TAG, cosPath + " delete failed caused by " + e.getMessage());
        }
    }
}
