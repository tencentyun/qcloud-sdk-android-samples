package com.tencent.qcloud.cosxml.sample.ObjectSample;

import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.bucket.ListMultiUploadsRequest;
import com.tencent.cos.xml.model.bucket.ListMultiUploadsResult;
import com.tencent.cos.xml.model.tag.ListMultipartUploads;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

import java.util.List;

/**
 * Created by bradyxiao on 2017/9/30.
 */

public class ResumeUploadSample {
    /**
     *  续传 ： 根据uploadId 来完成 续传，因为 uploadId 唯一确定了 此 bucket 下面还有那些 object
     *  未有上传完成。
     *  步骤：
     *  step 1: 首先 列举 bucket下面 有哪些未上传完成 的 object, 调用 ListMultiUploadsRequest实现，并
     *  获取 到 ListMultiUploadsResult；
     *
     *  step2: 根据 ListMultiUploadsResult中的 ListMultipartUploads 中的 Upload 就可以 获取到对应的
     *  uploadId 和 key
     *
     *  step3: 根据 Key 做匹配，确实是否是这个 object
     *
     */

    QServiceCfg qServiceCfg;
    public void start(){
        String bucket = "";
        String cosPath = "";

        ListMultiUploadsRequest listMultiUploadsRequest = new ListMultiUploadsRequest(bucket);
        listMultiUploadsRequest.setSign(600,null,null);
        try {
            ListMultiUploadsResult listMultiUploadsResult =
                    qServiceCfg.cosXmlService.listMultiUploads(listMultiUploadsRequest);
            Log.w("XIAO","success");

        }catch (CosXmlClientException e) {
            Log.w("XIAO","QCloudException =" + e.getMessage());
        } catch (CosXmlServiceException e) {
            Log.w("XIAO","QCloudServiceException =" + e.toString());
        }
    }

    private String getUploadId(String cosPath, ListMultiUploadsResult listMultiUploadsResult){
        if(listMultiUploadsResult == null)return null;
        if(listMultiUploadsResult.listMultipartUploads == null)return null;
        if(listMultiUploadsResult.listMultipartUploads.uploads == null)return  null;
        List<ListMultipartUploads.Upload>  uploadList = listMultiUploadsResult.listMultipartUploads.uploads;
        for(ListMultipartUploads.Upload upload : uploadList){
            if(upload.key.equals(cosPath)){
                return  upload.uploadID;
            }
        }
        return null;
    }


}
