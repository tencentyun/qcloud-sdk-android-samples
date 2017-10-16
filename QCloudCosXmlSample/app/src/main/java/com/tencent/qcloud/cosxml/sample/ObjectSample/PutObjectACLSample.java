package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.PutObjectACLRequest;
import com.tencent.cos.xml.model.object.PutObjectACLResult;
import com.tencent.cos.xml.model.tag.ACLAccount;
import com.tencent.cos.xml.model.tag.ACLAccounts;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * Put Object ACL 接口用来对某个 Bucket 中的某个的 Object 进行 ACL 表的配置，您可以通过 Header："x-cos-acl"，"x-cos-grant-read"，
 * "x-cos-grant-write"，"x-cos-grant-full-control" 传入 ACL 信息，或者通过 Body 以 XML 格式传入 ACL 信息。
 *
 */
public class PutObjectACLSample {
    PutObjectACLRequest putObjectACLRequest;
    QServiceCfg qServiceCfg;


    public PutObjectACLSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        String bucket = qServiceCfg.getBucketForObjectAPITest();
        String cosPath = qServiceCfg.getUploadCosPath();

        putObjectACLRequest = new PutObjectACLRequest(bucket, cosPath);

        putObjectACLRequest.setXCOSACL("public-read");
        ACLAccounts readAccounts = new ACLAccounts();
        readAccounts.addACLAccount(new ACLAccount("1278687956", "1278687956"));
        putObjectACLRequest.setXCOSGrantRead(readAccounts);

        ACLAccounts writeAccounts = new ACLAccounts();
        writeAccounts.addACLAccount(new ACLAccount("1278687956", "1278687956"));
        putObjectACLRequest.setXCOSGrantWrite(writeAccounts);

        putObjectACLRequest.setSign(600,null,null);
        try {
            PutObjectACLResult putObjectACLResult =
                    qServiceCfg.cosXmlService.putObjectACL(putObjectACLRequest);
            Log.w("XIAO","success");
            resultHelper.cosXmlResult = putObjectACLResult;
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

        putObjectACLRequest = new PutObjectACLRequest(bucket, cosPath);

        putObjectACLRequest.setXCOSACL("public-read");
        ACLAccounts readAccounts = new ACLAccounts();
        readAccounts.addACLAccount(new ACLAccount("1278687956", "1278687956"));
        putObjectACLRequest.setXCOSGrantRead(readAccounts);

        ACLAccounts writeAccounts = new ACLAccounts();
        writeAccounts.addACLAccount(new ACLAccount("1278687956", "1278687956"));
        putObjectACLRequest.setXCOSGrantWrite(writeAccounts);

        putObjectACLRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.putObjectACLAsync(putObjectACLRequest, new CosXmlResultListener() {
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
