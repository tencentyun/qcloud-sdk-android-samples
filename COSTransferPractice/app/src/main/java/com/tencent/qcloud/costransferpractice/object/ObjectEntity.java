package com.tencent.qcloud.costransferpractice.object;

import android.text.TextUtils;

import com.tencent.cos.xml.model.tag.ListBucket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordanqin on 2020/6/18.
 * 对象实体
 * 将文件和文件夹封装起来，便于adapter展示
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class ObjectEntity {
    private int type;//文件夹0 文件1
    private ListBucket.Contents contents;
    private ListBucket.CommonPrefixes commonPrefixes;

    public ObjectEntity(int type, ListBucket.Contents contents, ListBucket.CommonPrefixes commonPrefixes) {
        this.type = type;
        this.contents = contents;
        this.commonPrefixes = commonPrefixes;
    }

    public int getType() {
        return type;
    }

    public ListBucket.Contents getContents() {
        return contents;
    }

    public ListBucket.CommonPrefixes getCommonPrefixes() {
        return commonPrefixes;
    }

    public static List<ObjectEntity> listBucket2ObjectList(ListBucket listBucket, String prefix){
        List<ObjectEntity> list = new ArrayList<>();
        if(listBucket!=null){
            if(listBucket.commonPrefixesList!=null){
                for (ListBucket.CommonPrefixes commonPrefixes : listBucket.commonPrefixesList){
                    list.add(new ObjectEntity(0,null,commonPrefixes));
                }
            }
            if(listBucket.contentsList!=null){
                for (ListBucket.Contents contents : listBucket.contentsList){
                    //文件夹内容过滤掉自身
                    if(TextUtils.isEmpty(prefix) || !prefix.equals(contents.key)) {
                        list.add(new ObjectEntity(1, contents, null));
                    }
                }
            }
        }
        return list;
    }
}
