package com.tencent.tac.taccrash;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.tencent.tac.R;
import com.tencent.tac.crash.TACCrashService;
import com.tencent.tac.crash.TACCrashSimulator;

/**
 * <p>
 * Copyright 2010-2017 Tencent Cloud. All Rights Reserved.
 */

public class CrashMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crash_activity_main);
    }

    public void onJavaCrashClicked(View view) {

        TACCrashService crashService = TACCrashService.getInstance();
        crashService.setUserSceneTag(this, 9527);
        crashService.putUserData(this, "name", "唐伯虎");
        TACCrashSimulator.testJavaCrash();
    }

    public void onANRCrashClicked(View view) {

        TACCrashSimulator.testANRCrash();
    }

    public void onNativeCrashClicked(View view) {

        TACCrashSimulator.testNativeCrash();
    }


}
