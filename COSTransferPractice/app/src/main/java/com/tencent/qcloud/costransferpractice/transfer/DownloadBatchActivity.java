package com.tencent.qcloud.costransferpractice.transfer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLDownloadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.qcloud.costransferpractice.BuildConfig;
import com.tencent.qcloud.costransferpractice.CosServiceFactory;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.base.BaseActivity;

import java.io.File;

/**
 * Created by jordanqin on 2020/6/18.
 * 批量文件下载页面 仅用于测试，需要自行配置
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class DownloadBatchActivity extends BaseActivity {
    private LinearLayout ll_task;

    private String bucketName;

    /**
     * 下载时的本地父目录
     */
    private File downloadParentDir;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_batch_activity);

        bucketName = "download-1257101xxx";
        String bucketRegion = "ap-beijing";

        ll_task = findViewById(R.id.ll_task);
        LayoutInflater inflater = LayoutInflater.from(this);
        downloadParentDir = getExternalFilesDir("");

        findViewById(R.id.btn_start).setOnClickListener(v -> {
            int count = 200;
            for (int i = 0; i < count; i ++) {
                final String objectName = "big_object"+i%60;

                View view = inflater.inflate(R.layout.item_download_batch, ll_task, false);
                ll_task.addView(view);

                TextView tv_name = view.findViewById(R.id.tv_name);
                tv_name.setText(objectName.replace("big_object", ""));
                ProgressBar pb_download = view.findViewById(R.id.pb_download);

                TransferConfig transferConfig = new TransferConfig.Builder().build();
                TransferManager transferManager = new TransferManager(
                        CosServiceFactory.getCosXmlService(
                                DownloadBatchActivity.this,
                                bucketRegion,
                                BuildConfig.COS_SECRET_ID,
                                BuildConfig.COS_SECRET_KEY,
                                true
                        ),
                        transferConfig
                );

                final String localName = "30-60M_" + i + ".txt";
                COSXMLDownloadTask downloadTask = transferManager.download(DownloadBatchActivity.this,
                        bucketName, objectName,
                        downloadParentDir.toString(), localName);

                downloadTask.setCosXmlResultListener(new CosXmlResultListener() {
                    @Override
                    public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                        Log.d("DownloadActivity", objectName + "_onSuccess");
                    }

                    @Override
                    public void onFail(CosXmlRequest request, CosXmlClientException clientException, CosXmlServiceException serviceException) {
                        Log.d("DownloadActivity", objectName + "_onFail");
                        if(clientException != null) clientException.printStackTrace();
                        if(serviceException != null) serviceException.printStackTrace();
                    }
                });
                downloadTask.setCosXmlProgressListener((complete, target) -> {
                    Log.d("DownloadActivity", objectName + "_download Progress" + ": " + complete + "/" + target);
                    pb_download.setProgress((int) (100 * complete / target));
                });
            }
        });
    }
}
