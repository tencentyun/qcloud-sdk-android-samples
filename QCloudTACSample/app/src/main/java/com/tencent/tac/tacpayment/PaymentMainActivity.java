package com.tencent.tac.tacpayment;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.qcloud.core.common.QCloudClientException;
import com.tencent.qcloud.core.common.QCloudResultListener;
import com.tencent.qcloud.core.common.QCloudServiceException;
import com.tencent.qcloud.core.http.HttpResult;
import com.tencent.qcloud.core.http.QCloudHttpClient;
import com.tencent.qcloud.core.http.QCloudHttpRequest;
import com.tencent.qcloud.core.task.TaskExecutors;
import com.tencent.tac.R;
import com.tencent.tac.TACApplication;
import com.tencent.tac.option.TACApplicationOptions;
import com.tencent.tac.payment.PaymentRequest;
import com.tencent.tac.payment.PaymentResult;
import com.tencent.tac.payment.TACPaymentCallback;
import com.tencent.tac.payment.TACPaymentOptions;
import com.tencent.tac.payment.TACPaymentService;
import com.tencent.tac.tacpayment.order.KeyProvider;
import com.tencent.tac.tacpayment.order.PaymentCallback;
import com.tencent.tac.tacpayment.order.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class PaymentMainActivity extends Activity {


    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity_main);
        handler = new Handler(getMainLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        handler.removeCallbacksAndMessages(null);
    }

    public void onWechatPaymentClicked(View view) {

        TACApplicationOptions applicationOptions = TACApplication.options();
        TACPaymentOptions tacPaymentOptions = applicationOptions.sub("payment");
        String appid = tacPaymentOptions.getAppid();
        final String appKey = KeyProvider.MIDAS_SECRET_KEY;
        final HashMap<String, String> map = new HashMap<>();
        final String userId = "rickenwang";
        final String orderNo = "open_" + System.currentTimeMillis();
        map.put("user_id", userId);
        map.put("channel", "wechat");
        map.put("out_trade_no", orderNo);
        map.put("product_id", "product_test");
        map.put("currency_type", "CNY");
        map.put("amount", "4");
        map.put("product_name", "金元宝");
        map.put("product_detail", "你懂得");
        map.put("original_amount", "1");
        map.put("ts", Tools.getGMTime());


        // 不建议在终端直接下单，需要在 com.tencent.tac.KeyProvider 类中配置密钥，不安全
//        new PaymentOrderGoods(appid, appKey, map).connect(new PaymentCallback() {
//            @Override
//            public void onResult(String payInfo) {
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        launchPay(userId, payInfo);
//                    }
//                });
//            }
//        });

        // 在后台下单
        new PaymentOrder(appid, userId, "wechat", orderNo).connect(new PaymentCallback() {
            @Override
            public void onResult(String payInfo) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        launchPay(userId, payInfo, orderNo);
                    }
                });
            }
        });

    }

    public void onQQPaymentClicked(View view) {

        TACApplicationOptions applicationOptions = TACApplication.options();
        TACPaymentOptions tacPaymentOptions = applicationOptions.sub("payment");
        final String appid = tacPaymentOptions.getAppid();
        final String appKey = KeyProvider.MIDAS_SECRET_KEY;
        final HashMap<String, String> map = new HashMap<>();
        final String userId = "rickenwang";
        final String orderNo = "open_" + System.currentTimeMillis();
        map.put("user_id", userId);
        map.put("channel", "qqwallet");
        map.put("out_trade_no", orderNo);
        map.put("product_id", "product_test");
        map.put("currency_type", "CNY");
        map.put("amount", "1");
        map.put("original_amount", "1");
        map.put("product_name", "金元宝");
        map.put("product_detail", "你懂得");
        map.put("ts", Tools.getGMTime());

        // 不建议在终端直接下单，需要在 com.tencent.tac.KeyProvider 类中配置密钥，不安全
//        new PaymentOrderGoods(appid, appKey, map).connect(new PaymentCallback() {
//            @Override
//            public void onResult(String payInfo) {
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        launchPay(userId, payInfo);
//                    }
//                });
//            }
//        });

        // 在后台下单
        new PaymentOrder(appid, userId, "qqwallet", orderNo).connect(new PaymentCallback() {
            @Override
            public void onResult(String payInfo) {

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        launchPay(userId, payInfo, orderNo);
                    }
                });
            }
        });
    }

    private void launchPay(String userId, String payInfo, final String orderNo) {

        PaymentRequest paymentRequest = new PaymentRequest(userId, payInfo);
        paymentRequest.addMetaData("name", "rickenwang");

        TACPaymentService.getInstance().launchPayment(PaymentMainActivity.this, paymentRequest, new TACPaymentCallback() {
            @Override
            public void onResult(int resultCode, PaymentResult result) {
                Log.d("payment", "result code is " + resultCode + ", message is " + result.toString());
                if (resultCode == 0) {
                    checkOrderState(orderNo);
                }
            }
        });
    }

    private void timelyCheckOrderState(final String orderNo) {
        handler.removeCallbacksAndMessages(null);
        // 等待三秒再次检查支付状态
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkOrderState(orderNo);
            }
        }, 3000);
    }

    private void checkOrderState(final String orderNo) {
        QCloudHttpRequest<String> request = new QCloudHttpRequest.Builder<String>()
                .method("GET")
                .scheme("http")
                .host("203.195.147.180")
                .path("/client/pay/callback_data")
                .query("out_trade_no", orderNo)
                .build();
        QCloudHttpClient.getDefault().resolveRequest(request).schedule()
                .observeOn(TaskExecutors.UI_THREAD_EXECUTOR)
                .addResultListener(new QCloudResultListener<HttpResult<String>>() {
            @Override
            public void onSuccess(HttpResult<String> result) {
                try {
                    JSONObject json = new JSONObject(result.content());
                    String payOrderId = json.getString("pay_channel_orderid");
                    if (!TextUtils.isEmpty(payOrderId)) {
                        Toast.makeText(PaymentMainActivity.this, "支付成功", Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                timelyCheckOrderState(orderNo);
            }

            @Override
            public void onFailure(QCloudClientException clientException, QCloudServiceException serviceException) {
                Log.d("payment", "exception is " + serviceException);
                timelyCheckOrderState(orderNo);
            }
        });
    }

}
