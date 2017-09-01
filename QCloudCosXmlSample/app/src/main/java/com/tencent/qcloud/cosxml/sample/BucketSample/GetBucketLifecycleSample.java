package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.GetBucketLifecycleRequest;
import com.tencent.cos.xml.model.bucket.GetBucketLifecycleResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Get Bucket Lifecycle 用来查询 Bucket 的生命周期配置。如果该 Bucket 没有配置生命周期规则会返回 NoSuchLifecycle。
 *
 * 不建议使用
 */
@Deprecated
public class GetBucketLifecycleSample {
    GetBucketLifecycleRequest getBucketLifecycleRequest;
    QServiceCfg qServiceCfg;

    public GetBucketLifecycleSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        getBucketLifecycleRequest = new GetBucketLifecycleRequest();
        getBucketLifecycleRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketLifecycleRequest.setSign(600,null,null);
        try {
            GetBucketLifecycleResult getBucketLifecycleResult =
                    qServiceCfg.cosXmlService.getBucketLifecycle(getBucketLifecycleRequest);
            Log.w("XIAO",getBucketLifecycleResult.printHeaders());
            if(getBucketLifecycleResult.getHttpCode() >= 300){
                Log.w("XIAO",getBucketLifecycleResult.printError());
            }
            resultHelper.cosXmlResult = getBucketLifecycleResult;
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
        getBucketLifecycleRequest = new GetBucketLifecycleRequest();
        getBucketLifecycleRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketLifecycleRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.getBucketLifecycleAsync(getBucketLifecycleRequest, new CosXmlResultListener() {
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
