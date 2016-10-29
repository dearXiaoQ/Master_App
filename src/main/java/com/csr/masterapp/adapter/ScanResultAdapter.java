package com.csr.masterapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.utils.ScanInfo;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mars on 2016/10/26.
 *  121
 * The adapter that allows the contents of ScanInfo objects to be displayed in the ListView. The device name,
 * address, RSSI and the icon specified in appearances.xml are displayed.
 */
public class ScanResultAdapter extends BaseAdapter{

    private Activity activity;
    private ArrayList<ScanInfo> data;
    private LayoutInflater inflater = null;

    public ScanResultAdapter(Activity a, ArrayList<ScanInfo> object) {
        activity = a;
        data = object;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.scan_list_row, null);

        TextView nameText = (TextView) vi.findViewById(R.id.name);
        TextView addressText = (TextView) vi.findViewById(R.id.address);
        TextView rssiText = (TextView) vi.findViewById(R.id.rssi);

        ScanInfo info = (ScanInfo) data.get(position);
        nameText.setText(info.name);
        addressText.setText(info.address);
        if (info.rssi != 0) {
            rssiText.setText(String.valueOf(info.rssi) + "dBm");
        }
        return vi;
    }

    @Override
    public void notifyDataSetChanged() {
        // before notify. sort the data by RSSI.
        //notifyDataSetChanged
        Collections.sort(data);
        super.notifyDataSetChanged();
    }
}
