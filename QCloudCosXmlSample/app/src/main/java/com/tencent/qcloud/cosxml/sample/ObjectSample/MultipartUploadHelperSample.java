package com.tencent.qcloud.cosxml.sample.ObjectSample;


import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;

import com.tencent.cos.xml.transfer.MultipartUploadService;
import com.tencent.qcloud.core.network.QCloudProgressListener;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


/**
 * Created by bradyxiao on 2017/6/5.
 * author bradyxiao
 *
 * 一个分块上传的帮助类
 *
 */
public class MultipartUploadHelperSample {
    MultipartUploadService multipartUploadHelper;
    QServiceCfg qServiceCfg;

    public MultipartUploadHelperSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start() {
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        String srcPath = qServiceCfg.getMultiUploadFileUrl();

        multipartUploadHelper = new MultipartUploadService(qServiceCfg.cosXmlService);

        multipartUploadHelper.setBucket(bucket);
        multipartUploadHelper.setCosPath(cosPath);
        multipartUploadHelper.setSliceSize(1024 * 1024);
        multipartUploadHelper.setSrcPath(srcPath);
        multipartUploadHelper.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0 / max);
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
            resultHelper.cosXmlResult = multipartUploadHelper.upload();
            Log.w("XIAO",resultHelper.cosXmlResult.accessUrl);
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

}
