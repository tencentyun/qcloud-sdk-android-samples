package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.InitMultipartUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadResult;
import com.tencent.qcloud.cosxml.sample.ObjectDemoActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Initiate Multipart Upload 接口请求实现初始化分片上传，成功执行此请求以后会返回 UploadId 用于后续的 Upload Part 请求。
 *
 */
public class InitMultipartUploadSample {
    InitMultipartUploadRequest initMultipartUploadRequest;
    QServiceCfg qServiceCfg;

    public InitMultipartUploadSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        initMultipartUploadRequest = new InitMultipartUploadRequest();
        initMultipartUploadRequest.setBucket(qServiceCfg.bucket);
        initMultipartUploadRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        initMultipartUploadRequest.setSign(600,null,null);
        try {
            InitMultipartUploadResult initMultipartUploadResult =
                  qServiceCfg.cosXmlService.initMultipartUpload(initMultipartUploadRequest);
            Log.w("XIAO",initMultipartUploadResult.printHeaders());
            if(initMultipartUploadResult.getHttpCode() >= 300){
                Log.w("XIAO",initMultipartUploadResult.error.toString());
            }else {
                Log.w("XIAO","" + initMultipartUploadResult.printBody());
                Log.w("XIAO","uploadId =" + initMultipartUploadResult.initMultipartUpload.uploadId);
                qServiceCfg.setCurrentUploadId(initMultipartUploadResult.initMultipartUpload.uploadId);
            }
            resultHelper.cosXmlResult = initMultipartUploadResult;
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
    public void startAsync(final ObjectDemoActivity activity){
        initMultipartUploadRequest = new InitMultipartUploadRequest();
        initMultipartUploadRequest.setBucket(qServiceCfg.bucket);
        initMultipartUploadRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        initMultipartUploadRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.initMultipartUploadAsync(initMultipartUploadRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setCurrentUploadId(((InitMultipartUploadResult) cosXmlResult).initMultipartUpload.uploadId);
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
