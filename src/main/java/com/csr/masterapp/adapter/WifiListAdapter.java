package com.csr.masterapp.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.csr.masterapp.R;

import java.util.ArrayList;

/**
 * Created by mars on 2016/11/10.
 */
public class WifiListAdapter extends BaseAdapter {

    private Context mContext;
    ArrayList<ScanResult> xpgList;

    public WifiListAdapter(ArrayList<ScanResult> list, Context context) {
        this.xpgList  = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return xpgList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        Holder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_gos_wifi_list, null);
            holder = new Holder(view);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        String ssid = xpgList.get(position).SSID;
        holder.getTextView().setText(ssid);

        return view;
    }

    class Holder {
        View view;

        public Holder(View view) {
            this.view = view;
        }

        TextView textView;

        public TextView getTextView() {
            if (textView == null) {
                textView = (TextView) view.findViewById(R.id.SSID_text);
            }
            return textView;
        }

    }

}
