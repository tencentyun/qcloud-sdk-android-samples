package com.tencent.tac.tacpayment;

import android.util.Log;

import com.tencent.tac.tacpayment.order.NetworkConnection;

import static com.tencent.tac.tacpayment.order.Tools.getGMTime;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class PaymentOrder extends NetworkConnection {

    private String appid;

    private String userId;

    private String channel;

    private String orderNo;

    public PaymentOrder(String appid, String userId, String channel, String orderNo) {

        this.appid = appid;
        this.userId = userId;
        this.channel = channel;
        this.orderNo = orderNo;
    }

    @Override
    protected String url() {

        StringBuilder result = new StringBuilder("http://carsonxu.com/tac/androidOrder.php?");

        try {
            result.append("domain=" + "api.openmidas.com" + "&");
            result.append("appid=" + appid + "&");
            result.append("user_id=" + userId + "&");
            result.append("out_trade_no=" + orderNo + "&");
            result.append("product_id=product_test&");
            result.append("currency_type=cny&");
            result.append("channel=" + channel + "&");

//            result.append("sub_appid=1450013782&");
            result.append("amount=2&");
            result.append("original_amount=4&");
            result.append("product_name=年夜饭10人套餐&");
            result.append("product_detail=openmidas_android_test&");
            result.append("ts=" + getGMTime() + "&");
            result.append("sign=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa&");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Log.d("tag", result.toString());

        return result.toString();
    }
}
