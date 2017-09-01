package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.DeleteBucketTaggingRequest;
import com.tencent.cos.xml.model.bucket.DeleteBucketTaggingResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 */
public class DeleteBucketTaggingSample {
    DeleteBucketTaggingRequest deleteBucketTaggingRequest;
    QServiceCfg qServiceCfg;

    public DeleteBucketTaggingSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        deleteBucketTaggingRequest = new DeleteBucketTaggingRequest();
        deleteBucketTaggingRequest.setBucket(qServiceCfg.getUserBucket());
        deleteBucketTaggingRequest.setSign(600,null,null);

        try {
            DeleteBucketTaggingResult deleteBucketTaggingResult =
                    qServiceCfg.cosXmlService.deleteBucketTagging(deleteBucketTaggingRequest);
            Log.w("XIAO",deleteBucketTaggingResult.printHeaders());
            if(deleteBucketTaggingResult.getHttpCode() >= 300){
                Log.w("XIAO",deleteBucketTaggingResult.printError());
            }
            resultHelper.cosXmlResult = deleteBucketTaggingResult;
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
        deleteBucketTaggingRequest = new DeleteBucketTaggingRequest();
        deleteBucketTaggingRequest.setBucket(qServiceCfg.getUserBucket());
        deleteBucketTaggingRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.deleteBucketTaggingAsync(deleteBucketTaggingRequest, new CosXmlResultListener() {
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
