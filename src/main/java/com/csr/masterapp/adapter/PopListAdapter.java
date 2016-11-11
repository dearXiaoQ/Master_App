package com.csr.masterapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.csr.masterapp.R;

import java.util.List;

/**
 * Created by mars on 2016/11/10.
 */
public class PopListAdapter extends BaseAdapter{
    private Context mContext;
    private List<String> dataList;

    public PopListAdapter(Context context, List<String> dataList) {
        this.mContext = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (null == convertView) {
            convertView = View.inflate(mContext, R.layout.item_gos_mode_list, null);
        }

        TextView tvModeText = (TextView) convertView.findViewById(R.id.tvModeText);

        String modeText = dataList.get(position);
        tvModeText.setText(modeText);

     /*   ImageView ivChoosed = (ImageView) convertView.findViewById(R.id.ivChoosed);
        if (position == i) {
            ivChoosed.setVisibility(View.VISIBLE);
        }*/

        return convertView;

    }
}
