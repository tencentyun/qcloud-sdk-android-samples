package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.InitMultipartUploadRequest;
import com.tencent.cos.xml.model.object.InitMultipartUploadResult;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

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
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();


        initMultipartUploadRequest = new InitMultipartUploadRequest(bucket, cosPath);
        initMultipartUploadRequest.setSign(600,null,null);
        try {
            InitMultipartUploadResult initMultipartUploadResult =
                  qServiceCfg.cosXmlService.initMultipartUpload(initMultipartUploadRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = initMultipartUploadResult;
            qServiceCfg.setCurrentUploadId(initMultipartUploadResult.initMultipartUpload.uploadId);
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
    public void startAsync(){
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();

        initMultipartUploadRequest = new InitMultipartUploadRequest(bucket, cosPath);
        initMultipartUploadRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.initMultipartUploadAsync(initMultipartUploadRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
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
            }
        });
    }

}

