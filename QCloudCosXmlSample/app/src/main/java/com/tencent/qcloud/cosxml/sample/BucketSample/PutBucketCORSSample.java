package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.bucket.PutBucketCORSRequest;
import com.tencent.cos.xml.model.bucket.PutBucketCORSResult;
import com.tencent.cos.xml.model.tag.CORSConfiguration;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Put Bucket CORS 接口用来请求设置 Bucket 的跨域资源共享权限，您可以通过传入 XML 格式的配置文件来实现配置，
 * 文件大小限制为64 KB。默认情况下，Bucket 的持有者直接有权限使用该 API 接口，Bucket 持有者也可以将权限授予其他用户。
 *
 */
public class PutBucketCORSSample {
    PutBucketCORSRequest putBucketCORSRequest;
    QServiceCfg qServiceCfg;

    public PutBucketCORSSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForBucketAPITest();
        if(bucket == null){
            qServiceCfg.toastShow("bucket 不存在，需要创建");
        }

        putBucketCORSRequest = new PutBucketCORSRequest(bucket);
        /**
         * 实例化 PutBucketCORSRequest(bucketForObjectAPITest)
         *
         * CORSRule: 跨域访问配置信息
         * CORSRule.id： 配置规则的 ID
         * CORSRule.allowedOrigin: 允许的访问来源，支持通配符 * , 格式为：协议://域名[:端口]如：http://www.qq.com
         * CORSRule.maxAgeSeconds: 设置 OPTIONS 请求得到结果的有效期
         * CORSRule.allowedMethod: 允许的 HTTP 操作，如：GET，PUT，HEAD，POST，DELETE
         * CORSRule.allowedHeader：在发送 OPTIONS 请求时告知服务端，接下来的请求可以使用哪些自定义的 HTTP 请求头部，支持通配符 *
         * CORSRule.exposeHeader： 设置浏览器可以接收到的来自服务器端的自定义头部信息
         */


        putBucketCORSRequest.setSign(600,null,null);

        CORSConfiguration.CORSRule corsRule = new CORSConfiguration.CORSRule();

        corsRule.allowedOrigin = "http://cloud.tencent.com";

        corsRule.allowedHeader = new ArrayList<>();
        corsRule.allowedHeader.add("Host");
        corsRule.allowedHeader.add("Authorization");

        corsRule.allowedMethod = new ArrayList<>();
        corsRule.allowedMethod.add("PUT");
        corsRule.allowedMethod.add("GET");

        corsRule.exposeHeader = new ArrayList<>();
        corsRule.exposeHeader.add("x-cos-meta");
        corsRule.exposeHeader.add("x-cos-meta-2");
        corsRule.id = "CORSID";
        corsRule.maxAgeSeconds = 5000;

        putBucketCORSRequest.addCORSRule(corsRule);
        try {
            PutBucketCORSResult putBucketCORSResult =
                    qServiceCfg.cosXmlService.putBucketCORS(putBucketCORSRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = putBucketCORSResult;
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
        String bucket = qServiceCfg.getBucketForBucketAPITest();
        if(bucket == null){
            qServiceCfg.toastShow("bucket 不存在，需要创建");
        }

        putBucketCORSRequest = new PutBucketCORSRequest(bucket);

        putBucketCORSRequest.setSign(600,null,null);

        CORSConfiguration.CORSRule corsRule = new CORSConfiguration.CORSRule();

        corsRule.allowedOrigin = "http://cloud.tencent.com";

        corsRule.allowedHeader = new ArrayList<>();
        corsRule.allowedHeader.add("Host");
        corsRule.allowedHeader.add("Authorization");

        corsRule.allowedMethod = new ArrayList<>();
        corsRule.allowedMethod.add("PUT");
        corsRule.allowedMethod.add("GET");

        corsRule.exposeHeader = new ArrayList<>();
        corsRule.exposeHeader.add("x-cos-meta");
        corsRule.exposeHeader.add("x-cos-meta-2");
        corsRule.id = "CORSID";
        corsRule.maxAgeSeconds = 5000;

        putBucketCORSRequest.addCORSRule(corsRule);

        qServiceCfg.cosXmlService.putBucketCORSAsync(putBucketCORSRequest, new CosXmlResultListener() {
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
    }
}
