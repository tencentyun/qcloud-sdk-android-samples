package com.tencent.cosxml.assistant.transfer;

import android.content.Context;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.cos.xml.model.tag.ListAllMyBuckets;
import com.tencent.cosxml.assistant.R;
import com.tencent.cosxml.assistant.data.RemoteStorage;
import com.tencent.qcloud.core.logger.QCloudLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rickenwang on 2018/6/29.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class TransferPresenter implements TransferContract.Presenter {

    private RemoteStorage remoteStorage;

    private String appid;

    TransferContract.View view;

    public TransferPresenter(Context context, TransferContract.View view) {
        this.remoteStorage = new RemoteStorage(context);
        view.setPresenter(this);
        appid = context.getString(R.string.appid);
        this.view = view;
    }


    @Override
    public void listBucket(final String region, final TransferContract.ListBucketListener listBucketListener) {



        remoteStorage.listBucket(region, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {

                QCloudLogger.i("assistant", "list bucket success begin, region is " + region);

                GetServiceResult getServiceResult = (GetServiceResult) cosXmlResult;
                List<ListAllMyBuckets.Bucket> bucketList = getServiceResult.listAllMyBuckets.buckets;
                List<String> bucketNameList = new ArrayList<>();
                for (ListAllMyBuckets.Bucket bucket: bucketList) {

                    String bucketName = bucket.name;
                    if (bucketName != null && bucketName.endsWith('-'+appid) ) {
                        bucketName = bucketName.substring(0, bucketName.lastIndexOf('-'));
                    }
                    //if (bucket.location.equals(region)) {   // this is very strange

                    //}
                    if (region.equals(bucket.location)) {

                        bucketNameList.add(bucketName);
                    }
                    QCloudLogger.i("assistant", "bucket name is " + bucketName + "region is " + bucket.location);
                }

                QCloudLogger.i("assistant", "list bucket success end");
                listBucketListener.onSuccess(bucketNameList);
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException) {

                String message = null;
                if (clientException != null) {
                    message = clientException.getMessage();
                }
                if (serviceException != null) {
                    message = serviceException.getMessage();
                }

                listBucketListener.onFailed(message);
            }
        });
    }

    @Override
    public void createBucket(final String region, final String bucketName, final TransferContract.CreateBucketListener createBucketListener) {

        remoteStorage.createBucket(region, bucketName, new CosXmlResultListener() {

            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                createBucketListener.onSuccess(bucketName, region);

            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException) {

                String message = null;
                if (clientException != null) {
                    message = clientException.getMessage();
                }
                if (serviceException != null) {
                    message = serviceException.getMessage();
                }

                createBucketListener.onFailed(message);
            }
        });
    }

    @Override
    public String chooseLocalFile() {
        return null;
    }

    @Override
    public String uploadFile(String region, String bucketName, final String path, final TransferContract.UploadFileListener uploadFileListener) {

        return remoteStorage.uploadFile(region, bucketName, path, path, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                uploadFileListener.onSuccess(path);
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException clientException, CosXmlServiceException serviceException) {

                String message = null;
                if (clientException != null) {
                    message = clientException.getMessage();
                }
                if (serviceException != null) {
                    message = serviceException.getMessage();
                }
                uploadFileListener.onFailed(message);

            }
        }, new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long total) {
                uploadFileListener.onProgress(progress, total);
            }
        });
    }

    @Override
    public void cancelUpload(String transferId, final TransferContract.CancelTransferListener cancelTransferListener) {

        remoteStorage.cancelTransfer(transferId, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                cancelTransferListener.onSuccess();
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException e, CosXmlServiceException e1) {
                cancelTransferListener.onFailed();
            }
        });
    }

    @Override
    public void configUserInfo(String appid, String host, int port) {
        remoteStorage.setUserInfo(appid, host, port);
    }

    @Override
    public String downloadFile() {
        return null;
    }


    @Override
    public void start() {

    }


}
