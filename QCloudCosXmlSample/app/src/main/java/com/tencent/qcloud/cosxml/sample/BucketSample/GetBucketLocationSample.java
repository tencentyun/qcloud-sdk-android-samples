package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.GetBucketLocationRequest;
import com.tencent.cos.xml.model.bucket.GetBucketLocationResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Get Bucket Location 接口用于获取 Bucket 所在的地域信息，该 GET 操作使用 location 子资源返回 Bucket 所在的区域，只有 Bucket 持有者才有该 API 接口的操作权限。
 */
public class GetBucketLocationSample {
    GetBucketLocationRequest getBucketLocationRequest;
    QServiceCfg qServiceCfg;
    public GetBucketLocationSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        getBucketLocationRequest = new GetBucketLocationRequest();
        getBucketLocationRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketLocationRequest.setSign(600,null,null);
        try {
            GetBucketLocationResult getBucketLocationResult =
                    qServiceCfg.cosXmlService.getBucketLocation(getBucketLocationRequest);
            Log.w("XIAO",getBucketLocationResult.printHeaders());
            if(getBucketLocationResult.getHttpCode() >= 300){
                Log.w("XIAO",getBucketLocationResult.printError());
            }else{
                Log.w("XIAO","" + getBucketLocationResult.printBody());
            }
            resultHelper.cosXmlResult = getBucketLocationResult;
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
        getBucketLocationRequest = new GetBucketLocationRequest();
        getBucketLocationRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketLocationRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.getBucketLocationAsync(getBucketLocationRequest, new CosXmlResultListener() {
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
