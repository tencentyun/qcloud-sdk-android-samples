package com.tencent.qcloud.costransferpractice.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.qcloud.costransferpractice.MainActivity;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.LoadingDialogFragment;

import java.util.List;
import java.util.Map;

/**
 * Created by rickenwang on 2018/10/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class LoginFragment extends Fragment implements LoginContract.View {

    private final String APPID_RESTORE_KEY = "appid_restore_key";
    private final String URL_RESTORE_KEY = "url_restore_key";
    private final String SECRET_ID_RESTORE_KEY = "secret_id_restore_key";
    private final String SECRET_KEY_RESTORE_KEY = "secret_key_restore_key";


    private View contentView;

    private EditText appid;
    private EditText url;
    private EditText secretId;
    private EditText secretKey;

    private TextView urlName;
    private TextView secretIdName;
    private TextView secretKeyName;

    private CheckBox signType;
    private boolean isTemporary;

    private Button confirm;

    private LoginContract.Presenter presenter;

    String appidText;
    String signUrlText;
    String secretIdText;
    String secretKeyText;

    LoadingDialogFragment loadingDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_login, container, false);
        initContentView(contentView, savedInstanceState);
        presenter = new LoginPresenter(getContext(),this);
        isTemporary = true;

        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.start();
    }

    private void initContentView(View contentView, Bundle savedInstanceState) {

        appid = contentView.findViewById(R.id.appid);
        url = contentView.findViewById(R.id.sign_url);
        secretId = contentView.findViewById(R.id.secret_id);
        secretKey = contentView.findViewById(R.id.secret_key);

        urlName = contentView.findViewById(R.id.url_name);
        secretIdName = contentView.findViewById(R.id.secret_id_name);
        secretKeyName = contentView.findViewById(R.id.secret_key_name);

        signType = contentView.findViewById(R.id.sign_type);

        confirm = contentView.findViewById(R.id.confirm);

        signType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                refreshLoginMode(!isChecked);
            }
        });

        loadingDialog = new LoadingDialogFragment();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (configComplete()) {

                    if (isTemporary) {
                        presenter.confirmWithTemporaryKey(appidText, signUrlText);
                    } else {
                        presenter.confirmWithForeverKey(appidText, secretIdText, secretKeyText);
                    }

                } else {
                    toastMessage("请先完成配置信息");
                }
            }
        });

        if (savedInstanceState != null) {
            String storedAppid = savedInstanceState.getString(APPID_RESTORE_KEY);
            String storeUrl = savedInstanceState.getString(URL_RESTORE_KEY);
            String storeSecretId = savedInstanceState.getString(SECRET_ID_RESTORE_KEY);
            String storeSecretKey = savedInstanceState.getString(SECRET_KEY_RESTORE_KEY);
            config(storedAppid, storeUrl, storeSecretId, storeSecretKey, false);
        }
    }

    @Override
    public void refreshLoginMode(boolean isTemporary) {

        this.isTemporary = isTemporary;

        if (isTemporary) {
            url.setVisibility(View.VISIBLE);
            urlName.setVisibility(View.VISIBLE);

            secretId.setVisibility(View.GONE);
            secretIdName.setVisibility(View.GONE);
            secretKey.setVisibility(View.GONE);
            secretKeyName.setVisibility(View.GONE);
        } else {

            url.setVisibility(View.GONE);
            urlName.setVisibility(View.GONE);

            secretId.setVisibility(View.VISIBLE);
            secretIdName.setVisibility(View.VISIBLE);
            secretKey.setVisibility(View.VISIBLE);
            secretKeyName.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void toastMessage(final String message) {

        contentView.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void setLoading(boolean loading) {

        if (loading) {

            loadingDialog.show(getActivity().getFragmentManager(), "loading");
        } else {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void loginSuccess(Map<String, List<String>> regionAndBuckets) {

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.transferFragment(regionAndBuckets);
    }

    @Override
    public void config(String appid, String signUrl, String secretId, String secretKey, boolean force) {

        configTextView(this.appid, appid, force);
        configTextView(this.url, signUrl, force);
        configTextView(this.secretId, secretId, force);
        configTextView(this.secretKey, secretKey, force);

    }

    private void configTextView(TextView textView, String message, boolean force) {

        String currentText = textView.getText().toString();
        if (force || TextUtils.isEmpty(currentText)) {
            textView.setText(message);
        }
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {

        this.presenter = presenter;
    }

    private boolean configComplete() {

        appidText = appid.getText().toString();
        signUrlText = url.getText().toString();
        secretIdText = secretId.getText().toString();
        secretKeyText = secretKey.getText().toString();



        if (isTemporary) {
            return !TextUtils.isEmpty(appidText)
                    && !TextUtils.isEmpty(signUrlText);
        } else {

            return !TextUtils.isEmpty(appidText)
                    && !TextUtils.isEmpty(secretIdText)
                    && !TextUtils.isEmpty(secretKeyText);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(APPID_RESTORE_KEY, appid.getText().toString());
        outState.putString(URL_RESTORE_KEY, url.getText().toString());
        outState.putString(SECRET_ID_RESTORE_KEY, secretId.getText().toString());
        outState.putString(SECRET_KEY_RESTORE_KEY, secretKey.getText().toString());
    }
}
