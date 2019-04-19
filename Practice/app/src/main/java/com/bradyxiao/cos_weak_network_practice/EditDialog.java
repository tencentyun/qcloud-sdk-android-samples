package com.bradyxiao.cos_weak_network_practice;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class EditDialog extends DialogFragment implements View.OnClickListener{

    private EditText editText;
    private OnSelectListener onSelectListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_dialog, null, false);
        editText = view.findViewById(R.id.EDIT);
        view.findViewById(R.id.OK).setOnClickListener(this);
        view.findViewById(R.id.CANCEL).setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.OK:
                String value = editText.getText().toString();
                if(TextUtils.isEmpty(value)){
                    Toast.makeText(this.getContext(), "请输入文件存储于COS上的路径值", Toast.LENGTH_SHORT).show();
                }else {
                    this.onSelectListener.onConfirm(value);
                }
                break;
            case R.id.CANCEL:
                this.onSelectListener.onCancel();
                break;
        }
    }

    public void setOnSelectListener(OnSelectListener onSelectListener){
        this.onSelectListener = onSelectListener;
    }

    public interface OnSelectListener{
        void onConfirm(String value);
        void onCancel();
    }
}
