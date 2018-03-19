package com.tencent.tac;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.tencent.tac.tacanalytics.AnalyticsMainActivity;
import com.tencent.tac.tacauthorization.AuthMainActivity;
import com.tencent.tac.taccrash.CrashMainActivity;
import com.tencent.tac.tacmessaging.MessagingMainActivity;
import com.tencent.tac.tacpayment.PaymentMainActivity;
import com.tencent.tac.tacstorage.StorageActivity;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class MainActivity extends Activity {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tac_activity_main);
    }

    public void onCrashMainClicked(View view) {

        Intent intent = new Intent(this, CrashMainActivity.class);
        startActivity(intent);
    }

    public void onAnalyticsClicked(View view) {

        Intent intent = new Intent(this, AnalyticsMainActivity.class);
        startActivity(intent);
    }

    public void onPaymentClicked(View view) {

        Intent intent = new Intent(this, PaymentMainActivity.class);
        startActivity(intent);

    }

    public void onMessageClicked(View view) {

        Intent intent = new Intent(this, MessagingMainActivity.class);
        startActivity(intent);
    }

    public void onStorageClicked(View view) {
        startActivity(new Intent(this, StorageActivity.class));
    }

    public void onAuthorizationClicked(View view) {
        startActivity(new Intent(this, AuthMainActivity.class));
    }


    private boolean checkPermissions() {
        return Build.VERSION.SDK_INT < 23 || PackageManager.PERMISSION_GRANTED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    private void requestPermissions() {
        if (Build.VERSION.SDK_INT > 23) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }
}
