package com.tencent.qcloud.cosxml.sample;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.tencent.cos.xml.CosXml;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.CosXmlSimpleService;
import com.tencent.cos.xml.common.Region;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.CopyObjectRequest;
import com.tencent.cos.xml.model.object.GetObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.transfer.COSXMLCopyTask;
import com.tencent.cos.xml.transfer.COSXMLDownloadTask;
import com.tencent.cos.xml.transfer.COSXMLTask;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.cos.xml.utils.DigestUtils;
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bradyxiao on 2018/8/24.
 * Copyright 2010-2018 Tencent Cloud. All Rights Reserved.
 */

public class TransferManagerTest {

    String TAG = "TransferManagerTest";

    String appid = " 腾讯云 cos 服务的 appid";
    String bucket = "测试存储桶";
    String region = "存储桶所在的园区";
    String secretId = "密钥secretId";
    String secretKey = "密钥secretKey";

    TransferManager transferManager;

    public TransferManagerTest(Context context){

        CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .isHttps(false)
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .builder();
        CosXmlSimpleService cosXml = new CosXmlSimpleService(context, cosXmlServiceConfig,
                new ShortTimeCredentialProvider(secretId, secretKey,
                        600));

        TransferConfig transferConfig = new TransferConfig.Builder()
                .setDividsionForCopy(5 * 1024 * 1024)
                .setSliceSizeForCopy(5 * 1024 * 1024)
                .setDivisionForUpload(2 * 1024 * 1024)
                .setSliceSizeForCopy(1024 * 1024)
                .build();
        transferManager = new TransferManager((CosXmlSimpleService) cosXml, transferConfig);
    }

    public void testNull(){
        transferManager = new TransferManager(null, null);
    }

    public List<COSXMLUploadTask> upload(){
        String srcPath = Environment.getExternalStorageDirectory().getPath() + "/jpeg.zip";
        List<COSXMLUploadTask> taskList = new ArrayList<>();
        String cosPath = "transfer_" + 1;
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(bucket, cosPath, srcPath, null);
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                float progress = 1.0f * complete / target * 100;
                Log.d(TAG,  String.format("progress = %d%%", (int)progress));
            }
        });
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.d(TAG,  result.printResult());
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                Log.d(TAG,  exception == null ? serviceException.getMessage() : exception.toString());
            }
        });
        cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                Log.d(TAG,  state.name());
            }
        });
        taskList.add(cosxmlUploadTask);
       return taskList;
    }

    public List<COSXMLUploadTask> upload2(){
        String srcPath = Environment.getExternalStorageDirectory().getPath() + "/download.pdf";
        List<COSXMLUploadTask> taskList = new ArrayList<>();
        String cosPath = "transfer_" + 1;
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, cosPath, srcPath);
        putObjectRequest.setRegion(region);
        putObjectRequest.setSign(600);
        putObjectRequest.setNeedMD5(true);
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(putObjectRequest, null);
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                float progress = 1.0f * complete / target * 100;
                Log.d(TAG,  String.format("progress = %d%%", (int)progress));
            }
        });
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.d(TAG,  result.printResult());
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                Log.d(TAG,  exception == null ? serviceException.getMessage() : exception.toString());
            }
        });
        cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                Log.d(TAG,  state.name());
            }
        });
        taskList.add(cosxmlUploadTask);
        return taskList;
    }



    public List<COSXMLDownloadTask> download(Context context){
        List<COSXMLDownloadTask> downloadTaskList = new ArrayList<>();
        String cosPath = "transfer_" + 1;
        final String localDir = Environment.getExternalStorageDirectory().getPath();
        final String localFileName = "download2.pdf";
        COSXMLDownloadTask cosxmlDownloadTask = transferManager.download(context, bucket, cosPath, localDir, localFileName);
        cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                float progress = 1.0f * complete / target * 100;
                Log.d(TAG,  String.format("progress = %d%%", (int)progress));
            }
        });
        cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.d(TAG,  result.printResult());
                try {
                    String srcMd5 = DigestUtils.getMD5(Environment.getExternalStorageDirectory().getPath() + "/jpeg.zip");
                    String destMd5 = DigestUtils.getMD5(localDir + "/" + localFileName);
                    Log.d(TAG, String.format("src = %s | dst = %s | %s", srcMd5, destMd5, String.valueOf(srcMd5.equals(destMd5))));
                } catch (CosXmlClientException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                Log.d(TAG,  exception == null ? serviceException.getMessage() : exception.toString());
            }
        });
        cosxmlDownloadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                Log.d(TAG,  state.name());
            }
        });
        downloadTaskList.add(cosxmlDownloadTask);
        return downloadTaskList;
    }

    public List<COSXMLDownloadTask> download2(Context context){
        List<COSXMLDownloadTask> downloadTaskList = new ArrayList<>();
        String cosPath = "transfer_" + 1;
        final String localDir = Environment.getExternalStorageDirectory().getPath();
        final String localFileName = "download2.pdf";
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, cosPath, localDir, localFileName);
        getObjectRequest.setRegion(region);
        getObjectRequest.setSign(600);
        COSXMLDownloadTask cosxmlDownloadTask = transferManager.download(context, getObjectRequest);
        cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long complete, long target) {
                float progress = 1.0f * complete / target * 100;
                Log.d(TAG,  String.format("progress = %d%%", (int)progress));
            }
        });
        cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.d(TAG,  result.printResult());
                try {
                    String srcMd5 = DigestUtils.getMD5(Environment.getExternalStorageDirectory().getPath() + "/Java nio.pdf");
                    String destMd5 = DigestUtils.getMD5(localDir + "/" + localFileName);
                    Log.d(TAG, String.format("src = %s | dst = %s | %s", srcMd5, destMd5, String.valueOf(srcMd5.equals(destMd5))));
                } catch (CosXmlClientException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                Log.d(TAG,  exception == null ? serviceException.getMessage() : exception.toString());
            }
        });
        cosxmlDownloadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                Log.d(TAG,  state.name());
            }
        });
        downloadTaskList.add(cosxmlDownloadTask);
        return downloadTaskList;
    }


    public List<COSXMLCopyTask> copy(){
        List<COSXMLCopyTask> cosxmlCopyTaskList = new ArrayList<>();
        String cosPath = "transfer_1.copy";
        String sourceCosPath = "transfer_1";
        CopyObjectRequest.CopySourceStruct copySourceStruct = new CopyObjectRequest.CopySourceStruct(
                appid, bucket, region, sourceCosPath);
       COSXMLCopyTask cosxmlCopyTask = transferManager.copy(bucket, cosPath, copySourceStruct);
       cosxmlCopyTask.setCosXmlProgressListener(null);
       cosxmlCopyTask.setTransferStateListener(new TransferStateListener() {
           @Override
           public void onStateChanged(TransferState state) {
               Log.d(TAG,  state.name());
           }
       });
       cosxmlCopyTask.setCosXmlResultListener(new CosXmlResultListener() {
           @Override
           public void onSuccess(CosXmlRequest request, CosXmlResult result) {
               Log.d(TAG,  result.printResult());
           }

           @Override
           public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
               Log.d(TAG,  exception == null ? serviceException.getMessage() : exception.toString());
           }
       });
       cosxmlCopyTaskList.add(cosxmlCopyTask);
       return cosxmlCopyTaskList;
    }

    public List<COSXMLCopyTask> copy2(){
        List<COSXMLCopyTask> cosxmlCopyTaskList = new ArrayList<>();
        String cosPath = "copyTask.png";
        String sourceCosPath = "52B7B13D-8030-42BD-A299-BDE65B97E951.png";
        CopyObjectRequest.CopySourceStruct copySourceStruct = new CopyObjectRequest.CopySourceStruct(
                appid, bucket, region, sourceCosPath);
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(bucket, cosPath, copySourceStruct);
        copyObjectRequest.setRegion(region);
        copyObjectRequest.setSign(600);
        COSXMLCopyTask cosxmlCopyTask = transferManager.copy(copyObjectRequest);
        cosxmlCopyTask.setCosXmlProgressListener(null);
        cosxmlCopyTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(TransferState state) {
                Log.d(TAG,  state.name());
            }
        });
        cosxmlCopyTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                Log.d(TAG,  result.printResult());
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                Log.d(TAG,  exception == null ? serviceException.getMessage() : exception.toString());
            }
        });
        cosxmlCopyTaskList.add(cosxmlCopyTask);
        return cosxmlCopyTaskList;
    }


    public void pause(COSXMLTask cosxmlTask) {
        if(cosxmlTask != null){
            cosxmlTask.pause();
        }
    }

    public void cancel(COSXMLTask cosxmlTask) {
        if(cosxmlTask != null){
            cosxmlTask.cancel();
        }
    }

    public void resume(COSXMLTask cosxmlTask) {
        if(cosxmlTask != null){
            cosxmlTask.resume();
        }
    }
}
