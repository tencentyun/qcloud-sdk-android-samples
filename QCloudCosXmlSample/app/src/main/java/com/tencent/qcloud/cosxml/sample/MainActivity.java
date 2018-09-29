package com.tencent.qcloud.cosxml.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button serviceBtn;
    Button bucketBtn;
    Button objectBtn;
    Button exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Dynamic add permissions
        final int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck== PackageManager.PERMISSION_GRANTED) {
            Log.d("XIAO", "grant all permissions");
        } else if (permissionCheck <= PackageManager.PERMISSION_DENIED) {
            Log.d("XIAO", "need to grant permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        serviceBtn = (Button)findViewById(R.id.serviceTest);
        objectBtn = (Button)findViewById(R.id.objectTest);
        bucketBtn = (Button)findViewById(R.id.bucketTest);
        exitBtn = (Button)findViewById(R.id.kill);
        serviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ServiceDemoActivity.class);
                startActivity(intent);
            }
        });
        objectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ObjectDemoActivity.class);
                startActivity(intent);
            }
        });
        bucketBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BucketDemoActivity.class);
                startActivity(intent);
            }
        });
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.os.Process.killProcess(Process.myPid());
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.w("XIAO","requestCode =" + requestCode + "; permission =" + permissions[1] + "; grantResult =" + grantResults[0]);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


}

