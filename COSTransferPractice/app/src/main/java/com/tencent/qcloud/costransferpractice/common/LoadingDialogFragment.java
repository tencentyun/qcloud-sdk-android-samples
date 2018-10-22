package com.tencent.qcloud.costransferpractice.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.qcloud.costransferpractice.R;


/**
 * Copyright 2010-2018 Tencent Cloud. All Rights Reserved.
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
