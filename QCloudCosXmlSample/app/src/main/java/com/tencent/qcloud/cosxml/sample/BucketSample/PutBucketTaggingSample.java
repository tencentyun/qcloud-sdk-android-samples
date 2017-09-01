package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.PutBucketTaggingRequest;
import com.tencent.cos.xml.model.bucket.PutBucketTaggingResult;
import com.tencent.cos.xml.model.tag.Tag;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 */
public class PutBucketTaggingSample {
    PutBucketTaggingRequest putBucketTaggingRequest;
    QServiceCfg qServiceCfg;

    public PutBucketTaggingSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        putBucketTaggingRequest = new PutBucketTaggingRequest();
        putBucketTaggingRequest.setBucket(qServiceCfg.getUserBucket());
        Tag tag = new Tag();
        tag.key = "1";
        tag.value = "value_1";
        putBucketTaggingRequest.setTagList(tag);
        Tag tag2 = new Tag();
        tag2.key = "2";
        tag2.value = "value_2";
        putBucketTaggingRequest.setTagList(tag2);
        putBucketTaggingRequest.setSign(600,null,null);
        try {
            PutBucketTaggingResult putBucketTaggingResult =
                 qServiceCfg.cosXmlService.putBucketTagging(putBucketTaggingRequest);
            Log.w("XIAO",putBucketTaggingResult.printHeaders());
            if(putBucketTaggingResult.getHttpCode() >= 300){
                Log.w("XIAO",putBucketTaggingResult.printError());
            }
            resultHelper.cosXmlResult = putBucketTaggingResult;
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
        putBucketTaggingRequest = new PutBucketTaggingRequest();
        putBucketTaggingRequest.setBucket(qServiceCfg.getUserBucket());
        Tag tag = new Tag();
        tag.key = "1";
        tag.value = "value_1";
        putBucketTaggingRequest.setTagList(tag);
        Tag tag2 = new Tag();
        tag2.key = "2";
        tag2.value = "value_2";
        putBucketTaggingRequest.setTagList(tag2);
        putBucketTaggingRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.putBucketTaggingAsync(putBucketTaggingRequest, new CosXmlResultListener() {
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
