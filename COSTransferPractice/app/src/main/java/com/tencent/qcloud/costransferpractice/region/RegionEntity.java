package com.tencent.qcloud.costransferpractice.region;

/**
 * Created by jordanqin on 2020/6/18.
 * 区域实体
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class RegionEntity {
    //region值
    private String region;
    //界面显示的文案
    private String label;

    public RegionEntity(String region, String label) {
        this.region = region;
        this.label = label;
    }

    public String getRegion() {
        return region;
    }

    public String getLabel() {
        return label;
    }
}
