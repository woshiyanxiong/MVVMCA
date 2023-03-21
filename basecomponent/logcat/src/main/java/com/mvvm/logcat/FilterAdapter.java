package com.mvvm.logcat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lvzhendong
 * Date: 2020/10/30
 */
class FilterAdapter extends BaseAdapter {

    private List<String> mData;

    public void setData(List<String> data){
        mData=data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mData == null) return 0;
        else return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        FilterHolder holder;
        if (item == null) {
            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
            holder = new FilterHolder(item);
        } else {
            holder = (FilterHolder) item.getTag();
        }
        holder.show(getItem(position));

        return item;
    }

    public static class FilterHolder {

        TextView tvFilter;

        FilterHolder(View item) {
            tvFilter = (TextView) item;
            item.setTag(this);
        }

        void show(String data) {
            tvFilter.setText(data);
        }
    }
}
