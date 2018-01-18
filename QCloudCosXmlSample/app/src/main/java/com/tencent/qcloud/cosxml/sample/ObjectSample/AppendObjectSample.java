package com.tencent.qcloud.cosxml.sample.ObjectSample;


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.AppendObjectRequest;
import com.tencent.cos.xml.model.object.AppendObjectResult;
import com.tencent.cos.xml.model.object.HeadObjectRequest;
import com.tencent.cos.xml.model.object.HeadObjectResult;
import com.tencent.qcloud.core.common.QCloudProgressListener;
import com.tencent.qcloud.cosxml.sample.ProgressActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

import java.util.List;

/**
 * Created by bradyxiao on 2017/5/31.
 * author bradyxiao
 *
 * Append Object 接口请求可以将一个 Object（文件）以分块追加的方式上传至指定 Bucket 中。Object 属性为 Appendable 时，才能使用 Append Object 接口上传。
 * Object 属性可以在 Head Object 操作中查询到，发起 Head Object 请求时，会返回自定义 Header 的『x-cos-object-type』，
 * 该 Header 只有两个枚举值：Normal 或者 Appendable。通过 Append Object 操作创建的 Object 类型为 Appendable 文件；通过 Put Object 上传的 Object 是 Normal 文件。
 * 当 Appendable 的 Object 被执行 Put Object 的请求操作以后，原 Object 被覆盖，属性改变为 Normal 。
 * 追加上传的 Object 建议大小 1M-5G。如果 Position 的值和当前 Object 的长度不致，COS 会返回 409 错误。如果 Append 一个 Normal 属性的文件，COS 会返回 409 ObjectNotAppendable。
 *
 */
public class AppendObjectSample {
    AppendObjectRequest appendObjectRequest;
    QServiceCfg qServiceCfg;
    public AppendObjectSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getAppendCosPath();
        String srcPath = qServiceCfg.getAppendUploadFileUrl();
        long position = hasAlreadyExist(bucket, cosPath);
        appendObjectRequest = new AppendObjectRequest(bucket, cosPath,
                srcPath, position);
        appendObjectRequest.setSign(600,null,null);
        appendObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
            }
        });
        try {
            AppendObjectResult appendObjectResult =
                   qServiceCfg.cosXmlService.appendObject(appendObjectRequest);
            resultHelper.cosXmlResult = appendObjectResult;
            Log.w("XIAO","success");
            return resultHelper;
        }  catch (CosXmlClientException e) {
            Log.w("XIAO","QCloudException =" + e.getMessage());
            resultHelper.qCloudException = e;
            return resultHelper;
        } catch (CosXmlServiceException e) {
            Log.w("XIAO","QCloudServiceException =" + e.toString());
            resultHelper.qCloudServiceException = e;
            return resultHelper;
        }
    }

    /** 获取已上传的部分长度： Head Object API */
    private long hasAlreadyExist(String bucket, String cosPath){
        long appendLength = 0L;
        HeadObjectRequest headObjectRequest = new HeadObjectRequest(bucket, cosPath);
        headObjectRequest.setSign(600,null,null);
        headObjectRequest = new HeadObjectRequest(bucket, cosPath);
        headObjectRequest.setSign(600,null,null);
        try {
            HeadObjectResult headObjectResult =
                    qServiceCfg.cosXmlService.headObject(headObjectRequest);
            List<String> resultHeader = headObjectResult.headers.get("Content-Length");
            if(resultHeader != null && resultHeader.size() > 0){
                appendLength = Long.parseLong(resultHeader.get(0));
            }
        } catch (CosXmlClientException e) {

        } catch (CosXmlServiceException e) {
        }
        return appendLength;
    }

    public void startAsync(final Activity activity) {
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getAppendCosPath();
        String srcPath = qServiceCfg.getAppendUploadFileUrl();
        hasAlreadyExist(activity, bucket, cosPath, srcPath);
    }

    private void hasAlreadyExist(final Activity activity, final String bucket, final String cosPath, final String srcPath){

        HeadObjectRequest headObjectRequest = new HeadObjectRequest(bucket, cosPath);
        // headObjectRequest.setIfModifiedSince("");
        headObjectRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.headObjectAsync(headObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                long appendLength = 0L;
                List<String> resultHeader = cosXmlResult.headers.get("Content-Length");
                if(resultHeader != null && resultHeader.size() > 0){
                    appendLength = Long.parseLong(resultHeader.get(0));
                }
                doAsync(activity, bucket, cosPath, srcPath, appendLength);
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException, CosXmlServiceException qcloudServiceException) {
                doAsync(activity, bucket, cosPath, srcPath, 0L);
            }
        });
    }

    private void doAsync(final Activity activity, String bucket, String cosPath, String srcPath, long appendLength){
        appendObjectRequest = new AppendObjectRequest(bucket, cosPath,
                srcPath, appendLength);
        appendObjectRequest.setSign(600,null,null);
        appendObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
            }
        });
        qServiceCfg.cosXmlService.appendObjectAsync(appendObjectRequest, new CosXmlResultListener() {
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
