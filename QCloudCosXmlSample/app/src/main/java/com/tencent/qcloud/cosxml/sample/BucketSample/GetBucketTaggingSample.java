package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.GetBucketTaggingRequest;
import com.tencent.cos.xml.model.bucket.GetBucketTaggingResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 */
public class GetBucketTaggingSample {
    GetBucketTaggingRequest getBucketTaggingRequest;
    QServiceCfg qServiceCfg;

    public GetBucketTaggingSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        getBucketTaggingRequest = new GetBucketTaggingRequest();
        getBucketTaggingRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketTaggingRequest.setSign(600,null,null);
        try {
            GetBucketTaggingResult getBucketTaggingResult =
                   qServiceCfg.cosXmlService.getBucketTagging(getBucketTaggingRequest);
            Log.w("XIAO",getBucketTaggingResult.printHeaders());
            if(getBucketTaggingResult.getHttpCode() >= 300){
                Log.w("XIAO",getBucketTaggingResult.printError());
            }
            resultHelper.cosXmlResult = getBucketTaggingResult;
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
        getBucketTaggingRequest = new GetBucketTaggingRequest();
        getBucketTaggingRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketTaggingRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.getBucketTaggingAsync(getBucketTaggingRequest, new CosXmlResultListener() {
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
