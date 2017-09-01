package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.os.Handler;
import android.util.Log;

import com.tencent.cos.xml.common.ResumeData;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.MultipartUploadHelper;
import com.tencent.qcloud.cosxml.sample.ObjectDemoActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.QCloudProgressListener;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/5.
 * author bradyxiao
 *
 * 一个分块上传的帮助类
 *
 */
public class MultipartUploadHelperSample {
    MultipartUploadHelper multipartUploadHelper;
    QServiceCfg qServiceCfg;
    Handler handler;
    volatile boolean isAbort = false;

    volatile ResumeData cancelResult;
//    volatile AbortMultiUploadResult cancelResult;

    public MultipartUploadHelperSample(QServiceCfg qServiceCfg, Handler handler){
        this.qServiceCfg = qServiceCfg;
        this.handler = handler;
    }
    public ResultHelper start() {
        ResultHelper resultHelper = new ResultHelper();
        multipartUploadHelper = new MultipartUploadHelper(qServiceCfg.cosXmlService);
        multipartUploadHelper.setBucket(qServiceCfg.bucket);
        multipartUploadHelper.setCosPath(qServiceCfg.getMultiUploadCosPath());
        multipartUploadHelper.setSliceSize(1024 * 1024);
        multipartUploadHelper.setSrcPath(qServiceCfg.getMultiUploadFileUrl());
        multipartUploadHelper.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0 / max);
                handler.obtainMessage(0, (int) result).sendToTarget();
                Log.w("XIAO", "progress =" + (long) result + "%" + " ------------" + progress + "/" + max);
//                if(isAbort){
//                    Log.w("XIAO_RESUME","resume");
//                    Log.w("XIAO_RESUME", "progress =" + (long) result + "%" + " ------------" + progress + "/" + max);
//                }
//                if (result > 3.0f && !isAbort) {
//                    isAbort = true;
//                    cancelResult = multipartUploadHelper.cancel();
//                }
            }
        });
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(!isAbort);
//                cancelResult = multipartUploadHelper.cancel();
////                try {
////                    cancelResult = multipartUploadHelper.abort();
////                } catch (QCloudException e) {
////                    e.printStackTrace();
////                }
//            }
//        }).start();
        try {
            qServiceCfg.blockOtherUploadTask();
            resultHelper.cosXmlResult = multipartUploadHelper.upload();
            handler.sendEmptyMessage(1);
            qServiceCfg.releaseUploadBarrier();
            Log.w("XIAO",resultHelper.cosXmlResult.accessUrl);
            return resultHelper;
        } catch (QCloudException e) {
            Log.w("XIAO_NEW", "exception =" + e.getExceptionType() + "; " + e.getDetailMessage());
            resultHelper.exception = e;
//            while (cancelResult == null) ;
//            Log.w("XIAO_NEW", cancelResult.bucket + "|" + cancelResult.cosPath + "|" +
//                    cancelResult.uploadId + "|" + cancelResult.sliceSize + "|");
//            try {
//                resultHelper.cosXmlResult = multipartUploadHelper.resume(cancelResult);
//                Log.w("XIAO_NEW",resultHelper.cosXmlResult.printHeaders() + "|" + resultHelper.cosXmlResult.printBody() + "|" +
//                        resultHelper.cosXmlResult.printError());
//                return resultHelper;
//            } catch (Exception e1) {
//                e1.printStackTrace();
//                resultHelper.exception = (QCloudException) e1;
//            }
            return resultHelper;
        }
    }

    public void abort() {
        if (multipartUploadHelper != null) {
            multipartUploadHelper.abortAsync(new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                    qServiceCfg.releaseUploadBarrier();
                }

                @Override
                public void onFail(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                    qServiceCfg.releaseUploadBarrier();
                }
            });
        }
    }
}
