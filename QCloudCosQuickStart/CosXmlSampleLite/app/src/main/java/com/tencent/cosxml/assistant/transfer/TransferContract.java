package com.tencent.cosxml.assistant.transfer;

import com.tencent.cos.xml.common.Region;
import com.tencent.cos.xml.transfer.TransferListener;
import com.tencent.cosxml.assistant.BasePresenter;
import com.tencent.cosxml.assistant.BaseView;

import java.util.List;

/**
 * Created by rickenwang on 2018/6/29.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class TransferContract {


    interface View extends BaseView<Presenter> {

        void setUploadProgress(int progress);

        void setDownloadProgress(int progress);

        void showUploadResult(String message);

        void showDownloadResult(String message);

        void refreshBucketList(List<String> bucketList);

        void showInterError(String error);

        void showError(String error);

        void showLoadingDialog(boolean enable);

        void showCreateBucketDialog(boolean enable);

        void refreshUploadResult(String result);

        void refreshUploadProgress(String progress);

        void refreshUploadProgressBar(int progress);
    }


    interface Presenter extends BasePresenter {


        /**
         * list bucket with the specific region.
         * <br>
         *
         * @return bucket list
         */
        void listBucket(String region, ListBucketListener listBucketListener);

        void createBucket(String region, String bucketName, CreateBucketListener createBucketListener);

        String chooseLocalFile();

        String uploadFile(String region, String bucketName, String path, UploadFileListener uploadFileListener);

        void cancelUpload(String uploadId, CancelTransferListener cancelTransferListener);

        void configUserInfo(String appid, String host, int port);

        String downloadFile();
    }

    interface ListBucketListener {

        void onSuccess(List<String> bucketList);

        void onFailed(String error);
    }

    interface CreateBucketListener {

        void onSuccess(String bucketName, String region);

        void onFailed(String error);
    }

    interface UploadFileListener {

        void onSuccess(String fileName);

        void onFailed(String error);

        void onProgress(long hasComplete, long total);
    }

    interface CancelTransferListener {

        void onSuccess();

        void onFailed();
    }

}
