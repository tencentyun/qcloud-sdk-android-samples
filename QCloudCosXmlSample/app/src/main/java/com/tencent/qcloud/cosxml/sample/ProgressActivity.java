package com.tencent.qcloud.cosxml.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.cosxml.sample.ObjectSample.AbortMultiUploadSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.CompleteMultiUploadSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.GetObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.ListPartsSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.MultipartUploadHelperSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.UploadPartSample;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

public class ProgressActivity extends AppCompatActivity {

    QServiceCfg qServiceCfg;
    ProgressBar progressBar;
    Button abortButton;
    Button listPartButton;
    int taskType;
    boolean criticalOp;
    UploadPartSample uploadPartSample;
    MultipartUploadHelperSample multipartUploadHelperSample;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    progressBar.setProgress((Integer) msg.obj);
                    break;
                case 1:
                    Toast.makeText(ProgressActivity.this, "任务完成", Toast.LENGTH_LONG).show();
                    if (taskType == 1) {
                        // 分片上传完成后，必须要做一次complete操作，才能完成文件的上传
                        onComplete();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_progress);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setMax(100);

        abortButton = (Button) findViewById(R.id.abort);
        listPartButton = (Button) findViewById(R.id.list_part);

        qServiceCfg = QServiceCfg.instance(this);

        String cosPath = getIntent().getStringExtra("cosPath");
        String label = getIntent().getStringExtra("label");

        // 0: 下载
        // 1：普通分片上传
        // 2：用upload helper分片上传
        // 3：追加块
        taskType = getIntent().getIntExtra("taskType", 0);
        // 对于首次下载分片的sample数据，不允许退出，demo内部状态，可以无视
        criticalOp = getIntent().getBooleanExtra("critical", false);

        if (taskType == 1) {
            abortButton.setVisibility(View.VISIBLE);
            abortButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onAbort();
                }
            });
            listPartButton.setVisibility(View.VISIBLE);
            listPartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onListPart();
                }
            });
        }

        ((TextView) findViewById(R.id.label)).setText(label);

        onGetAsync(cosPath, taskType);
    }

    void onListPart() {
        ListPartsSample listPartsSample = new ListPartsSample(qServiceCfg);
//        listPartsSample.startAsync(this);
    }

    @Override
    public void onBackPressed() {
        if (taskType == 1 || taskType == 2) {
            // 对于上传任务，因为耗时较长，退出就终止任务
            new AlertDialog.Builder(this)
                    .setTitle("是否要终止任务")
                    .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onAbort();
                            ProgressActivity.super.onBackPressed();

                        }
                    }).create().show();
        } else if (criticalOp) {
            // 对于首次下载分片的sample数据，不允许退出，否则会影响分片上传的功能演示
            Toast.makeText(ProgressActivity.this, "请耐心等待任务完成~", Toast.LENGTH_LONG).show();
        } else {
            super.onBackPressed();
        }
    }

    void onComplete() {
        CompleteMultiUploadSample completeMultiUploadSample = new CompleteMultiUploadSample(qServiceCfg);
//        completeMultiUploadSample.startAsync(this);
    }

    void onAbort() {
        if (uploadPartSample != null) {
//            uploadPartSample.abort();
            AbortMultiUploadSample abortMultiUploadSample = new AbortMultiUploadSample(qServiceCfg);
            abortMultiUploadSample.startAsync(this);
        } else if (multipartUploadHelperSample != null) {
//            multipartUploadHelperSample.abort();
        }
    }

//    void onGet(final String cosPath, final int taskType) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (taskType == 0) {
//                    GetObjectSample getObjectSample = new GetObjectSample(qServiceCfg, cosPath, handler);
//                    getObjectSample.start();
//                } else if (taskType == 1) {
//                    uploadPartSample = new UploadPartSample(qServiceCfg, handler);
//                    uploadPartSample.start();
//                } else if (taskType == 2) {
//                    MultipartUploadHelperSample multipartUploadHelperSample = new MultipartUploadHelperSample(qServiceCfg, handler);
//                    multipartUploadHelperSample.start();
//                }
//            }
//        }).start();
//    }

    void onGetAsync(final String cosPath, final int taskType) {
//        if (taskType == 0) {
//            GetObjectSample getObjectSample = new GetObjectSample(qServiceCfg, cosPath, handler);
//            getObjectSample.startAsync(this);
//        } else if (taskType == 1) {
//            uploadPartSample = new UploadPartSample(qServiceCfg, handler);
//            uploadPartSample.startAsync(this);
//        } else if (taskType == 2) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    multipartUploadHelperSample = new MultipartUploadHelperSample(qServiceCfg, handler);
//                    ResultHelper result = multipartUploadHelperSample.start();
//                    if(result != null){
//                        Intent intent = new Intent(ProgressActivity.this, ResultActivity.class);
//                        intent.putExtra("RESULT", result.showMessage());
//                        startActivity(intent);
//                    } else {
//                        Toast.makeText(ProgressActivity.this, "result is null", Toast.LENGTH_LONG).show();
//                    }
//                    finish();
//                }
//            }).start();
//        } else if (taskType == 3) {
//            AppendObjectSample appendObjectSample = new AppendObjectSample(qServiceCfg, handler);
//            appendObjectSample.startAsync(this);
//        }
    }
}
