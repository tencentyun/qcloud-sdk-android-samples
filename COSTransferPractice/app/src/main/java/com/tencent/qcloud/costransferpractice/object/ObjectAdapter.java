package com.tencent.qcloud.costransferpractice.object;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tencent.qcloud.costransferpractice.R;
import com.tencent.qcloud.costransferpractice.common.Utils;
import com.tencent.qcloud.costransferpractice.common.base.BaseAbstractAdapter;

import java.text.ParseException;
import java.util.List;

/**
 * Created by jordanqin on 2020/6/18.
 * 对象适配器
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public class ObjectAdapter extends BaseAbstractAdapter<ObjectEntity> {
    private OnObjectListener listener;
    private String prefix;

    public ObjectAdapter(List<ObjectEntity> list, Context context, OnObjectListener listener, String prefix) {
        super(list, context);
        this.listener = listener;
        this.prefix = prefix;
    }

    @Override
    public int getItemViewType(int i) {
        return getList().get(i).getType();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    protected int getItemLayoutId(int type) {
        if(type==0){
            return R.layout.object_item_folder;
        } else {
            return R.layout.object_item_file;
        }
    }

    @Override
    protected void inflate(final ObjectEntity entity, int position) {
        if(entity.getType()==0){//文件夹
            TextView tv_name = findViewById(R.id.tv_name);
            if (TextUtils.isEmpty(this.prefix)) {
                tv_name.setText(entity.getCommonPrefixes().prefix);
            } else {
                tv_name.setText(entity.getCommonPrefixes().prefix.replaceFirst(this.prefix,""));
            }
            tv_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        listener.onFolderClick(entity.getCommonPrefixes().prefix);
                    }
                }
            });
        } else {//文件
            TextView tv_name = findViewById(R.id.tv_name);
            TextView tv_create_date = findViewById(R.id.tv_create_date);
            TextView tv_size = findViewById(R.id.tv_size);
            if (TextUtils.isEmpty(this.prefix)) {
                tv_name.setText(entity.getContents().key);
            } else {
                tv_name.setText(entity.getContents().key.replaceFirst(this.prefix,""));
            }
            try {
                tv_create_date.setText("创建时间："+Utils.utc2normalWithCOSPattern(entity.getContents().lastModified));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tv_size.setText("大小："+ Utils.readableStorageSize(entity.getContents().size));

            findViewById(R.id.tv_download).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        listener.onDownload(entity);
                    }
                }
            });
            findViewById(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener!=null){
                        listener.onDelete(entity);
                    }
                }
            });
        }
    }

    public interface OnObjectListener {
        void onFolderClick(String prefix);

        void onDownload(ObjectEntity object);

        void onDelete(ObjectEntity object);
    }
}
