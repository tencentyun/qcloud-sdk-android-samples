package com.tencent.qcloud.costransferpractice.region;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordanqin on 2020/6/18.
 * 区域选择页面
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class RegionActivity extends BaseActivity {
    public final static String RESULT_REGION = "REGION";
    public final static String RESULT_LABLE = "LABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.region_activity);

        final ListView listview = findViewById(R.id.listview);
        final RegionAdapter adapter = new RegionAdapter(buildData(), this);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_REGION, adapter.getItem(position).getRegion());
                intent.putExtra(RESULT_LABLE, adapter.getItem(position).getLabel());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    /**
     * 生成区域数据集
     */
    private List<RegionEntity> buildData() {
        List<RegionEntity> data = new ArrayList<>();
        data.add(new RegionEntity("ap-chengdu", "成都"));
        data.add(new RegionEntity("ap-beijing", "北京"));
        data.add(new RegionEntity("ap-guangzhou", "广州"));
        data.add(new RegionEntity("ap-shanghai", "上海"));
        data.add(new RegionEntity("ap-chongqing", "重庆"));
        data.add(new RegionEntity("ap-hongkong", "中国香港"));
        data.add(new RegionEntity("ap-beijing-fsi", "北京金融"));
        data.add(new RegionEntity("ap-shanghai-fsi", "上海金融"));
        data.add(new RegionEntity("ap-shenzhen-fsi", "深圳金融"));
        data.add(new RegionEntity("ap-singapore", "新加坡"));
        data.add(new RegionEntity("ap-mumbai", "印度孟买"));
        data.add(new RegionEntity("ap-seoul", "韩国首尔"));
        data.add(new RegionEntity("ap-bangkok", "泰国曼谷"));
        data.add(new RegionEntity("ap-tokyo", "日本东京"));
        data.add(new RegionEntity("eu-moscow", "俄罗斯莫斯科"));
        data.add(new RegionEntity("eu-frankfurt", "德国法兰克福"));
        data.add(new RegionEntity("na-toronto", "加拿大多伦多"));
        data.add(new RegionEntity("na-ashburn", "美东弗吉尼亚"));
        data.add(new RegionEntity("na-siliconvalley", "美西硅谷"));

        return data;
    }
}
