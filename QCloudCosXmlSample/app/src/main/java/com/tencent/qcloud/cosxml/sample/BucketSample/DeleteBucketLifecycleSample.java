package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.DeleteBucketLifecycleRequest;
import com.tencent.cos.xml.model.bucket.DeleteBucketLifecycleResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Delete Bucket Lifecycle 用来删除 Bucket 的生命周期配置。如果该 Bucket 没有配置生命周期规则会返回 NoSuchLifecycle。
 *
 * 不建议使用
 */
@Deprecated
public class DeleteBucketLifecycleSample {
    DeleteBucketLifecycleRequest deleteBucketLifecycleRequest;
    QServiceCfg qServiceCfg;

    public DeleteBucketLifecycleSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        deleteBucketLifecycleRequest = new DeleteBucketLifecycleRequest();
        deleteBucketLifecycleRequest.setBucket(qServiceCfg.getUserBucket());
        deleteBucketLifecycleRequest.setSign(600,null,null);
        try {
            DeleteBucketLifecycleResult deleteBucketCORSResult =
                    qServiceCfg.cosXmlService.deleteBucketLifecycle(deleteBucketLifecycleRequest);
            Log.w("XIAO",deleteBucketCORSResult.printHeaders());
            if(deleteBucketCORSResult.getHttpCode() >= 300){
                Log.w("XIAO",deleteBucketCORSResult.printError());
            }else{
                Log.w("XIAO"," " + deleteBucketCORSResult.printBody());
            }
            resultHelper.cosXmlResult = deleteBucketCORSResult;
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
        deleteBucketLifecycleRequest = new DeleteBucketLifecycleRequest();
        deleteBucketLifecycleRequest.setBucket(qServiceCfg.getUserBucket());
        deleteBucketLifecycleRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.deleteBucketLifecycleAsync(deleteBucketLifecycleRequest, new CosXmlResultListener() {
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
