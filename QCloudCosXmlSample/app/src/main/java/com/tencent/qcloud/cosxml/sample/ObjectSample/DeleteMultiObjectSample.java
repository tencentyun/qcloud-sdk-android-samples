package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.DeleteMultiObjectRequest;
import com.tencent.cos.xml.model.object.DeleteMultiObjectResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 *
 * Delete Multiple Object 接口请求实现在指定 Bucket 中批量删除 Object，单次请求最大支持批量删除 1000 个 Object。对于响应结果，
 * COS 提供 Verbose 和 Quiet 两种模式：Verbose 模式将返回每个 Object 的删除结果；Quiet 模式只返回报错的 Object 信息。
 *
 */
public class DeleteMultiObjectSample {
    DeleteMultiObjectRequest deleteMultiObjectRequest;
    QServiceCfg qServiceCfg;

    public DeleteMultiObjectSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        deleteMultiObjectRequest = new DeleteMultiObjectRequest();
        deleteMultiObjectRequest.setBucket(qServiceCfg.bucket);
        deleteMultiObjectRequest.setQuiet(false);
        // 可以设置多个Object
        deleteMultiObjectRequest.setObjectList(qServiceCfg.uploadCosPath);
        deleteMultiObjectRequest.setSign(600,null,null);
        try {
            DeleteMultiObjectResult deleteMultiObjectResult =
                     qServiceCfg.cosXmlService.deleteMultiObject(deleteMultiObjectRequest);
            Log.w("XIAO",deleteMultiObjectResult.printHeaders());
            if(deleteMultiObjectResult.getHttpCode() >= 300){
                Log.w("XIAO",deleteMultiObjectResult.printError());
            } else {
                qServiceCfg.setUserObject(null);
            }
            resultHelper.cosXmlResult = deleteMultiObjectResult;
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
        deleteMultiObjectRequest = new DeleteMultiObjectRequest();
        deleteMultiObjectRequest.setBucket(qServiceCfg.bucket);
        deleteMultiObjectRequest.setQuiet(false);
        // 可以设置多个Object
        deleteMultiObjectRequest.setObjectList(qServiceCfg.uploadCosPath);
        deleteMultiObjectRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.deleteMultiObjectAsync(deleteMultiObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setUserObject(null);
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
