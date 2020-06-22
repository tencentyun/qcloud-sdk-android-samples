package com.tencent.qcloud.costransferpractice.transfer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.cos.xml.CosXmlService;
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
import com.tencent.qcloud.costransferpractice.BuildConfig;
import com.tencent.qcloud.costransferpractice.CosServiceFactory;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.FilePathHelper;
import com.tencent.qcloud.costransferpractice.common.Utils;
import com.tencent.qcloud.costransferpractice.common.base.BaseActivity;

import java.io.File;

import static com.tencent.qcloud.costransferpractice.object.ObjectActivity.ACTIVITY_EXTRA_BUCKET_NAME;
import static com.tencent.qcloud.costransferpractice.object.ObjectActivity.ACTIVITY_EXTRA_FOLDER_NAME;
import static com.tencent.qcloud.costransferpractice.object.ObjectActivity.ACTIVITY_EXTRA_REGION;

/**
 * Created by jordanqin on 2020/6/18.
 * 文件上传页面
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class UploadActivity extends BaseActivity implements View.OnClickListener {
    private final int OPEN_FILE_CODE = 10001;

    //图片文件本地显示
    private ImageView iv_image;
    //本地文件路径
    private TextView tv_name;
    //上传状态
    private TextView tv_state;
    //上传进度
    private TextView tv_progress;
    //上传进度条
    private ProgressBar pb_upload;

    //操作按钮（开始和取消）
    private Button btn_left;
    //操作按钮（暂停和恢复）
    private Button btn_right;

    private String bucketName;
    private String bucketRegion;
    private String folderName;

    /**
     * {@link CosXmlService} 是您访问 COS 服务的核心类，它封装了所有 COS 服务的基础 API 方法。
     * <p>
     * 每一个{@link CosXmlService} 对象只能对应一个 region，如果您需要同时操作多个 region 的
     * Bucket，请初始化多个 {@link CosXmlService} 对象。
     */
    private CosXmlService cosXmlService;

    /**
     * {@link TransferManager} 进一步封装了 {@link CosXmlService} 的上传和下载接口，当您需要
     * 上传文件到 COS 或者从 COS 下载文件时，请优先使用这个类。
     */
    private TransferManager transferManager;
    private COSXMLUploadTask cosxmlTask;
    /**
     * 上传时的本地和 COS 路径
     */
    private String currentUploadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_activity);

        bucketName = getIntent().getStringExtra(ACTIVITY_EXTRA_BUCKET_NAME);
        bucketRegion = getIntent().getStringExtra(ACTIVITY_EXTRA_REGION);
        folderName = getIntent().getStringExtra(ACTIVITY_EXTRA_FOLDER_NAME);

        iv_image = findViewById(R.id.iv_image);
        tv_name = findViewById(R.id.tv_name);
        tv_state = findViewById(R.id.tv_state);
        tv_progress = findViewById(R.id.tv_progress);
        pb_upload = findViewById(R.id.pb_upload);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);

        btn_right.setOnClickListener(this);
        btn_left.setOnClickListener(this);

        if (TextUtils.isEmpty(BuildConfig.COS_SECRET_ID) || TextUtils.isEmpty(BuildConfig.COS_SECRET_KEY)) {
            finish();
        }

        cosXmlService = CosServiceFactory.getCosXmlService(this, bucketRegion, BuildConfig.COS_SECRET_ID, BuildConfig.COS_SECRET_KEY, true);
        TransferConfig transferConfig = new TransferConfig.Builder().build();
        transferManager = new TransferManager(cosXmlService, transferConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.choose_photo) {
            //为了示例简单明了 正在处理文件时不允许选择文件
            if (cosxmlTask == null) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, OPEN_FILE_CODE);
                return true;
            } else {
                toastMessage("当前文件未处理完毕，不能选择新文件");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_left) {
            if ("开始".contentEquals(btn_left.getText())) {
                upload();
            } else {//取消
                if (cosxmlTask != null) {
                    cosxmlTask.cancel();
                    finish();
                } else {
                    toastMessage("操作失败");
                }
            }
        } else if (v.getId() == R.id.btn_right) {
            if ("暂停".contentEquals(btn_right.getText())) {
                if (cosxmlTask != null && cosxmlTask.getTaskState() == TransferState.IN_PROGRESS) {
                    cosxmlTask.pause();
                    btn_right.setText("恢复");
                } else {
                    toastMessage("操作失败");
                }
            } else {//恢复
                if (cosxmlTask != null && cosxmlTask.getTaskState() == TransferState.PAUSED) {
                    cosxmlTask.resume();
                    btn_right.setText("暂停");
                } else {
                    toastMessage("操作失败");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OPEN_FILE_CODE && resultCode == Activity.RESULT_OK && data != null) {
            String path = FilePathHelper.getAbsPathFromUri(this, data.getData());
            if (TextUtils.isEmpty(path)) {
                iv_image.setImageBitmap(null);
                tv_name.setText("");
            } else {
                //直接用选择的文件URI去展示图片
                //如果所选文件不是图片文件，则展示文件图标
                try{
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if(bitmap!=null){
                        iv_image.setImageBitmap(bitmap);
                    } else {
                        iv_image.setImageResource(R.drawable.file);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                tv_name.setText(path);
            }
            currentUploadPath = path;
            pb_upload.setProgress(0);
            tv_progress.setText("");
            tv_state.setText("无");
        }
    }

    /**
     * 刷新上传状态
     * @param state 状态 {@link TransferState}
     */
    private void refreshUploadState(final TransferState state) {
        uiAction(new Runnable() {
            @Override
            public void run() {
                tv_state.setText(state.toString());
            }
        });

    }

    /**
     * 刷新上传进度
     * @param progress 已上传文件大小
     * @param total 文件总大小
     */
    private void refreshUploadProgress(final long progress, final long total) {
        uiAction(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                pb_upload.setProgress((int) (100 * progress / total));
                tv_progress.setText(Utils.readableStorageSize(progress) + "/" + Utils.readableStorageSize(total));
            }
        });
    }

    private void upload() {
        if (TextUtils.isEmpty(currentUploadPath)) {
            toastMessage("请先选择文件");
            return;
        }

        if (cosxmlTask == null) {
            File file = new File(currentUploadPath);
            String cosPath;
            if(TextUtils.isEmpty(folderName)){
                cosPath = file.getName();
            } else {
                cosPath = folderName + File.separator + file.getName();
            }
            cosxmlTask = transferManager.upload(bucketName, cosPath,
                    currentUploadPath, null);

            cosxmlTask.setTransferStateListener(new TransferStateListener() {
                @Override
                public void onStateChanged(final TransferState state) {
                    refreshUploadState(state);
                }
            });
            cosxmlTask.setCosXmlProgressListener(new CosXmlProgressListener() {
                @Override
                public void onProgress(final long complete, final long target) {
                    refreshUploadProgress(complete, target);
                }
            });

            cosxmlTask.setCosXmlResultListener(new CosXmlResultListener() {
                @Override
                public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                    COSXMLUploadTask.COSXMLUploadTaskResult cOSXMLUploadTaskResult = (COSXMLUploadTask.COSXMLUploadTaskResult) result;

                    cosxmlTask = null;
                    toastMessage("上传成功");
                    setResult(RESULT_OK);
                    uiAction(new Runnable() {
                        @Override
                        public void run() {
                            btn_left.setVisibility(View.GONE);
                            btn_right.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                    if (cosxmlTask.getTaskState() != TransferState.PAUSED) {
                        cosxmlTask = null;
                        uiAction(new Runnable() {
                            @Override
                            public void run() {
                                pb_upload.setProgress(0);
                                tv_progress.setText("");
                                tv_state.setText("无");
                            }
                        });
                    }
                    exception.printStackTrace();
                    serviceException.printStackTrace();
                }
            });
            btn_left.setText("取消");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cosXmlService != null) {
            cosXmlService.release();
        }
    }
}
