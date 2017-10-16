package com.tencent.qcloud.cosxml.sample.BucketSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.common.COSACL;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.bucket.PutBucketRequest;
import com.tencent.cos.xml.model.bucket.PutBucketResult;
import com.tencent.cos.xml.model.tag.ACLAccount;
import com.tencent.cos.xml.model.tag.ACLAccounts;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;


/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Put Bucket 接口请求可以在指定账号下创建一个 Bucket。该 API 接口不支持匿名请求，
 * 您需要使用帯 Authorization 签名认证的请求才能创建新的 Bucket 。
 * 创建 Bucket 的用户默认成为 Bucket 的持有者。
 *
 */
public class PutBucketSample {
    PutBucketRequest putBucketRequest;
    QServiceCfg qServiceCfg;

    public PutBucketSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForBucketAPITest();
        if(bucket == null){
            bucket = "buckettest";
        }else{
            qServiceCfg.toastShow("同名的 bucket 已存在，可以先删除再创建");
        }

        putBucketRequest = new PutBucketRequest(bucket);

        putBucketRequest.setXCOSACL(COSACL.PRIVATE);
        ACLAccounts aclAccounts = new ACLAccounts();
        ACLAccount aclAccount = new ACLAccount("1278687956", "1278687956");
        aclAccounts.addACLAccount(aclAccount);
        putBucketRequest.setXCOSGrantRead(aclAccounts);

        ACLAccounts aclAccounts2 = new ACLAccounts();
        ACLAccount aclAccount2 = new ACLAccount("1278687956", "1278687956");
        aclAccounts2.addACLAccount(aclAccount2);
        putBucketRequest.setXCOSGrantWrite(aclAccounts2);

        ACLAccounts aclAccounts3 = new ACLAccounts();
        ACLAccount aclAccount3 = new ACLAccount("1278687956", "1278687956");
        aclAccounts3.addACLAccount(aclAccount3);
        putBucketRequest.setXCOSReadWrite(aclAccounts3);

        putBucketRequest.setSign(600,null,null);
        try {
            PutBucketResult putBucketResult =
                   qServiceCfg.cosXmlService.putBucket(putBucketRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = putBucketResult;
            qServiceCfg.setBucketForBucketAPITest(bucket);
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
        String bucket = qServiceCfg.getBucketForBucketAPITest();
        if(bucket == null){
            bucket = "buckettest";
        }else{
            qServiceCfg.toastShow("同名的 bucket 已存在，可以先删除再创建");
        }
        final String finalBucket = bucket;

        putBucketRequest = new PutBucketRequest(bucket);

        putBucketRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.putBucketAsync(putBucketRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printHeaders())
                        .append(cosXmlResult.printBody());
                Log.w("XIAO", "success = " + stringBuilder.toString());
                qServiceCfg.setBucketForBucketAPITest(finalBucket);
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
