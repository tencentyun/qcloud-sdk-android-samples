package com.tencent.qcloud.costransferpractice.login;

import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.qcloud.costransferpractice.BasePresenter;
import com.tencent.qcloud.costransferpractice.BaseView;

import java.util.List;
import java.util.Map;

/**
 * Created by rickenwang on 2018/10/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class LoginContract {

    interface Presenter extends BasePresenter {

        void confirmWithTemporaryKey(String appid, String url);

        void confirmWithForeverKey(String appid, String secretId, String secretKey);

    }

    interface View extends BaseView<Presenter> {

        void refreshLoginMode(boolean isTemporary);

        void toastMessage(String message);

        void setLoading(boolean loading);

        void loginSuccess(Map<String, List<String>> regionAndBuckets);

        void config(String appid, String signUrl, String secretId, String secretKey, boolean force);
    }

}
