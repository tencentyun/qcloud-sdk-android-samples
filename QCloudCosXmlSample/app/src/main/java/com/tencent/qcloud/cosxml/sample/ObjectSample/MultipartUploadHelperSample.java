package com.tencent.qcloud.cosxml.sample.ObjectSample;


import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;

import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.transfer.UploadService;
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
    UploadService uploadService;
    QServiceCfg qServiceCfg;

    public MultipartUploadHelperSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start() {
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        String srcPath = qServiceCfg.getMultiUploadFileUrl();

        UploadService.ResumeData resumeData = new UploadService.ResumeData();
        resumeData.bucket = bucket;
        resumeData.cosPath = cosPath;
        resumeData.sliceSize = 1024 * 1024;
        resumeData.srcPath = srcPath;
        resumeData.uploadId = null;
        uploadService = new UploadService(qServiceCfg.cosXmlService, resumeData);
        uploadService.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0 / max);
                Log.w("XIAO", "progress =" + (long) result + "%" + " ------------" + progress + "/" + max);
            }
        });
        try {
            resultHelper.cosXmlResult = uploadService.upload();
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
