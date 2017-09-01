package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.PutBucketRequest;
import com.tencent.cos.xml.model.bucket.PutBucketResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Put Bucket 接口请求可以在指定账号下创建一个 Bucket。该 API 接口不支持匿名请求，
 * 您需要使用帯 Authorization 签名认证的请求才能创建新的 Bucket 。
 * 创建 Bucket 的用户默认成为 Bucket 的持有者。
 *
 */
public class PutBucketSample {
    PutBucketRequest putBucketRequest;
    QServiceCfg qServiceCfg;

    public PutBucketSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        Log.d("TAG", "put bucket sample start");
        ResultHelper resultHelper = new ResultHelper();
        putBucketRequest = new PutBucketRequest();
        putBucketRequest.setBucket(qServiceCfg.userBucketName);
        putBucketRequest.setSign(600,null,null);
        try {
            PutBucketResult putBucketResult =
                   qServiceCfg.cosXmlService.putBucket(putBucketRequest);
            Log.w("XIAO",putBucketResult.printHeaders());
            if(putBucketResult.getHttpCode() >= 300){
                Log.w("XIAO",putBucketResult.printError());
            } else {
                qServiceCfg.setUserBucket(qServiceCfg.userBucketName);
            }
            resultHelper.cosXmlResult = putBucketResult;
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
        putBucketRequest = new PutBucketRequest();
        putBucketRequest.setBucket(qServiceCfg.userBucketName);
        putBucketRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.putBucketAsync(putBucketRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setUserBucket(qServiceCfg.userBucketName);
                show(activity, stringBuilder.toString());
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printError());
                Log.w("XIAO", "failed = " + stringBuilder.toString());
                if(cosXmlResult.getHttpCode() == 409){
                    // bucket已存在
                    qServiceCfg.setUserBucket(qServiceCfg.userBucketName);
                }
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
