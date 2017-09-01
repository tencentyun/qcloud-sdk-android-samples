package com.tencent.qcloud.cosxml.sample;

import android.util.Log;

import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.qcloud.network.exception.QCloudException;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by bradyxiao on 2017/6/8.
 * author bradyxiao
 */
public class ResultHelper {
    public CosXmlResult cosXmlResult;
    public QCloudException exception;

    public String showMessage(){
        StringBuilder stringBuilder = new StringBuilder();
        if(cosXmlResult != null){
            stringBuilder.append("Headers:\n")
                    .append(cosXmlResult.printHeaders()).append("\n")
                    .append("Body:\n")
                    .append(cosXmlResult.printBody()).append("\n")
                    .append("Error:\n")
                    .append(cosXmlResult.printError()).append("\n");
            Log.w("XIAO",stringBuilder.toString());
            return stringBuilder.toString();
        }else if(exception != null){
            stringBuilder.append("Exception:\n")
                    .append(exception.getExceptionType().getMessage())
                    .append("---")
                    .append(exception.getDetailMessage()).append("\n");
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            exception.printStackTrace(pw);
            stringBuilder.append("detail:\n")
                    .append(stringWriter.toString());
            Log.w("XIAO",stringBuilder.toString());
            return stringBuilder.toString();
        }
        return null;
    }
}
