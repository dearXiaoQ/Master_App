package com.csr.masterapp.device;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.adapter.GosDeviceListAdapter;
import com.csr.masterapp.base.SlideListView2;
import com.csr.masterapp.utils.CacheUtils;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mars on 2016/11/16.
 * 11468
 */
public class DeviceListActivity extends Activity implements View.OnClickListener{
    /** 当前Context */
    private Context mContext;
    /** uid */
    private String uid;
    /** token */
    private String token;
    /** handler */
    private Handler mHandler;
    /** 控件 */
    private Button myDeviceBtn;
    private Button addDeviceBtn;
    /** 已绑定设备的Listview 侧滑 item */
    private RelativeLayout delete2;
    /** The ic BoundDevices */
    private View icBoundDevices;
    /** The ic FoundDevices */
    private View icFoundDevices;
    /** The ic OfflineDevices */
    private View icOfflineDevices;
    /** The slv BoundDevices */
    private SlideListView2 slvBoundDevices;
    /** The slv FoundDevices */
    private SlideListView2 slvFoundDevices;
    /** The slv OfflineDevices */
    private SlideListView2 slvOfflineDevices;
    /** The tv BoundDevicesListTitle */
    private TextView tvBoundDevicesListTitle;
    /** The tv FoundDevicesListTitle */
    private TextView tvFoundDevicesListTitle;
    /** The tv OfflineDevicesListTitle */
    private TextView tvOfflineDevicesListTitle;
    /** 设备列表 */
    List<String> pks ;
    /** 等待框 */
    public ProgressDialog progressDialog;
    /** 获取设备列表 */
    protected static final int GETLIST = 0;

    /** 刷新设备列表 */
    protected static final int UPDATALIST = 1;

    /** 订阅成功前往控制页面 */
    protected static final int TOCONTROL = 2;

    /** 通知 */
    protected static final int TOAST = 3;

    /** 设备绑定 */
    protected static final int BOUND = 9;

    /** 设备解绑 */
    protected static final int UNBOUND = 99;

    /** 新设备提醒 */
    protected static final int SHOWDIALOG = 999;
    /** 适配器 */
    GosDeviceListAdapter myadapter;
    /** 使用缓存的设备列表刷新ui */
    List<GizWifiDevice> devices = GizWifiSDK.sharedInstance().getDeviceList();
    /** 设备列表分类 */
    List<GizWifiDevice> boundDevicesList, foundDevicesList, offlineDevicesList;
    /** 机智云回调 */
    private GizWifiSDKListener mListener = new GizWifiSDKListener() {
        /** 获取设备列表 */
        @Override
        public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
            devices.clear();
            progressDialog.cancel();
            // 提示错误原因
            if(result != GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                Log.i("refreshData", "result: " + result.name());
            }
            //显示变化后的设备列表
            devices = deviceList;
            Log.i("refreshData", "result = " + result + " discovered deviceList: " + deviceList);
            mHandler.sendEmptyMessage(UPDATALIST);
        }

        /** 设备解绑 */
        @Override
        public void didUnbindDevice(GizWifiErrorCode result, String did) {
            progressDialog.cancel();
            if( result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                Log.i("Gizdevice" ,"解绑成功");
            } else {
                Log.i("Gizdevice", "解绑失败");
            }
        }
    };

    /** 机智云设备监听 */
    private GizWifiDeviceListener mDeviceListener = new GizWifiDeviceListener(){
        /** 订阅回调(绑定) */
        @Override
        public void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            progressDialog.cancel();
            if(result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 订阅成功或解除订阅成功回调
                Log.i("Gizdevice", "device = " + device + " isSubscribed = " + isSubscribed);
                foundDevicesList.remove(device);
                boundDevicesList.add(device);
                mHandler.sendEmptyMessage(UPDATALIST);
            } else {
                Log.i("Gizdevice", "result = " + result);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list_activity);

        mContext = this;

        /** 初始化控件 */
        initView();

        /** 设置Handler */
        setHandler();

        /** 监听事件 */
        initEven();

        /** 初始化数据 */
        initData();

        /** 机智云初始化 */
        initGizWifiSdk();
    }

    /** 初始化数据 */
    private void initData() {
        uid = CacheUtils.getString(mContext, "uid");
        token = CacheUtils.getString(mContext, "token");
        Log.i("uid and token ", "uid = " + uid + " token = " + token);
    }

    /** 设置handler */
    private void setHandler() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case  GETLIST:
                        GizWifiSDK.sharedInstance().setListener(mListener);
                        //主动刷新绑定设备列表、制定筛选的设备productkey
                        pks = new ArrayList<>();
                        pks.add("e86106861a49433eb8108bf9f1c56046"); //抽油烟机
                        GizWifiSDK.sharedInstance().getBoundDevices(uid, token, pks);
                        break;
                    case UPDATALIST:
                        progressDialog.show();
                        UpdateUI();
                        break;
                    case UNBOUND :
                        progressDialog.show();
                        GizWifiSDK.sharedInstance().unbindDevice(uid, token, (String) msg.obj);
                        break;
                    case TOCONTROL :

                        GizWifiDevice devcie = (GizWifiDevice) msg.obj;
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("device", devcie);
                        Intent intent = new Intent(mContext, GizDeviceControl.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        break;
                }
            }
        };
    }

    /** 初始化控件事件 */
    private void initEven() {
        /** 跳转按钮的点击事件 */
        myDeviceBtn.setOnClickListener(this);
        addDeviceBtn.setOnClickListener(this);
        slvBoundDevices.initSlideMode(SlideListView2.MOD_RIGHT);
        slvOfflineDevices.initSlideMode(SlideListView2.MOD_RIGHT);
    }

    /** 控件初始化 */
    private void initView() {
        /** 跳转按钮 */
        myDeviceBtn  = (Button) findViewById(R.id.refreshMyDeviceBtn);
        addDeviceBtn = (Button) findViewById(R.id.add_deviceBtn);

        icBoundDevices = findViewById(R.id.icBoundDevices);
        icFoundDevices = findViewById(R.id.icFoundDevices);
        icOfflineDevices = findViewById(R.id.icOfflineDevices);
        /** 侧滑ListView */
        slvBoundDevices = (SlideListView2) icBoundDevices.findViewById(R.id.slideListView1);
        slvFoundDevices = (SlideListView2) icFoundDevices.findViewById(R.id.slideListView1);
        slvOfflineDevices = (SlideListView2) icOfflineDevices.findViewById(R.id.slideListView1);

        tvBoundDevicesListTitle = (TextView) icBoundDevices.findViewById(R.id.tvListViewTitle);
        tvFoundDevicesListTitle = (TextView) icFoundDevices.findViewById(R.id.tvListViewTitle);
        tvOfflineDevicesListTitle = (TextView) icOfflineDevices.findViewById(R.id.tvListViewTitle);

        String boundDevicesListTitle = (String) getText(R.string.bound_divices);
        tvBoundDevicesListTitle.setText(boundDevicesListTitle);
        String foundDevicesListTitle = (String) getText(R.string.found_devices);
        tvFoundDevicesListTitle.setText(foundDevicesListTitle);
        String offlineDevicesListTitle = (String) getText(R.string.offline_devices);
        tvOfflineDevicesListTitle.setText(offlineDevicesListTitle);

        setProgressDialog();

        setListViewItemListener();
    }

    private void setListViewItemListener() {

        slvBoundDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /** 跳转到控制界面 */
                Message msg = mHandler.obtainMessage();
                msg.what = TOCONTROL;
                msg.obj = boundDevicesList.get(position);
                mHandler.sendMessage(msg);
            }
        });
        /** 点击绑定设备 */
        slvFoundDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("GizDevice", "你点击了订阅设备");
                progressDialog.show();
                GizWifiDevice mDevice = devices.get(position);
                mDevice.setListener(mDeviceListener);
                mDevice.setSubscribe(true);
            }
        });

        slvOfflineDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    /** 初始化机智云SDK */
    private void initGizWifiSdk() {
        /** 通用监听器 */
        GizWifiSDK.sharedInstance().setListener(mListener);
        /** 获取设备列表 */
        progressDialog.show();
        mHandler.sendEmptyMessage(GETLIST);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.refreshMyDeviceBtn:
                mHandler.sendEmptyMessage(GETLIST);
                break;
            case  R.id.add_deviceBtn :
                Intent intent = new Intent(DeviceListActivity.this, GizConnDevice.class);
                startActivity(intent);
                break;
        }
    }


    /** 设置ProgressDialog */
    public void setProgressDialog() {
        progressDialog = new ProgressDialog(this);
        String loadingText = getString(R.string.loadingtext);
        progressDialog.setMessage(loadingText);
        progressDialog.setCanceledOnTouchOutside(false);
    }

    /** 更新 */
    private void UpdateUI() {


        boundDevicesList = new ArrayList<GizWifiDevice>();
        foundDevicesList = new ArrayList<GizWifiDevice>();
        offlineDevicesList = new ArrayList<GizWifiDevice>();

        /** 对设备状态进行筛选 */
        for (GizWifiDevice gizWifiDevice : devices) {
            if (GizWifiDeviceNetStatus.GizDeviceOnline == gizWifiDevice.getNetStatus()
                    || GizWifiDeviceNetStatus.GizDeviceControlled == gizWifiDevice.getNetStatus()) {
                if (gizWifiDevice.isBind()) {
                    boundDevicesList.add(gizWifiDevice);
                } else {
                    foundDevicesList.add(gizWifiDevice);
                }
            } else {
                offlineDevicesList.add(gizWifiDevice);
            }
        }


        myadapter = new GosDeviceListAdapter(this, boundDevicesList);
        myadapter.setHandler(mHandler);
        slvBoundDevices.setAdapter(myadapter);

        myadapter = new GosDeviceListAdapter(this, foundDevicesList);
        slvFoundDevices.setAdapter(myadapter);

        myadapter = new GosDeviceListAdapter(this, offlineDevicesList);
        myadapter.setHandler(mHandler);
        slvOfflineDevices.setAdapter(myadapter);

        progressDialog.cancel();
    }

}













