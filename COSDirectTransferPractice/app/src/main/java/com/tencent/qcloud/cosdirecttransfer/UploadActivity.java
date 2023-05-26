package com.tencent.qcloud.cosdirecttransfer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.qcloud.cosdirecttransfer.common.BaseActivity;
import com.tencent.qcloud.cosdirecttransfer.common.FilePathHelper;
import com.tencent.qcloud.cosdirecttransfer.common.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jordanqin on 2020/6/18.
 * 文件直传页面
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class UploadActivity extends BaseActivity {
    private static final String TAG = "UploadActivity";
    // 读取本地文件权限回调
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10005;
    // 选择文件回调
    private final int OPEN_FILE_CODE = 10001;
    private static final int BUFFER_SIZE = 4096;

    //图片文件本地显示
    private ImageView iv_image;
    //本地文件路径
    private TextView tv_name;
    //上传进度
    private TextView tv_progress;
    //上传进度条
    private ProgressBar pb_upload;
    //开始按钮
    private Button btn_start;
    // 选择的本地路径
    private String currentUploadPath;

    //用于执行网络操作的线程池
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_activity);

        //申请权限
        requestPermissions();

        iv_image = findViewById(R.id.iv_image);
        tv_name = findViewById(R.id.tv_name);
        tv_progress = findViewById(R.id.tv_progress);
        pb_upload = findViewById(R.id.pb_upload);
        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(v -> {
            if (TextUtils.isEmpty(currentUploadPath)) {
                toastMessage("请先选择文件");
                return;
            }

            executor.execute(() -> uploadFile(currentUploadPath, (ProgressListener) (bytesUploaded, totalBytes) ->
                    runOnUiThread(() -> refreshUploadProgress(bytesUploaded, totalBytes))));
        });
    }

    /**
     * 上传文件
     * @param filePath 本地文件路径
     * @param listener 进度回调
     */
    private void uploadFile(String filePath, final ProgressListener listener) {
        runOnUiThread(() -> btn_start.setEnabled(false));

        File file = new File(filePath);
        // 获取直传签名等数据
        JSONObject directTransferData = getStsDirectSign(FilePathHelper.getFileExtension(file));
        if (directTransferData == null) {
            toastMessage("getStsDirectSign fail");
            runOnUiThread(() -> btn_start.setEnabled(true));
            return;
        }
        String cosHost = directTransferData.optString("cosHost");
        String cosKey = directTransferData.optString("cosKey");
        String authorization = directTransferData.optString("authorization");
        String securityToken = directTransferData.optString("securityToken");

        //生成上传的url
        URL url;
        try {
            url = new URL(String.format("https://%s/%s", cosHost, cosKey));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            runOnUiThread(() -> btn_start.setEnabled(true));
            return;
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);

            // 设置请求头
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Content-Length", String.valueOf(file.length()));
            conn.setRequestProperty("Authorization", authorization);
            conn.setRequestProperty("x-cos-security-token", securityToken);
            conn.setRequestProperty("Host", cosHost);

            // 获取输出流
            OutputStream outputStream = conn.getOutputStream();

            // 读取文件并上传
            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                if (listener != null) {
                    listener.onProgress(totalBytesRead, file.length());
                }
            }
            // 关闭流
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            // 获取响应码
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                toastMessage("上传成功");
            } else {
                Log.e(TAG, "uploadFile HTTP error code: " + responseCode);
                toastMessage("uploadFile HTTP error code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "uploadFile Error sending PUT request: " + e.getMessage());
            toastMessage("uploadFile Error sending PUT request: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        runOnUiThread(() -> btn_start.setEnabled(true));
    }

    /**
     * 获取直传的url和签名等
     *
     * @param ext 文件后缀 直传后端会根据后缀生成cos key
     * @return 直传url和签名等
     */
    private JSONObject getStsDirectSign(String ext) {
        runOnUiThread(() -> setLoading(true));
        // 获取上传路径和签名
        HttpURLConnection getConnection = null;
        try {
            //直传签名业务服务端url（正式环境 请替换成正式的直传签名业务url）
            //直传签名业务服务端代码示例可以参考：https://github.com/tencentyun/cos-demo/blob/main/server/direct-sign/nodejs/app.js
            URL url = new URL("http://127.0.0.1:3000/sts-direct-sign?ext=" + ext);
            getConnection = (HttpURLConnection) url.openConnection();
            getConnection.setRequestMethod("GET");

            int responseCode = getConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(getConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(stringBuilder.toString());
                    if(jsonObject.has("code") && jsonObject.optInt("code") == 0){
                        runOnUiThread(() -> setLoading(false));
                        return jsonObject.optJSONObject("data");
                    } else {
                        Log.e(TAG, String.format("getStsDirectSign error code: %d, error message: %s", jsonObject.optInt("code"), jsonObject.optString("message")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e(TAG, "getStsDirectSign HTTP error code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getStsDirectSign Error sending GET request: " + e.getMessage());
        } finally {
            if (getConnection != null) {
                getConnection.disconnect();
            }
        }
        runOnUiThread(() -> setLoading(false));
        return null;
    }

    /**
     * 刷新上传进度
     * @param progress 已上传文件大小
     * @param total 文件总大小
     */
    private void refreshUploadProgress(final long progress, final long total) {
        uiAction(() -> {
            pb_upload.setProgress((int) (100 * progress / total));
            tv_progress.setText(Utils.readableStorageSize(progress) + "/" + Utils.readableStorageSize(total));
        });
    }

    @Override
    protected boolean isDisplayHomeAsUpEnabled(){
        return false;
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
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, OPEN_FILE_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    if (bitmap != null) {
                        iv_image.setImageBitmap(bitmap);
                    } else {
                        iv_image.setImageResource(R.drawable.file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                tv_name.setText(path);
            }
            currentUploadPath = path;
            pb_upload.setProgress(0);
            tv_progress.setText("");
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length == 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                }
            }
        }
    }

    public interface ProgressListener {
        void onProgress(long bytesUploaded, long totalBytes);
    }
}
