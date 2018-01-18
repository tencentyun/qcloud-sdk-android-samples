package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.UploadPartRequest;
import com.tencent.cos.xml.model.object.UploadPartResult;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


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
    int partNumber;

    public UploadPartSample(QServiceCfg qServiceCfg, int partNumber){
        this.qServiceCfg = qServiceCfg;
        this.partNumber = partNumber;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getMultiUploadCosPath();
        String srcPath = qServiceCfg.getMultiUploadFileUrl();
        String uploadId = qServiceCfg.getCurrentUploadId();

        uploadPartRequest = new UploadPartRequest(bucket,cosPath,
                partNumber, srcPath, uploadId);
        uploadPartRequest.setSign(600,null,null);

        uploadPartRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
            }
        });
        try {
            UploadPartResult uploadPartResult =
                    qServiceCfg.cosXmlService.uploadPart(uploadPartRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = uploadPartResult;
            qServiceCfg.setPartNumberAndEtag(partNumber, uploadPartResult.eTag);
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
        String srcPath = qServiceCfg.getMultiUploadFileUrl();
        String uploadId = qServiceCfg.getCurrentUploadId();

        uploadPartRequest = new UploadPartRequest(bucket,cosPath,
                partNumber, srcPath, uploadId);

        uploadPartRequest.setSign(600,null,null);
        uploadPartRequest.setSrcPath(qServiceCfg.getMultiUploadFileUrl());
        uploadPartRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
            }
        });
        qServiceCfg.cosXmlService.uploadPartAsync(uploadPartRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printResult());
                Log.w("XIAO", "success = " + stringBuilder.toString());

            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException, CosXmlServiceException qcloudServiceException) {
                StringBuilder stringBuilder = new StringBuilder();
                if(qcloudException != null){
                    stringBuilder.append(qcloudException.getMessage());
                }else {
                    stringBuilder.append(qcloudServiceException.getMessage());
                }
                Log.w("XIAO", "failed = " + stringBuilder.toString());
            }
        });
    }

}
