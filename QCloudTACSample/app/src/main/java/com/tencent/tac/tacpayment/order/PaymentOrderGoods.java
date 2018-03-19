package com.tencent.tac.tacpayment.order;

import android.util.Log;

import java.util.Locale;
import java.util.Map;


/**
 * Midas 下单接口。
 *
 * <p>
 * 一般情况下应该由后台下单，这里仅仅是作为测试方便，直接在终端下单。
 *
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class PaymentOrderGoods extends NetworkConnection {


    private String protocal = "https";

    private String host = "api.openmidas.com";

    private String path = "v1/r";

    private String appId;

    private String appKey;

    private Map<String, String> params;

    public PaymentOrderGoods(String appId, String appKey, Map<String, String> params) {

        this.appId = appId;
        this.appKey = appKey;
        this.params = params;
    }


    @Override
    protected String url() {

        String sign = new CredentialProvider().sign(params, appKey);
        params.put("sign", sign);

        String url = String.format(Locale.ENGLISH, "%s://%s/%s/%s/unified_order?%s", protocal, host, path, appId, Tools.flatParams(params));
        Log.d("TAG", "url is " + url);

        return url;
    }



}
