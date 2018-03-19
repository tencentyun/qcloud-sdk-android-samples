package com.tencent.tac.tacanalytics;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.tencent.tac.analytics.TACAnalyticsService;

/**
 * Created by wjielai on 2017/11/17.
 */

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(this);
        textView.setText("This page will close soon.");
        setContentView(textView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TACAnalyticsService.getInstance().trackPageAppear(this, "secondPage");
    }

    @Override
    protected void onPause() {
        super.onPause();

        TACAnalyticsService.getInstance().trackPageDisappear(this, "secondPage");
    }
}
