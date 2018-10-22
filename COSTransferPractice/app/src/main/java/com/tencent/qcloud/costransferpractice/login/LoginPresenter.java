package com.tencent.qcloud.costransferpractice.login;

import android.content.Context;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.service.GetServiceRequest;
import com.tencent.cos.xml.model.service.GetServiceResult;
import com.tencent.cos.xml.model.tag.ListAllMyBuckets;
import com.tencent.qcloud.costransferpractice.COSConfigManager;
import com.tencent.qcloud.costransferpractice.CosServiceFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by rickenwang on 2018/10/19.
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class LoginPresenter implements LoginContract.Presenter {

    private Context context;

    private CosXmlService foreverKeyService;

    private CosXmlService temporaryKeyService;

    private LoginContract.View view;

    COSConfigManager cosConfigManager;

    LoginPresenter(Context context, LoginContract.View view) {

        this.context = context;
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void confirmWithTemporaryKey(final String appid, final String url) {

        view.setLoading(true);

        if (temporaryKeyService == null) {

            temporaryKeyService = CosServiceFactory.getCosXmlServiceWithTemporaryKey(context,
                    appid, url, true);
        }

        temporaryKeyService.getServiceAsync(new GetServiceRequest(), new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {

                cosConfigManager.setAppid(appid);
                cosConfigManager.setSignUrl(url);
                cosConfigManager.save2Disk(context);
                view.loginSuccess(getRegionAndBuckets((GetServiceResult) result));
                view.setLoading(false);
            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {

                view.setLoading(false);
                view.toastMessage("填写的配置信息不正确");
            }
        });
    }

    @Override
    public void confirmWithForeverKey(final String appid, final String secretId, final String secretKey) {

        view.setLoading(true);

        if (foreverKeyService == null) {

            foreverKeyService = CosServiceFactory.getCosXmlServiceWithForeverKey(context,
                    appid, secretId, secretKey, true);
        }

        foreverKeyService.getServiceAsync(new GetServiceRequest(), new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest request, CosXmlResult result) {

                cosConfigManager.setAppid(appid);
                cosConfigManager.setSecretId(secretId);
                cosConfigManager.setSecretKey(secretKey);
                cosConfigManager.save2Disk(context);

                view.loginSuccess(getRegionAndBuckets((GetServiceResult) result));
                view.setLoading(false);

            }

            @Override
            public void onFail(CosXmlRequest request, CosXmlClientException exception, CosXmlServiceException serviceException) {
                view.setLoading(false);
                view.toastMessage("请填写正确的配置信息");
            }
        });

    }

    @Override
    public void start() {

        cosConfigManager = COSConfigManager.getInstance();
        cosConfigManager.loadFromDisk(context);

        view.config(cosConfigManager.getAppid(), cosConfigManager.getSignUrl(),
                cosConfigManager.getSecretId(), cosConfigManager.getSecretKey(), false);
    }


    private Map<String, List<String>> getRegionAndBuckets(GetServiceResult getServiceResult) {

        Map<String, List<String>> regionAndBuckets = new HashMap<>();

        List<ListAllMyBuckets.Bucket> buckets = getServiceResult.listAllMyBuckets.buckets;
        for (ListAllMyBuckets.Bucket bucket : buckets) {

            String region = bucket.location;
            String name = bucket.name;


            if (!regionAndBuckets.containsKey(region)) {
                regionAndBuckets.put(region, new LinkedList<String>());
            }
            regionAndBuckets.get(region).add(name);
        }

        return regionAndBuckets;
    }

}
