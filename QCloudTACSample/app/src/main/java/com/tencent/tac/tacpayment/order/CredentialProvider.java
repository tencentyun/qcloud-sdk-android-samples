package com.tencent.tac.tacpayment.order;

import android.util.Log;

import java.util.Map;

/**
 * 用于计算下单的签名
 *
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class CredentialProvider {


    public String sign(Map<String, String> params, String appKey) {

        String sourceToSign = Tools.flatParams(params) + appKey;

        Log.d("payment", "sourceToSign is " + sourceToSign);
        Log.d("payment", "appkey is " + appKey);

        return Tools.RASEncode(KeyProvider.RSA_PRIVATE_KEY, sourceToSign);
    }



}
