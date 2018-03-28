package com.tencent.qcloud.cosxml.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.cos.xml.model.object.AbortMultiUploadResult;
import com.tencent.qcloud.cosxml.sample.ObjectSample.AbortMultiUploadSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.DeleteMultiObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.DeleteObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.GetObjectACLSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.GetObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.HeadObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.InitMultipartUploadSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.MultiUploadSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.MultipartUploadHelperSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.OptionObjectSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.PutObjectACLSample;
import com.tencent.qcloud.cosxml.sample.ObjectSample.PutObjectSample;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

public class ObjectDemoActivity extends AppCompatActivity implements View.OnClickListener{

    TextView backText;
    Button getObject;
    Button getObjectACL;
    Button putObject;
    Button putObjectACL;
    Button deleteObject;
    Button deleteMultipleObject;
    Button headObject;
    Button optionsObject;
    Button multipartHelper;
    Button mulitipart;
    Button abortMulit;
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

        getObject = (Button)findViewById(R.id.getObject);
        getObjectACL = (Button)findViewById(R.id.getObjectACL);
        putObject = (Button)findViewById(R.id.putObject);
        putObjectACL = (Button)findViewById(R.id.putObjectACL);
        deleteObject = (Button)findViewById(R.id.deleteObject);
        deleteMultipleObject = (Button)findViewById(R.id.deleteMultipleObject);
        headObject = (Button)findViewById(R.id.headObject);
        optionsObject = (Button)findViewById(R.id.optionsObject);
        mulitipart = (Button)findViewById(R.id.multipartUpload);
        multipartHelper = (Button)findViewById(R.id.multipartUploadHelper);
        abortMulit = (Button)findViewById(R.id.abortMultiUpload);

        sampleObjectText = (TextView) findViewById(R.id.sample_object);
        userObjectText = (TextView) findViewById(R.id.user_object);
        uploadIdText = (TextView) findViewById(R.id.multipart_upload_id);

        getObject.setOnClickListener(this);
        getObjectACL.setOnClickListener(this);
        putObject.setOnClickListener(this);
        putObjectACL.setOnClickListener(this);
        deleteObject.setOnClickListener(this);
        deleteMultipleObject.setOnClickListener(this);
        headObject.setOnClickListener(this);
        optionsObject.setOnClickListener(this);
        mulitipart.setOnClickListener(this);
        multipartHelper.setOnClickListener(this);
        abortMulit.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        String uploadId = qServiceCfg.getCurrentUploadId();
        uploadIdText.setText(uploadId == null ? "uploadId： null" : "uploadId： " + uploadId);
        uploadIdText.setTag(uploadId);
    }

    @Override
    protected void onPause() {
        Log.d("XIAO", "onPause");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onPause();
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


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.back){
            finish();
        }else{
            start(id);
//            startAsync(id);
        }
    }

    private boolean checkPreparedBigFile() {
//        if (!qServiceCfg.hasMultiUploadFile()) {
//            // 准备数据，把远程的一个大文件下载到本地，作为后续分片上传功能的sample
//            startProgressActivity("第一次需要准备数据，可能需要较长时间", 0, qServiceCfg.bigfileCosPath, true);
//            return false;
//        }

        return true;
    }

    private boolean checkAppendFile() {
//        if (qServiceCfg.getAppendFileUrl() == null) {
//            // 使用sample文件作为追加块的文件
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Toast.makeText(ObjectDemoActivity.this, "请先点击 \"Get Object\" 下载文件", Toast.LENGTH_LONG).show();
//                    progressDialog.dismiss();
//                }
//            });
//            return false;
//        }

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
                    case R.id.putObject:
                        PutObjectSample putObjectSample = new PutObjectSample(qServiceCfg);
                        result = putObjectSample.start();
                        break;
                    case R.id.headObject:
                        HeadObjectSample headObjectSample = new HeadObjectSample(qServiceCfg);
                        result = headObjectSample.start();
                        break;
                    case R.id.optionsObject:
                        OptionObjectSample optionObjectSample = new OptionObjectSample(qServiceCfg);
                        result = optionObjectSample.start();
                        break;
                    case R.id.getObject:
                        GetObjectSample getObjectSample = new GetObjectSample(qServiceCfg);
                        result = getObjectSample.start();
                        break;
                    case R.id.putObjectACL:
                        PutObjectACLSample putObjectACLSample = new PutObjectACLSample(qServiceCfg);
                        result = putObjectACLSample.start();
                        break;
                    case R.id.getObjectACL:
                        GetObjectACLSample getObjectACLSample = new GetObjectACLSample(qServiceCfg);
                        result = getObjectACLSample.start();
                        break;
                    case R.id.deleteObject:
                        DeleteObjectSample deleteObjectSample = new DeleteObjectSample(qServiceCfg);
                        result = deleteObjectSample.start();
                        break;
                    case R.id.deleteMultipleObject:
                        DeleteMultiObjectSample deleteMultiObjectSample = new DeleteMultiObjectSample(qServiceCfg);
                        result = deleteMultiObjectSample.start();
                        break;
                    case R.id.multipartUpload:
                        MultiUploadSample multiUploadSample = new MultiUploadSample(qServiceCfg);
                        result = multiUploadSample.start();
                        break;
                    case R.id.multipartUploadHelper:
                        MultipartUploadHelperSample multipartUploadHelperSample = new MultipartUploadHelperSample(qServiceCfg);
                        result = multipartUploadHelperSample.start();
                        break;
                    case R.id.abortMultiUpload:
                        AbortMultiUploadSample abortMultiUploadSample = new AbortMultiUploadSample(qServiceCfg);
                        result = abortMultiUploadSample.start();
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
            case R.id.putObject:
                PutObjectSample putObjectSample = new PutObjectSample(qServiceCfg);
                putObjectSample.startAsync(this);
                break;
            case R.id.headObject:
                HeadObjectSample headObjectSample = new HeadObjectSample(qServiceCfg);
                headObjectSample.startAsync(this);
                break;
            case R.id.optionsObject:
                OptionObjectSample optionObjectSample = new OptionObjectSample(qServiceCfg);
                optionObjectSample.startAsync(this);
                break;
            case R.id.getObject:
                GetObjectSample getObjectSample = new GetObjectSample(qServiceCfg);
                getObjectSample.startAsync(this);
                break;
            case R.id.putObjectACL:
                PutObjectACLSample putObjectACLSample = new PutObjectACLSample(qServiceCfg);
                putObjectACLSample.startAsync(this);
                break;
            case R.id.getObjectACL:
                GetObjectACLSample getObjectACLSample = new GetObjectACLSample(qServiceCfg);
                getObjectACLSample.startAsync(this);
                break;
            case R.id.deleteObject:
                DeleteObjectSample deleteObjectSample = new DeleteObjectSample(qServiceCfg);
                deleteObjectSample.startAsync(this);
                break;
            case R.id.deleteMultipleObject:
                DeleteMultiObjectSample deleteMultiObjectSample = new DeleteMultiObjectSample(qServiceCfg);
                deleteMultiObjectSample.startAsync(this);
                break;
            case R.id.multipartUpload:
                MultiUploadSample multiUploadSample = new MultiUploadSample(qServiceCfg);
                multiUploadSample.startAsync(this);
                break;
//            case R.id.multipartUploadHelper:
//                MultipartUploadHelperSample multipartUploadHelperSample = new MultipartUploadHelperSample(qServiceCfg);
//                multipartUploadHelperSample.start();
//                break;
            case R.id.abortMultiUpload:
                AbortMultiUploadSample abortMultiUploadSample = new AbortMultiUploadSample(qServiceCfg);
                abortMultiUploadSample.startAsync(this);
                break;
        }
    }

}
