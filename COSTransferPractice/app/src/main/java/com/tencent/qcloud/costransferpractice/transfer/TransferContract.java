package com.tencent.qcloud.costransferpractice.transfer;

import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.qcloud.costransferpractice.BasePresenter;
import com.tencent.qcloud.costransferpractice.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by rickenwang on 2018/10/18.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class TransferContract {

    interface Presenter extends BasePresenter {

        void startUpload();

        void pauseUpload();

        void resumeUpload();

        void cancelUpload();


        void startDownload();

        void pauseDownload();

        void resumeDownload();

        void cancelDownload();

        void refreshRegion(String region, int position);

        void refreshBucket(String bucket, int position);

        void refreshUploadCosAndLocalPath(String path);

        void release();

    }

    interface View extends BaseView<Presenter> {

        void toastMessage(String message);

        void refreshUploadState(TransferState state);

        void refreshDownloadState(TransferState state);

        void refreshUploadProgress(long progress, long total);

        void refreshDownloadProgress(long progress, long total);

        void setLoading(boolean loading);

        void clearTransferProgressAndState();

        void showRegionAndBucket(Map<String, List<String>> buckets);

        void restore(int regionPosition, int bucketPosition);
    }
}
