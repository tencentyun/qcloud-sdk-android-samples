package com.tencent.qcloud.cosdirecttransfer.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.cosdirecttransfer.R;


/**
 * Created by jordanqin on 2020/6/18.
 * 加载弹窗
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class LoadingDialogFragment extends DialogFragment {

    private String message;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_loading, null);
        builder.setView(view);


        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = getResources().getDimensionPixelSize(R.dimen.loading_dialog_width);

        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }

    public void setMessage(String message) {

        this.message = message;
    }
}
