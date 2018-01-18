package com.tencent.qcloud.cosxml.sample.ObjectSample;


import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.CompleteMultiUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadResult;
import com.tencent.cos.xml.model.object.UploadPartRequest;
import com.tencent.cos.xml.model.object.UploadPartResult;
import com.tencent.qcloud.cosxml.sample.ProgressActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

/**
 * Created by bradyxiao on 2017/9/28.
 */

public class MultiUploadSample {
    /**
     *  step1: init multipartUpload, then get uploadId.
     *  step2: uploadPart and get each part's eTag for loop.
     *  step3: complete multiUpload.
     *  notice: between step1 and step2, you can invoke list part api to check what parts has been upload.
     */
    QServiceCfg qServiceCfg;
    public MultiUploadSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start(){
        ResultHelper resultHelper;
        qServiceCfg.getPartNumberAndEtag().clear();
        qServiceCfg.setCurrentUploadId(null);

        // step1: init
        InitMultipartUploadSample initMultipartUploadSample = new InitMultipartUploadSample(qServiceCfg);
        resultHelper = initMultipartUploadSample.start();
        if(resultHelper.cosXmlResult == null){
            Log.d("XIAO", "init multipartUpload failed");
            return resultHelper;
        }

        // check parts has been upload.
        ListPartsSample listPartsSample = new ListPartsSample(qServiceCfg);
        resultHelper = listPartsSample.start();
        if(resultHelper.cosXmlResult == null){
            Log.d("XIAO", "list part failed");
            return resultHelper;
        }

        // step2: upload part
        int partNumber = 1; //此处只演示一个分片， 编号必须从 1开始
        UploadPartSample uploadPartSample = new UploadPartSample(qServiceCfg, partNumber);
        resultHelper = uploadPartSample.start();
        if(resultHelper.cosXmlResult == null){
            Log.d("XIAO", "upload part failed");
            return resultHelper;
        }

        //step3: complete multiUpload
        CompleteMultiUploadSample completeMultiUploadSample = new CompleteMultiUploadSample(qServiceCfg);
        resultHelper = completeMultiUploadSample.start();
        return resultHelper;
    }

    public void startAsync(final Activity activity){
        qServiceCfg.getPartNumberAndEtag().clear();
        qServiceCfg.setCurrentUploadId(null);

        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        InitMultipartUploadRequest initMultipartUploadRequest = new InitMultipartUploadRequest(bucket, cosPath);
        initMultipartUploadRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.initMultipartUploadAsync(initMultipartUploadRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printResult());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setCurrentUploadId(((InitMultipartUploadResult)cosXmlResult).initMultipartUpload.uploadId);

                // step2
                int partNumber = 1; //此处只演示一个分片， 编号必须从 1开始
                uploadPart(activity, partNumber);
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

    private void uploadPart(final Activity activity, final int partNumber){
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        String srcPath = qServiceCfg.getMultiUploadFileUrl();
        String uploadId = qServiceCfg.getCurrentUploadId();

        UploadPartRequest uploadPartRequest = new UploadPartRequest(bucket,cosPath,
                partNumber, srcPath, uploadId);

        uploadPartRequest.setSign(600,null,null);
        uploadPartRequest.setSrcPath(qServiceCfg.getMultiUploadFileUrl());
        uploadPartRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
               // Log.w("XIAO","progress =" + progress * 1.0/max);
            }
        });
        qServiceCfg.cosXmlService.uploadPartAsync(uploadPartRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printResult());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setPartNumberAndEtag(partNumber, ((UploadPartResult)cosXmlResult).eTag);

                // step3
                completeUpload(activity);
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

    private void completeUpload(final Activity activity){
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        String uploadId = qServiceCfg.getCurrentUploadId();

        CompleteMultiUploadRequest completeMultiUploadRequest = new CompleteMultiUploadRequest(bucket, cosPath,
                uploadId, qServiceCfg.getPartNumberAndEtag());
        completeMultiUploadRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.completeMultiUploadAsync(completeMultiUploadRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printResult());
                Log.w("XIAO", "success = " + stringBuilder.toString());
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
