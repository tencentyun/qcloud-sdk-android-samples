package com.tencent.qcloud.cosdirecttransfer.common;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by jordanqin on 2020/6/18.
 * 基础activity
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public abstract class BaseActivity extends AppCompatActivity {
    LoadingDialogFragment loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingDialog = new LoadingDialogFragment();

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(isDisplayHomeAsUpEnabled());
        }
    }

    /**
     * 不需要actionbar返回的复写该方法 返回false
     */
    protected boolean isDisplayHomeAsUpEnabled(){
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void uiAction(Runnable runnable){
        findViewById(android.R.id.content).post(runnable);
    }

    protected void setLoading(boolean loading) {
        if (loading) {
            loadingDialog.show(getFragmentManager(), "loading");
        } else {
            loadingDialog.dismiss();
        }
    }

    protected void toastMessage(final String message) {
        findViewById(android.R.id.content).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
