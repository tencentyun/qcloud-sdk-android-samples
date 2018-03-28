package com.tencent.qcloud.cosxml.sample.tools;

import android.content.Context;
import android.util.Log;

import com.tencent.cos.xml.CosXml;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.bucket.PutBucketRequest;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

import static android.content.ContentValues.TAG;

/**
 * Created by bradyxiao on 2018/3/6.
 */

public class CreateBucketTool {

    private static final String TAG = CreateBucketTool.class.getName();

    /** 腾讯云 cos 服务的 appid */
    private final String appid = "1252448703";

    /** appid 对应的 秘钥 */
    private final String secretId = "AKID15IsskiBQKTZbAo6WhgcBqVls9SmuG00";

    /** appid 对应的 秘钥 */
    private final String secretKey = "ciivKvnnrMvSvQpMAWuIz12pThGGlWRW";

    /** bucketForObjectAPITest 所处在的地域 */
    private String region = "yfb";

    private Context context;

    CosXml cosXml;

    public CreateBucketTool(Context context){
        this.context = context;
    }

    public void createBucket(String bucket){

        if(cosXml == null){
            CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .isHttps(false)
                    .setAppidAndRegion(appid, region)
                    .setDebuggable(true)
                    .builder();
            cosXml = new CosXmlService(context, cosXmlServiceConfig,
                    new ShortTimeCredentialProvider(secretId,secretKey,600) );
        }

        PutBucketRequest putBucketRequest = new PutBucketRequest(bucket);
        try {
            cosXml.putBucket(putBucketRequest);
            Log.d(TAG, bucket + " crate success !");

        } catch (CosXmlClientException e) {
            Log.d(TAG, bucket + " crate failed caused by " + e.getMessage());
        } catch (CosXmlServiceException e) {
            Log.d(TAG, bucket + " crate failed caused by " + e.getMessage());
        }
    }
}
