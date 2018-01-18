package com.tencent.qcloud.cosxml.sample;

import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.CosXmlResult;


import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by bradyxiao on 2017/6/8.
 * author bradyxiao
 */
public class ResultHelper {
    /** success */
    public CosXmlResult cosXmlResult;

    /** failed because of local exception, such as parameters error */
    public CosXmlClientException qCloudException;

    /** failed because of service exception, such as has no authority to operator */
    public CosXmlServiceException qCloudServiceException;

    public String showMessage(){
        StringBuilder stringBuilder = new StringBuilder();
        if(cosXmlResult != null){
            stringBuilder.append(cosXmlResult.printResult()).append("\n");
            Log.w("XIAO",stringBuilder.toString());
            return stringBuilder.toString();
        }else if(qCloudException != null){
            stringBuilder.append("ClientException:\n");
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            qCloudException.printStackTrace(pw);
            stringBuilder.append("detail:\n")
                    .append(stringWriter.toString())
                    .append("\n");
            Log.w("XIAO",stringBuilder.toString());
            return stringBuilder.toString();
        }else if(qCloudServiceException != null){
            stringBuilder.append("ServiceException:\n")
                    .append("detail:\n")
                    .append(qCloudServiceException.getMessage())
                    .append("\n");
            Log.w("XIAO",stringBuilder.toString());
            return stringBuilder.toString();
        }
        return null;
    }
}
