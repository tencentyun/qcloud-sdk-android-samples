package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.OptionObjectRequest;
import com.tencent.cos.xml.model.object.OptionObjectResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

import java.util.List;

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
        optionObjectRequest = new OptionObjectRequest();
        optionObjectRequest.setBucket(qServiceCfg.bucket);
        optionObjectRequest.setCosPath(qServiceCfg.sampleCosPath);
        optionObjectRequest.setOrigin("http://www.qcloud.com");
        optionObjectRequest.setAccessControlMethod("get");
        optionObjectRequest.setAccessControlHeaders("host");
        optionObjectRequest.setSign(600,null,null);
        try {
            OptionObjectResult optionObjectResult =
                     qServiceCfg.cosXmlService.optionObject(optionObjectRequest);
            Log.w("XIAO",optionObjectResult.printHeaders());
            if(optionObjectResult.getHttpCode() >= 300){
                Log.w("XIAO",optionObjectResult.printError());
            }else{
                StringBuilder stringBuilder = new StringBuilder();
                List<String> accessControlAllowMethods = optionObjectResult.getAccessControlAllowMethods();
                List<String> accessControlAllowHeaders = optionObjectResult.getAccessControlAllowHeaders();
                List<String> accessControlExposeHeaders = optionObjectResult.getAccessControlExposeHeaders();
                if(accessControlAllowHeaders != null){
                    int size = accessControlAllowHeaders.size();
                    for(int i = 0; i < size -1; ++ i){
                        stringBuilder.append(accessControlAllowHeaders.get(i)).append(",");
                    }
                    stringBuilder.append(accessControlAllowHeaders.get(size -1)).append("\n");
                }
                if(accessControlAllowMethods != null){
                    int size = accessControlAllowMethods.size();
                    for(int i = 0; i < size -1; ++ i){
                        stringBuilder.append(accessControlAllowMethods.get(i)).append(",");
                    }
                    stringBuilder.append(accessControlAllowMethods.get(size -1)).append("\n");
                }
                if(accessControlExposeHeaders != null){
                    int size = accessControlExposeHeaders.size();
                    for(int i = 0; i < size -1; ++ i){
                        stringBuilder.append(accessControlExposeHeaders.get(i)).append(",");
                    }
                    stringBuilder.append(accessControlExposeHeaders.get(size -1)).append("\n");
                }
                Log.w("XIAO", stringBuilder.toString());
            }
            resultHelper.cosXmlResult = optionObjectResult;
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
        optionObjectRequest = new OptionObjectRequest();
        optionObjectRequest.setBucket(qServiceCfg.bucket);
        optionObjectRequest.setCosPath(qServiceCfg.sampleCosPath);
        optionObjectRequest.setOrigin("http://www.qcloud.com");
        optionObjectRequest.setAccessControlMethod("get");
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
    }
}
