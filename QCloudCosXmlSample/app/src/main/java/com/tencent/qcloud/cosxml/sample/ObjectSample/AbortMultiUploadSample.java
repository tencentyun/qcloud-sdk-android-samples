package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.AbortMultiUploadRequest;
import com.tencent.cos.xml.model.object.AbortMultiUploadResult;
import com.tencent.qcloud.cosxml.sample.ProgressActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 *
 * Abort Multipart Upload 用来实现舍弃一个分块上传并删除已上传的块。
 * 当您调用 Abort Multipart Upload 时，如果有正在使用这个 Upload Parts 上传块的请求，
 * 则 Upload Parts 会返回失败。当该 UploadId 不存在时，会返回 404 NoSuchUpload。
 *
 */
public class AbortMultiUploadSample {
    AbortMultiUploadRequest abortMultiUploadRequest;
    QServiceCfg qServiceCfg;
    public AbortMultiUploadSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        String uploadId = qServiceCfg.getCurrentUploadId();

        abortMultiUploadRequest = new AbortMultiUploadRequest(bucket, cosPath,
                uploadId);
        abortMultiUploadRequest.setSign(600,null,null);
        try {
            AbortMultiUploadResult abortMultiUploadResult =
                 qServiceCfg.cosXmlService.abortMultiUpload(abortMultiUploadRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = abortMultiUploadResult;
            return resultHelper;
        } catch (CosXmlClientException e) {
            Log.w("XIAO","QCloudException =" + e.getMessage());
            resultHelper.qCloudException = e;
            return resultHelper;
        } catch (CosXmlServiceException e) {
            Log.w("XIAO","QCloudServiceException =" + e.toString());
            resultHelper.qCloudServiceException = e;
            return resultHelper;
        }
    }

    /**
     *
     * 采用异步回调操作
     *
     */
    public void startAsync(final Activity activity){
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        String uploadId = qServiceCfg.getCurrentUploadId();

        abortMultiUploadRequest = new AbortMultiUploadRequest(bucket, cosPath,
                uploadId);
        abortMultiUploadRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.abortMultiUploadAsync(abortMultiUploadRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setCurrentUploadId(null);
                show(activity, stringBuilder.toString());
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException, CosXmlServiceException qcloudServiceException) {
                StringBuilder stringBuilder = new StringBuilder();
                if(qcloudException != null){
                    stringBuilder.append(qcloudException.getMessage());
                }else {
                    stringBuilder.append(qcloudServiceException.toString());
                }
                Log.w("XIAO", "failed = " + stringBuilder.toString());
                show(activity, stringBuilder.toString());
            }
        });
    }

    private void show(Activity activity, String message){
        Intent intent = new Intent(activity, ResultActivity.class);
        intent.putExtra("RESULT", message);
        activity.startActivity(intent);
        if (activity instanceof ProgressActivity) {
            activity.finish();
        }
    }
}
