package com.tencent.qcloud.csp.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private static Context context;
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1000;
    private final int OPEN_FILE_CODE = 10000;

    private RemoteStorage remoteStorage;

    private String appid = "1255000008";
    private String region = "wh";
    private String domainSuffix = "yun.ccb.com";

    EditText bucketName;

    TaskFactory taskFactory;

    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        remoteStorage = new RemoteStorage(this, appid, region, domainSuffix);
        bucketName = findViewById(R.id.bucket_name);
        taskFactory = TaskFactory.getInstance();
        requestPermissions();
    }

    public void onGetServiceClick(View view) {

        taskFactory.createGetServiceTask(this, remoteStorage).execute();

    }

    public void onPutBucketClick(View view) {

        String bucketNameText = bucketName.getText().toString();
        if (!TextUtils.isEmpty(bucketNameText)) {
            taskFactory.createPutBucketTask(this, remoteStorage, bucketNameText).execute();
        }
    }

    public void onPutObjectClick(View view) {

        openFileSelector();
    }

    private void requestPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "您必须允许读取外部存储权限，否则无法上传文件", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_FILE_CODE && resultCode == Activity.RESULT_OK) {

            filePath = FilePathHelper.getPath(this, data.getData());

            String bucketNameText = bucketName.getText().toString();
            if (!TextUtils.isEmpty(bucketNameText)) {
                //taskFactory.createPutObjectTask(this, remoteStorage, bucketNameText, filePath, filePath).execute();
                taskFactory.createPutObjectTask(this, remoteStorage, bucketNameText, filePath, filePath).execute();
            }
        }
    }

    private void openFileSelector() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, OPEN_FILE_CODE);
    }


}
