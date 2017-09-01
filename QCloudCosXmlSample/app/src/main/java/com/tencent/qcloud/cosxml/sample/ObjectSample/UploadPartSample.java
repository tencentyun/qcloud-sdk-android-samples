package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.UploadPartRequest;
import com.tencent.cos.xml.model.object.UploadPartResult;
import com.tencent.qcloud.cosxml.sample.ProgressActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.QCloudProgressListener;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Upload Part 接口请求实现在初始化以后的分块上传，支持的块的数量为1到10000，块的大小为1 MB 到5 GB。
 * 使用 Initiate Multipart Upload 接口初始化分片上传时会得到一个 uploadId，该 ID 不但唯一标识这一分块数据，也标识了这分块数据在整个文件内的相对位置。
 * 在每次请求 Upload Part 时候，需要携带 partNumber 和 uploadId，partNumber为块的编号，支持乱序上传。当传入 uploadId 和 partNumber 都相同的时候，
 * 后传入的块将覆盖之前传入的块。当 uploadId 不存在时会返回 404 错误，NoSuchUpload.
 *
 */
public class UploadPartSample {
    UploadPartRequest uploadPartRequest;
    QServiceCfg qServiceCfg;
    Handler handler;

    boolean isAbort;

    public UploadPartSample(QServiceCfg qServiceCfg, Handler handler){
        this.qServiceCfg = qServiceCfg;
        this.handler = handler;
    }

    public void abort() {
        isAbort = true;
        if (uploadPartRequest != null) {
            qServiceCfg.cosXmlService.cancel(uploadPartRequest);
        }
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        uploadPartRequest = new UploadPartRequest();
        uploadPartRequest.setBucket(qServiceCfg.bucket);
        uploadPartRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        uploadPartRequest.setUploadId(qServiceCfg.getCurrentUploadId());
        uploadPartRequest.setPartNumber(1);
        uploadPartRequest.setSign(600,null,null);
        uploadPartRequest.setSrcPath(qServiceCfg.getMultiUploadFileUrl());
        uploadPartRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
                handler.obtainMessage(0, (int) ((100.00 * progress / max))).sendToTarget();
            }
        });
        try {
            qServiceCfg.blockOtherUploadTask();
            UploadPartResult uploadPartResult =
                    qServiceCfg.cosXmlService.uploadPart(uploadPartRequest);
            Log.w("XIAO",uploadPartResult.printHeaders());
            if(uploadPartResult.getHttpCode() >= 300){
                Log.w("XIAO",uploadPartResult.printError());
            }else{
                Log.w("XIAO","etag= " + uploadPartResult.getETag());
            }
            handler.sendEmptyMessage(1);
            resultHelper.cosXmlResult = uploadPartResult;
            qServiceCfg.releaseUploadBarrier();
            return resultHelper;
        } catch (QCloudException e) {
            Log.w("XIAO", "exception =" + e.getExceptionType() + "; " + e.getDetailMessage());
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
        uploadPartRequest = new UploadPartRequest();
        uploadPartRequest.setBucket(qServiceCfg.bucket);
        uploadPartRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        uploadPartRequest.setUploadId(qServiceCfg.getCurrentUploadId());
        uploadPartRequest.setPartNumber(1);
        uploadPartRequest.setSign(600,null,null);
        uploadPartRequest.setSrcPath(qServiceCfg.getMultiUploadFileUrl());
        uploadPartRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
                handler.obtainMessage(0, (int) ((100.00 * progress / max))).sendToTarget();
            }
        });
        qServiceCfg.blockOtherUploadTask();
        qServiceCfg.cosXmlService.uploadPartAsync(uploadPartRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.releaseUploadBarrier();
                if (cosXmlResult.getHttpCode() < 300) {
                    handler.sendEmptyMessage(1);
                } else {
                    show(activity, stringBuilder.toString());
                }
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printError());
                Log.w("XIAO", "failed = " + stringBuilder.toString());
                qServiceCfg.releaseUploadBarrier();
                if( !isAbort) {
                    show(activity, stringBuilder.toString());
                }
            }
        });
    }

    private void show(Activity activity, String message){
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ResultActivity.class);
        intent.putExtra("RESULT", message);
        activity.startActivity(intent);
        if (activity instanceof ProgressActivity) {
            activity.finish();
        }
    }
}
