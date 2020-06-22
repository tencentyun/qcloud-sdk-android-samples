package com.tencent.qcloud.costransferpractice.bucket;

import android.content.Context;
import android.widget.TextView;

import com.tencent.cos.xml.model.tag.ListAllMyBuckets;
import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.Utils;
import com.tencent.qcloud.costransferpractice.common.base.BaseAbstractAdapter;

import java.text.ParseException;
import java.util.List;

/**
 * Created by jordanqin on 2020/6/18.
 * 存储桶适配器
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class BucketsAdapter extends BaseAbstractAdapter<ListAllMyBuckets.Bucket> {
    public BucketsAdapter(List<ListAllMyBuckets.Bucket> list, Context context) {
        super(list, context);
    }

    @Override
    protected int getItemLayoutId(int type) {
        return R.layout.bucket_item;
    }

    @Override
    protected void inflate(final ListAllMyBuckets.Bucket entity, int position) {
        TextView tv_name = findViewById(R.id.tv_name);
        TextView tv_create_date = findViewById(R.id.tv_create_date);
        TextView tv_location = findViewById(R.id.tv_location);

        tv_name.setText(entity.name);
        try {
            tv_create_date.setText(Utils.utc2normalWithCOSPattern(entity.createDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tv_location.setText(entity.location);
    }
}
