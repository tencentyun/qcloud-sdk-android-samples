package com.tencent.qcloud.costransferpractice.transfer;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLDownloadTask;
import com.tencent.cos.xml.transfer.COSXMLTask;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.QCloudLifecycleCredentials;
import com.tencent.qcloud.core.auth.SessionCredentialProvider;
import com.tencent.qcloud.core.auth.SessionQCloudCredentials;
import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.http.HttpRequest;
import com.tencent.qcloud.costransferpractice.COSConfigManager;
import com.tencent.qcloud.costransferpractice.CosServiceFactory;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by rickenwang on 2018/10/18.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class TransferPresenter implements TransferContract.Presenter {

    private Context context;

    private TransferContract.View view;

    private COSConfigManager cosConfig;

    /**
     * {@link CosXmlService} 是您访问 COS 服务的核心类，它封装了所有 COS 服务的基础 API 方法。
     *
     * 每一个{@link CosXmlService} 对象只能对应一个 region，如果您需要同时操作多个 region 的
     * Bucket，请初始化多个 {@link CosXmlService} 对象。
     */
    private CosXmlService cosXmlService;

    /**
     * {@link TransferManager} 进一步封装了 {@link CosXmlService} 的上传和下载接口，当您需要
     * 上传文件到 COS 或者从 COS 下载文件时，请优先使用这个类。
     */
    private TransferManager transferManager;

    /**
     * 为了显示方便，这里只允许同一时间只能有一个上传和下载任务，您可以根据自己的逻辑修改
     */
    private COSXMLDownloadTask cosxmlDownloadTask;
    private COSXMLUploadTask cosxmlUploadTask;

    private Map<String, List<String>> regionAndBuckets;

    private String currentBucket;
    private String currentRegion;

    /**
     * 上传时的本地和 COS 路径
     */
    private String currentUploadPath;

    /**
     * 下载时的 COS 路径
     */
    private String currentDownloadCosPath;

    /**
     * 下载时的本地父目录
     */
    private File downloadParentDir;

    private int regionPosition;
    private int bucketPosition;


    public TransferPresenter(Context context, TransferContract.View view, Map<String, List<String>> regionAndBuckets) {

        this.context = context;
        cosConfig = COSConfigManager.getInstance();
        this.regionAndBuckets = regionAndBuckets;
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            downloadParentDir = Environment.getExternalStorageDirectory();
        } else {
            downloadParentDir = context.getFilesDir();
        }

        view.showRegionAndBucket(regionAndBuckets);

        if (regionPosition != 0 || bucketPosition != 0) {
            view.restore(regionPosition, bucketPosition);
        }
    }

    @Override
    public void startUpload() {

        if (TextUtils.isEmpty(currentUploadPath)) {

            view.toastMessage("请先选择文件");
            return;
        }

        if (cosxmlUploadTask == null) {

            cosxmlUploadTask = transferManager.upload(currentBucket, currentUploadPath,
                    currentUploadPath, null);

            cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
                @Override
                public void onStateChanged(final TransferState state) {
                    view.refreshUploadState(state);
                }
            });
            cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
                @Override
                public void onProgress(final long complete, final long target) {
                    view.refreshUploadProgress(complete, target);
                }
            });

            cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    cosxmlUploadTask = null;
                    currentDownloadCosPath = currentUploadPath;
                }

                @Override
                public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {

                    if (cosxmlUploadTask.getTaskState() != TransferState.PAUSED) {
                        cosxmlUploadTask = null;
                    }

                    if (exception != null) {
                        view.toastMessage(exception.getMessage());
                    }
                    if (serviceException != null) {
                        view.toastMessage(serviceException.getMessage());
                    }
                }
            });
        } else {
            view.toastMessage("不能开始");
        }
    }

    @Override
    public void pauseUpload() {

        pauseTransferTask(cosxmlUploadTask);
    }

    @Override
    public void resumeUpload() {

       resumeTransferTask(cosxmlUploadTask);
    }

    @Override
    public void cancelUpload() {

       cancelTransferTask(cosxmlUploadTask);
    }

    @Override
    public void startDownload() {

        if (TextUtils.isEmpty(currentDownloadCosPath)) {

            view.toastMessage("请先上传文件");
            return;
        }

        if (cosxmlDownloadTask == null) {

            File file = new File(currentUploadPath);

            cosxmlDownloadTask = transferManager.download(context, currentBucket, currentDownloadCosPath,
                    downloadParentDir.toString(), "cos_download_" + file.getName());

            cosxmlDownloadTask.setTransferStateListener(new TransferStateListener() {
                @Override
                public void onStateChanged(final TransferState state) {

                    view.refreshDownloadState(state);
                }
            });

            cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
                @Override
                public void onProgress(final long complete, final long target) {

                    view.refreshDownloadProgress(complete, target);
                }
            });

            cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    cosxmlDownloadTask = null;
                }

                @Override
                public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {

                    if (cosxmlDownloadTask.getTaskState() != TransferState.PAUSED) {
                        cosxmlDownloadTask = null;
                    }

                    if (exception != null) {
                        view.toastMessage(exception.getMessage());
                    }
                    if (serviceException != null) {
                        view.toastMessage(serviceException.getMessage());
                    }

                }
            });

        } else {
            view.toastMessage("不能开始");
        }
    }

    @Override
    public void pauseDownload() {
        pauseTransferTask(cosxmlDownloadTask);
    }

    @Override
    public void resumeDownload() {

       resumeTransferTask(cosxmlDownloadTask);
    }

    @Override
    public void cancelDownload() {

       cancelTransferTask(cosxmlDownloadTask);
    }

    @Override
    public void refreshRegion(String region, int position) {

        regionPosition = position;
        cosXmlService = CosServiceFactory.getCosXmlServiceWithProperWay(context, region);
        TransferConfig transferConfig = new TransferConfig.Builder()
                .build();
        /**
         * {@link TransferManager} 进一步封装了 {@link CosXmlService} 的上传和下载接口，当您需要
         * 上传文件到 COS 或者从 COS 下载文件时，请优先使用这个类。
         */
        transferManager = new TransferManager(cosXmlService, transferConfig);
    }

    @Override
    public void refreshBucket(String bucket, int position) {

        bucketPosition = position;
        this.currentBucket = bucket;
    }

    @Override
    public void refreshUploadCosAndLocalPath(String path) {

        currentUploadPath = path;
    }


    @Override
    public void release() {
        if (cosXmlService != null) {
            cosXmlService.release();
        }
    }

    private void pauseTransferTask(COSXMLTask cosxmlTask) {

        if (cosxmlTask != null && cosxmlTask.getTaskState() == TransferState.IN_PROGRESS) {
            cosxmlTask.pause();
        } else {
            view.toastMessage("不能暂停");
        }
    }

    private void resumeTransferTask(COSXMLTask cosxmlTask) {

        if (cosxmlTask != null && cosxmlTask.getTaskState() == TransferState.PAUSED) {
            cosxmlTask.resume();
        } else {
            view.toastMessage("不能恢复");
        }
    }

    private void cancelTransferTask(COSXMLTask cosxmlTask) {

        if (cosxmlTask != null) {
            cosxmlTask.cancel();
        } else {
            view.toastMessage("不能取消");
        }
    }


    private void checkConfig(String appid, String region, String bucket, String signUrl, String localFilePath) {

        if (TextUtils.isEmpty(appid) || appid.length() != 10) {
            throw new RuntimeException("请正确配置您的 10 位 appid");
        }

        if (TextUtils.isEmpty(bucket)) {
            throw new RuntimeException("请正确配置您的 Bucket");
        }

        if (TextUtils.isEmpty(region) || !region.startsWith("ap-")) {
            throw new RuntimeException("请正确配置您的 bucket 对应的 region，region 一般以 ap- 开头");
        }

        if (TextUtils.isEmpty(signUrl)) {
            throw new RuntimeException("请正确配置您的临时密钥获取地址");
        }

        if (TextUtils.isEmpty(localFilePath)) {

            localFilePath = getProperUploadFileInExternalStorage();
        }

        if (TextUtils.isEmpty(localFilePath) || !new File(localFilePath).exists()) {

            //throw new RuntimeException("请填写正确的本地文件地址");
        } else {

            Log.i("cos", "auto choose local file path " + localFilePath);
            // cosConfig.localFilePath = localFilePath;
        }

    }


    private String getProperUploadFileInExternalStorage() {

        File externalStorageDirectory = Environment.getExternalStorageDirectory();

        File[] files = externalStorageDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {

                long _10M = 1024 * 1024 * 10;
                long _1G = 1024 * 1024 * 1024;

                return pathname.length() >= _10M
                        && pathname.length() <= _1G;
            }
        });

        return files.length > 0 ? files[0].getAbsolutePath() : null;
    }

}
