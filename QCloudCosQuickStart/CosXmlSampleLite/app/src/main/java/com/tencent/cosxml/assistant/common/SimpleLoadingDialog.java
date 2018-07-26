package com.tencent.cosxml.assistant.common;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tencent.cosxml.assistant.R;

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
public class SimpleLoadingDialog {

    private final int DIALOG_WIDTH_DP = 110;
    private final int DIALOG_HEIGHT_DP = 90;

    private Context context;

    private AlertDialog alertDialog;

    private TextView titleView;

    public SimpleLoadingDialog(Context context) {

        this.context  = context;

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_simple_loading, null);
        titleView = view.findViewById(R.id.title);

        alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(false)
                .create();
    }

    /**
     * you must call this method in UI thread.
     *
     * @param title title
     */
    public void show(String title) {

        titleView.setText(title);
        alertDialog.show();
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setLayout(ScreenTools.dip2px(context, DIALOG_WIDTH_DP), ScreenTools.dip2px(context, DIALOG_HEIGHT_DP));
        }
    }

    /**
     * you must call this method in UI thread.
     *
     * @param title title
     */
    public void refreshTitle(String title) {

        titleView.setText(title);
    }

    public void dismiss() {

        alertDialog.dismiss();
    }


}
