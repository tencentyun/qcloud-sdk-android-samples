package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.HeadBucketRequest;
import com.tencent.cos.xml.model.bucket.HeadBucketResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Head Bucket 请求可以确认该 Bucket 是否存在，是否有权限访问。Head 的权限与 Read 一致。
 * 当该 Bucket 存在时，返回 HTTP 状态码 200；当该 Bucket 无访问权限时，返回 HTTP 状态码 403；
 * 当该 Bucket 不存在时，返回 HTTP 状态码 404。
 */
public class HeadBucketSample {
    HeadBucketRequest headBucketRequest;
    QServiceCfg qServiceCfg;

    public HeadBucketSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        headBucketRequest = new HeadBucketRequest();
        headBucketRequest.setBucket(qServiceCfg.getUserBucket());
        headBucketRequest.setSign(600,null,null);
        try {
            HeadBucketResult headBucketResult =
                   qServiceCfg.cosXmlService.headBucket(headBucketRequest);
            Log.w("XIAO",headBucketResult.printHeaders());
            if(headBucketResult.getHttpCode() >= 300){
                Log.w("XIAO",headBucketResult.printError());
            }
            resultHelper.cosXmlResult = headBucketResult;
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
        headBucketRequest = new HeadBucketRequest();
        headBucketRequest.setBucket(qServiceCfg.getUserBucket());
        headBucketRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.headBucketAsync(headBucketRequest, new CosXmlResultListener() {
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
