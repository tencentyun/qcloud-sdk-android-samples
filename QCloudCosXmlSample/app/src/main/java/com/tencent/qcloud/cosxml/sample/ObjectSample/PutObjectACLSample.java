package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.PutObjectACLRequest;
import com.tencent.cos.xml.model.object.PutObjectACLResult;
import com.tencent.qcloud.cosxml.sample.R;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    String idFormat = "uin/%s:uin/%s";
    String subAccountId = "151453739";

    public PutObjectACLSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        putObjectACLRequest = new PutObjectACLRequest();
        putObjectACLRequest.setBucket(qServiceCfg.bucket);
        putObjectACLRequest.setCosPath(qServiceCfg.uploadCosPath);
        putObjectACLRequest.setXCOSACL("public-read");
        List<String> readIdList = new ArrayList<>();
        readIdList.add(String.format(idFormat, qServiceCfg.accountId, qServiceCfg.accountId));
        readIdList.add(String.format(idFormat, qServiceCfg.accountId, subAccountId));
        putObjectACLRequest.setXCOSGrantReadWithUIN(readIdList);
        List<String> writeIdList = new ArrayList<>();
        writeIdList.add(String.format(idFormat, qServiceCfg.accountId, qServiceCfg.accountId));
        writeIdList.add(String.format(idFormat, qServiceCfg.accountId, subAccountId));
        putObjectACLRequest.setXCOSGrantWriteWithUIN(writeIdList);
        Set<String> header = new HashSet<>();
        header.add("content-length");
        header.add("content-type");
        header.add("date");
        putObjectACLRequest.setSign(600,null,null);
        try {
            PutObjectACLResult putObjectACLResult =
                    qServiceCfg.cosXmlService.putObjectACL(putObjectACLRequest);
            Log.w("XIAO",putObjectACLResult.printHeaders());
            if(putObjectACLResult.getHttpCode() >= 300){
                Log.w("XIAO",putObjectACLResult.printError());
            }
            resultHelper.cosXmlResult = putObjectACLResult;
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
        putObjectACLRequest = new PutObjectACLRequest();
        putObjectACLRequest.setBucket(qServiceCfg.bucket);
        putObjectACLRequest.setCosPath(qServiceCfg.uploadCosPath);
        putObjectACLRequest.setXCOSACL("public-read");
        List<String> readIdList = new ArrayList<>();
        readIdList.add(String.format(idFormat, qServiceCfg.accountId, qServiceCfg.accountId));
        readIdList.add(String.format(idFormat, qServiceCfg.accountId, subAccountId));
        putObjectACLRequest.setXCOSGrantReadWithUIN(readIdList);
        List<String> writeIdList = new ArrayList<>();
        writeIdList.add(String.format(idFormat, qServiceCfg.accountId, qServiceCfg.accountId));
        writeIdList.add(String.format(idFormat, qServiceCfg.accountId, subAccountId));
        putObjectACLRequest.setXCOSGrantWriteWithUIN(writeIdList);
        Set<String> header = new HashSet<>();
        header.add("content-length");
        header.add("content-type");
        header.add("date");
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
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(activity.getString(R.string.acl_warning))
                        .append(cosXmlResult.printHeaders())
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
