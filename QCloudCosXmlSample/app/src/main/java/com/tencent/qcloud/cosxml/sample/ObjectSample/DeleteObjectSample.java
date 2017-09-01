package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.DeleteObjectRequest;
import com.tencent.cos.xml.model.object.DeleteObjectResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 *
 * Delete Object 接口请求可以在 COS 的 Bucket 中将一个文件（Object）删除。该操作需要请求者对 Bucket 有 WRITE 权限。
 *
 */
public class DeleteObjectSample {
    QServiceCfg qServiceCfg;
    DeleteObjectRequest deleteObjectRequest;
    public DeleteObjectSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        deleteObjectRequest = new DeleteObjectRequest();
        deleteObjectRequest.setBucket(qServiceCfg.bucket);
        deleteObjectRequest.setCosPath(qServiceCfg.uploadCosPath);
        deleteObjectRequest.setSign(600,null,null);
        try {
            DeleteObjectResult deleteObjectResult =
                    qServiceCfg.cosXmlService.deleteObject(deleteObjectRequest);
            Log.w("XIAO",deleteObjectResult.printHeaders());
            if(deleteObjectResult.getHttpCode() >= 300){
                Log.w("XIAO",deleteObjectResult.printError());
            } else {
                qServiceCfg.setUserObject(null);
            }
            resultHelper.cosXmlResult = deleteObjectResult;
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
        deleteObjectRequest = new DeleteObjectRequest();
        deleteObjectRequest.setBucket(qServiceCfg.bucket);
        deleteObjectRequest.setCosPath(qServiceCfg.uploadCosPath);
        deleteObjectRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.deleteObjectAsync(deleteObjectRequest, new CosXmlResultListener() {
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
