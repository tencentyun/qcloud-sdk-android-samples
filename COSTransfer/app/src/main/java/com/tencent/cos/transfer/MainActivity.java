package com.tencent.cos.transfer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.transfer.COSXMLDownloadTask;
import com.tencent.cos.xml.transfer.COSXMLUploadTask;
import com.tencent.cos.xml.transfer.TransferConfig;
import com.tencent.cos.xml.transfer.TransferManager;
import com.tencent.cos.xml.transfer.TransferState;
import com.tencent.cos.xml.transfer.TransferStateListener;
import com.tencent.qcloud.core.auth.QCloudCredentialProvider;
import com.tencent.qcloud.core.auth.SessionCredentialProvider;
import com.tencent.qcloud.core.http.HttpRequest;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10001;

    private CosXmlService cosXmlService;
    private TransferManager transferManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initCosService();
        requestPermissions();
    }

    public void onUploadClick(View view) {

        String bucket = "rickenwang-1252386093";
        String cosPath = "10Mfile.txt";
        String localPath = Environment.getExternalStorageDirectory() + "/download_10Mfile.txt";

        TransferConfig transferConfig = new TransferConfig.Builder().build();
        TransferManager transferManager = new TransferManager(cosXmlService, transferConfig);

        // 开始上传，并返回生成的 COSXMLUploadTask
        COSXMLUploadTask cosxmlUploadTask = transferManager.upload(bucket, cosPath,
                localPath, null);

        // 设置上传状态监听
        cosxmlUploadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(final TransferState state) {
                // TODO: 2018/10/22
            }
        });

        // 设置上传进度监听
        cosxmlUploadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(final long complete, final long target) {
                // TODO: 2018/10/22
            }
        });

        // 设置结果监听
        cosxmlUploadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                // TODO: 2018/10/22
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                // TODO: 2018/10/22
            }
        });
    }

    public void onDownloadClick(View view) {

        String bucket = "rickenwang-1252386093";
        String cosPath = "10Mfile.txt";
        String localDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        // 开始下载，并返回生成的 COSXMLDownloadTask
        COSXMLDownloadTask cosxmlDownloadTask = transferManager.download(this, bucket, cosPath,
                localDirPath);

        // 设置下载状态监听
        cosxmlDownloadTask.setTransferStateListener(new TransferStateListener() {
            @Override
            public void onStateChanged(final TransferState state) {
                // TODO: 2018/10/22
            }
        });

        // 设置下载进度监听
        cosxmlDownloadTask.setCosXmlProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(final long complete, final long target) {
                // TODO: 2018/10/22
            }
        });

        // 设置下载结果监听
        cosxmlDownloadTask.setCosXmlResultListener(new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {
                // TODO: 2018/10/22
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                // TODO: 2018/10/22
            }
        });
    }

    private void initCosService() {

        String appid = "1252386093"; // appid
        String region = "ap-guangzhou"; // bucket 的地域
        String signUrl = "https://rickenwang-1252386093.cos.ap-guangzhou.myqcloud.com/sign.json?q-sign-algorithm=sha1&q-ak=AKIDsJsX7mSbCJZai7foOJi637ruf6BZru3s&q-sign-time=1540192019;1540193819&q-key-time=1540192019;1540193819&q-header-list=&q-url-param-list=&q-signature=13b74d31297e4b103e8db2f73dc293ade8cf1beb&x-cos-security-token=302c37b957941dc9f4aa3158cee53dca24e975eb10001&response-content-disposition=attachment"; // 临时密钥服务地址

        CosXmlServiceConfig cosXmlServiceConfig = new CosXmlServiceConfig.Builder()
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .builder();

        URL url = null;

        try {
            url = new URL(signUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /**
         * 初始化 {@link QCloudCredentialProvider} 对象，来给 SDK 提供临时密钥。
         */
        QCloudCredentialProvider credentialProvider = new SessionCredentialProvider(new HttpRequest.Builder<String>()
                .url(url)
                /**
                 * 注意这里的 HTTP method 为 GET，请根据您自己密钥服务的发布方式进行修改
                 */
                .method("GET")
                .build());

        cosXmlService = new CosXmlService(this, cosXmlServiceConfig, credentialProvider);

        TransferConfig transferConfig = new TransferConfig.Builder().build();
        transferManager = new TransferManager(cosXmlService, transferConfig);
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

}
