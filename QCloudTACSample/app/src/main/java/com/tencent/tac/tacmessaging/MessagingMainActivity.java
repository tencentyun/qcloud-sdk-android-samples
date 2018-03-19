package com.tencent.tac.tacmessaging;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tencent.tac.R;
import com.tencent.tac.messaging.TACMessagingLocalMessage;
import com.tencent.tac.messaging.TACMessagingNotificationBuilder;
import com.tencent.tac.messaging.TACMessagingService;
import com.tencent.tac.messaging.type.NotificationActionType;
import com.tencent.tac.messaging.type.NotificationTime;
import com.tencent.tac.messaging.type.NotificationType;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class MessagingMainActivity extends Activity {

    private EditText builderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging_activity_main);
        builderId = findViewById(R.id.builder_id);
    }

    public void onStartMessaging(View view) {

        TACMessagingService.getInstance().start(this);
    }

    public void onStopMessaging(View view) {

        TACMessagingService.getInstance().stop(this);
    }

    public void onGetToken(View view) {

        String token = TACMessagingService.getInstance().getToken().getTokenString();
        Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        Log.d("messaging", "token is " + token);
    }


    public void onCreateLocalNotification(View view) {

        TACMessagingLocalMessage localMessage = new TACMessagingLocalMessage();
        localMessage.setTitle("Local Notification");
        localMessage.setType(NotificationType.NOTIFICATION);
        localMessage.setNotificationTime(new NotificationTime.NotificationTimeBuilder()
                //.setHourAndMinute(16,50)
                .build());
        String res = getResources().getResourceName(R.mipmap.addnum);
        localMessage.setIconRes(res);
        localMessage.setActionType(NotificationActionType.ACTION_OPEN_BROWSER);
        localMessage.setUrl("http://www.baidu.com");
        //localMessage.setActivity("com.tencent.tac.MainActivity");

        Log.d("messaging", "add local notification" + TACMessagingService.getInstance().addLocalNotification(this, localMessage));
    }

    public void onAddNotificationBuilder(View view) {

        String builderIdString = builderId.getText().toString();
        int builderIdInt = -1;
        try {
            builderIdInt = Integer.parseInt(builderIdString);
        } catch (NumberFormatException exception) {
            Toast.makeText(this, "请输入 int 型 builder id", Toast.LENGTH_SHORT).show();
        }
        if (builderIdInt > 0) {
            TACMessagingNotificationBuilder notificationBuilder = new TACMessagingNotificationBuilder()
                    .setNotificationLargeIcon(R.mipmap.addnum);
            TACMessagingService.getInstance().addNotificationBuilder(this, builderIdInt, notificationBuilder);
        }

    }


}
