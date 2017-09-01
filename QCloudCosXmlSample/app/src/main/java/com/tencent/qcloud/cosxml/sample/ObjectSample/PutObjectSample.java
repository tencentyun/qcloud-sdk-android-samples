package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.QCloudProgressListener;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 * <p>
 * Put Object 接口请求可以将本地的文件（Object）上传至指定 Bucket 中。该操作需要请求者对 Bucket 有 WRITE 权限。
 */
public class PutObjectSample {
    PutObjectRequest putObjectRequest;
    QServiceCfg qServiceCfg;

    volatile boolean isCancel = false;

    public PutObjectSample(QServiceCfg qServiceCfg) {
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start() {
        ResultHelper resultHelper = new ResultHelper();
        putObjectRequest = new PutObjectRequest();
        putObjectRequest.setBucket(qServiceCfg.bucket);
        putObjectRequest.setCosPath(qServiceCfg.uploadCosPath);
        putObjectRequest.setSrcPath(qServiceCfg.getUploadFileUrl());

        putObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0 / max);
                Log.w("XIAO", "progress =" + (long) result + "%" + " ------------" + progress + "/" + max);
            }
        });
        putObjectRequest.setSign(600, null, null);


        try {
            final PutObjectResult putObjectResult =
                    qServiceCfg.cosXmlService.putObject(putObjectRequest);

            Log.w("XIAO", putObjectResult.printHeaders());
            if (putObjectResult.getHttpCode() >= 300) {
                Log.w("XIAO", putObjectResult.printError());
                StringBuilder stringBuilder = new StringBuilder("Error\n");
                stringBuilder.append(putObjectResult.error.code)
                        .append(putObjectResult.error.message)
                        .append(putObjectResult.error.resource)
                        .append(putObjectResult.error.requestId)
                        .append(putObjectResult.error.traceId);
                Log.w("TEST", stringBuilder.toString());
            } else {
                Log.w("TEST", putObjectResult.accessUrl);
                qServiceCfg.setUserObject(qServiceCfg.uploadCosPath);
            }
            resultHelper.cosXmlResult = putObjectResult;
            return resultHelper;
        } catch (QCloudException e) {
            Log.w("XIAO", "exception =" + e.getExceptionType() + "; " + e.getDetailMessage());
            resultHelper.exception = e;
            return resultHelper;
        }
    }

    /**
     * 采用异步回调操作
     */
    public void startAsync(final Activity activity) {
        putObjectRequest = new PutObjectRequest();
        putObjectRequest.setBucket(qServiceCfg.bucket);
        putObjectRequest.setCosPath(qServiceCfg.uploadCosPath);
        putObjectRequest.setSrcPath(qServiceCfg.getUploadFileUrl());
        //putObjectRequest.setXCOSContentSha1(SHA1Utils.getSHA1FromPath(Environment.getExternalStorageDirectory().getPath() + "/test1.jpg"));

        putObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0 / max);
                Log.w("XIAO", "progress =" + (long) result + "%" + " ------------" + progress + "/" + max);
            }
        });
        putObjectRequest.setSign(600, null, null);
        qServiceCfg.cosXmlService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setUserObject(qServiceCfg.uploadCosPath);
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

    private void show(Activity activity, String message) {
        Intent intent = new Intent(activity, ResultActivity.class);
        intent.putExtra("RESULT", message);
        activity.startActivity(intent);
    }
}
