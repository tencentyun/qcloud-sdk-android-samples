package com.tencent.qcloud.cosxml.sample.ObjectSample;


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.AppendObjectRequest;
import com.tencent.cos.xml.model.object.AppendObjectResult;
import com.tencent.cos.xml.model.object.HeadObjectRequest;
import com.tencent.cos.xml.model.object.HeadObjectResult;
import com.tencent.qcloud.cosxml.sample.ProgressActivity;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.QCloudProgressListener;
import com.tencent.qcloud.network.exception.QCloudException;

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
    Handler handler;
    public AppendObjectSample(QServiceCfg qServiceCfg, Handler handler){
        this.qServiceCfg = qServiceCfg;
        this.handler = handler;
    }
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        appendObjectRequest = new AppendObjectRequest();
        appendObjectRequest.setBucket(qServiceCfg.bucket);
        appendObjectRequest.setCosPath(qServiceCfg.getAppendCosPath());
        appendObjectRequest.setPosition(0);
        appendObjectRequest.setSign(600,null,null);
        appendObjectRequest.setSrcPath(qServiceCfg.getAppendFileUrl());
        //appendObjectRequest.setData(new String("this is a append object test by data").getBytes());
        appendObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
            }
        });
        try {
            AppendObjectResult appendObjectResult =
                   qServiceCfg.cosXmlService.appendObject(appendObjectRequest);
            resultHelper.cosXmlResult = appendObjectResult;
            Log.w("XIAO",appendObjectResult.printHeaders());
            if(appendObjectResult.getHttpCode() >= 300){
                Log.w("XIAO",appendObjectResult.printError());
            }
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
        HeadObjectRequest headObjectRequest = new HeadObjectRequest();
        headObjectRequest.setCosPath(qServiceCfg.getAppendCosPath());
        headObjectRequest.setBucket(qServiceCfg.bucket);
        headObjectRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.headObjectAsync(headObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                List<String> resultHeader = cosXmlResult.getHeaders().get("Content-Length");
                if(resultHeader != null && resultHeader.size() > 0) {
                    long contentLength = Long.parseLong(resultHeader.get(0));
                    append(activity, contentLength);
                } else {
                    show(activity, "获取文件长度失败");
                }
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printError());
                Log.w("XIAO", "failed = " + stringBuilder.toString());
                if (cosXmlResult.getHttpCode() == 404) {
                    // 第一次上传
                    append(activity, 0);
                } else {
                    show(activity, "获取文件长度失败\n\n" + stringBuilder.toString());
                }
            }
        });
    }

    private void append(final Activity activity, long contentLength) {
        appendObjectRequest = new AppendObjectRequest();
        appendObjectRequest.setBucket(qServiceCfg.bucket);
        appendObjectRequest.setCosPath(qServiceCfg.getAppendCosPath());
        appendObjectRequest.setPosition(contentLength);
        appendObjectRequest.setSign(600,null,null);
        appendObjectRequest.setSrcPath(qServiceCfg.getAppendFileUrl());
        //appendObjectRequest.setData(new String("this is a append object test by data").getBytes());
        appendObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                Log.w("XIAO","progress =" + progress * 1.0/max);
                handler.obtainMessage(0, (int) ((100.00 * progress / max))).sendToTarget();
            }
        });
        qServiceCfg.cosXmlService.appendObjectAsync(appendObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                handler.sendEmptyMessage(1);
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
