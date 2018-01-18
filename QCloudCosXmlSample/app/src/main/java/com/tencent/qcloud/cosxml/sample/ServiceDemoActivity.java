package com.tencent.qcloud.cosxml.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tencent.qcloud.cosxml.sample.R;
import com.tencent.qcloud.cosxml.sample.ServiceSample.GetServiceSample;
import com.tencent.qcloud.cosxml.sample.common.QServiceCfg;

public class ServiceDemoActivity extends AppCompatActivity implements View.OnClickListener{

    TextView backText;
    Button serviceBtn;
    QServiceCfg qServiceCfg;
    private  Handler mainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    progressDialog.dismiss();
                    Intent intent = new Intent();
                    intent.putExtras(msg.getData());
                    intent.setClass(ServiceDemoActivity.this,ResultActivity.class);
                    startActivity(intent);
            }

        }
    };

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_demo);
        serviceBtn = (Button)findViewById(R.id.GetService);
        backText = (TextView)findViewById(R.id.back);

        qServiceCfg = QServiceCfg.instance(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("运行中......");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        backText.setOnClickListener(this);
        serviceBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.back:
                finish();
                break;
            case R.id.GetService:
               // start();
                startAsync();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        Log.d("XIAO", "onPause");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onPause();
    }

    protected void start(){
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetServiceSample getServiceSample = new GetServiceSample(qServiceCfg);
                ResultHelper result = getServiceSample.start();
                Bundle bundle = new Bundle();
                bundle.putString("RESULT",result.showMessage());
                Message msg = mainHandler.obtainMessage();
                msg.what = 0;
                msg.setData(bundle);
                mainHandler.sendMessage(msg);
        }}).start();
    }

    protected void startAsync(){
        progressDialog.show();
        GetServiceSample getServiceSample = new GetServiceSample(qServiceCfg);
        getServiceSample.startAsync(this);
    }

}
