package com.tencent.qcloud.cosxml.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.cosxml.sample.ObjectSample.DeleteMultiObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.DeleteObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.GetObjectACLSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.HeadObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.InitMultipartUploadSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.OptionObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.PutObjectACLSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.PutObjectSample;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

public class ObjectDemoActivity extends AppCompatActivity implements View.OnClickListener{

    TextView backText;
    Button appendObject;
    Button getObject;
    Button getObjectACL;
    Button putObject;
    Button putObjectACL;
    Button deleteObject;
    Button deleteMultipleObject;
    Button headObject;
    Button optionsObject;
    Button initiateMultipartUpload;
    Button uploadPart;
    Button listParts;
    Button completeMultipartUpload;
    Button abortMultipartUpload;
    Button multipartHelper;
    QServiceCfg qServiceCfg;
    ProgressDialog progressDialog;

    TextView sampleObjectText;
    TextView userObjectText;
    TextView uploadIdText;

    private Handler mainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    progressDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtras(msg.getData());
                    intent.setClass(ObjectDemoActivity.this,ResultActivity.class);
                    startActivity(intent);
                    break;
                case 1:
                    progressDialog.dismiss();
                    Toast.makeText(ObjectDemoActivity.this,"请确定选择了操作",Toast.LENGTH_SHORT).show();
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_demo);
        backText = (TextView)findViewById(R.id.back);

        qServiceCfg = QServiceCfg.instance(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("运行中......");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        backText.setOnClickListener(this);

        appendObject = (Button)findViewById(R.id.appendObject);
        getObject = (Button)findViewById(R.id.getObject);
        getObjectACL = (Button)findViewById(R.id.getObjectACL);
        putObject = (Button)findViewById(R.id.putObject);
        putObjectACL = (Button)findViewById(R.id.putObjectACL);
        deleteObject = (Button)findViewById(R.id.deleteObject);
        deleteMultipleObject = (Button)findViewById(R.id.deleteMultipleObject);
        headObject = (Button)findViewById(R.id.headObject);
        optionsObject = (Button)findViewById(R.id.optionsObject);
        initiateMultipartUpload = (Button)findViewById(R.id.initiateMultipartUpload);
        uploadPart = (Button)findViewById(R.id.uploadPart);
//        listParts = (Button)findViewById(R.id.listParts);
//        completeMultipartUpload = (Button)findViewById(R.id.completeMultipartUpload);
//        abortMultipartUpload = (Button)findViewById(R.id.abortMultipartUpload);
        multipartHelper = (Button)findViewById(R.id.multipartHelper);

        sampleObjectText = (TextView) findViewById(R.id.sample_object);
        userObjectText = (TextView) findViewById(R.id.user_object);
        uploadIdText = (TextView) findViewById(R.id.multipart_upload_id);
        sampleObjectText.setText("sample object：" + qServiceCfg.sampleCosPath);

        appendObject.setOnClickListener(this);
        getObject.setOnClickListener(this);
        getObjectACL.setOnClickListener(this);
        putObject.setOnClickListener(this);
        putObjectACL.setOnClickListener(this);
        deleteObject.setOnClickListener(this);
        deleteMultipleObject.setOnClickListener(this);
        headObject.setOnClickListener(this);
        optionsObject.setOnClickListener(this);
        initiateMultipartUpload.setOnClickListener(this);
        uploadPart.setOnClickListener(this);
//        listParts.setOnClickListener(this);
//        completeMultipartUpload.setOnClickListener(this);
//        abortMultipartUpload.setOnClickListener(this);
        multipartHelper.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        String userObject = qServiceCfg.getUserObject();
        userObjectText.setText(userObject == null ? "object： 暂无" : "object： " + userObject);
        userObjectText.setTag(userObject);

        String uploadId = qServiceCfg.getCurrentUploadId();
        uploadIdText.setText(uploadId == null ? "uploadId： null" : "uploadId： " + uploadId);
        uploadIdText.setTag(uploadId);
    }

    private boolean checkUserObject() {
        if (userObjectText.getTag() == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ObjectDemoActivity.this, "请先点击 \"Put Object\" 创建 Object", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
            return false;
        }

        return true;
    }

    private boolean checkUploadIdNotExisted() {
        if (uploadIdText.getTag() != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ObjectDemoActivity.this, "上传任务已存在", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
            return false;
        }

        return true;
    }

    private boolean checkUploadId() {
        if (uploadIdText.getTag() == null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ObjectDemoActivity.this, "请先点击 \"Initiate Multipart Upload\" 创建 上传任务", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
            return false;
        }

        return true;
    }

    private boolean checkOtherUploadTask() {
        if (!qServiceCfg.canUpload()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ObjectDemoActivity.this, "之前的上传任务未完全退出，请稍后再试", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
            return false;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.back){
            finish();
        }else{
            //start(id);
            startAsync(id);
        }
    }

    private boolean checkPreparedBigFile() {
        if (!qServiceCfg.hasMultiUploadFile()) {
            // 准备数据，把远程的一个大文件下载到本地，作为后续分片上传功能的sample
            startProgressActivity("第一次需要准备数据，可能需要较长时间", 0, qServiceCfg.bigfileCosPath, true);
            return false;
        }

        return true;
    }

    private boolean checkAppendFile() {
        if (qServiceCfg.getAppendFileUrl() == null) {
            // 使用sample文件作为追加块的文件
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ObjectDemoActivity.this, "请先点击 \"Get Object\" 下载文件", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
            return false;
        }

        return true;
    }

    private void startProgressActivity(String label, int taskType, String cosPath) {
        startProgressActivity(label, taskType, cosPath, false);
    }

    private void startProgressActivity(String label, int taskType, String cosPath, boolean critical) {
        Intent intent = new Intent(this, ProgressActivity.class);
        intent.putExtra("label", label);
        intent.putExtra("taskType", taskType);
        intent.putExtra("cosPath", cosPath);
        intent.putExtra("critical", critical);
        startActivity(intent);
    }

    public void start(final int id) {
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ResultHelper result = null;
                switch (id) {
                    case R.id.appendObject:
                        if (checkAppendFile()) {
                            startProgressActivity("正在上传", 3, null);
                        }
                        break;
                    case R.id.getObject:
                        startProgressActivity("正在下载", 0, qServiceCfg.sampleCosPath);
                        break;
                    case R.id.getObjectACL:
                        GetObjectACLSample getObjectACLSample = new GetObjectACLSample(qServiceCfg);
                        result = getObjectACLSample.start();
                        break;
                    case R.id.putObject:
                        PutObjectSample putObjectSample = new PutObjectSample(qServiceCfg);
                        result = putObjectSample.start();
                        break;
                    case R.id.putObjectACL:
                        if (checkUserObject()) {
                            PutObjectACLSample putObjectACLSample = new PutObjectACLSample(qServiceCfg);
                            result = putObjectACLSample.start();
                        }
                        break;
                    case R.id.deleteObject:
                        if (checkUserObject()) {
                            DeleteObjectSample deleteObjectSample = new DeleteObjectSample(qServiceCfg);
                            result = deleteObjectSample.start();
                        }
                        break;
                    case R.id.deleteMultipleObject:
                        if (checkUserObject()) {
                            DeleteMultiObjectSample deleteMultiObjectSample = new DeleteMultiObjectSample(qServiceCfg);
                            result = deleteMultiObjectSample.start();
                        }
                        break;
                    case R.id.headObject:
                        HeadObjectSample headObjectSample = new HeadObjectSample(qServiceCfg);
                        result = headObjectSample.start();
                        break;
                    case R.id.optionsObject:
                        OptionObjectSample optionObjectSample = new OptionObjectSample(qServiceCfg);
                        result = optionObjectSample.start();
                        break;
                    case R.id.initiateMultipartUpload:
                        if (checkPreparedBigFile() && checkUploadIdNotExisted()) {
                            InitMultipartUploadSample initMultipartUploadSample = new InitMultipartUploadSample(qServiceCfg);
                            result = initMultipartUploadSample.start();
                        }
                        break;
                    case R.id.uploadPart:
                        if (checkUploadId() && checkOtherUploadTask()) {
                            startProgressActivity("正在上传", 1, null);
                        }
                        break;
//                    case R.id.listParts:
//                        if (checkUploadId()) {
//                            ListPartsSample listPartsSample = new ListPartsSample(qServiceCfg);
//                            result = listPartsSample.start();
//                        }
//                        break;
//                    case R.id.completeMultipartUpload:
//                        if (checkUploadId()) {
//                            CompleteMultiUploadSample completeMultiUploadSample = new CompleteMultiUploadSample(qServiceCfg);
//                            result = completeMultiUploadSample.start();
//                        }
//                        break;
//                    case R.id.abortMultipartUpload:
//                        if (checkUploadId()) {
//                            AbortMultiUploadSample abortMultiUploadSample = new AbortMultiUploadSample(qServiceCfg);
//                            result = abortMultiUploadSample.start();
//                        }
//                        break;
                    case R.id.multipartHelper:
                        if(checkPreparedBigFile() && checkOtherUploadTask()) {
                            startProgressActivity("正在上传", 2, null);
                        }
                        break;
                }
                if(result != null){
                    Bundle bundle = new Bundle();
                    bundle.putString("RESULT",result.showMessage());
                    Message msg = mainHandler.obtainMessage();
                    msg.what = 0;
                    msg.setData(bundle);
                    mainHandler.sendMessage(msg);
                }else{
                    Message msg = mainHandler.obtainMessage();
                    msg.what = 1;
                    mainHandler.sendMessage(msg);
                }
            }
        }).start();
    }
    private void startAsync(int id){
        progressDialog.show();
        switch (id) {
            case R.id.appendObject:
                if (checkAppendFile()) {
                    startProgressActivity("正在上传", 3, null);
                }
                break;
            case R.id.getObject:
                startProgressActivity("正在下载", 0, qServiceCfg.sampleCosPath);
                break;
            case R.id.getObjectACL:
                GetObjectACLSample getObjectACLSample = new GetObjectACLSample(qServiceCfg);
                getObjectACLSample.startAsync(this);
                break;
            case R.id.putObject:
                PutObjectSample putObjectSample = new PutObjectSample(qServiceCfg);
                putObjectSample.startAsync(this);
                break;
            case R.id.putObjectACL:
                if (checkUserObject()) {
                    PutObjectACLSample putObjectACLSample = new PutObjectACLSample(qServiceCfg);
                    putObjectACLSample.startAsync(this);
                }
                break;
            case R.id.deleteObject:
                if (checkUserObject()) {
                    DeleteObjectSample deleteObjectSample = new DeleteObjectSample(qServiceCfg);
                    deleteObjectSample.startAsync(this);
                }
                break;
            case R.id.deleteMultipleObject:
                if (checkUserObject()) {
                    DeleteMultiObjectSample deleteMultiObjectSample = new DeleteMultiObjectSample(qServiceCfg);
                    deleteMultiObjectSample.startAsync(this);
                }
                break;
            case R.id.headObject:
                HeadObjectSample headObjectSample = new HeadObjectSample(qServiceCfg);
                headObjectSample.startAsync(this);
                break;
            case R.id.optionsObject:
                OptionObjectSample optionObjectSample = new OptionObjectSample(qServiceCfg);
                optionObjectSample.startAsync(this);
                break;
            case R.id.initiateMultipartUpload:
                if (checkUploadIdNotExisted() && checkPreparedBigFile()) {
                    InitMultipartUploadSample initMultipartUploadSample = new InitMultipartUploadSample(qServiceCfg);
                    initMultipartUploadSample.startAsync(this);
                }
                break;
            case R.id.uploadPart:
                if (checkUploadId() && checkOtherUploadTask()) {
                    startProgressActivity("正在上传", 1, null);
                }
                break;
//            case R.id.listParts:
//                if (checkUploadId()) {
//                    ListPartsSample listPartsSample = new ListPartsSample(qServiceCfg);
//                    listPartsSample.startAsync(this);
//                }
//                break;
//            case R.id.completeMultipartUpload:
//                if (checkUploadId()) {
//                    CompleteMultiUploadSample completeMultiUploadSample = new CompleteMultiUploadSample(qServiceCfg);
//                    completeMultiUploadSample.startAsync(this);
//                }
//                break;
//            case R.id.abortMultipartUpload:
//                if (checkUploadId()) {
//                    AbortMultiUploadSample abortMultiUploadSample = new AbortMultiUploadSample(qServiceCfg);
//                    abortMultiUploadSample.startAsync(this);
//                }
//                break;
            case R.id.multipartHelper:
                if(checkPreparedBigFile() && checkOtherUploadTask()) {
                    startProgressActivity("正在上传", 2, null);
                }
                break;
        }
    }

}
