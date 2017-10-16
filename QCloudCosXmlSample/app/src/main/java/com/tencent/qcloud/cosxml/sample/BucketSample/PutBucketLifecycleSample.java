package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.common.COSStorageClass;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.PutBucketLifecycleRequest;
import com.tencent.cos.xml.model.bucket.PutBucketLifecycleResult;
import com.tencent.cos.xml.model.tag.Filter;
import com.tencent.cos.xml.model.tag.Rule;
import com.tencent.cos.xml.model.tag.Transition;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


/**
 * Created by bradyxiao on 2017/6/2.
 * author bradyxiao
 *
 * Put Bucket Lifecycle 用于为 Bucket 创建一个新的生命周期配置。如果该 Bucket 已配置生命周期，使用该接口创建新的配置的同时则会覆盖原有的配置。
 *
 *
 *
 */

public class PutBucketLifecycleSample {
    PutBucketLifecycleRequest putBucketLifecycleRequest;
    QServiceCfg qServiceCfg;

    public PutBucketLifecycleSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForBucketAPITest();
        if(bucket == null){
            qServiceCfg.toastShow("bucket 不存在，需要创建");
        }

        putBucketLifecycleRequest = new PutBucketLifecycleRequest(bucket);

        Rule rule = new Rule();
        rule.id = "lifeID";
        Filter filter = new Filter();
        filter.prefix = "2/";
        rule.filter = filter;
        rule.status = "Enabled";
        Transition transition = new Transition();
        transition.days = 100;
        transition.storageClass = COSStorageClass.NEARLINE.getStorageClass();
        rule.transition = transition;
        putBucketLifecycleRequest.setRuleList(rule);

        putBucketLifecycleRequest.setSign(600,null,null);
        try {
            PutBucketLifecycleResult putBucketLifecycleResult =
                    qServiceCfg.cosXmlService.putBucketLifecycle(putBucketLifecycleRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = putBucketLifecycleResult;
            return resultHelper;
        }catch (CosXmlClientException e) {
            Log.w("XIAO","QCloudException =" + e.getMessage());
            resultHelper.qCloudException = e;
            return resultHelper;
        } catch (CosXmlServiceException e) {
            Log.w("XIAO","QCloudServiceException =" + e.toString());
            resultHelper.qCloudServiceException = e;
            return resultHelper;
        }
    }

    /**
     *
     * 采用异步回调操作
     *
     */
    public void startAsync(final Activity activity){
        String bucket = qServiceCfg.getBucketForBucketAPITest();
        if(bucket == null){
            qServiceCfg.toastShow("bucket 不存在，需要创建");
        }

        putBucketLifecycleRequest = new PutBucketLifecycleRequest(bucket);

        Rule rule = new Rule();
        rule.id = "lifeID";
        Filter filter = new Filter();
        filter.prefix = "2/";
        rule.filter = filter;
        rule.status = "Enabled";
        Transition transition = new Transition();
        transition.days = 100;
        transition.storageClass = COSStorageClass.NEARLINE.getStorageClass();
        rule.transition = transition;
        putBucketLifecycleRequest.setRuleList(rule);

        putBucketLifecycleRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.putBucketLifecycleAsync(putBucketLifecycleRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                show(activity, stringBuilder.toString());
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException, CosXmlServiceException qcloudServiceException) {
                StringBuilder stringBuilder = new StringBuilder();
                if(qcloudException != null){
                    stringBuilder.append(qcloudException.getMessage());
                }else {
                    stringBuilder.append(qcloudServiceException.toString());
                }
                Log.w("XIAO", "failed = " + stringBuilder.toString());
                show(activity, stringBuilder.toString());
            }
        });
    }

    private void show(Activity activity, String message){
        Intent intent = new Intent(activity, ResultActivity.class);
        intent.putExtra("RESULT", message);
        activity.startActivity(intent);
    }
}
