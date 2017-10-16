package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.OptionObjectRequest;
import com.tencent.cos.xml.model.object.OptionObjectResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Options Object 接口实现 Object 跨域访问配置的预请求。即在发送跨域请求之前会发送一个 OPTIONS 请求并带上特定的来源域，
 * HTTP 方法和 HEADER 信息等给 COS，以决定是否可以发送真正的跨域请求。当 CORS 配置不存在时，请求返回 403 Forbidden。
 * 可以通过 Put Bucket CORS 接口来开启 Bucket 的 CORS 支持。
 *
 */
public class OptionObjectSample {
    OptionObjectRequest optionObjectRequest;
    QServiceCfg qServiceCfg;

    public OptionObjectSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getUploadCosPath();

        String origin = "http://www.qcloud.com";
        String method = "GET";
        optionObjectRequest = new OptionObjectRequest(bucket,cosPath,
                origin, method);

        optionObjectRequest.setAccessControlHeaders("host");
        optionObjectRequest.setSign(600,null,null);
        try {
            OptionObjectResult optionObjectResult =
                     qServiceCfg.cosXmlService.optionObject(optionObjectRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = optionObjectResult;
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
    public void startAsync(final Activity activity){
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getUploadCosPath();

        String origin = "http://www.qcloud.com";
        String method = "GET";
        optionObjectRequest = new OptionObjectRequest(bucket,cosPath,
                origin, method);

        optionObjectRequest.setAccessControlHeaders("host");
        optionObjectRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.optionObjectAsync(optionObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
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
    }
}
