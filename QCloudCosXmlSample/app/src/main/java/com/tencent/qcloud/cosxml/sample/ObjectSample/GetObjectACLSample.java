package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.GetObjectACLRequest;
import com.tencent.cos.xml.model.object.GetObjectACLResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 *
 * Get Object ACL 接口用来获取某个 Bucket 下的某个 Object 的访问权限。只有 Bucket 的持有者才有权限操作。
 *
 */
public class GetObjectACLSample {
    QServiceCfg qServiceCfg;
    GetObjectACLRequest getObjectACLRequest;
    public GetObjectACLSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        getObjectACLRequest = new GetObjectACLRequest();
        getObjectACLRequest.setBucket(qServiceCfg.bucket);
        getObjectACLRequest.setCosPath(qServiceCfg.sampleCosPath);
        getObjectACLRequest.setSign(600,null,null);
        try {
            GetObjectACLResult getObjectACLResult =
                   qServiceCfg.cosXmlService.getObjectACL(getObjectACLRequest);
            Log.w("XIAO",getObjectACLResult.printHeaders());
            if(getObjectACLResult.getHttpCode() >= 300){
                Log.w("XIAO",getObjectACLResult.printError());
            }else{
                Log.w("XIAO",getObjectACLResult.printBody());
            }
            resultHelper.cosXmlResult = getObjectACLResult;
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
        getObjectACLRequest = new GetObjectACLRequest();
        getObjectACLRequest.setBucket(qServiceCfg.bucket);
        getObjectACLRequest.setCosPath(qServiceCfg.sampleCosPath);
        getObjectACLRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.getObjectACLAsync(getObjectACLRequest, new CosXmlResultListener() {
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
