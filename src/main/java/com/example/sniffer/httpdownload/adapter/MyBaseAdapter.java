package com.example.sniffer.httpdownload.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * 万能数据适配器
 */
public abstract class MyBaseAdapter<T> extends BaseAdapter {
    protected Context context;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;
    protected int laoyoutId;

    public MyBaseAdapter(Context context, List<T> datas, int laoyoutId) {
        this.context = context;
        this.mDatas = datas;
        this.laoyoutId = laoyoutId;
        if (context != null) {
            mInflater = LayoutInflater.from(context);
        }
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder myViewHolder = MyViewHolder.get(context, convertView, parent, laoyoutId, position);
        convert(myViewHolder, getItem(position), position);
        getDatas(mDatas);
        return myViewHolder.getConvertView();
    }

    public abstract void convert(MyViewHolder myViewHolder, T t, int position);

    public abstract void getDatas(List<T> mDatas);

    public void setDatas(List<T> datas) {
        mDatas = datas;
    }

    public List<T> getListDatas() {
        return mDatas;
    }
}
