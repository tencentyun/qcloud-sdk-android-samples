package com.tencent.qcloud.cosxml.sample.ServiceSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.service.GetServiceRequest;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Get Service 接口是用来获取请求者名下的所有存储空间列表（Bucket list）。该 API 接口不支持匿名请求，您需要使用帯 Authorization 签名认证的请求才能获取 Bucket 列表，
 * 且只能获取签名中 AccessID 所属账户的 Bucket 列表。
 *
 */
public class GetServiceSample {
    GetServiceRequest getServiceRequest;
    QServiceCfg qServiceCfg;

    public GetServiceSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    /**
     *
     * 采用同步操作
     *
     */
    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        getServiceRequest = new GetServiceRequest();
        getServiceRequest.setSign(600,null,null);
        try {
            GetServiceResult getServiceResult =
                    qServiceCfg.cosXmlService.getService(getServiceRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = getServiceResult;
            return resultHelper;
        }catch (CosXmlClientException e) {
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
        getServiceRequest = new GetServiceRequest();
        getServiceRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.getServiceAsync(getServiceRequest, new CosXmlResultListener() {
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
