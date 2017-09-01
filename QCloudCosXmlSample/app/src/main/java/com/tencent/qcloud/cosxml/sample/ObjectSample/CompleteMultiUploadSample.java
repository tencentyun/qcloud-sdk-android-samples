package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.CompleteMultiUploadRequest;
import com.tencent.cos.xml.model.object.CompleteMultiUploadResult;
import com.tencent.qcloud.cosxml.sample.ProgressActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.MD5Utils;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 *
 * Complete Multipart Upload 接口请求用来实现完成整个分块上传。当使用 Upload Parts 上传完所有块以后，必须调用该 API 来完成整个文件的分块上传。
 * 在使用该 API 时，您必须在请求 Body 中给出每一个块的 PartNumber 和 ETag，用来校验块的准确性。
 * 由于分块上传完后需要合并，而合并需要数分钟时间，因而当合并分块开始的时候，COS 就立即返回 200 的状态码，
 * 在合并的过程中，COS 会周期性的返回空格信息来保持连接活跃，直到合并完成，COS会在 Body 中返回合并后块的内容。
 * 当上传块小于 1 MB 的时候，在调用该 API 时，会返回 400 EntityTooSmall；
 * 当上传块编号不连续的时候，在调用该 API 时，会返回 400 InvalidPart；
 * 当请求 Body 中的块信息没有按序号从小到大排列的时候，在调用该 API 时，会返回 400 InvalidPartOrder；
 * 当 UploadId 不存在的时候，在调用该 API 时，会返回 404 NoSuchUpload。
 *
 */
public class CompleteMultiUploadSample {
    CompleteMultiUploadRequest completeMultiUploadRequest;
    QServiceCfg qServiceCfg;

    public CompleteMultiUploadSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        completeMultiUploadRequest = new CompleteMultiUploadRequest();
        completeMultiUploadRequest.setBucket(qServiceCfg.bucket);
        completeMultiUploadRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        completeMultiUploadRequest.setUploadId(qServiceCfg.getCurrentUploadId());
        completeMultiUploadRequest.setPartNumberAndETag(1,MD5Utils.getMD5FromPath(qServiceCfg.getMultiUploadFileUrl()));
        completeMultiUploadRequest.setSign(600,null,null);
        try {
            CompleteMultiUploadResult completeMultiUploadResult =
                    qServiceCfg.cosXmlService.completeMultiUpload(completeMultiUploadRequest);
            Log.w("XIAO",completeMultiUploadResult.printHeaders());
            if(completeMultiUploadResult.getHttpCode() >= 300){
                Log.w("XIAO",completeMultiUploadResult.printError());
            }else{
                qServiceCfg.setCurrentUploadId(null);
                Log.w("XIAO","" + completeMultiUploadResult.printBody());
                Log.w("XIAO","accessUrl =" + completeMultiUploadResult.accessUrl);
            }
            resultHelper.cosXmlResult = completeMultiUploadResult;
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
    public void startAsync(final Activity activity){
        completeMultiUploadRequest = new CompleteMultiUploadRequest();
        completeMultiUploadRequest.setBucket(qServiceCfg.bucket);
        completeMultiUploadRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        completeMultiUploadRequest.setUploadId(qServiceCfg.getCurrentUploadId());
        completeMultiUploadRequest.setPartNumberAndETag(1,MD5Utils.getMD5FromPath(qServiceCfg.getMultiUploadFileUrl()));
        completeMultiUploadRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.completeMultiUploadAsync(completeMultiUploadRequest, new CosXmlResultListener() {
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
        if (activity instanceof ProgressActivity) {
            activity.finish();
        }
    }
}
