package com.tencent.qcloud.cosxml.sample.tools;

import android.content.Context;
import android.util.Log;

import com.tencent.cos.xml.CosXml;
import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.common.Region;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.bucket.DeleteBucketRequest;
import com.tencent.cos.xml.model.bucket.GetBucketRequest;
import com.tencent.cos.xml.model.bucket.GetBucketResult;
import com.tencent.cos.xml.model.service.GetServiceRequest;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.cos.xml.model.tag.ListAllMyBuckets;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tencent.cos.xml.common.Region.AP_Shanghai;

/**
 * Created by bradyxiao on 2018/3/5.
 */

public class DeleteEmptyBuckets {

    private static final String TAG = "DeleteEmptyBuckets";

    Map<String, CosXml> cosXmlMap;

    CosXml cosXml;

    Context context;

    /** 腾讯云 cos 服务的 appid */
    private final String appid = "1253653367";

    /** appid 对应的 秘钥 */
    private final String secretId= "xxx";

    /** appid 对应的 秘钥 */
    private final String secretKey = "xxx";

    /** bucketForObjectAPITest 所处在的地域 */
    private String region = Region.AP_Shanghai.getRegion();

    public DeleteEmptyBuckets(Context context){
        this.context = context;
        cosXmlMap = new HashMap<>();
    }

    public void delete(){
        List<ListAllMyBuckets.Bucket> listBuckets = getBuckets();
        if(listBuckets != null){
            for(ListAllMyBuckets.Bucket bucket : listBuckets){
                boolean isEmpty = isEmpty(bucket);
                if(isEmpty){
                    deleteBucket(bucket);
                }
            }
        }else {
            Log.d(TAG, "delete none");
        }
    }

    public List<ListAllMyBuckets.Bucket> getBuckets(){
        if(cosXmlMap.get(region) != null){
            cosXml = cosXmlMap.get(region);
        }else {
            CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .isHttps(true)
                    .setAppidAndRegion(appid, region)
                    .setDebuggable(true)
                    .builder();
            cosXml = new CosXmlService(context, cosXmlServiceConfig,
                    new ShortTimeCredentialProvider(secretId,secretKey,600) );
            cosXmlMap.put(region, cosXml);
        }

        GetServiceRequest getServiceRequest = new GetServiceRequest();
        try {
            GetServiceResult getServiceResult = cosXml.getService(getServiceRequest);
            Log.d(TAG, "list buckets success" + getServiceResult.listAllMyBuckets.toString());
            if(getServiceResult != null && getServiceResult.listAllMyBuckets != null
                    && getServiceResult.listAllMyBuckets.buckets != null){
                return getServiceResult.listAllMyBuckets.buckets;
            }
        } catch (CosXmlClientException e) {
            Log.d(TAG, "list buckets failed caused by " + e.getMessage());
        } catch (CosXmlServiceException e) {
            Log.d(TAG, "list buckets failed caused by " + e.getMessage());
        }
        return null;
    }

    public boolean isEmpty(ListAllMyBuckets.Bucket bucket){
        if(cosXmlMap.get(bucket.location) != null){
            cosXml = cosXmlMap.get(bucket.location);
        }else {
            CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .isHttps(true)
                    .setAppidAndRegion(appid, bucket.location)
                    .setDebuggable(true)
                    .builder();
            cosXml = new CosXmlService(context, cosXmlServiceConfig,
                    new ShortTimeCredentialProvider(secretId,secretKey,600) );
            cosXmlMap.put(bucket.location, cosXml);
        }

        GetBucketRequest getBucketRequest = new GetBucketRequest(bucket.name);
        try {
            GetBucketResult getBucketResult = cosXml.getBucket(getBucketRequest);
            if(getBucketResult.listBucket != null && getBucketResult.listBucket.contentsList != null
                    && getBucketResult.listBucket.contentsList.size() > 0){
                Log.d(TAG, bucket.location + "_" + bucket.location + " is not empty!");
                return false;
            }else {
                Log.d(TAG, bucket.location + "_" + bucket.location  + " is empty!");
                return true;
            }
        } catch (CosXmlClientException e) {
            Log.d(TAG, bucket.location + "_" + bucket.location  + " got failed caused by " + e.getMessage());
        } catch (CosXmlServiceException e) {
            Log.d(TAG, bucket.location + "_" + bucket.location  + " got failed caused by " + e.getMessage());
        }
        return false;
    }

    public void deleteBucket(final ListAllMyBuckets.Bucket bucket){
        if(cosXmlMap.get(bucket.location) != null){
            cosXml = cosXmlMap.get(bucket.location);
        }else {
            CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                    .isHttps(true)
                    .setAppidAndRegion(appid, bucket.location)
                    .setDebuggable(true)
                    .builder();
            cosXml = new CosXmlService(context, cosXmlServiceConfig,
                    new ShortTimeCredentialProvider(secretId,secretKey,600) );
            cosXmlMap.put(bucket.location, cosXml);
        }
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucket.name);
        try {
            cosXml.deleteBucket(deleteBucketRequest);
            Log.d(TAG, bucket.location + "_" + bucket.location + " deleted success!");
        } catch (CosXmlClientException e) {
            Log.d(TAG, bucket.location + "_" + bucket.location + " deleted failed caused by " + e.getMessage());
        } catch (CosXmlServiceException e) {
            Log.d(TAG, bucket.location + "_" + bucket.location + " deleted failed caused by " + e.getMessage());
        }
    }

    public Region whichRegion(String region){
        for(Region region1 : Region.values()){
            if(region1.getRegion().equalsIgnoreCase(region)){
                return region1;
            }
        }
        Log.d(TAG, region  + " is not exist!");
        return null;
    }

}
