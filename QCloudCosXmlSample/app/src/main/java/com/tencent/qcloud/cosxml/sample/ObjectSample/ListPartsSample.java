package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.CosXmlResultListener;
import com.tencent.cos.xml.model.object.ListPartsRequest;
import com.tencent.cos.xml.model.object.ListPartsResult;
import com.tencent.qcloud.cosxml.sample.ResultActivity;
import com.tencent.qcloud.cosxml.sample.ResultHelper;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;
import com.tencent.qcloud.network.exception.QCloudException;

/**
 * Created by bradyxiao on 2017/6/1.
 * author bradyxiao
 *
 * List Parts 用来查询特定分块上传中的已上传的块，即罗列出指定 UploadId 所属的所有已上传成功的分块。
 *
 */
public class ListPartsSample {
    ListPartsRequest listPartsRequest;
    QServiceCfg qServiceCfg;

    public ListPartsSample(QServiceCfg qServiceCfg){
        this.qServiceCfg = qServiceCfg;
    }

    public ResultHelper start(){
        ResultHelper resultHelper = new ResultHelper();
        listPartsRequest = new ListPartsRequest();
        listPartsRequest.setBucket(qServiceCfg.bucket);
        listPartsRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        listPartsRequest.setUploadId(qServiceCfg.getCurrentUploadId());
        listPartsRequest.setSign(600,null,null);
        try {
            ListPartsResult listPartsResult =
                    qServiceCfg.cosXmlService.listParts(listPartsRequest);
            Log.w("XIAO",listPartsResult.printHeaders());
            if(listPartsResult.getHttpCode() >= 300){
                Log.w("XIAO",listPartsResult.printError());
            }else {
                Log.w("XIAO",listPartsResult.printBody());
            }
            resultHelper.cosXmlResult = listPartsResult;
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
        listPartsRequest = new ListPartsRequest();
        listPartsRequest.setBucket(qServiceCfg.bucket);
        listPartsRequest.setCosPath(qServiceCfg.getMultiUploadCosPath());
        listPartsRequest.setUploadId(qServiceCfg.getCurrentUploadId());
        listPartsRequest.setSign(600,null,null);
        qServiceCfg.cosXmlService.listPartsAsync(listPartsRequest, new CosXmlResultListener() {
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
