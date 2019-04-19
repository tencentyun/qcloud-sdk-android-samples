package com.bradyxiao.cos_weak_network_practice;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final static int OPEN_FILE = 1000;
    private ResumeHelper resumeHelper;
    private String srcPath;
    private String cosPath;
    private TextView srcPathTextView;
    private TextView cosPathTextView;
    private TextView progressTextView;
    private TextView tryAgainTextView;
    private TextView resultTextView;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        grantPermission();
        initView();

        resumeHelper = new ResumeHelper(this.getApplicationContext());
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

    private void initView(){
        uploadButton = findViewById(R.id.UPLOAD);
        uploadButton.setOnClickListener(this);
        findViewById(R.id.COS_PATH_EDIT).setOnClickListener(this);
        findViewById(R.id.FILE_PATH_SELECT).setOnClickListener(this);
        srcPathTextView = findViewById(R.id.FILE_PATH_DESC);
        cosPathTextView = findViewById(R.id.COS_PATH_DESC);
        progressTextView = findViewById(R.id.PROGRESS);
        tryAgainTextView = findViewById(R.id.TRY_AGAIN);
        resultTextView = findViewById(R.id.RESULT);
    }

    private void selectFile(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, OPEN_FILE);
    }

    private void setCosPath(){
        String tag = "Dialog Fragment";
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(tag);
        if(prev != null){
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);

        final EditDialog editDialog = new EditDialog();
        editDialog.setCancelable(false);
        editDialog.setOnSelectListener(new EditDialog.OnSelectListener() {
            @Override
            public void onConfirm(String value) {
                cosPath = value;
                cosPathTextView.setText(cosPath);
                editDialog.dismiss();
            }

            @Override
            public void onCancel() {
                cosPathTextView.setText("");
                editDialog.dismiss();
            }
        });
        editDialog.show(fragmentTransaction, tag);

    }

    private void upload(){

        progressTextView.setText("");
        tryAgainTextView.setText("");
        resultTextView.setText("");

        if(!checkParameter())return;
        String uploadId = null;
        long sliceSize = 1024 * 1024;
        resumeHelper.enableTryAgain(false); //允许弱网下重试一次
        resumeHelper.setOnStateListener(new ResumeHelper.OnStateListener() {
            @Override
            public void onProgress(long completed, long total) {
                String progress = "progress: " + completed + "/" + total;
                progressTextView.setText(progress);
            }

            @Override
            public void onFinish(String message) {
                resultTextView.setText(message);
                uploadButton.setClickable(true);
            }

            @Override
            public void onTryAgain() {
                String tryAgain = "try again ....";
                tryAgainTextView.setText(tryAgain);
            }
        });
        resumeHelper.upload(srcPath, cosPath, uploadId, sliceSize);
        uploadButton.setClickable(false);

    }

    private boolean checkParameter(){
        if(TextUtils.isEmpty(srcPath)){
            Toast.makeText(this, "请输入 srcPath 的值",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if(TextUtils.isEmpty(cosPath)){
            Toast.makeText(this, "请输入 cosPath 的值",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        File file = new File(srcPath);
        if(!file.exists()){
            Toast.makeText(this, "请输入 srcPath 对应的文件不存在",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.UPLOAD:
                upload();
                break;
            case R.id.COS_PATH_EDIT:
                setCosPath();
                break;
            case R.id.FILE_PATH_SELECT:
                selectFile();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == OPEN_FILE && resultCode == RESULT_OK){
            Uri uri = data.getData();
            //先排除 file
            if("file".equalsIgnoreCase(uri.getScheme())){
                srcPath = uri.getPath();
                srcPathTextView.setText(srcPath);
                return;
            }
            //根据 SDK_INT 处理 content
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                //4.4及以上系统
                srcPath = getPathFromURI(this, uri);
                srcPathTextView.setText(srcPath);
                return;
            }else { // 4.4 以下系统
                srcPath = searchFromURI(this, uri, null, null);
                srcPathTextView.setText(srcPath);
                return;
            }
        }
        srcPathTextView.setText("");
    }

    private String searchFromURI(Context context, Uri contentUri, String selection,
                                  String[] selectionArgs){
        String columnName = "_data";
        String[] projection = {columnName};
        Cursor cursor = null;
        try{
            cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
            if(cursor != null && cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndexOrThrow(columnName);
                return cursor.getString(columnIndex);
            }
        }finally {
            if(cursor != null){
                cursor.close();
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getPathFromURI(Context context, Uri contentUri){
        if(DocumentsContract.isDocumentUri(context, contentUri)){
            return getPathFromDocument(context, contentUri);
        }else if("content".equalsIgnoreCase(contentUri.getScheme())){
            return searchFromURI(context, contentUri, null, null);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getPathFromDocument(Context context, Uri documentUri){
        String id = DocumentsContract.getDocumentId(documentUri);
        if("com.android.externalstorage.documents".equals(documentUri.getAuthority())){
            String[] splits = id.split(":");
            String type = splits[0];
            if("primary".equalsIgnoreCase(type)){
                return Environment.getExternalStorageDirectory() + "/" + splits[1];
            }
        }else if( "com.android.providers.downloads.documents".equals(documentUri.getAuthority())){
            Uri downloadDocumentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                    Long.valueOf(id));
            return searchFromURI(context, downloadDocumentUri, null, null);
        }else if("com.android.providers.media.documents".equals(documentUri.getAuthority())){
            String[] splits = id.split(":");
            String type = splits[0];
            Uri mediaDocumentUri = null;
            if("image".equals(type)){
                mediaDocumentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            }else if("video".equals(type)){
                mediaDocumentUri =  MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            }else if("audio".equals(type)){
                mediaDocumentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String selection = "_id=?";
            String[] selectArgs = new String[]{splits[1]};
            return searchFromURI(context, mediaDocumentUri, selection, selectArgs);
        }
        return null;
    }
}
