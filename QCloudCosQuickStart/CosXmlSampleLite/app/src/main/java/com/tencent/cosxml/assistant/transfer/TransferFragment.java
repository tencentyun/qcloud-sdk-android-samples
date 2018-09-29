package com.tencent.cosxml.assistant.transfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.cos.xml.common.Region;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.transfer.TransferListener;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cosxml.assistant.R;
import com.tencent.cosxml.assistant.common.CommonHelper;
import com.tencent.cosxml.assistant.common.ConfigUserInfoDialog;
import com.tencent.cosxml.assistant.common.FilePathHelper;
import com.tencent.cosxml.assistant.common.ScreenTools;
import com.tencent.cosxml.assistant.common.SimpleLoadingDialog;
import com.tencent.qcloud.core.logger.QCloudLogger;

import java.util.List;

/**
 * Created by rickenwang on 2018/6/29.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class TransferFragment extends Fragment implements TransferContract.View {

    private Spinner regionSpinner;
    private Spinner bucketSpinner;

    private ProgressBar uploadProgressBar;
    private TextView uploadState;
    private TextView uploadProgress;
    private TextView uploadDescribe;

    private ProgressBar downloadProgressBar;
    private TextView downloadSpeed;
    private TextView downloadProgress;
    private TextView downloadDescribe;

    //private Button createBucket;

    private Button uploadFile;

    private String chooseFileText;
    private String uploadFileText;
    private String cancelText;

    private TransferContract.Presenter presenter;

    private String[] regions = new String[]{Region.AP_Beijing.getRegion(),
            Region.AP_Guangzhou.getRegion(), Region.AP_Chengdu.getRegion(),
            Region.AP_Shanghai.getRegion(), Region.AP_Hongkong.getRegion(),
            Region.AP_Singapore.getRegion(), Region.EU_Frankfurt.getRegion(),
            Region.NA_Toronto.getRegion()};

    private String region = Region.AP_Beijing.getRegion();

    private String bucketName;

    private Handler mainHandler;

    private ContentLoadingProgressBar loadingProgressBar;

    private SimpleLoadingDialog loadingDialog;

    private ConfigUserInfoDialog configUserInfoDialog;

    private AlertDialog createBucketDialog;

    private final int OPEN_FILE_CODE = 10000;

    private String filePath;

    private final int UPLOAD_STEP_CHOOSE_FILE = 1;
    private final int UPLOAD_STEP_PREPARED = 2;
    private final int UPLOAD_STEP_UPLOADING = 3;

    private List<String> bucketList;

    private String transferId;

    @Override
    public void setUploadProgress(int progress) {

    }

    @Override
    public void setDownloadProgress(int progress) {

    }

    @Override
    public void showUploadResult(String message) {

    }

    @Override
    public void showDownloadResult(String message) {

    }

    @Override
    public void refreshBucketList(List<String> bucketList) {

        SpinnerAdapter spinnerAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_bucket_list, R.id.bucket, bucketList);
        bucketSpinner.setAdapter(spinnerAdapter);
        this.bucketList = bucketList;
    }

    @Override
    public void showInterError(final String error) {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void showError(final String error) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void showCreateBucketDialog(boolean enable) {

        if (enable) {
            createBucketDialog.show();
            createBucketDialog.getWindow().setLayout(ScreenTools.dip2px(getActivity(), 200), ScreenTools.dip2px(getActivity(), 150));
        } else {
            createBucketDialog.dismiss();
        }
    }

    @Override
    public void refreshUploadResult(final String result) {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                uploadDescribe.setText(result);
            }
        });
    }

    @Override
    public void refreshUploadProgress(final String progress) {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                uploadProgress.setText(progress);
            }
        });
    }

    @Override
    public void refreshUploadProgressBar(final int progress) {

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                uploadProgressBar.setProgress(progress);
            }
        });
    }

    @Override
    public void showLoadingDialog(boolean enable) {

        if (enable) {
            loadingDialog.show("please wait");
        } else {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void setPresenter(TransferContract.Presenter presenter) {

        this.presenter = presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(getActivity().getMainLooper());

        loadingDialog = new SimpleLoadingDialog(getActivity());
        configUserInfoDialog = new ConfigUserInfoDialog(getActivity(), new ConfigUserInfoDialog.OnConfirmListener() {
            @Override
            public void onSuccess(String appid, String host, int port) {
                presenter.configUserInfo(appid, host, port);
            }

            @Override
            public void onFailed(String error) {
                showError(error);
            }
        });

        View createBucketView = getActivity().getLayoutInflater().inflate(R.layout.dialog_create_bucket, null);
        final EditText bucketName = createBucketView.findViewById(R.id.bucket_name);
        Button confirm = createBucketView.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() { // create bucket
            @Override
            public void onClick(View v) {
                //

                String bucket = bucketName.getText().toString();
                if (!TextUtils.isEmpty(bucket)) {

                    showCreateBucketDialog(false);
                    showLoadingDialog(true);

                    presenter.createBucket(region, bucket, new TransferContract.CreateBucketListener() {
                        @Override
                        public void onSuccess(String bucketName, String region) {

                            //showLoadingDialog(false);
                            //showLoadingDialog(true);
                            refreshBucketListWithRegion(region);
                            showError("创建 bucket 成功");
                        }

                        @Override
                        public void onFailed(String error) {

                            showLoadingDialog(false);
                            showError("创建 bucket 失败：" + error);
                        }
                    });
                }
            }
        });

        Button cancel = createBucketView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateBucketDialog(false);
            }
        });

        createBucketDialog = new AlertDialog.Builder(getActivity())
                .setView(createBucketView)
                .create();

        chooseFileText = getActivity().getString(R.string.choose_file);
        uploadFileText = getActivity().getString(R.string.upload_file);
        cancelText = getActivity().getString(R.string.cancel);

        setHasOptionsMenu(true);
    }

    private void refreshBucketListWithRegion(String region) {

        presenter.listBucket(region, new TransferContract.ListBucketListener() {
            @Override
            public void onSuccess(final List<String> bucketList) {

                QCloudLogger.i("assistant", "list bucket success");

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshBucketList(bucketList);
                        showLoadingDialog(false);
                    }
                });
            }

            @Override
            public void onFailed(String error) {
                showError("获取 bucket 列表失败 : " + error);
                showLoadingDialog(false);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_transfer, container, false);

        regionSpinner = root.findViewById(R.id.regions);
        bucketSpinner = root.findViewById(R.id.buckets);

        regionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if (regions != null && regions.length > position) {

                    showLoadingDialog(true);
                    Log.d("tag", "position is " + position + ", region is " + regions[position]);
                    region = regions[position];
                    refreshBucketListWithRegion(region);

                } else {
                    showInterError("选择 Region 错误");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bucketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                bucketName = bucketList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uploadProgressBar = root.findViewById(R.id.upload_progressbar);
        uploadState = root.findViewById(R.id.upload_speed);
        uploadProgress = root.findViewById(R.id.upload_progress);
        uploadDescribe = root.findViewById(R.id.upload_describe);

//        downloadProgressBar = root.findViewById(R.id.download_progressbar);
//        downloadSpeed = root.findViewById(R.id.download_speed);
//        downloadProgress = root.findViewById(R.id.download_progress);
//        downloadDescribe = root.findViewById(R.id.download_describe);

        loadingProgressBar = root.findViewById(R.id.loading);

//        createBucket = root.findViewById(R.id.create_bucket);
//
//        createBucket.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                showCreateBucketDialog(true);
//            }
//        });

        uploadFile = root.findViewById(R.id.upload_file);
        uploadFile.setOnClickListener(new View.OnClickListener() {

            long startTime;
            long totalSize = 0;

            @Override
            public void onClick(View v) {
                if (uploadFile.getText().toString().equals(chooseFileText)) {  // choose file

                    openFileSelector();

                } else if (uploadFile.getText().toString().equals(uploadFileText)){ // upload file

                    startTime = System.currentTimeMillis();
                    transferId = presenter.uploadFile(region, bucketName, filePath, new TransferContract.UploadFileListener() {

                        @Override
                        public void onSuccess(String fileName) {
                            uploadTextStep(UPLOAD_STEP_CHOOSE_FILE);
                            long duration = System.currentTimeMillis() - startTime;

                            refreshUploadResult("upload success \r\n"
                                    + "文件大小 : " + CommonHelper.size(totalSize) + "\r\n"
                                    + "上传时长 : " + duration + "ms" + "\r\n"
                                    + "平均速度 : " + CommonHelper.size(totalSize * 1000 / duration) + "/s");

                            Log.i("test", "upload sucess");

                            refreshUploadProgress("");
                        }

                        @Override
                        public void onFailed(String error) {

                            uploadTextStep(UPLOAD_STEP_CHOOSE_FILE);
                            showError(error);
                        }

                        @Override
                        public void onProgress(long hasComplete, long total) {
                            totalSize = total;
                            refreshUploadProgress(CommonHelper.size(hasComplete) + "/" + CommonHelper.size(total));
                            refreshUploadProgressBar((int) (100 * hasComplete/total));
                        }
                    });

                    uploadTextStep(UPLOAD_STEP_UPLOADING);

                } else {  // cancel

                    if (transferId != null) {
                        presenter.cancelUpload(transferId, new TransferContract.CancelTransferListener() {
                            @Override
                            public void onSuccess() {
                                TransferFragment.this.showError("取消成功");
                            }

                            @Override
                            public void onFailed() {
                                TransferFragment.this.showError("取消失败");
                            }
                        });
                    }

                    uploadTextStep(UPLOAD_STEP_CHOOSE_FILE);
                }
            }
        });

        return root;
    }

    private void refreshTransferState(final String state) {

        Log.d("todo", "stat is " + state);

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                uploadState.setText(state);
            }
        });
    }

    private void openFileSelector() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_FILE_CODE);
    }

    private void uploadTextStep(int step) {

        switch (step) {

            case UPLOAD_STEP_CHOOSE_FILE : mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    uploadFile.setText(chooseFileText);
                }
            }); break;

            case UPLOAD_STEP_PREPARED : mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    uploadFile.setText(uploadFileText);
                    refreshUploadProgress("");
                    refreshUploadResult("");
                    refreshUploadProgressBar(0);
                }
            }); break;
            case UPLOAD_STEP_UPLOADING : mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    uploadFile.setText(cancelText);
                }
            }); break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_FILE_CODE && resultCode == Activity.RESULT_OK) {

            filePath = FilePathHelper.getPath(getActivity(), data.getData());
            uploadTextStep(UPLOAD_STEP_PREPARED);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.transfer, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.create_bucket : showCreateBucketDialog(true); break;
            case R.id.config_paras : configUserInfoDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
