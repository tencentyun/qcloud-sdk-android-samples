package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.GetBucketACLRequest;
import com.tencent.cos.xml.model.bucket.GetBucketACLResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Get Bucket ACL 接口用来获取 Bucket 的 ACL(access control list)， 即用户空间（Bucket）的访问权限控制列表。 此 API 接口只有 Bucket 的持有者有权限操作。
 */
public class GetBucketACLSample {
    GetBucketACLRequest getBucketACLRequest;
    QServiceCfg qServiceCfg;

    public GetBucketACLSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        getBucketACLRequest = new GetBucketACLRequest();
        getBucketACLRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketACLRequest.setSign(600,null,null);
        try {
            GetBucketACLResult getBucketACLResult =
                    qServiceCfg.cosXmlService.getBucketACL(getBucketACLRequest);
            Log.w("XIAO",getBucketACLResult.printHeaders());
            if(getBucketACLResult.getHttpCode() >= 300){
                Log.w("XIAO",getBucketACLResult.printError());
            }
            resultHelper.cosXmlResult = getBucketACLResult;
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
        getBucketACLRequest = new GetBucketACLRequest();
        getBucketACLRequest.setBucket(qServiceCfg.getUserBucket());
        getBucketACLRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.getBucketACLAsync(getBucketACLRequest, new CosXmlResultListener() {
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
