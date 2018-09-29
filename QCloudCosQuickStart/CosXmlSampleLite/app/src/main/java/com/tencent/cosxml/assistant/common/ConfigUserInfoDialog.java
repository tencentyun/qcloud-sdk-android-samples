package com.tencent.cosxml.assistant.common;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tencent.cosxml.assistant.R;

import org.w3c.dom.Text;

/**
 * this is a simple loading dialog with one TextView and one ProgressBar.
 * <br>
 * the TextView can indicate what's the app doing in the background, and the ProgressBar tell the user
 * you should waiting for something important.
 *
 * Created by rickenwang on 2018/6/29.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class ConfigUserInfoDialog {

    private final int DIALOG_WIDTH_DP = 220;
    private final int DIALOG_HEIGHT_DP = 220;

    private Context context;

    private AlertDialog alertDialog;

    private EditText appid;

    private EditText host;

    private EditText port;

    private Button confirm;

    private Button cancel;

    private View.OnClickListener onConfirm;

    private View.OnClickListener onCancel;

    public ConfigUserInfoDialog(Context context, final OnConfirmListener onConfirmListener) {

        this.context  = context;

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_config_paras, null);
        appid = view.findViewById(R.id.config_appid);
        host = view.findViewById(R.id.config_host);
        port = view.findViewById(R.id.config_port);
        confirm = view.findViewById(R.id.confirm);
        cancel = view.findViewById(R.id.cancel);

        this.onCancel = onCancel;
        this.onConfirm = onConfirm;

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userAppid = appid.getText().toString();
                String userHost = host.getText().toString();
                String userPort = port.getText().toString();

                try {
                    int userPortInteger = Integer.parseInt(userPort);
                    if (TextUtils.isEmpty(userAppid) || TextUtils.isEmpty(userHost) || TextUtils.isEmpty(userPort)) {
                        onConfirmListener.onFailed("请配置正确的信息");
                    } else {
                        onConfirmListener.onSuccess(userAppid, userHost, userPortInteger);
                        dismiss();
                    }
                } catch (NumberFormatException exception) {
                    onConfirmListener.onFailed("port 必须为数字");
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();
    }

    /**
     * you must call this method in UI thread.
     *
     */
    public void show() {

        alertDialog.show();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setLayout(ScreenTools.dip2px(context, DIALOG_WIDTH_DP), ScreenTools.dip2px(context, DIALOG_HEIGHT_DP));
        }
    }

    private void dismiss() {

        alertDialog.dismiss();
    }

    public interface OnConfirmListener {

        void onSuccess(String appid, String host, int port);

        void onFailed(String error);
    }


}
