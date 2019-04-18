package com.bradyxiao.cos_weak_network_practice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ResumeHelper resumeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grantPermission();
        resumeHelper = new ResumeHelper(this.getApplicationContext());
        findViewById(R.id.UPLOAD).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cosPath = "resume.txt";
                String srcPath = Environment.getExternalStorageDirectory().getPath() + "/slice_10M";
                String uploadId = null;
                long sliceSize = 1024 * 1024;
                resumeHelper.upload(srcPath, cosPath, uploadId, sliceSize);
            }
        });
    }

    private void grantPermission(){
        List<String> permission = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(permission.size() > 0){
            ActivityCompat.requestPermissions(this, permission.toArray(new String[permission.size()]), 1000);
        }
    }
}
