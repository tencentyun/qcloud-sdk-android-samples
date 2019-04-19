package com.bradyxiao.cos_weak_network_practice;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.common.ClientErrorCode;
import com.tencent.cos.xml.common.Region;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

import java.util.List;
import java.util.Map;

public class ResumeHelper {
    private final static String TAG = ResumeHelper.class.getSimpleName();

    private Context context;
    String bucket = "examplebucket-1250000000"; // 存储桶，格式 BucketName-Appid, 填写您的bucket
    String region = Region.AP_Guangzhou.getRegion(); // 存储桶所在地域, 填写您的bucket所在的地域
    private CosXmlService cosXmlService;

    private Handler mainHandler;
    private boolean isTriedOnce = false; // 是否已重传过，避免无限制重传
    private final int MESSAGE_RETRY = 1;
    private final int MESSAGE_FINISH = 2;
    private final int MESSAGE_PROGRESS = 3;

    private OnStateListener onStateListener;

    public ResumeHelper(Context context){
        this.context = context;
        this.mainHandler = new Handler(context.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case MESSAGE_RETRY:
                        /** 已重传过 */
                        if(isTriedOnce)return;
                        isTriedOnce = true;
                        Parameter parameter = (Parameter) msg.obj;
                        onStateListener.onTryAgain();
                        /** 再次上传 */
                        upload(parameter.srcPath, parameter.cosPath, parameter.uploadId, parameter.sliceSize);
                        break;
                    case MESSAGE_FINISH:
                        onStateListener.onFinish((String)msg.obj);
                        break;
                    case MESSAGE_PROGRESS:
                        Bundle bundle = (Bundle) msg.obj;
                        onStateListener.onProgress(bundle.getLong("COMPLETE"), bundle.getLong("TOTAL"));
                        break;
                }
            }
        };
        initCosXml();
    }

    /**
     * 初始化 CosXmlService
     */
    private void initCosXml(){
        /** 初始化 CosXmlServiceConfig */
        CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .setDebuggable(true)
                .isHttps(true)
                .setRegion(region)
                .builder();
          //此处使用的时，永久密钥，建议使用临时密钥
        String secretId = "AKIDPxxx"; //填写您的 云 api 密钥 secretId
        String secretKey = "EH8oxx"; //填写您的 云 api 密钥 secretKey
        /** 初始化 密钥信息 */
        QCloudCredentialProvider qCloudCredentialProvider = new ShortTimeCredentialProvider(secretId, secretKey, 6000);
        /** 初始化 CosXmlService */
        cosXmlService = new CosXmlService(context, cosXmlServiceConfig, qCloudCredentialProvider);
    }

    /**
     * 上传
     * @param srcPath 本地文件路径
     * @param cosPath 存储在 cos 上的路径
     * @param uploadId 是否续传，若无，则为null
     * @param sliceSize 分片上传时，设置的分片块大小
     */
    public void upload(final String srcPath, final String cosPath, final String uploadId, long sliceSize){

        /** 设置分片上传时，分片块的大小 */
        TransferConfig transferConfig = new TransferConfig.Builder()
                .setSliceSizeForUpload(sliceSize)
                .build();
        /** 初始化TransferManager */
        TransferManager transferManager = new TransferManager(cosXmlService, transferConfig);
        /** 开始上传: 若 uploadId != null,则可以进行续传 */
        final COSXMLUploadTask uploadTask = transferManager.upload(bucket, cosPath, srcPath, uploadId);
        /** 显示任务状态信息 */
        uploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                Log.d(TAG, "upload task state: " + state.name());
            }
        });
        /** 显示任务上传进度 */
        uploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                Log.d(TAG, "upload task progress: " + complete + "/" + target);
                Bundle bundle = new Bundle();
                bundle.putLong("COMPLETE", complete);
                bundle.putLong("TOTAL", target);
                Message msg = mainHandler.obtainMessage();
                msg.what = MESSAGE_PROGRESS;
                msg.obj = bundle;
                mainHandler.sendMessage(msg);
            }
        });
        /** 显示任务上传结果 */
        uploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            /** 任务上传成功 */
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                COSXMLUploadTask.COSXMLUploadTaskResult uploadTaskResult = (COSXMLUploadTask.COSXMLUploadTaskResult) result;
                Log.d(TAG, "upload task success: " + uploadTaskResult.printResult());
                Message msg = mainHandler.obtainMessage();
                msg.what = MESSAGE_FINISH;
                StringBuilder header = new StringBuilder(uploadTaskResult.accessUrl).append("\n");
                header.append(uploadTaskResult.httpCode).append(" ").append(uploadTaskResult.httpMessage).append("\n");
                for(Map.Entry<String, List<String>> entry : uploadTaskResult.headers.entrySet()){
                    header.append(entry.getKey()).append(": ").append(entry.getValue().get(0)).append("\n");
                }
                msg.obj = header.toString();
                mainHandler.sendMessage(msg);
            }

            /** 任务上传失败 */
            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                Log.d(TAG, "upload task failed: " + (exception == null ? serviceException.getMessage() :
                        (exception.errorCode + "," + exception.getMessage())));
                if(exception != null && !isTriedOnce){
                    /** 若是因为网络导致失败， 则可尝试将分片大小设置 100k 再跑一次*/
                    if(exception.errorCode == ClientErrorCode.INTERNAL_ERROR.getCode()
                            || exception.errorCode == ClientErrorCode.IO_ERROR.getCode()
                            || exception.errorCode != ClientErrorCode.POOR_NETWORK.getCode()){
                        Log.d(TAG, "upload task try again");
                        Message msg = mainHandler.obtainMessage();
                        msg.what = MESSAGE_RETRY;
                        Parameter parameter = new Parameter();
                        parameter.cosPath = cosPath;
                        parameter.srcPath = srcPath;
                        parameter.uploadId = uploadTask.getUploadId();
                        parameter.sliceSize = 100 * 1024L;
                        msg.obj = parameter;
                        mainHandler.sendMessage(msg);
                        return;
                    }
                }
                /** 失败 结束*/
                Message msg = mainHandler.obtainMessage();
                msg.what = MESSAGE_FINISH;
                msg.obj = (exception != null ? exception.getMessage() : serviceException.getMessage());
                mainHandler.sendMessage(msg);
            }
        });
    }

    public void enableTryAgain(boolean isTriedOnce){
        this.isTriedOnce = isTriedOnce;
    }

    public void setOnStateListener(OnStateListener onStateListener){
        this.onStateListener = onStateListener;
    }

    private static class Parameter{
        private String cosPath;
        private String srcPath;
        private String uploadId;
        private long sliceSize;
    }

    public interface OnStateListener{
        void onProgress(long completed, long total);
        void onFinish(String message);
        void onTryAgain();
    }
}
