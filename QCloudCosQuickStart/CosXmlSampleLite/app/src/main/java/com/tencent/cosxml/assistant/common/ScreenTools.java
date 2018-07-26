package com.tencent.cosxml.assistant.common;

import android.content.Context;

/**
 * Created by rickenwang on 2018/6/29.
 * <p>
 * Copyright (c) 2010-2017 Tencent Cloud. All rights reserved.
 */
public class ScreenTools {

    public static int dip2px(Context context, float dipValue){

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5f);
    }
    public static int px2dip(Context context, float pxValue){

        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }
}
