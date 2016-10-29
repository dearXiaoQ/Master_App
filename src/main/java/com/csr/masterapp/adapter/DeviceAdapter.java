
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.entities.Device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * An Adapter that can be used to display a list of Device objects in sorted order.
 * 
 */
public class DeviceAdapter extends BaseAdapter {
    // List of device ids    
    private LinkedHashMap<Integer, String> mDevices = new LinkedHashMap<Integer, String>();

    //存储EcookPan的集合
   // private LinkedHashMap<>

    private final Context mContext;

    private int type ;

    public DeviceAdapter(Context context) {
      //  Log.i("mDevice", "DeviceAdapter + mDevices = " + mDevices.toString());
        this.mContext = context;
        this.type = type;
    }

    public DeviceAdapter(Context context, List<Device> devices) {
        this.mContext = context;                    
        setDevices(devices);
        for (Device dev: devices)  {
            Log.i("device", " DeviceAdapter + device = " + dev.toString());
        }
    }

    /**
     * Add a new device.
     * 
     * @param device
     *            The device to add.
     */
    public void addDevice(Device device) {      //通过调用此方法来添加设备
        Log.i("mDevice", "addDevice()");
    	mDevices.put(device.getDeviceId(), device.getName());
        notifyDataSetChanged();
    }
           
    /**
     * Set the list of devices. The list will be sorted.
     * 
     * @param devices
     *            The list of devices.
     */
    public void setDevices(List<Device> devices) {
        Log.i("mDevices", "进入setDevices()");

        mDevices.clear();
        notifyDataSetChanged();
    }
    
    /**
     * Remove a single device from the adapter.
     * 
     * @param deviceId
     *            The device to remove.
     */
    public void remove(int deviceId) {
        mDevices.remove(deviceId);        
        notifyDataSetChanged();
    }
    
    /**
     * Remove all devices from this adapter.
     */
    public void clear() {
        mDevices.clear();
        notifyDataSetChanged();
    }    
        
    @Override
    public int getCount() {
        Log.i("mDevice", "getCount() mDevices.size() = " + mDevices.size());
        return mDevices.size();
    }

    @Override
    public String getItem(int position) {
    	List<String> list = new ArrayList<String>(mDevices.values());
  //      Log.i("mDevice", "getItem()");
    //    Log.i("mDevices", "getItem + mDevices = " + list.toString());
        return list.get(position);
    }
    
    public int getItemDeviceId(int position) {
        Iterator<Integer> iterator = mDevices.keySet().iterator();
        int index = 0;
        int valueToReturn = 0;
        while(index <= position) {
        	valueToReturn = iterator.next();
        	index++;
        }
        return valueToReturn;
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getDevicePosition(int selectedDeviceId) {
    	int index = 0;
    	Iterator<Integer> iterator = mDevices.keySet().iterator();
    	while(iterator.hasNext()) {
    		int value = iterator.next();
    		if (value == selectedDeviceId) {
    			return index;
    		}
    		index++;
    	}

    	throw new IllegalArgumentException("Device id does not exist: " + String.format("0x%x",index));
        
    }
    
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return customView(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return customView(position, convertView, parent, android.R.layout.simple_spinner_item);
    }

    private View customView(int position, View convertView, ViewGroup parent, int layoutResource) {
        TextView deviceView;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(layoutResource, parent, false);
            deviceView = (TextView) convertView.findViewById(android.R.id.text1);
            deviceView.setTextSize(30);
            deviceView.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
            deviceView.setTextColor(mContext.getResources().getColor(R.color.device_name));
            convertView.setTag(new ViewHolder(deviceView));
        }
        else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            deviceView = viewHolder.deviceView;
        }

        String deviceName = getItem(position);

        deviceView.setText(deviceName);

        return convertView;
    }

    private static class ViewHolder {
        public final TextView deviceView;

        public ViewHolder(TextView deviceView) {
            this.deviceView = deviceView;
        }
    }          
}