package com.tencent.qcloud.costransferpractice.common.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jordanqin on 2020/6/18.
 * 基础数据适配器
 * <p>
 * Copyright (c) 2010-2020 Tencent Cloud. All rights reserved.
 */
public abstract class BaseAbstractAdapter<T> extends BaseAdapter {

    protected List<T> mList = null;
    protected LayoutInflater mLayoutInflater = null;
    private Holder mHolder = null;
    protected Context mContext;

    public BaseAbstractAdapter(List<T> list, Context context) {
        this.mList = list;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    /**
     * 重设数据集
     *
     * @param list
     */
    public void setDataList(List<T> list) {
        if (mList == null) mList = new ArrayList<T>();
        mList.clear();
        if (list != null) {
            mList.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void addDataList(List<T> list) {
        if (list == null) {
            return;
        }
        if (mList == null) {
            mList = new ArrayList<>();
        }
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void add(T t) {
        if (t == null) {
            return;
        }
        if (mList == null) {
            mList = new ArrayList<>();
        }
        //如果已包含当前T,
        if (mList.contains(t)) {
            return;
        }

        mList.add(t);
        notifyDataSetChanged();
    }

    public void delete(T t) {
        if (t == null) {
            return;
        }
        if (mList == null) {
            mList = new ArrayList<>();
        }
        if (!mList.contains(t)) {
            return;
        }

        mList.remove(t);
        notifyDataSetChanged();
    }

    @Override
    public final int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public final T getItem(int position) {
        if (mList != null && position >= 0 && position < mList.size()) {
            return mList.get(position);
        }
        return null;
    }

    public List<T> getList() {
        return mList;
    }

    @Override
    public final long getItemId(int position) {
        return position;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(getItemLayoutId(getItemViewType(position)), parent, false);
            mHolder = new Holder(convertView);
            convertView.setTag(mHolder);
        } else {
            mHolder = (Holder) convertView.getTag();
        }
        inflate(mList.get(position), position);
        return convertView;
    }

    protected <V extends View> V findViewById(int resId) {
        return (V) mHolder.findViewById(resId);
    }

    /**
     * 得到Item的布局的ID
     *
     * @param type
     * @return
     */
    protected abstract int getItemLayoutId(int type);

    /**
     * 完成布局的填充和返回
     */
    protected abstract void inflate(T t, int position);


    protected Holder getHolder() {
        return mHolder;
    }

    protected static class Holder {
        public SparseArray<View> views = new SparseArray<>();
        private View convertView = null;

        private Holder(View convertView) {
            this.convertView = convertView;
        }

        public View findViewById(int resId) {
            View v = views.get(resId);
            if (null == v) {
                v = convertView.findViewById(resId);
                views.put(resId, v);
            }
            return v;
        }

        public View getConvertView() {
            return convertView;
        }
    }
}
