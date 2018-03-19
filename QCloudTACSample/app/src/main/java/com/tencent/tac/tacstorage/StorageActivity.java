package com.tencent.tac.tacstorage;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.tac.R;
import com.tencent.tac.storage.StorageProgressListener;
import com.tencent.tac.storage.StorageResultListener;
import com.tencent.tac.storage.TACStorageReference;
import com.tencent.tac.storage.TACStorageService;
import com.tencent.tac.storage.TACStorageTaskSnapshot;

import java.io.File;

/**
 * <p>
 * </p>
 * Created by wjielai on 2017/12/4.
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class StorageActivity extends Activity {

    private TACStorageService tacStorageService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.storage_activity_main);

        tacStorageService = TACStorageService.getInstance();
    }

    public void uploadFile(View view) {
        TACStorageReference reference = tacStorageService.referenceWithPath("/tac_test/tmp");
        byte[] tmpData = new byte[200];
        for (int i = 0; i < tmpData.length; i++) {
            tmpData[i] = 1;
        }
        reference.putData(tmpData, null).addResultListener(new StorageResultListener<TACStorageTaskSnapshot>() {
            @Override
            public void onSuccess(final TACStorageTaskSnapshot snapshot) {
                showMessage(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StorageActivity.this, "上传成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(final TACStorageTaskSnapshot snapshot) {
                showMessage(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StorageActivity.this, "上传失败，" +
                                snapshot.getError(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    public void downloadFile(View view) {
        TACStorageReference reference = tacStorageService.referenceWithPath("/tac_test/tmp");
        Uri fileUri = Uri.fromFile(new File(getExternalCacheDir() + File.separator + "local_tmp"));
        reference.downloadToFile(fileUri).addResultListener(new StorageResultListener<TACStorageTaskSnapshot>() {
            @Override
            public void onSuccess(TACStorageTaskSnapshot snapshot) {
                showMessage(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StorageActivity.this, "下载成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(final TACStorageTaskSnapshot snapshot) {
                showMessage(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StorageActivity.this, "下载失败，" +
                                snapshot.getError(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addProgressListener(new StorageProgressListener<TACStorageTaskSnapshot>() {
            @Override
            public void onProgress(TACStorageTaskSnapshot snapshot) {
                Log.i("QCloudStorage", "progress = " + snapshot.getBytesTransferred() + "," +
                        snapshot.getTotalByteCount());
            }
        });
    }

    public void deleteFile(View view) {
        TACStorageReference reference = tacStorageService.referenceWithPath("/tac_test/tmp");
        reference.delete().addResultListener(new StorageResultListener<TACStorageTaskSnapshot>() {
            @Override
            public void onSuccess(TACStorageTaskSnapshot snapshot) {
                showMessage(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StorageActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(final TACStorageTaskSnapshot snapshot) {
                showMessage(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(StorageActivity.this, "删除失败，" +
                                snapshot.getError(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showMessage(Runnable runnable) {
        runOnUiThread(runnable);
    }
}
