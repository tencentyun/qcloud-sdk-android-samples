package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.PutBucketLifecycleRequest;
import com.tencent.cos.xml.model.bucket.PutBucketLifecycleResult;
import com.tencent.cos.xml.model.tag.AbortIncompleteMultiUpload;
import com.tencent.cos.xml.model.tag.Expiration;
import com.tencent.cos.xml.model.tag.Rule;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/2.
 * author bradyxiao
 *
 * Put Bucket Lifecycle 用于为 Bucket 创建一个新的生命周期配置。如果该 Bucket 已配置生命周期，使用该接口创建新的配置的同时则会覆盖原有的配置。
 *
 * 不建议使用
 *
 */
@Deprecated
public class PutBucketLifecycleSample {
    PutBucketLifecycleRequest putBucketLifecycleRequest;
    QServiceCfg qServiceCfg;

    public PutBucketLifecycleSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        putBucketLifecycleRequest = new PutBucketLifecycleRequest();
        putBucketLifecycleRequest.setBucket(qServiceCfg.getUserBucket());

        Rule rule = new Rule();
        rule.id = "lifeID";
        rule.status = "Enabled";
        //配置未完成分块上传的定期删除规则
        rule.abortIncompleteMultiUpload = new AbortIncompleteMultiUpload();
        rule.abortIncompleteMultiUpload.daysAfterInitiation = "1";
        putBucketLifecycleRequest.setRuleList(rule);

//        Rule rule2 = new Rule();
//        rule2.id = "lifeID2";
//        rule2.status = "Enabled";
//        //配置文件的定期删除规则
//        rule2.expiration = new Expiration();
//        rule2.expiration.days = "1";
//        putBucketLifecycleRequest.setRuleList(rule2);

        putBucketLifecycleRequest.setSign(600,null,null);
        try {
            PutBucketLifecycleResult putBucketLifecycleResult =
                    qServiceCfg.cosXmlService.putBucketLifecycle(putBucketLifecycleRequest);
            Log.w("XIAO",putBucketLifecycleResult.printHeaders());
            if(putBucketLifecycleResult.getHttpCode() >= 300){
                Log.w("XIAO",putBucketLifecycleResult.printError());
            }
            resultHelper.cosXmlResult = putBucketLifecycleResult;
            return resultHelper;
        } catch (QCloudException e) {
            Log.w("XIAO","exception =" + e.getExceptionType() + "; " + e.getDetailMessage());
            resultHelper.exception = e;
            return resultHelper;
        }
    }

    /**
     *
     * 采用异步回调操作
     *
     */
    public void startAsync(final Activity activity){
        putBucketLifecycleRequest = new PutBucketLifecycleRequest();
        putBucketLifecycleRequest.setBucket(qServiceCfg.getUserBucket());

        Rule rule = new Rule();
        rule.id = "lifeID";
        rule.status = "Enabled";
        //配置未完成分块上传的定期删除规则
        rule.abortIncompleteMultiUpload = new AbortIncompleteMultiUpload();
        rule.abortIncompleteMultiUpload.daysAfterInitiation = "1";
        putBucketLifecycleRequest.setRuleList(rule);
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
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printError());
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
