package com.tencent.tac.tacmessaging;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.tencent.qcloud.core.logger.QCloudLogger;
import com.tencent.tac.messaging.TACMessagingReceiver;
import com.tencent.tac.messaging.TACMessagingText;
import com.tencent.tac.messaging.TACMessagingToken;
import com.tencent.tac.messaging.TACNotification;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class MyReceiver extends TACMessagingReceiver {

    @Override
    public void onRegisterResult(Context context, int errorCode, TACMessagingToken token) {
        Toast.makeText(context, "注册结果返回：" + token, Toast.LENGTH_SHORT).show();
        Log.d("messaging", "MyReceiver::OnRegisterResult : code is " + errorCode + ", token is " + token.getTokenString());
    }

    @Override
    public void onTextMessage(Context context, TACMessagingText message) {
        Toast.makeText(context, "收到应用内消息：" + message, Toast.LENGTH_LONG).show();
        Log.d("messaging", "MyReceiver::OnTextMessage : message is " + message);
    }

    @Override
    public void onNotificationShowed(Context context, TACNotification notification, int notificationId) {
        Log.d("messaging", "MyReceiver::OnNotificationShowed : notification is " + notification + " notification id is " + notificationId);
    }

    @Override
    public void onNotificationClicked(Context context, TACNotification notification, long actionType) {
        Log.d("messaging", "MyReceiver::onNotificationClicked : notification is " + notification + " actionType is " + actionType);
    }

    @Override
    public void onUnregisterResult(Context context, int code) {
        Toast.makeText(context, "取消注册结果返回：" + code, Toast.LENGTH_SHORT).show();

        Log.d("messaging", "MyReceiver::onUnregisterResult : code is " + code);
    }

}
