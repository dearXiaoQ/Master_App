package com.csr.masterapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.csr.masterapp.adapter.ScanResultAdapter;
import com.csr.masterapp.device.GizConnDevice;
import com.csr.masterapp.utils.CacheUtils;
import com.csr.masterapp.utils.ScanInfo;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

public class WelcomeUI extends Activity implements AdapterView.OnItemClickListener {

    private final static long ANIMATION_DURATION = 2000;

    private ImageView ic_logo;

    private Button addDeviceBtn;

    public static  Context GLOBAL_CONTEXT = null;

    private static final String TAG = "WelcomeUI";

    public static final String KEY_FIRST_START = "is_first_start";//标记是否第一次打开的key
    public static final String MASTER_APP_ID = "md5_mac_address";//标记是否第一次打开的key

    private static final int REQUEST_ENABLE_BT = 1;

    // Adjust this value to control how long scan should last for. Higher values will drain the battery more.
    // Adjust this value in the derived class.
    protected long mScanPeriodMillis = 6000;

    ListView mScanListView = null;

    private static ArrayList<ScanInfo> mScanResults = new ArrayList<ScanInfo>();

    private static HashSet<String> mScanAddreses = new HashSet<String>();

    private static ScanResultAdapter mScanResultsAdapter;

    private BluetoothAdapter mBtAdapter = null;

    private static Handler mHandler = new Handler();

    private Button mScanButton = null;

    private boolean mCheckBt = false;

    private static final int INDEX_UUID_1 = 5;
    private static final int INDEX_UUID_2 = 6;
    private static final byte UUID_1 = (byte) 0xF1;
    private static final byte UUID_2 = (byte) 0xFE;

    private ProgressBar pbar_Welcome;

    // number of connection
    private int scanAttempts = 0;
    public static WelcomeUI mWelcomeUI;

    private int scanTimes = 0;

    boolean isFirstStart;

    String masterAppId;

    //机智云初始化监听器
    GizWifiSDKListener wifiSdkListener = new GizWifiSDKListener() {
        @Override
        public void didNotifyEvent(GizEventType eventType, Object eventSource, GizWifiErrorCode eventID,
                                   String eventMessage) {
            if(eventType == GizEventType.GizEventSDK) {
                //SDK的事件通知
                Log.i("GizWifiSDK", "SDK even happend: " + eventID + "," + eventMessage);
            }else if(eventType == GizEventType.GizEventDevice) {
                // 设备连接中断断开时可能产生的通知
                GizWifiDevice device = (GizWifiDevice) eventSource;
                Log.i("GizWifiSDK", "device mac: " + device.getMacAddress() + "disconnect caused by eventID:" +
                        eventID + ",eventMessage: " + eventMessage);
            } else if(eventType == GizEventType.GizEventM2MService) {
                // M2M服务返回的异常通知
                Log.i("GizWifiSDK", "M2M domain " + (String) eventSource + " exection happend, evenID:"
                        + eventID + ", eventMessage: " + eventMessage);
            } else if(eventType == GizEventType.GizEventToken) {
                // token失效通知
                Log.i("GizWifiSDK", "token " + (String) eventSource + " expired: " + eventMessage);
            }
        }

        //等待配置完成或超时，回调配置完成接口
       @Override
        public void didSetDeviceOnboarding(GizWifiErrorCode result, String mac, String did, String productKey) {
            if(result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                //配置成功
                Log.i("GizWifiSdk", "AirLink配置成功！");
            } else {
                //配置失败
                Log.i("GizWifiSdk", "AirLink配置失败或超时！");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String Language = Locale.getDefault().getLanguage();
        Toast.makeText(this, "当前选择的语言：" + Language, Toast.LENGTH_SHORT).show();
        GLOBAL_CONTEXT = getApplicationContext();
        mWelcomeUI = WelcomeUI.this;
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去除状态栏
        setContentView(R.layout.welcome);
        //初始化数据（机智云初始化）
        initData();
        //初始化控件
        initView();
        //初始化蓝牙
        initBluetooth();
        //跳转到对应的页面
        jump();
    }

    //初始化蓝牙
    private void initBluetooth() {
        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBtAdapter = btManager.getAdapter();

        // Register for broadcasts on BluetoothAdapter state change so that we can tell if it has been turned off.
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(mReceiver, filter);
        checkEnableBt();

         isFirstStart = CacheUtils.getBoolean(this, KEY_FIRST_START, true);
         masterAppId = CacheUtils.getString(this, WelcomeUI.MASTER_APP_ID);
    }

    //初始化控件
    private void initView() {
        mScanListView = (ListView) this.findViewById(R.id.scanListView);
        mScanResultsAdapter = new ScanResultAdapter(this, mScanResults);
        //     mScanResultsAdapter = new ScanResultsAdapter(this, mScanResults);
        //     mScanResultsAdapter = new ScanResultsAdapter(this, mScanResults);
        mScanListView.setAdapter(mScanResultsAdapter);
        mScanListView.setOnItemClickListener(this);
        //重新扫描按钮
        mScanButton = (Button) findViewById(R.id.buttonScan);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });
        pbar_Welcome = (ProgressBar) findViewById(R.id.pbar_Welcome);
        addDeviceBtn = (Button) findViewById(R.id.add_device);
        addDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeUI.this, GizConnDevice.class);
                startActivity(intent);
            }
        });



    }

    //跳转到对应的页面
    private void jump() {
        boolean isFirstStart = CacheUtils.getBoolean(this, KEY_FIRST_START, true);
        String masterAppId = CacheUtils.getString(this, WelcomeUI.MASTER_APP_ID);

        if (isFirstStart || masterAppId == null) {
            Log.d(TAG, "进入引导页");
            startActivity(new Intent(this, GuideUI.class));
            finish();
            return;
        }

        int isLogin = CacheUtils.getInt(WelcomeUI.this, "userId", 0);
        if (isLogin == 0) {
            Log.d(TAG, "进入登录页");
            startActivity(new Intent(WelcomeUI.this, LoginAndRegisterUI.class));
            finish();
            return;
        }

        if (mBtAdapter.isEnabled()) {
            scanLeDevice(true);
        }


    }

    //初始化数据（机智云初始化）
    private void initData() {
        GizWifiSDK.sharedInstance().startWithAppID(this, "1231241");

        //初始化机智云服务
        GizWifiSDK.sharedInstance().setListener(wifiSdkListener);
        GizWifiSDK.sharedInstance().startWithAppID(getApplicationContext(), "3d6496883e6a4e76a378e3b762ff886d");    //appid
    }

    /**
     * When the Activity is resumed, clear the scan results list.
     */
    @Override
    protected void onResume() {
        super.onResume();
        clearScanResults();
        if (mCheckBt) {
            checkEnableBt();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        // Set flag to check Bluetooth is still enabled when we are resumed.
        // If we end up being destroyed this flag's state will be forgotten, but that's fine because then
        // onCreate will perform the Bluetooth check anyway.
        mCheckBt = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    /**
     * Handle Bluetooth connection when the user selects a device.
     */
    protected void connectBluetooth(int i) {
        Intent intent = new Intent(this, MainActivity.class);
        // Try top 3 devices
        ArrayList<BluetoothDevice> devices = new ArrayList<>();
//        for (int i = 0; i < mScanResults.size() && i < 3; i++) {
        ScanInfo info = mScanResults.get(i);
        devices.add(mBtAdapter.getRemoteDevice(info.address));          //getRemoteDevice()返回相应的被指定蓝牙连接的远端设备。
//       }
        intent.putParcelableArrayListExtra(BluetoothDevice.EXTRA_DEVICE, devices);
        Log.i(TAG, "Devices : " + intent.putParcelableArrayListExtra(BluetoothDevice.EXTRA_DEVICE, devices));

        this.startActivity(intent);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //跳转主界面
        connectBluetooth(position);
    }

    /**
     * Display a dialogue requesting Bluetooth to be enabled if it isn't already.
     */
    private void checkEnableBt() {      //蓝牙是否可用
        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /**
     * Callback activated after the user responds to the enable Bluetooth dialogue.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCheckBt = false;
        if (requestCode == REQUEST_ENABLE_BT && resultCode != RESULT_OK) {
            mScanButton.setVisibility(View.GONE);
            Toast.makeText(this, getString(R.string.bluetooth_not_enabled), Toast.LENGTH_LONG).show();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    Toast.makeText(context, getString(R.string.bluetooth_disabled), Toast.LENGTH_SHORT).show();
                    scanLeDevice(false);
                    clearScanResults();
                    mScanButton.setVisibility(View.GONE);
                } else if (state == BluetoothAdapter.STATE_ON) {
                    Toast.makeText(context, getString(R.string.bluetooth_enabled), Toast.LENGTH_SHORT).show();
                    mScanButton.setVisibility(View.VISIBLE);
                    mBtAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
                }
            }
        }
    };


    /**
     * Clear the cached scan results, and update the display.
     */
    private void clearScanResults() {
        mScanResults.clear();
        mScanAddreses.clear();
        // Make sure the display is updated; any old devices are removed from the ListView.
        mScanResultsAdapter.notifyDataSetChanged();
    }


    private Runnable scanTimeout = new Runnable() {
        @Override
        public void run() {
            mBtAdapter.stopLeScan(mLeScanCallback);
            pbar_Welcome.setVisibility(View.GONE);
            // Connect to the device with the smallest RSSI value
            if (!mScanResults.isEmpty()) {
                scanAttempts = 0;
//	            connectBluetooth();
            } else {
                Toast.makeText(getApplicationContext(),
                        getApplicationContext().getString(R.string.not_discove_can_connect_device), Toast.LENGTH_SHORT).show();
                mScanButton.setVisibility(View.VISIBLE);
                scanAttempts++;
                if (scanAttempts >= 2) {
                    //show an alert asking to reset the Bluetooth
                    askResetBluetooth();
                }
            }
        }
    };

    /**
     * Pop up an alert asking if the user want to reset the bluetooth
     */
    private void askResetBluetooth() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WelcomeUI.this);

        alertDialogBuilder.setMessage(R.string.not_discove_can_connect_device_please_reset_buletooth);
        // set positive button: Yes message
        alertDialogBuilder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetBluetooth();
                dialog.cancel();
            }
        });
        // set negative button: No message
        alertDialogBuilder.setNegativeButton(R.string.later_handle, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }

    /**
     * Stop and Start the Bluetooth. We shoudn't use this method without user permission.
     */
    private void resetBluetooth() {
        if (mBtAdapter != null) {
            mBtAdapter.disable();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    checkEnableBt();
                }
            }, 200);

        }
    }

    /**
     * Start or stop scanning. Only scan for a limited amount of time defined by SCAN_PERIOD.
     *
     * @param enable Set to true to enable scanning, false to stop.
     */
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            scanTimes += 1;
            // Stops scanning after a predefined scan period.
            mHandler.postDelayed(scanTimeout, mScanPeriodMillis);   //每次扫描6秒钟
            clearScanResults();     //清除搜索结果
            pbar_Welcome.setVisibility(View.VISIBLE);   //进度条
            mBtAdapter.startLeScan(mLeScanCallback);    //扫描
        } else {
            // Cancel the scan timeout callback if still active or else it may fire later.
            mHandler.removeCallbacks(scanTimeout);
            //setProgressBarIndeterminateVisibility(false);
            pbar_Welcome.setVisibility(View.GONE);
            mBtAdapter.stopLeScan(mLeScanCallback);
            mScanButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Callback for scan results.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device.getName() == null) {
                        // Sometimes devices are seen with a null name and empirically connection
                        // to such devices is less reliable, so ignore them.
                        return;
                    }
                    // Check that this isn't a device we have already seen, and add it to the list.
                    if (!mScanAddreses.contains(device.getAddress())) {
                        if (device.getType() == BluetoothDevice.DEVICE_TYPE_LE &&
                                scanRecord[INDEX_UUID_1] == UUID_1 && scanRecord[INDEX_UUID_2] == UUID_2) {
                            ScanInfo scanResult = new ScanInfo(device.getName(), device.getAddress(), rssi);
                            mScanAddreses.add(device.getAddress());
                            //    Log.i("MESSAGE" , "devcie Address = " + device.getAddress());
                            mScanResults.add(scanResult);
                            //      Log.i("MESSAGE", "不排序01");
                            Collections.sort(mScanResults);
                            mScanResults = filterPriority(mScanResults);
                            mScanResultsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        for (ScanInfo info : mScanResults) {
                            if (info.address.equalsIgnoreCase((device.getAddress()))) {
                                //           Log.i("MESSAGE" , "devcie Address = " + device.getAddress());
                                info.rssi = rssi;
                                //           Log.i("MESSAGE", "不排序02");
                                Collections.sort(mScanResults);
                                mScanResults = filterPriority(mScanResults);
                                mScanResultsAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                }
            });
        }
    };

    //优先级过滤
    private ArrayList<ScanInfo> filterPriority(ArrayList<ScanInfo> data){
        //优先级设置
        switch (scanTimes){
            case 1:
                for(int i=0; i<data.size(); i++){
                    Log.d(TAG, "filterPriority: " + data.get(i).name);
                    if(!data.get(i).name.equals("RHood")){
                        data.remove(i);
                    }
                }
                Log.d(TAG, "getView: 1---"  + data.size());
                break;
            case 2:
                Log.d(TAG, "getView: 2-1---" + data.size() );
                for(int i=0; i<data.size(); i++){
                    Log.d(TAG, "filterPriority: " + data.get(i).name);
                    if(!data.get(i).name.equals("RHood") && !data.get(i).name.equals("Light")){
                        data.remove(i);
                    }
                }
                Log.d(TAG, "getView: 2---" + data.size() );
                break;
        }
        Log.d(TAG, "getView: 3---" + data.size() );
        return data;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CacheUtils.setInt(WelcomeUI.this, "userId", 0);
            Intent loginIntent = new Intent(WelcomeUI.this, LoginAndRegisterUI.class);
            String autoLoginUserName = CacheUtils.getString(WelcomeUI.this, "autoLoginUserName");
            String autoLoginPassword = CacheUtils.getString(WelcomeUI.this, "autoLoginPassword");
            if (autoLoginUserName != null && autoLoginPassword != null) {
                loginIntent.putExtra("resultUserName", autoLoginUserName);
                loginIntent.putExtra("resultPassword", autoLoginPassword);
                CacheUtils.setString(WelcomeUI.this, "autoLoginUserName", null);
                CacheUtils.setString(WelcomeUI.this, "autoLoginPassword", null);
                startActivityForResult(loginIntent, RESULT_OK);
            }
            finish();
        }
        return false;
    }

}
