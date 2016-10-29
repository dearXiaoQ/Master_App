
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.csr.masterapp.DeviceState.StateType;
import com.csr.masterapp.Recipe.RecipeListFragment;
import com.csr.masterapp.device.DeviceControlFragment;
import com.csr.masterapp.device.GroupAssignFragment;
import com.csr.masterapp.device.NotificationFragment;
import com.csr.masterapp.entities.Device;
import com.csr.masterapp.entities.DeviceStream;
import com.csr.masterapp.entities.DeviceType;
import com.csr.masterapp.entities.GroupDevice;
import com.csr.masterapp.entities.Setting;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.masterapp.fragment.ContentFragment;
import com.csr.masterapp.fragment.MenuFragment;
import com.csr.masterapp.interfaces.AssociationListener;
import com.csr.masterapp.interfaces.AssociationStartedListener;
import com.csr.masterapp.interfaces.DataListener;
import com.csr.masterapp.interfaces.GroupListener;
import com.csr.masterapp.interfaces.InfoListener;
import com.csr.masterapp.interfaces.RemovedListener;
import com.csr.masterapp.interfaces.SimpleNavigationListener;
import com.csr.masterapp.interfaces.TemperatureListener;
import com.csr.masterapp.scene.SceneListFragment;
import com.csr.masterapp.scene.util.SceneItemModel;
import com.csr.masterapp.scene.util.SceneModel;
import com.csr.masterapp.utils.CacheUtils;
import com.csr.masterapp.utils.Constans;
import com.csr.masterapp.utils.DirUtils;
import com.csr.masterapp.utils.Utils;
import com.csr.masterapp.utils.ZipUtils;
import com.csr.mesh.ActuatorModelApi;
import com.csr.mesh.AttentionModelApi;
import com.csr.mesh.BatteryModelApi;
import com.csr.mesh.ConfigModelApi;
import com.csr.mesh.DataModelApi;
import com.csr.mesh.FirmwareModelApi;
import com.csr.mesh.GroupModelApi;
import com.csr.mesh.LightModelApi;
import com.csr.mesh.MeshService;
import com.csr.mesh.PowerModelApi;
import com.csr.mesh.SensorModelApi;
import com.csr.mesh.SwitchModelApi;
import com.csr.mesh.sensor.DesiredAirTemperature;
import com.csr.mesh.sensor.InternalAirTemperature;
import com.csr.mesh.sensor.SensorValue;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends SlidingFragmentActivity implements RecipeListFragment.RecipeCallback, DeviceController, SceneListFragment.CallbackActivity,SceneController, NotificationFragment.NotificationFragmentCallbacks {

    public static Context mContext;

    private static Boolean isExit = false;

    public static Resources mResources;

    //是否是菜谱中电磁炉发送的数据
    public static boolean  RECIPE_IND_SEND_DATA = false;

    //调试模式
    public static boolean DEBUG_MODLE = false;

    private int indDeviceId = Device.DEVICE_ID_UNKNOWN;

    private boolean isOpenTask = false;

    private static final String TAG_CONTENT = "content";
    private static final String TAG_MENU = "menu";

    public static Context getContext() {
        return mContext;
    }
    public static MainActivity mMainActivity;

    private static final String TAG = "MainActivity";

    /*package*/ static final int DEVICE_LOCAL_ADDRESS =  0x8000;
    /*package*/ static final int ATTENTION_DURATION_MS = 20000;
    /*package*/ static final int ATTRACTION_DURATION_MS = 5000;

    /*package*/ static final int MAX_TTL_MASP = 0xFF;

    // Result code after to get the callback from the filepicker.
    private static final int SCANCODE_RESULT_CODE = 0;
    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int SHARING_RESULT_CODE = 2;
    private static final int REQUEST_BT_RESULT_CODE = 3;

    // How often to send a colour - i.e. how often the periodic timer fires.
    private static final int TRANSMIT_COLOR_PERIOD_MS = 240;

    // How often to send a temperature value - i.e. how often the periodic timer fires.
    private static final int TRANSMIT_TEMPERATURE_PERIOD_MS = 200;

    // Time to wait for device UUID after removing a device.
    private static final int REMOVE_ACK_WAIT_TIME_MS = (10 * 1000);

    // Time to wait showing the progress dialog.
    private static final int PROGRESS_DIALOG_TIME_MS = (10 * 1000);

    private static final int DATA_BUFFER_SIZE = 200;

    private boolean mConnected = false;
    private HashSet<String> mConnectedDevices = new HashSet<String>();

    private DeviceStore mDeviceStore;

    // The address to send packets to.
    private int mSendDeviceId = Device.DEVICE_ID_UNKNOWN;
    private int mSendSceneDeviceId = Device.DEVICE_ID_UNKNOWN;

    private static String mCurrentUuid;

    private ArrayList<DeviceStream> mStreams;
    private ArrayList<SceneModel> mScenes;

    // The colour sent every time the periodic timer fires (if mNewColor is true).
    // This will be updated by calls to setLightColor.
    private int mColorToSend = Color.rgb(0, 0, 0);

    // A new colour is only sent every TRANSMIT_PERIOD_MS if this is true. Set to true by setLightColour.
    private boolean mNewColor = false;

    private SensorValue mTemperatureToSend = null;

    private byte[] mDataToSend = null;
    private byte[] mSceneDataToSend;

    private int mGroupAcksWaiting = 0;
    private boolean mGroupSuccess = true;

    private ArrayList<Integer> mNewGroups = new ArrayList<Integer>();
    private List <Integer> mGroupsToSend;
    private int mLastActuatorMeshId = 0;
    private boolean mPendingDesiredTemperatureRequest = false;

    // A list of model ids that are waiting on a query being sent to find out how many groups are supported.
    private Queue<Integer> mModelsToQueryForGroups = new LinkedList<Integer>();

    // HashMap with all the temperature status linked to a deviceID.
    private HashMap<Integer, TemperatureStatus> mTemperatureStatus = new HashMap<>();
    private HashMap<Integer, byte[]> mInitData = new HashMap<>();

    private SparseIntArray mDeviceIdtoUuidHash = new SparseIntArray();
    private SparseArray<String> mUuidHashToAppearance = new SparseArray<String>();

    private MeshService mService = null;

    private int mRemovedUuidHash;
    private int mRemovedDeviceId;
    private int mAssociationTransactionId = -1;

    private ProgressDialog mProgress;

    private ContentFragment mNavListener;

    private byte [] mData = new byte[DATA_BUFFER_SIZE];

    // Keys used to save settings
    private static final String SETTING_LAST_ID = "lastID";

    // Listeners
    private GroupListener mGroupAckListener;
    private InfoListener mInfoListener;
    private AssociationListener mAssListener;
    private AssociationStartedListener mAssStartedListener;
    private RemovedListener mRemovedListener;
    private DataListener mDataListener;
    private TemperatureListener mTemperatureListener;

    // ConfigModelApi.DeviceInfo.VID_PID_VERSION
    byte[] vid;
    byte[] pid;
    byte[] version;

    // Temporal file that will be created for sharing purposes. It will be deleted after the sharing process will be completed.
    File tmpSharingFile = null;

    // Variables used by the notification fragment.
    private NotificationFragment mNotificationFragment;
    private boolean mRemoveNotificationAfterClick = true;

    private ArrayList<Parcelable> mDevices;

    //用于回调控制悬浮按钮动画
    ContentFragment contentFragment;

    //重连机制的实现
    //   private boolean AGAIN_CONNECTION_DEVICE = false;

    private boolean AGAIN_IS_SUCESS  = false;

    private static final int AGAIN_SUCESS = 300;

    private static final int AGAIN_FAILED = 301;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mMainActivity = MainActivity.this;
        mResources = this.getResources();
        //数据库，蓝牙
        setData();
        //侧滑菜单
        setView();

        initFragment();
    }

    private void setView() {
        setBehindContentView(R.layout.menu_container);
        SlidingMenu mMenu = getSlidingMenu();  //侧滑菜单
        mMenu.setMode(SlidingMenu.RIGHT);
        mMenu.setBehindWidth(140);
        mMenu.setShadowDrawable(R.drawable.drawer_shadow);//设置阴影图片
        mMenu.setShadowWidthRes(R.dimen.shadow_width); //设置阴影图片的宽度
        mMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);//侧滑菜单拉出位置
    }

    private void setData() {
        //数据库初始化
        mDeviceStore = new DeviceStore(this);
        mStreams = mDeviceStore.getDeviceStream();
        setContentView(R.layout.content_container);
        showProgress(getString(R.string.connecting));   //进度条等待蓝牙连接

        //绑定蓝牙服务
        Intent intent = getIntent();
        mDevices = intent.getExtras().getParcelableArrayList(BluetoothDevice.EXTRA_DEVICE);
        if (mDevices !=null && mDevices.get(0) != null) {   //启动服务连接蓝牙模块
            Intent bindIntent = new Intent(this, MeshService.class);
            bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getScenes();
    }

    @Override
    public void onBackPressed()
    {
        mService.disconnectBridge();
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            hideProgress();
            mService.setHandler(null);
            mMeshHandler.removeCallbacksAndMessages(null);
            unbindService(mServiceConnection);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(TAG_CONTENT).getChildFragmentManager().findFragmentByTag("devices");

     //   Log.d(TAG, "onKeyDown: " + manager.findFragmentByTag(TAG_CONTENT).getChildFragmentManager().findFragmentByTag("devices"));
        if(fragment instanceof DeviceControlFragment){
        //    Log.d(TAG, "onKeyDown: " + 123);
            //GamesFragment.onKeyDown(keyCode, event);
            return false;
        }
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            exitBy2Click(); //调用双击退出函数
        }
        return false;
    }

    private void exitBy2Click() {
        Timer tExit = null;
        if (!isExit) {
            isExit = true; // 准备退出
            Toast.makeText(this, mResources.getString(R.string.again_press_exit_program), Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {    //定时器检测，指定时间内没动作，重置
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000);
        } else {
            finish();
            CacheUtils.setInt(MainActivity.this, "userId", 0);
            WelcomeUI.mWelcomeUI.finish();
            System.exit(0);
        }
    }

    public void initFragment(){
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        contentFragment = new ContentFragment();
        transaction.replace(R.id.main_container_content, contentFragment, TAG_CONTENT);
        transaction.replace(R.id.main_container_menu, new MenuFragment(), TAG_MENU);
        transaction.commit();
    }


    public MenuFragment getMenuFragment() {
        FragmentManager manager = getSupportFragmentManager();
        return (MenuFragment) manager.findFragmentByTag(TAG_MENU);
    }

    public ContentFragment getContentFragment() {
        FragmentManager manager = getSupportFragmentManager();
        return (ContentFragment) manager.findFragmentByTag(TAG_CONTENT);
    }

    // Handling of notification fragments /////
    @Override
    public void onNotificationDismiss() {
        mNotificationFragment = null;
    }

    @Override
    public void onNotificationClick() {

        // try to cancel association.
        if (mAssociationTransactionId != -1) {
            mService.cancelTransaction(mAssociationTransactionId);
        }
        else {
            mNavListener.restorePosition();
        }

        // remove notification if it's the case.
        if (mRemoveNotificationAfterClick) {
            removeNotificationFragment();
        }
    }

    @Override
    public boolean isNotificationClickEnabled() {
        return mRemoveNotificationAfterClick;
    }

    private void showNotificationFragment(String title, String subtitle, int position, boolean replace, boolean removeWithClick) {
        if (mNotificationFragment == null || replace) {
            mNavListener.savePosition(position);
            mRemoveNotificationAfterClick = removeWithClick;

            if (mNotificationFragment == null) {
                Bundle fragBundle = new Bundle();
                fragBundle.putString(NotificationFragment.EXTRA_TITLE, title);
                fragBundle.putString(NotificationFragment.EXTRA_SUBTITLE, subtitle);
                mNotificationFragment = new NotificationFragment();
                mNotificationFragment.setArguments(fragBundle);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.notifier, mNotificationFragment);
                ft.commitAllowingStateLoss();
            }
            else {
                mNotificationFragment.setTitle(title);
                mNotificationFragment.setSubTitle(subtitle);
            }
        }
    }

    private void removeNotificationFragment() {
        if (mNotificationFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(mNotificationFragment);
            ft.commitAllowingStateLoss();
            mNotificationFragment = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if (mNavListener != null && mNavListener.getCurrentPosition() == SimpleNavigationListener.POSITION_GROUP_CONFIG) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_config, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_share:
                shareConfiguration();
                return true;
            case R.id.menu_load:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This method launches a sharing intent with the purpose of sharing the current configuration (database),
     * for this, it creates a temporary file within the private application folder (which will be deleted after it is used).
     */
    private void shareConfiguration() {
        // Get configuration data.
        String fileName = "CSRmesh_" + System.currentTimeMillis() + ".json";
        String configuration = mDeviceStore.getDataBaseAsJson();

        // Create the temporal file within the private application folder,
        tmpSharingFile = Utils.writeToSDFile(fileName,configuration);

        if (tmpSharingFile == null) {
            Toast.makeText(this, getString(R.string.error_share_configuration), Toast.LENGTH_SHORT).show();
            return;
        }

        // Launch a sharing intent
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tmpSharingFile));
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_configuration));
        shareIntent.setType("file/*");
        startActivityForResult(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)), SHARING_RESULT_CODE);
    }

    private LeScanCallback mScanCallBack = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mService.processMeshAdvert(device, scanRecord, rssi);
        }
    };

    /**
     * Callbacks for changes to the state of the connection.
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((MeshService.LocalBinder) rawBinder).getService();
            if (mService != null) {
                // Try to get the last setting ID used.
                SharedPreferences activityPrefs = getPreferences(Activity.MODE_PRIVATE);
                int lastIdUsed = activityPrefs.getInt(SETTING_LAST_ID, Setting.UKNOWN_ID);
                restoreSettings(lastIdUsed);

                connect();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
        }
    };

    private void connect() {
        BluetoothDevice bridgeDevice = (BluetoothDevice) mDevices.get(0);
        mService.setHandler(mMeshHandler);
        mService.setLeScanCallback(mScanCallBack);
        mService.setMeshListeningMode(true, true);
        mService.connectBridge(bridgeDevice);
//      mService.autoConnect(1, 15000, 0, 2);
    }

    /**
     * Executed when LE link to bridge is connected.
     */
    private void onConnected() {
        hideProgress();
        mConnected = true;
        if (mNavListener == null) {
            mNavListener = new ContentFragment();
        }
        startPeriodicTransmit();

    }



    /**
     * Handle messages from mesh service.
     */
    private final Handler mMeshHandler = new MeshHandler(this);

    @Override
    public void onCallbackActivity(boolean isTouch) {
        contentFragment.upDateFloatBtn();

    }

    //菜谱界面的回调函数
    @Override
    public void onRecipeCallback(boolean isCallback) {
     //   Log.i("RecipeCallback", "MainActiv 回调！");
        contentFragment.hideMenuBtn();
    }


    private class MeshHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MeshHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        public void handleMessage(Message msg) {
            MainActivity parentActivity = mActivity.get();
            switch (msg.what) {

                case 500 :
                    try {
                        parentActivity.finish();
                    } catch (Exception e) {e.printStackTrace();}
                    break;

                case MeshService.MESSAGE_LE_CONNECTED: {
                    AGAIN_IS_SUCESS = true;
                  //  Log.i("again", "设备已经连接!");
                    parentActivity.mConnectedDevices.add(msg.getData().getString(MeshService.EXTRA_DEVICE_ADDRESS));
                    if (!parentActivity.mConnected) {
                        parentActivity.onConnected();
                    }
                    break;
                }
                case MeshService.MESSAGE_LE_DISCONNECTED: {
                  //  Log.i("again", "收到MESSAGE_LE_DISCONNECTED");
                    int numConnections = msg.getData().getInt(MeshService.EXTRA_NUM_CONNECTIONS);
                    String address = msg.getData().getString(MeshService.EXTRA_DEVICE_ADDRESS);

                    if (address != null) {
                        String toRemove = null;
                        for (String s : parentActivity.mConnectedDevices) {
                            if (s.compareTo(address) == 0) {
                                toRemove = s;
                                break;
                            }
                        }
                        if (toRemove != null) {
                            parentActivity.mConnectedDevices.remove(toRemove);
                        }
                    }
                    if (numConnections == 0) {
                        AGAIN_IS_SUCESS = false;
                        parentActivity.mConnected = false;
                        Toast.makeText(parentActivity, mResources.getString
                                (R.string.connect_alread_disconnected_please_again_connect),Toast.LENGTH_LONG).show();
                        if(!isOpenTask) {
                            Timer mTimer = new Timer();
                            mTimer.schedule(new MyTask(), 1000);

                        }
                    }
                    break;
                }
                case MeshService.MESSAGE_LE_DISCONNECT_COMPLETE:
                    parentActivity.finish();
                    break;
                case MeshService.MESSAGE_REQUEST_BT:

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    parentActivity.startActivityForResult(enableBtIntent, REQUEST_BT_RESULT_CODE);
                    break;
                case MeshService.MESSAGE_TIMEOUT:{
                    int expectedMsg = msg.getData().getInt(MeshService.EXTRA_EXPECTED_MESSAGE);
                    int id;
                    int meshRequestId;
                    if (msg.getData().containsKey(MeshService.EXTRA_UUIDHASH_31)) {
                        id = msg.getData().getInt(MeshService.EXTRA_UUIDHASH_31);
                    }
                    else {
                        id = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    }
                    meshRequestId = msg.getData().getInt(MeshService.EXTRA_MESH_REQUEST_ID);
                    parentActivity.onMessageTimeout(expectedMsg, id, meshRequestId);
                    break;
                }
                case MeshService.MESSAGE_DEVICE_DISCOVERED: {
                    ParcelUuid uuid = msg.getData().getParcelable(MeshService.EXTRA_UUID);
                    int uuidHash = msg.getData().getInt(MeshService.EXTRA_UUIDHASH_31);
                    int rssi = msg.getData().getInt(MeshService.EXTRA_RSSI);
                    int ttl = msg.getData().getInt(MeshService.EXTRA_TTL);
                    if (parentActivity.mRemovedListener != null && parentActivity.mRemovedUuidHash == uuidHash) {
                        // This was received after a device was removed, so let the removed listener know.

                        parentActivity.mDeviceStore.removeDevice(parentActivity.mRemovedDeviceId);
                        parentActivity.mRemovedListener.onDeviceRemoved(parentActivity.mRemovedDeviceId, true);
                        parentActivity.mRemovedListener = null;
                        parentActivity.mRemovedUuidHash = 0;
                        parentActivity.mRemovedDeviceId = 0;
                        parentActivity.mService.setDeviceDiscoveryFilterEnabled(false);
                        removeCallbacks(parentActivity.removeDeviceTimeout);
                    } else if (parentActivity.mAssListener != null) {
                        // This was received after discover was enabled so let the UUID listener know.
                        parentActivity.mAssListener.newUuid(uuid.getUuid(), uuidHash, rssi, ttl);
                    }
                    break;
                }
                case MeshService.MESSAGE_DEVICE_APPEARANCE: {
                    // This is the appearance received when a device is in association mode.
                    // If appearance has been explicitly requested via CONFIG_DEVICE_INFO, then the appearance
                    // will be received in a MESSAGE_CONFIG_DEVICE_INFO.
                    byte[] appearance = msg.getData().getByteArray(MeshService.EXTRA_APPEARANCE);
                    String shortName = msg.getData().getString(MeshService.EXTRA_SHORTNAME);
                    int uuidHash = msg.getData().getInt(MeshService.EXTRA_UUIDHASH_31);
                    if (parentActivity.mAssListener != null) {
                        parentActivity.mUuidHashToAppearance.put(uuidHash, shortName);
                        // This was received after discover was enabled so let the UUID listener know.
                        parentActivity.mAssListener.newAppearance(uuidHash, appearance, shortName);
                    }
                    break;
                }
                case MeshService.MESSAGE_DEVICE_ASSOCIATED: {
                    // New device has been associated and is telling us its device id.
                    // Request supported models before adding to DeviceStore, and the UI.
                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    String uuid = String.valueOf(msg.getData().getInt(MeshService.EXTRA_UUID));
                    int uuidHash = msg.getData().getInt(MeshService.EXTRA_UUIDHASH_31);
                  //  Log.d(TAG, "New device associated with id " + String.format("0x%x", deviceId));

                    if (parentActivity.mDeviceStore.getDevice(deviceId) == null) {
                        // Save the device id with the UUID hash so that we can store the UUID hash in the device
                        // object when MESSAGE_CONFIG_MODELS is received.
                        parentActivity.mDeviceIdtoUuidHash.put(deviceId, uuidHash);

                        // We add the device with no supported models. We will update that once we get the info.
                        if (uuidHash != 0) {
                            parentActivity.addDevice(deviceId,uuid, uuidHash, null, 0, false);
                        }

                        // If we don't already know about this device request its model support.
                        // We only need the lower 64-bits, so just request those.
                        ConfigModelApi.getInfo(deviceId, ConfigModelApi.DeviceInfo.MODEL_LOW);
                    }
                    break;
                }
                case MeshService.MESSAGE_CONFIG_DEVICE_INFO: {
                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    int uuidHash = parentActivity.mDeviceIdtoUuidHash.get(deviceId);

                    ConfigModelApi.DeviceInfo infoType =
                            ConfigModelApi.DeviceInfo.values()[msg.getData().getByte(MeshService.EXTRA_DEVICE_INFO_TYPE)];
                    if (infoType == ConfigModelApi.DeviceInfo.MODEL_LOW) {
                        long bitmap = msg.getData().getLong(MeshService.EXTRA_DEVICE_INFORMATION);
                        // If the uuidHash was saved for this device id then this is an expected message, so process it.
                        if (uuidHash != 0) {
                            // Remove the uuidhash from the array as we have received its model support now.
                            parentActivity.mDeviceIdtoUuidHash
                                    .removeAt(parentActivity.mDeviceIdtoUuidHash.indexOfKey(deviceId));
                            String shortName = parentActivity.mUuidHashToAppearance.get(uuidHash);
                            if (shortName != null) {
                                parentActivity.mUuidHashToAppearance.remove(uuidHash);
                            }
                            parentActivity.addDevice(deviceId, mCurrentUuid, uuidHash, shortName, bitmap, true);
                            parentActivity.deviceAssociated(true, null);
                        } else if (parentActivity.mDeviceIdtoUuidHash.size() == 0) {
                            if (parentActivity.mInfoListener != null) {
                                SingleDevice device = parentActivity.mDeviceStore.getSingleDevice(deviceId);
                                if (device != null) {
                                    device.setModelSupport(bitmap, 0);
                                    parentActivity.mDeviceStore.addDevice(device);
                                    parentActivity.mInfoListener.onDeviceConfigReceived(true);
                                } else {
                                    parentActivity.mInfoListener.onDeviceConfigReceived(false);
                                }
                            }
                        }
                    } else if (infoType == ConfigModelApi.DeviceInfo.VID_PID_VERSION) {
                        parentActivity.vid = msg.getData().getByteArray(MeshService.EXTRA_VID_INFORMATION);
                        parentActivity.pid = msg.getData().getByteArray(MeshService.EXTRA_PID_INFORMATION);
                        parentActivity.version = msg.getData().getByteArray(MeshService.EXTRA_VERSION_INFORMATION);
                        if (parentActivity.mDeviceStore.getSingleDevice(deviceId).isModelSupported(BatteryModelApi.MODEL_NUMBER)) {
                            parentActivity.getBatteryState(parentActivity.mInfoListener);
                        } else if (parentActivity.mInfoListener != null) {
                            parentActivity.mInfoListener.onDeviceInfoReceived(parentActivity.vid, parentActivity.pid, parentActivity.version, GroupAssignFragment.UNKNOWN_BATTERY_LEVEL,GroupAssignFragment.UNKNOWN_BATTERY_STATE, deviceId, true);
                        } else {
                            // shouldn't happen. Just in case for avoiding endless loops.
                            parentActivity.hideProgress();
                        }

                    }
                    break;
                }
                case MeshService.MESSAGE_BATTERY_STATE: {

                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    byte batteryLevel = msg.getData().getByte(MeshService.EXTRA_BATTERY_LEVEL);
                    byte batteryState = msg.getData().getByte(MeshService.EXTRA_BATTERY_STATE);



                    if (parentActivity.mInfoListener != null) {
                        parentActivity.mInfoListener.onDeviceInfoReceived(parentActivity.vid, parentActivity.pid, parentActivity.version, batteryLevel,batteryState, deviceId, true);
                    } else {
                        // shouldn't happen. Just in case for avoiding endless loops.
                        parentActivity.hideProgress();
                    }
                    break;
                }
                case MeshService.MESSAGE_GROUP_NUM_GROUPIDS: {
                    if (parentActivity.mGroupAckListener != null) {
                        int numIds = msg.getData().getByte(MeshService.EXTRA_NUM_GROUP_IDS);
                        int modelNo = msg.getData().getByte(MeshService.EXTRA_MODEL_NO);
                        int expectedModelNo = parentActivity.mModelsToQueryForGroups.peek();
                        int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);

                        if (expectedModelNo == modelNo) {
                            SingleDevice currentDev = parentActivity.mDeviceStore.getSingleDevice(deviceId);
                            if (currentDev != null) {
                                currentDev.setNumSupportedGroups(numIds, modelNo);
                                parentActivity.mDeviceStore.addDevice(currentDev);
                                // We know how many groups are supported for this model now so remove it from the queue.
                                parentActivity.mModelsToQueryForGroups.remove();
                                if (parentActivity.mModelsToQueryForGroups.isEmpty()) {
                                    // If there are no more models to query then we can assign groups now.
                                    parentActivity.assignGroups(currentDev.getMinimumSupportedGroups());
                                } else {
                                    // Otherwise ask how many groups the next model supports, by taking the next model number from the queue.
                                    GroupModelApi.getNumModelGroupIds(parentActivity.mSendDeviceId, parentActivity.mModelsToQueryForGroups.peek());
                                }
                            } else {
                                parentActivity.mGroupAckListener.groupsUpdated(parentActivity.mSendDeviceId, false, parentActivity.getString(R.string.group_query_fail));
                            }
                        }
                    }
                    break;
                }
                case MeshService.MESSAGE_GROUP_MODEL_GROUPID: {
                    // This is the ACK returned after calling setModelGroupId.
                    if (parentActivity.mGroupAckListener != null && parentActivity.mGroupAcksWaiting > 0) {
                        parentActivity.mGroupAcksWaiting--;
                        int index = msg.getData().getByte(MeshService.EXTRA_GROUP_INDEX);
                        int groupId = msg.getData().getInt(MeshService.EXTRA_GROUP_ID);
                        // Update the group membership of this device in the device store.
                        SingleDevice updatedDev = parentActivity.mDeviceStore.getSingleDevice(parentActivity.mSendDeviceId);
                        try {
                            updatedDev.setGroupId(index, groupId);

                        } catch (IndexOutOfBoundsException exception) {
                            parentActivity.mGroupSuccess = false;
                        }
                        parentActivity.mDeviceStore.addDevice(updatedDev);


                        if (parentActivity.mGroupAcksWaiting == 0) {
                            // Tell the listener that the update was OK.
                            parentActivity.mGroupAckListener.groupsUpdated(
                                    parentActivity.mSendDeviceId, true,
                                    parentActivity.mGroupSuccess ? parentActivity.getString(R.string.group_update_ok) : parentActivity.getString(R.string.group_update_with_problems));
                        }
                    }
                    break;
                }
                case MeshService.MESSAGE_FIRMWARE_VERSION:
                    parentActivity.mInfoListener.onFirmwareVersion(msg.getData().getInt(MeshService.EXTRA_DEVICE_ID), msg
                                    .getData().getInt(MeshService.EXTRA_VERSION_MAJOR),
                            msg.getData().getInt(MeshService.EXTRA_VERSION_MINOR), true);
                    parentActivity.mInfoListener = null;
                    break;
                case MeshService.MESSAGE_ACTUATOR_VALUE_ACK: {
                    if (parentActivity.mTemperatureListener == null) {
                        // do nothing
                        return;
                    }

                    // Clear mLastActuatorMeshId if this is the mesh Id we were expecting.
                    int meshRequestId = msg.getData().getInt(MeshService.EXTRA_MESH_REQUEST_ID);

                    if (parentActivity.mLastActuatorMeshId == meshRequestId) {
                        parentActivity.mPendingDesiredTemperatureRequest = false;
                        parentActivity.mLastActuatorMeshId = 0;

                        // notify to the listener
                        parentActivity.mTemperatureListener.confirmDesiredTemperature();
                    }

                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    // update device's temperature status
                    TemperatureStatus status = parentActivity.mTemperatureStatus.get(deviceId);
                    if (status == null) {
                        status = new TemperatureStatus();
                    }
                    status.setDesiredTemperatureConfirmed(true);
                    parentActivity.mTemperatureStatus.put(deviceId, status);


                    break;
                }

                case MeshService.MESSAGE_SENSOR_VALUE: {
                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);

                    //数据接收
                    SensorValue value1 = (SensorValue) msg.getData().getParcelable(MeshService.EXTRA_SENSOR_VALUE1);
                    SensorValue value2 = null;

                    //是否包含键值"SENSORVALUE2"对象
                    if (msg.getData().containsKey(MeshService.EXTRA_SENSOR_VALUE2)) {
                        value2 = (SensorValue) msg.getData().getParcelable(MeshService.EXTRA_SENSOR_VALUE2);
                    }

                    TemperatureStatus status = parentActivity.mTemperatureStatus.get(deviceId);
                    if (status == null) {
                        status = new TemperatureStatus();
                    }

                    //接收数据
                    parentActivity.storeAndNotifyNewSensorValue(value1,status,deviceId);
                    parentActivity.storeAndNotifyNewSensorValue(value2,status,deviceId);

                }
                break;
                case MeshService.MESSAGE_RECEIVE_BLOCK_DATA: {
                    //接收数据
                    int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                    byte [] data = msg.getData().getByteArray(MeshService.EXTRA_DATA);

                    parentActivity.notifyUpDataIndData(deviceId, data);
                    parentActivity.storeAndNotifyNewDataValue(deviceId,data);
                    break;
                }
                case MeshService.MESSAGE_RECEIVE_STREAM_DATA:
                    if (parentActivity.mDataListener != null) {
                        int deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                        byte [] data = msg.getData().getByteArray(MeshService.EXTRA_DATA);
                        int sqn = msg.getData().getInt(MeshService.EXTRA_DATA_SQN);
                        if (deviceId == parentActivity.mSendDeviceId && sqn + data.length < DATA_BUFFER_SIZE) {
                            System.arraycopy(data, 0, parentActivity.mData, sqn, data.length);
                        }
                    }
                    break;
                case MeshService.MESSAGE_ASSOCIATING_DEVICE:
                    int progress = msg.getData().getInt(MeshService.EXTRA_PROGRESS_INFORMATION);
                    parentActivity.notifyAssociationFragment(progress);
                    break;
                case MeshService.MESSAGE_RECEIVE_STREAM_DATA_END:
                    if (parentActivity.mDataListener != null) {
                        int  deviceId = msg.getData().getInt(MeshService.EXTRA_DEVICE_ID);
                        if (deviceId == parentActivity.mSendDeviceId) {
                            parentActivity.mDataListener.dataReceived(deviceId, parentActivity.mData);
                        } else {
                            parentActivity.mDataListener.dataGroupReceived(deviceId);
                        }
                    }
                    break;
                case MeshService.MESSAGE_TRANSACTION_NOT_CANCELLED: {
                    Toast.makeText(parentActivity, "Association couldn't be cancelled.", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MeshService.MESSAGE_TRANSACTION_CANCELLED: {
                    parentActivity.deviceAssociated(false, parentActivity.getString(R.string.association_cancelled));
                    break;
                }
            }
        }
    }

    private void notifyUpDataIndData(int deviceId, byte[] data) {
        if (deviceId == indDeviceId && data != null) {

                if(mTemperatureListener != null) {
                    mTemperatureListener.setIndData(data);
                }
        }
    }

    /**
     * Called when a response is not seen to a sent command.
     *
     * @param expectedMessage
     *            The message that would have been received in the Handler if there hadn't been a timeout.
     */
    private void onMessageTimeout(int expectedMessage, int id, int meshRequestId) {
        switch (expectedMessage) {

            case MeshService.MESSAGE_ACTUATOR_VALUE_ACK: {
                // Clear mLastActuatorMeshId if this is the mesh Id we were expecting.
                if (mLastActuatorMeshId == meshRequestId) {
                    mPendingDesiredTemperatureRequest = false;
                    mLastActuatorMeshId = 0;
                }

            }
            case MeshService.MESSAGE_GROUP_MODEL_GROUPID:
                if (mGroupAcksWaiting > 0) {
                    if (mGroupAckListener != null) {
                        // Timed out waiting for group update ACK.
                        mGroupAckListener.groupsUpdated(mSendDeviceId, false,
                                getString(R.string.group_timeout));
                    }
                    mGroupAcksWaiting = 0;
                }
                break;
            case MeshService.MESSAGE_DEVICE_ASSOCIATED:
                // Fall through.
            case MeshService.MESSAGE_CONFIG_MODELS:
                // If we couldn't find out the model support for the device then we have to report association failed.
                deviceAssociated(false, getString(R.string.association_failed));
                if (mInfoListener!= null) {
                    mInfoListener.onDeviceConfigReceived(false);
                }
                break;
            case MeshService.MESSAGE_FIRMWARE_VERSION:
                if (mInfoListener != null) {
                    mInfoListener.onFirmwareVersion(0, 0, 0, false);
                }
                break;
            case MeshService.MESSAGE_BATTERY_STATE:
                if (mInfoListener!= null) {
                    mInfoListener.onDeviceInfoReceived(vid,pid,version, GroupAssignFragment.UNKNOWN_BATTERY_LEVEL,GroupAssignFragment.UNKNOWN_BATTERY_STATE, mSendDeviceId, true);
                }
                break;
            case MeshService.MESSAGE_GROUP_NUM_GROUPIDS:
                if (mGroupAckListener != null) {
                    mGroupAckListener.groupsUpdated(mSendDeviceId, false, getString(R.string.group_query_fail));
                }
                break;
            case MeshService.MESSAGE_CONFIG_DEVICE_INFO:

                // if we were waiting to get the configModels once we associate the device, we just assume we couldn't get the models
                // that the device support, but the association was successful.
                if (mDeviceIdtoUuidHash.size() > 0) {

                    Device device =mDeviceStore.getDevice(mDeviceIdtoUuidHash.keyAt(0));
                    mDeviceIdtoUuidHash.removeAt(0);
                    if (device != null) {
                        String name = device.getName();
                        Toast.makeText(getApplicationContext(),
                                name == null ? "Device" : name + " " + getString(R.string.added),
                                Toast.LENGTH_SHORT).show();
                    }
                    deviceAssociated(true,null);
                }
                if (mInfoListener!= null) {
                    mInfoListener.onDeviceConfigReceived(false);
                }
                if (mInfoListener != null) {
                    mInfoListener.onDeviceInfoReceived(new byte[0],new byte[0],new byte[0],GroupAssignFragment.UNKNOWN_BATTERY_LEVEL,GroupAssignFragment.UNKNOWN_BATTERY_STATE, 0, false);
                }
                break;
        }
    }

    /**
     * 接收场景数据
     *
     * @param data
     *              场景数据
     * @param deviceId
     *              设备id
     */
    private void receiveSceneData(byte[] data, int deviceId){

        int receiveValue;
        SingleDevice dev = mDeviceStore.getSingleDevice(deviceId);

        if(dev == null){
          //  Log.d(TAG, "receiveSceneData: " + dev);
            return;
        }
        int index = 0;//索引标志
        for (DeviceStream stream : mStreams){
            //判断设备类型shortname，且设备类型不可控
            if(stream.getShortname().equals(dev.getShortName().trim()) && stream.getType() == 0){
                if(stream.getData_type() == 0 || (stream.getData_type() == 1 && stream.getMax_value() <= 127 && stream.getMin_value() >= -128)){
                    receiveValue = (byte)(data[index] & 0xff);
                    executeScene(receiveValue,deviceId);
                    index += 1;
                    Log.d(TAG, "readingSceneData: 一位" + receiveValue);
                }
                if(stream.getData_type() == 1 && (stream.getMax_value() >= 127 || stream.getMin_value() <= -128)){
                    receiveValue = (short) (((data[index] << 8) | data[index + 1] & 0xff));
                    executeScene(receiveValue,deviceId);
                    index += 2;
                    Log.d(TAG, "readingSceneData: 两位" + receiveValue);
                }
            }
        }
    }

    /**
     * 判断，执行场景
     *
     * @param receiveValue
     *                  接收到的数据
     * @param deviceId
     *                  设备id
     */
    public void executeScene (int receiveValue, int deviceId){
        if(mScenes.size() != 0){
            for (SceneModel scene : mScenes){
                if(scene.getStatus() == 1){
                    for(SceneItemModel condition : scene.getConditions()){
                        Log.d(TAG, "secne-----------------------getStatus"+ condition.getDeviceId() +" --- "+ deviceId );
                        Log.d(TAG, "storeAndNotifyNewSensorValue: condition" + condition.getComparison_opt()  + condition.getValue());
                        if(condition.getDeviceId() == deviceId){
                            //满足任一条件
                            if((condition.getComparison_opt().equals("") && condition.getValue() == receiveValue) ||
                                    (condition.getComparison_opt().equals("<") && receiveValue < condition.getValue()) ||
                                    (condition.getComparison_opt().equals(">") && receiveValue > condition.getValue()) ){
                                executeTask(scene.getTasks());
                                break;
                            }
                            // TODO 满足所有条件
                        }
                    }
                }
            }
        }
    }

    /**
     * 执行场景任务
     *
     * @param taskList
     *                  任务列表
     */
    public void executeTask(List<SceneItemModel> taskList){

        for(SceneItemModel task : taskList){
            SingleDevice device = (SingleDevice) getDevice(task.getDeviceId());
            if(device == null){
                Log.d(TAG, "executeTask: 没有找到场景的设备");
                return;
            }
            String shortName = device.getShortName().trim();

            ArrayList<Byte> dataList = new ArrayList<>();
            for (DeviceStream stream : mStreams){
                if(shortName.equals(stream.getShortname()) && stream.getType() == 1){
                    if(Objects.equals(stream.getStream_name(), task.getStream_name())){
                        if(stream.getData_type() == 0 || (stream.getData_type() == 1 && stream.getMax_value() <= 127 && stream.getMin_value() >= -128)){
                            //一位
                            dataList.add((byte) ((task.getValue().shortValue()) & 0xff));
                        }
                        if(stream.getData_type() == 1 && (stream.getMax_value() >= 127 || stream.getMin_value() <= -128)){
                            //两位
                            short i = task.getValue().shortValue();
                            dataList.add((byte) ((i >>> 8) & 0xff));
                            dataList.add((byte) ((i) & 0xff));
                        }
                    }else{
                        dataList.add((byte)0xff);
                    }
                }
            }
            byte[] dataArray = new byte[dataList.size()];
            for(int i=0; i<dataList.size(); i++){
                dataArray[i] =  dataList.get(i);
            }
            Log.d(TAG, "executeTask: " + task.getDeviceId() + "--" + bytesToHexString(dataArray));
            setDesiredDataWithId(task.getDeviceId(),dataArray);
        }
    }

    public void executeTask2(List<SceneItemModel> taskList){
        for(SceneItemModel task : taskList){
        }
    }


    private void storeAndNotifyNewDataValue(int deviceId,byte[] data){
        if (data == null) return;
        if (mTemperatureListener !=null && deviceId == mSendDeviceId){
            mTemperatureListener.setDesiredData(data);
        }
        //场景
        mInitData.put(deviceId, data);
        receiveSceneData(data,deviceId);

    }

    private void storeAndNotifyNewSensorValue (SensorValue value, TemperatureStatus status, int deviceId) {

        if (value == null) return;

        //判断 value是否类InternalAirTemperature的实例
        if (value instanceof InternalAirTemperature) {

            // store the temperature in the status array.
            double tempCelsius = ((InternalAirTemperature) value).getCelsiusValue();

            String deviceStatu = bytesToHexString(value.getValue());

            Log.d(TAG, "接收的值: " +"（" + bytesToHexString(value.getValue()) +  "）--设备id：" +deviceId + "--发送的设备：" + mSendDeviceId);
            Log.d(TAG, "接收的值: " +"（" + bytesToInit(value.getValue()) +  "）--设备id：" +deviceId + "--发送的设备：" + mSendDeviceId);
            Log.d(TAG, "接收的温度tempCelsius:" + tempCelsius);
            //Toast.makeText(getApplicationContext(),"十六进制格式温度" + deviceStatu + " ---> 十进制温度" + tempCelsius ,Toast.LENGTH_SHORT).show();

            status.setCurrentTemperature(tempCelsius);
            mTemperatureStatus.put(deviceId, status);

            int receiveValue =  (int)tempCelsius;

            //场景执行代码
            if(mScenes.size() != 0){
                for (SceneModel scene : mScenes){
                    if(scene.getStatus() == 1){
                        for(SceneItemModel condition : scene.getConditions()){
                            Log.d(TAG, "secne-----------------------getStatus"+ condition.getDeviceId() +" --- "+ deviceId );
                            Log.d(TAG, "storeAndNotifyNewSensorValue: condition" + condition.getComparison_opt()  + condition.getValue());
                            if(condition.getDeviceId() == deviceId){
                                //满足任一条件
                                if( (condition.getComparison_opt().equals("") && condition.getValue() == receiveValue) ||
                                        (condition.getComparison_opt().equals("<") && receiveValue < condition.getValue()) ||
                                        (condition.getComparison_opt().equals(">") && receiveValue > condition.getValue()) ){
                                    executeTask2(scene.getTasks());
                                    break;
                                }
                                // TODO 满足所有条件
                            }
                        }
                    }
                }
            }

            // notify to temperatureFragment if the info received is related to the selected device.
            //通知 temperaturefragment ,如果收到的信息是选择的设备有关。
            if (mTemperatureListener != null && deviceId == mSendDeviceId) {
                mTemperatureListener.setCurrentTemperature(bytesToInit(value.getValue()));
                mTemperatureListener.setCurrentStatus(String.valueOf(bytesToInit(value.getValue())));
                Log.d(TAG, "接收的值是: " +"（---------------------------------------" + deviceStatu);
            }
        }
        else if (value instanceof DesiredAirTemperature) {
            double tempCelsius = ((DesiredAirTemperature) value).getCelsiusValue();
            status.setDesiredTemperature(tempCelsius);
            status.setDesiredTemperatureConfirmed(true);

            // notify to temperatureFragment if the info received is related to the selected device.
            if (mTemperatureListener != null && deviceId == mSendDeviceId && !mPendingDesiredTemperatureRequest) {

                mTemperatureListener.setDesiredTemperature(tempCelsius);

            }
        }
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static int bytesToInit(byte[] src){
        int temp = 0;
        temp = src[1]&0xff;
        temp <<= 8;
        temp |= src[0]&0xff;
        if((src[1]&0x80) == 0x80){
            temp |= 0x80000000;
            temp &= 0xffff7fff;
        }
        return temp;
    }
    /**
     * Send group assign messages to the currently selected device using the groups contained in mNewGroups.
     */
    private void assignGroups(int numSupportedGroups) {
        if (mSendDeviceId == Device.DEVICE_ID_UNKNOWN)
            return;
        // Check the number of supported groups matches the number requested to be set.
        if (numSupportedGroups >= mNewGroups.size()) {

            mGroupAcksWaiting = 0;

            // Make a copy of existing groups for this device.
            mGroupsToSend = mDeviceStore.getSingleDevice(mSendDeviceId).getGroupMembershipValues();
            // Loop through existing groups.
            for (int i = 0; i < mGroupsToSend.size(); i++) {
                int groupId = mGroupsToSend.get(i);
                if (groupId != 0) {
                    int foundIndex = mNewGroups.indexOf(groupId);
                    if (foundIndex > -1) {
                        // The device is already a member of this group so remove it from the list of groups to add.
                        mNewGroups.remove(foundIndex);
                    }
                    else {
                        // The device should no longer be a member of this group, so set that index to -1 to flag
                        // that a message must be sent to update this index.
                        mGroupsToSend.set(i, -1);
                    }
                }
            }
            // Now loop through currentGroups, and for every index set to -1 or zero send a group update command for
            // that index with one of our new groups if one is available. If there are no new groups to set, then just
            // send a message for all indices set to -1, to set them to zero.
            boolean commandSent = false;
            for (int i = 0; i < mGroupsToSend.size(); i++) {
                int groupId = mGroupsToSend.get(i);
                if (groupId == -1 || groupId == 0) {
                    if (mNewGroups.size() > 0) {
                        int newGroup = mNewGroups.get(0);
                        mNewGroups.remove(0);
                        commandSent = true;
                        sendGroupCommands(mSendDeviceId, i, newGroup);
                    }
                    else if (groupId == -1) {
                        commandSent = true;
                        sendGroupCommands(mSendDeviceId, i, 0);
                    }
                }
            }
            if (!commandSent) {
                // There were no changes to the groups so no updates were sent. Just tell the listener
                // that the operation is complete.
                if (mGroupAckListener != null) {
                    mGroupAckListener.groupsUpdated(mSendDeviceId, true, getString(R.string.group_no_changes));
                }
            }
        }
        else {
            // Not enough groups supported on device.
            if (mGroupAckListener != null) {
                mGroupAckListener.groupsUpdated(mSendDeviceId, false,
                        getString(R.string.group_max_fail) + " " + numSupportedGroups + " " + getString(R.string.groups));
            }
        }
    }

    private void sendGroupCommands(int deviceId, int index, int group) {
        mGroupSuccess = true;

        SingleDevice dev = mDeviceStore.getSingleDevice(deviceId);

        if (dev.isModelSupported(LightModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(LightModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, LightModelApi.MODEL_NUMBER,index, 0, group );
            // If a light also supports power then set groups for that too.
            if (dev.isModelSupported(LightModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(LightModelApi.MODEL_NUMBER) != 0) {
                mGroupAcksWaiting++;
                GroupModelApi.setModelGroupId(deviceId, PowerModelApi.MODEL_NUMBER, index, 0, group);
            }
        }
        else if (dev.isModelSupported(SwitchModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(SwitchModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, SwitchModelApi.MODEL_NUMBER, index, 0, group);
        }
        else if (dev.isModelSupported(SensorModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(SensorModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, SensorModelApi.MODEL_NUMBER, index, 0, group);
        }
        else if (dev.isModelSupported(ActuatorModelApi.MODEL_NUMBER) && dev.getNumSupportedGroups(ActuatorModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, ActuatorModelApi.MODEL_NUMBER, index, 0, group);
        }

        // Check if device supports data model and that it supports groups. If it does, then setModelGroupId
        if (dev.isModelSupported(DataModelApi.MODEL_NUMBER) &&
                dev.getNumSupportedGroups(DataModelApi.MODEL_NUMBER) != 0) {
            mGroupAcksWaiting++;
            GroupModelApi.setModelGroupId(deviceId, DataModelApi.MODEL_NUMBER, index, 0, group);
        }

    }

    // Runnables that execute after a timeout /////

    /**
     * This is the implementation of the periodic timer that will call sendLightRgb() every TRANSMIT_PERIOD_MS if
     * mNewColor is set to TRUE.色彩
     */
    private Runnable transmitColorCallback = new Runnable() {
        @Override
        public void run() {
            if (mNewColor) {
                if (mSendDeviceId != Device.DEVICE_ID_UNKNOWN) {
                    byte red = (byte) (Color.red(mColorToSend) & 0xFF);
                    byte green = (byte) (Color.green(mColorToSend) & 0xFF);
                    byte blue = (byte) (Color.blue(mColorToSend) & 0xFF);

                    LightModelApi.setRgb(mSendDeviceId, red, green, blue, (byte)0xFF, 0, false);

                    Device light = mDeviceStore.getDevice(mSendDeviceId);
                    LightState state = (LightState)light.getState(StateType.LIGHT);
                    if (light != null) {
                        state.setRed(red);
                        state.setGreen(green);
                        state.setBlue(blue);
                        state.setStateKnown(true);
                        light.setState(state);
                        mDeviceStore.addDevice(light);
                    }
                }
                // Colour sent so clear the flag.
                mNewColor = false;
            }
            mMeshHandler.postDelayed(this, TRANSMIT_COLOR_PERIOD_MS);
        }
    };

    /**
     * This is the implementation of the periodic temperature timer that will call setDesiredTemperature() every TRANSMIT_TEMPERATURE_PERIOD_MS if
     * mNewTemperature is set to TRUE.温度
     */
    private Runnable transmitTempCallback = new Runnable() {
        @Override
        public void run() {

            if (mLastActuatorMeshId != 0) {
                mService.killTransaction(mLastActuatorMeshId);
            }

            if (mSendDeviceId != Device.DEVICE_ID_UNKNOWN && mTemperatureToSend != null) {
                mLastActuatorMeshId = ActuatorModelApi.setValue(mSendDeviceId, mTemperatureToSend, true);
                Log.d(TAG, "run: 手动控制的设备id： --- " + mSendDeviceId);
            }

            // update device's temperature status
//            TemperatureStatus status = mTemperatureStatus.get(mSendDeviceId);
//            if (status == null) {
//                status = new TemperatureStatus();
//            }
//            status.setDesiredTemperatureConfirmed(false);
//            double celsiusValue = ((DesiredAirTemperature) mTemperatureToSend).getCelsiusValue();
//            status.setDesiredTemperature(celsiusValue);
//            mTemperatureStatus.put(mSendDeviceId,status);
        }
    };

    private Runnable transmitdataCallback = new Runnable() {
        @Override
        public void run() {
            if (mLastActuatorMeshId != 0) {
                mService.killTransaction(mLastActuatorMeshId);
            }

            if (mSendDeviceId != Device.DEVICE_ID_UNKNOWN && mDataToSend != null) {
                if(!RECIPE_IND_SEND_DATA) {
                    DataModelApi.sendData(mSendDeviceId, mDataToSend, false);
                    Log.d(TAG, "run: 设备控制---设备id ： " + mSendDeviceId + "---  发送的值是：" + bytesToHexString(mDataToSend));
                } else{
                    DataModelApi.sendData(indDeviceId, mDataToSend, false);
                    Log.d(TAG, "run: 设备控制 indDeviceId ---设备id ： " + indDeviceId + "---  发送的值是：" + bytesToHexString(mDataToSend));
                }
            }
        }
    };

//    private Runnable transmitscenedataCallback = new Runnable() {
//        @Override
//        public void run() {
//
//            if (mLastActuatorMeshId != 0) {
//                mService.killTransaction(mLastActuatorMeshId);
//            }
//
//            if (mSendSceneDeviceId != Device.DEVICE_ID_UNKNOWN && mSceneDataToSend != null) {
//                DataModelApi.sendData(mSendSceneDeviceId,mSceneDataToSend ,false);
//                Log.d(TAG, "run: 场景控制---设备id ： " + mSendSceneDeviceId  + "---  发送的值是："+ bytesToHexString(mSceneDataToSend) );
//            }
//        }
//    };

    private Runnable removeDeviceTimeout = new Runnable() {
        @Override
        public void run() {
            // Handle timeouts on removing devices.
            if (mRemovedListener != null) {
                // Timed out waiting for device UUID that indicates device removal happened.
                mRemovedListener.onDeviceRemoved(mRemovedDeviceId, false);
                mRemovedListener = null;
                mRemovedUuidHash = 0;
                mRemovedDeviceId = 0;
                mService.setDeviceDiscoveryFilterEnabled(false);
            }
        }
    };

    private Runnable progressTimeOut =  new Runnable() {
        @Override
        public void run() {

            if (mDataListener != null) {
                mDataListener.UITimeout();
            }

        }
    };

    // End of timeout handlers /////

    /**
     * Start transmitting colours and temperatures to the current send address. Colours are sent every TRANSMIT_PERIOD_MS ms and temperature values every TRANSMIT_TEMP_PERIOD_MS ms.
     */
    private void startPeriodicTransmit() {
        mMeshHandler.postDelayed(transmitColorCallback, TRANSMIT_COLOR_PERIOD_MS);
    }


    /**
     * Show a modal progress dialogue until hideProgress is called.
     *
     * @param message
     *            The message to display in the dialogue.
     */
    private void showProgress(String message) {
        if (mProgress == null) {
            mProgress = new ProgressDialog(MainActivity.this);
            mProgress.setMessage(message);
            mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgress.setIndeterminate(true);
            mProgress.setCancelable(true);
            mProgress.setCanceledOnTouchOutside(false);
            mProgress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    mService.disconnectBridge();
                }
            });
            mProgress.show();
        }
    }

    /**
     * Hide the progress dialogue.
     */
    private void hideProgress() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress=null;
        }
    }

    /**
     * Add a device to the device store, creating state based on model support.
     * @param deviceId Device id of the device to add.
     * @param uuidHash 31-bit UUID hash of the device to add.
     * @param shortName Appearance short name if known, otherwise null.
     * @param modelSupportBitmapLow The low part of the model support bitmap. Currently the only part we care about.
     */
    private void addDevice(final int deviceId, String uuid, int uuidHash, String shortName, long modelSupportBitmapLow, final boolean showToast) {
        Log.i("shortName", "进入addDevice  uuid=  " + uuidHash);
        Log.d(TAG, "addDevice: " + uuid + "............" + uuidHash);
        if (shortName == null) {
            return;
        }
        int id = deviceId - Device.DEVICE_ADDR_BASE;
        String name = String.format(shortName.trim() + " %d", id);

        Log.i("shortName" ,"name = " + name);

        final SingleDevice device = new SingleDevice(deviceId, uuid, uuidHash, name, shortName , modelSupportBitmapLow, 0);
        Log.d(TAG, "addDevice: " + device.toString());
        Log.i("shortName", "shortName = " + device.toString());
        mDeviceStore.addDevice(device);//存进数据库

        String masterAppId = CacheUtils.getString(MainActivity.this, WelcomeUI.MASTER_APP_ID);



        if (showToast && masterAppId != null) {
            checkIfDeviceRequiredAction();

            if (MenuFragment.NETWORK_ONLINE) {
                /**
                 * 发送masterAppId到服务器，与服务器进行绑定
                 *
                 * 成功：
                 *      返回json串{errorCode:0,res:""},添加设备到主设备页面
                 * 失败：
                 *      删除设备数据库中数据，重新获取数据，返回到点击界面
                 */

                final HttpUtils utils = new HttpUtils();
                final RequestParams params = new RequestParams();
                params.addBodyParameter("uuid", mCurrentUuid);
                params.addBodyParameter("deviceId", String.valueOf(deviceId));
                params.addBodyParameter("masterAppId", masterAppId);
                utils.configCurrentHttpCacheExpiry(1000 * 5);
                utils.send(HttpRequest.HttpMethod.POST,
                        Constans.DEVICE_BIND_MASTERAPP, params, new RequestCallBack<String>() {
                            @Override
                            public void onSuccess(ResponseInfo<String> responseInfo) {
                                DeviceType typeData = null;
                                Log.d(TAG, "onSuccess: " + responseInfo.result);
                                int errorCode = Integer.parseInt(Utils.ParseJSON(responseInfo.result, "errorCode"));
                                if (errorCode == 0) {
                                    String info = Utils.ParseJSON(Utils.ParseJSON(responseInfo.result, "res"), "detailInfo");
                                    if (info == null) {
                                        Toast.makeText(getContext(), mResources.getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    final String shortname = Utils.ParseJSON(info, "categoryName");
                                    String src = Utils.ParseJSON(info, "src");
                                    String ver = Utils.ParseJSON(info, "ver");
                                    Log.d(TAG, "onSucces: " + shortname + "--" + src + "--" + Utils.ParseJSON(info, "ver"));
                                    if (shortname == null || src == null || ver == null) {
                                        Toast.makeText(getContext(), mResources.getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                                        return;
                                    }


                                    String[] version = ver.split("\\.");
                                    //绑定成功
                                    ArrayList<DeviceType> localTypes = mDeviceStore.getDeviceType();


                                    Boolean isNeedUpdate = true;
                                    // TODO 下载文件并解压
                                    if (localTypes != null) {
                                        for (DeviceType type : localTypes) {
                                            Log.d(TAG, "onSuccess: " + type.getId() + "--" + type.getShortname() + "---" + type.getVersion());
                                            //已存在，不下载
                                            if (type.getShortname().equals(shortname)) {
                                                isNeedUpdate = false;
                                                //判断version
                                                String[] localVersion = type.getVersion().split("\\.");
                                                if (localVersion.length == version.length) {
                                                    for (int i = 0; i < localVersion.length; i++) {
                                                        if (Integer.parseInt(localVersion[i]) < Integer.parseInt(version[i])) {
                                                            //版本需更新，下载
                                                            isNeedUpdate = true;
                                                            typeData = new DeviceType(shortname, Utils.ParseJSON(info, "ver"));
                                                            type.setId(type.getId());
                                                            break;
                                                        } else {
                                                            //版本没更新，不下载
                                                            isNeedUpdate = false;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (typeData == null) {
                                            typeData = new DeviceType(shortname, Utils.ParseJSON(info, "ver"));
                                        }
                                    }

                                    if (isNeedUpdate) {
                                        final DeviceType data = typeData;
                                        final String downloadUrl = Environment.getExternalStorageDirectory().getPath() + "/master/";
                                        //不存在或要更新，需下载
                                        HttpUtils http = new HttpUtils();
                                        http.download(Constans.BASE_URL + src, downloadUrl + shortname + ".zip", true, true, new RequestCallBack<File>() {

                                            @Override
                                            public void onStart() {
                                                Toast.makeText(getContext(), mResources.getString(R.string.beging_download), Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onLoading(long total, long current, boolean isUploading) {
                                                Toast.makeText(getContext(), (current / 1024) + "KB" + "/" + (total / 1024) + "KB", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onFailure(HttpException error, String msg) {
                                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onSuccess(ResponseInfo<File> responseInfo) {
                                                Toast.makeText(getContext(), mResources.getString(R.string.beging_install), Toast.LENGTH_SHORT).show();
                                                //tvInfo.setText("downloaded:" + responseInfo.result.getPath());
                                                File file = DirUtils.getFilePath(downloadUrl, shortname + ".zip");

                                                try {
                                                    ZipUtils.upZipFile(file, downloadUrl + "tpl/");
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }

                                                mDeviceStore.addDevicetype(data);
                                                if (file.isFile()) {
                                                    file.delete();
                                                }
                                            }
                                        });
                                    }
                                    Toast.makeText(getContext(), mResources.getString(R.string.device_added_success), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), mResources.getString(R.string.device_added_failed), Toast.LENGTH_SHORT).show();
                                    //发送数据失败，删除数据库
                                    setSelectedDeviceId(deviceId);
                                    removeDevice(mRemovedListener);
                                    mDeviceStore.removeDevice(deviceId);
                                }
                            }

                            @Override
                            public void onFailure(HttpException e, String s) {
                                Toast.makeText(getContext(), mResources.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
                                //发送数据失败，删除数据库
                                setSelectedDeviceId(deviceId);
                                removeDevice(mRemovedListener);
                                mDeviceStore.removeDevice(deviceId);
                            }
                        });

            } else {
                Log.i("shortName", "进入离线加载模式！");
            }
        }
    }


    /**
     * Check if there is any device which needs any user action and create the notification for it.
     */
    private void checkIfDeviceRequiredAction() {
        List<Device> devices = mDeviceStore.getAllSingleDevices();
        for (Device device : devices) {
            SingleDevice singleDevice = (SingleDevice) device;

            // check if there is any sensor or actuator device which haven't been grouped yet.
            if ((singleDevice.isModelSupported(SensorModelApi.MODEL_NUMBER) || singleDevice.isModelSupported(ActuatorModelApi.MODEL_NUMBER))
                    && (singleDevice.getGroupMembership().size() == 0)) {
                showNotificationFragment(getString(R.string.user_action_req),
                        getString(R.string.need_device_group),
                        ContentFragment.POSITION_GROUP_CONFIG, false, true);
            }

        }
    }

    /**
     * Restore app settings including devices and groups.
     */
    private void restoreSettings(int settingsID) {

        // Try to get the settings if we know the ID.
        if (settingsID != Setting.UKNOWN_ID) {
            mDeviceStore.loadSetting(settingsID);
        }
        // save in sharePreferences the last settings used.
        SharedPreferences activityPrefs = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = activityPrefs.edit();

        if (mDeviceStore.getSetting() != null) {

            // set the networkKey to MeshService.
            if (mDeviceStore.getSetting().getNetworkKey() != null) {
                mService.setNetworkPassPhrase(mDeviceStore.getSetting().getNetworkKey());
            }

            // save in sharePreferences the last settings used.
            editor.putInt(SETTING_LAST_ID, settingsID);
            editor.commit();

            // get all the SingleDevices and GroudDevices from the dataBase.
            mDeviceStore.loadAllDevices();

            // set next device id to be used according with the last device used in the database.
            mService.setNextDeviceId(mDeviceStore.getSetting().getLastDeviceIndex()+1);

            // set TTL to the library
            mService.setTTL(mDeviceStore.getSetting().getTTL());
        }
        else {
            // No setting founded. We need to create one...
            Setting setting = new Setting();
            setting.setLastGroupIndex(Device.GROUP_ADDR_BASE + 5);
            mDeviceStore.setSetting(setting, true);

            // add group devices. By default we add 5 groups (1 for "All" with id=0 and 4 extra with ids 1-4).
            for (int i=0; i < 5 ; i++) {
                GroupDevice group;
                if (i==0) {
                    group = new GroupDevice(Device.GROUP_ADDR_BASE, getString(R.string.all_lights));
                }
                else {
                    group = new GroupDevice(Device.GROUP_ADDR_BASE + i, getString(R.string.group) + " " + i);
                }

                // store the group in the database.
                mDeviceStore.addGroupDevice(group,true);
            }

            // save in sharePreferences the last settings used.
            editor.putInt(SETTING_LAST_ID, mDeviceStore.getSetting().getId());
            editor.commit();
        }
    }

    @Override
    public void getScenes() {
        mScenes = mDeviceStore.getScenes();
    }

    @Override
    public void initAssociaction(boolean enabled, AssociationListener listener) {
        if (enabled) {
            mAssListener = listener;
        } else {
            mAssListener = null;
        }
    }

    @Override
    public void discoverDevices(boolean enabled) {
        //avoiding crashes
        if (mService != null) {
            mService.setDeviceDiscoveryFilterEnabled(enabled);
        }
    }

    @Override
    public boolean associateDevice(int uuidHash, String shortCode) {
        try {
            Log.i("shortName", "uuidHash = " + uuidHash);
            Log.i("shortName", "shortCode = " + shortCode);
            if (shortCode == null) {
                mAssociationTransactionId = mService.associateDevice(uuidHash, 0, false);
                notifyAssociationFragment(0);
                return true;
            } else {
                int decodedHash = MeshService.getDeviceHashFromShortcode(shortCode);

                if (decodedHash == uuidHash) {
                    mAssociationTransactionId = mService.associateDevice(uuidHash, MeshService.getAuthorizationCode(shortCode), true);
                    notifyAssociationFragment(0);
                    return true;
                }
                return false;
            }
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    @Override
    public Device getDevice(int deviceId) {
        return mDeviceStore.getDevice(deviceId);
    }

    @Override
    public void setSelectedDeviceId(int deviceId) {
        Log.d(TAG, String.format("Device id is now 0x%x", deviceId));
        mSendDeviceId = deviceId;
    }

    @Override
    public void requestCurrentTemperature() {
        SensorModelApi.getValue(mSendDeviceId, SensorValue.SensorType.INTERNAL_AIR_TEMPERATURE,SensorValue.SensorType.DESIRED_AIR_TEMPERATURE);
    }


    @Override
    public int getSelectedDeviceId() {
        return mSendDeviceId;
    }

    //亮度
    @Override
    public void setLightColor(int color, int brightness) {
        if (brightness < 0 || brightness > 99) {
            throw new NumberFormatException("Brightness value should be between 0 and 99");
        }

        // Convert currentColor to HSV space and make the brightness (value) calculation. Then convert back to RGB to
        // make the colour to send.
        // Don't modify currentColor with the brightness or else it will deviate from the HS colour selected on the
        // wheel due to accumulated errors in the calculation after several brightness changes.
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = ((float) brightness + 1) / 100.0f;
        mColorToSend = Color.HSVToColor(hsv);

        // Indicate that there is a new colour for next time the timer fires.
        mNewColor = true;
    }

    //开关
    @Override
    public void setLightPower(PowerModelApi.PowerState state) {
        PowerModelApi.setState(mSendDeviceId, state, false);
        setLocalLightPower(state);
    }


    @Override
    public void setLocalLightPower(PowerModelApi.PowerState state) {
        Device dev = mDeviceStore.getDevice(mSendDeviceId);
        if (dev != null) {
            PowState powState = (PowState)dev.getState(StateType.POWER);
            powState.setPowerState(state);
            mDeviceStore.addDevice(dev);
        }
    }

    @Override
    public void removeDevice(RemovedListener listener) {
        if (mSendDeviceId < Device.DEVICE_ADDR_BASE && mSendDeviceId >= Device.GROUP_ADDR_BASE) {
            mDeviceStore.removeDevice(mSendDeviceId);
            listener.onDeviceRemoved(mSendDeviceId, true);
            mSendDeviceId = Device.GROUP_ADDR_BASE;
        }
        else {
            mRemovedUuidHash = mDeviceStore.getSingleDevice(mSendDeviceId).getUuidHash();
            mRemovedDeviceId = mSendDeviceId;
            mRemovedListener = listener;
            // Enable discovery so that the device uuid message is received when the device is unassociated.
            mService.setDeviceDiscoveryFilterEnabled(true);
            // Send CONFIG_RESET
            ConfigModelApi.resetDevice(mSendDeviceId);
            mSendDeviceId = Device.GROUP_ADDR_BASE;
            // Start a timer so that we don't wait for the ack forever.
            mMeshHandler.postDelayed(removeDeviceTimeout, REMOVE_ACK_WAIT_TIME_MS);
        }
    }

    @Override
    public void getFwVersion(InfoListener listener) {
        mInfoListener = listener;
        FirmwareModelApi.getVersionInfo(mSendDeviceId);
    }

    @Override
    public void getVID_PID_VERSION(InfoListener listener) {
        mInfoListener = listener;
        // reset values
        vid = null;
        pid = null;
        version = null;

        // ask for new values
        ConfigModelApi.getInfo(mSendDeviceId, ConfigModelApi.DeviceInfo.VID_PID_VERSION);
    }

    @Override
    public void requestModelsSupported(InfoListener listener) {
        mInfoListener = listener;
        ConfigModelApi.getInfo(mSendDeviceId, ConfigModelApi.DeviceInfo.MODEL_LOW);
    }

    private void getBatteryState(InfoListener listener){
        mInfoListener = listener;
        BatteryModelApi.getState(mSendDeviceId);
    }


    @Override
    public void setDeviceGroups(List<Integer> groups, GroupListener listener) {
        if (mSendDeviceId == Device.DEVICE_ID_UNKNOWN)
            return;
        mNewGroups.clear();
        mGroupAckListener = listener;
        boolean inProgress = false;
        for (int group : groups) {
            mNewGroups.add(group);
        }
        SingleDevice selectedDev = mDeviceStore.getSingleDevice(mSendDeviceId);

        //

        // Send message to find out how many group ids the device supports for each model type.
        // Once a response is received to this command sendGroupAssign will be called to assign the groups.
        if (selectedDev.isModelSupported(LightModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(LightModelApi.MODEL_NUMBER)) {
            // Only query light model and assume power model supports the same number.
            mModelsToQueryForGroups.add(LightModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (selectedDev.isModelSupported(SwitchModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(SwitchModelApi.MODEL_NUMBER)) {
            mModelsToQueryForGroups.add(SwitchModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (selectedDev.isModelSupported(SensorModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(SensorModelApi.MODEL_NUMBER)) {
            mModelsToQueryForGroups.add(SensorModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (selectedDev.isModelSupported(ActuatorModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(ActuatorModelApi.MODEL_NUMBER)) {
            mModelsToQueryForGroups.add(ActuatorModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (selectedDev.isModelSupported(DataModelApi.MODEL_NUMBER) && !selectedDev.isNumSupportedGroupsKnown(DataModelApi.MODEL_NUMBER)) {
            mModelsToQueryForGroups.add(DataModelApi.MODEL_NUMBER);
            inProgress = true;
        }
        if (inProgress) {
            GroupModelApi.getNumModelGroupIds(mSendDeviceId,mModelsToQueryForGroups.peek());
        }
        else {
            // We already know the number of supported groups from a previous query, so go straight to assigning.
            assignGroups(selectedDev.getMinimumSupportedGroups());
            inProgress = true;
        }


        // There isn't any operation to do, so the dialog should be dismissed.
        if (!inProgress) {
            mGroupAckListener.groupsUpdated(mSendDeviceId, false, getString(R.string.group_query_fail));
        }

    }

    @Override
    public void setDeviceName(int deviceId, String name) {
        mDeviceStore.updateDeviceName(deviceId, name);
    }

    @Override
    public void setSecurity(String networkKeyPhrase, boolean authRequired) {
        Setting setting = mDeviceStore.getSetting();
        if (setting != null) {
            // Set the new setting values
            setting.setNetworkKey(networkKeyPhrase);
            setting.setAuthRequired(authRequired);
        }
        else {
            // if we don't have settings yet we need to create one and set the new setting values.
            setting = new Setting();
            setting.setNetworkKey(networkKeyPhrase);
            setting.setAuthRequired(authRequired);
        }
        // store the setting in the database.
        mDeviceStore.setSetting(setting, true);

        // set the new NetworkPassPhrase to the MeshService
        mService.setNetworkPassPhrase(mDeviceStore.getSetting().getNetworkKey());

        // change to the association fragment.
        mNavListener.setNavigationEnabled(true);

        // If there are devices already associated we lead the user to go to association page otherwise we lead the user to go to light control.
        if (mDeviceStore.getAllSingleDevices().size() > 0) {
            getActionBar().setSelectedNavigationItem(SimpleNavigationListener.POSITION_LIGHT_CONTROL);
        }
        else {
            getActionBar().setSelectedNavigationItem(SimpleNavigationListener.POSITION_ASSOCIATION);
        }


    }

    @Override
    public boolean isAuthRequired() {
        if (mDeviceStore.getSetting() != null) {
            return mDeviceStore.getSetting().isAuthRequired();
        }
        else {
            return false;
        }
    }

    @Override
    public String getNetworkKeyPhrase() {
        if (mDeviceStore.getSetting() != null) {
            return mDeviceStore.getSetting().getNetworkKey();
        }
        else {
            return null;
        }
    }

    @Override
    public void associateWithQrCode(AssociationStartedListener listener) {
        mAssStartedListener = listener;
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, SCANCODE_RESULT_CODE);
        }
        catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);
        }
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCANCODE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                String url = data.getStringExtra("SCAN_RESULT");
                long auth = 0;
                UUID uuid = null;


                Uri uri=Uri.parse(url);
                String uuidS = uri.getQueryParameter("UUID");
                String ac = uri.getQueryParameter("AC");

                // Trying to get the UUID and AC from a URL
                if (uuidS != null && ac != null && uuidS.length() == 32 && ac.length() == 16) {
                    long uuidMsb =
                            ((Long.parseLong(uuidS.substring(0, 8), 16) & 0xFFFFFFFFFFFFFFFFL) << 32)
                                    | ((Long.parseLong(uuidS.substring(8,16),16) & 0xFFFFFFFFFFFFFFFFL));
                    long uuidLsb =
                            ((Long.parseLong(uuidS.substring(16,24),16) & 0xFFFFFFFFFFFFFFFFL) << 32)
                                    | ((Long.parseLong(uuidS.substring(24),16) & 0xFFFFFFFFFFFFFFFFL));

                    auth = ((Long.parseLong(ac.substring(0,8), 16) & 0xFFFFFFFFFFFFFFFFL) << 32)
                            | ((Long.parseLong(ac.substring(8), 16) & 0xFFFFFFFFFFFFFFFFL));

                    uuid = new UUID(uuidMsb, uuidLsb);

                }
                else { // trying to get the UUID and AC directly from params.

                    Pattern	pattern =
                            Pattern.compile("&UUID=([0-9A-F]{8})"
                                            + "([0-9A-F]{8})([0-9A-F]{8})([0-9A-F]{8})"
                                            + "&AC=([0-9A-F]{8})([0-9A-F]{8})",
                                    Pattern.CASE_INSENSITIVE);
                    Matcher  matcher = pattern.matcher(url);
                    if (matcher.find()) {
                        long uuidMsb =
                                ((Long.parseLong(matcher.group(1), 16) & 0xFFFFFFFFFFFFFFFFL) << 32)
                                        | ((Long.parseLong(matcher.group(2), 16) & 0xFFFFFFFFFFFFFFFFL));
                        long uuidLsb =
                                ((Long.parseLong(matcher.group(3), 16) & 0xFFFFFFFFFFFFFFFFL) << 32)
                                        | ((Long.parseLong(matcher.group(4), 16) & 0xFFFFFFFFFFFFFFFFL));

                        uuid = new UUID(uuidMsb, uuidLsb);

                        auth = ((Long.parseLong(matcher.group(5), 16) & 0xFFFFFFFFFFFFFFFFL) << 32)
                                | ((Long.parseLong(matcher.group(6), 16) & 0xFFFFFFFFFFFFFFFFL));


                    }

                }

                // checking if we got the uuid
                if (uuid != null && mService != null) {
                    if (mAssStartedListener != null) {
                        mAssStartedListener.associationStarted();
                        mAssociationTransactionId = mService.associateDevice(MeshService.getDeviceHashFromUuid(uuid), auth, true);
                    }

                } else {

                    // bad QR code
                    Toast.makeText(this, getString(R.string.qr_to_uuid_fail), Toast.LENGTH_LONG).show();
                }

            }
        } else if (requestCode == SHARING_RESULT_CODE) {
            if (tmpSharingFile != null) {
                tmpSharingFile.delete();
            }
        } else if (requestCode == PICKFILE_RESULT_CODE) {

            if (data == null || data.getData() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_opening_file), Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri =data.getData();
            File file = new File(uri.getPath());

            // Check the extension of the file. App only accept .json extensions.
            if (!Utils.getFileExtension(file).equalsIgnoreCase(".json")) {
                Toast.makeText(this,getString(R.string.invalid_file_extension), Toast.LENGTH_SHORT).show();

                // no continue.
                return;
            }

            //Start reading a file...
            final StringBuilder json = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    json.append(line);
                }
                br.close();
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_opening_file), Toast.LENGTH_SHORT).show();
                return;
            }
            // end reading file

            // Confirm replacing database.
            confirmReplacingDatabase(json.toString());
        }
    }

    @Override
    public Device addLightGroup(String groupName) {
        GroupDevice result = new GroupDevice(mDeviceStore.getSetting().getNextGroupIndexAndIncrement(), groupName);
        mDeviceStore.addGroupDevice(result, true);
        return result;
    }

    @Override
    public void setAttentionEnabled(boolean enabled) {
        AttentionModelApi.setState(mSendDeviceId, enabled, ATTENTION_DURATION_MS);
    }

    @Override
    public void removeDeviceLocally(RemovedListener removedListener) {

        mDeviceStore.removeDevice(mSendDeviceId);
        removedListener.onDeviceRemoved(mSendDeviceId, true);
        mSendDeviceId = Device.GROUP_ADDR_BASE;
        removedListener = null;
    }

    @Override
    public String getBridgeAddress() {
        if (mConnected) {
            return mConnectedDevices.toString();
        }
        else {
            return null;
        }
    }

    //发送数据
    @Override
    public void setDesiredTemperature(float celsius) {

        //double kelvin = Utils.convertCelsiusToKelvin(celsius);
        //double kelvin = 0xff00;
        //double kelvin = 0x000a/32.0D;
        double kelvin = celsius/32.0D;
        mTemperatureToSend = new DesiredAirTemperature((float)kelvin);

        if (mSendDeviceId != Device.DEVICE_ID_UNKNOWN && mTemperatureToSend != null) {
            mPendingDesiredTemperatureRequest = true;
        }
        mMeshHandler.removeCallbacks(transmitTempCallback);
        mMeshHandler.postDelayed(transmitTempCallback, TRANSMIT_TEMPERATURE_PERIOD_MS);
    }

    @Override
    public void setDesiredData(byte[] data) {

        mDataToSend = data;

        if (mSendDeviceId != Device.DEVICE_ID_UNKNOWN && mDataToSend != null) {
            mPendingDesiredTemperatureRequest = true;
        }
        mMeshHandler.removeCallbacks(transmitdataCallback);
        mMeshHandler.postDelayed(transmitdataCallback, TRANSMIT_TEMPERATURE_PERIOD_MS);
        Log.d(TAG, "executeTask-------------1: " + mSendDeviceId + "---" +bytesToHexString(data));
    }

    @Override
    public void setIndDesireData(byte[] data, int deviceId) {

        indDeviceId = deviceId;
        mDataToSend = data;
        mMeshHandler.removeCallbacks(transmitdataCallback);
        mMeshHandler.postDelayed(transmitdataCallback, TRANSMIT_TEMPERATURE_PERIOD_MS);
        Log.d(TAG, "executeTask-------------1: " + deviceId + "---" +bytesToHexString(data));

    }

    @Override
    public void setDesiredDataWithId(final int deviceId,final byte[] data) {

        mSceneDataToSend = data;
        mSendSceneDeviceId = deviceId;

        if (mSendDeviceId != Device.DEVICE_ID_UNKNOWN && mDataToSend != null) {
            mPendingDesiredTemperatureRequest = true;
        }
//        mMeshHandler.removeCallbacks(transmitscenedataCallback);
        mMeshHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (mLastActuatorMeshId != 0) {
                    mService.killTransaction(mLastActuatorMeshId);
                }

                if (deviceId != Device.DEVICE_ID_UNKNOWN && data != null) {
                    DataModelApi.sendData(deviceId,data ,false);
                    Log.d(TAG, "run: 场景控制---设备id ： " + deviceId  + "---  发送的值是："+ bytesToHexString(data) );
                }
            }
        }, TRANSMIT_TEMPERATURE_PERIOD_MS);
        Log.d(TAG, "executeTask-------------2: " + deviceId + "---" +bytesToHexString(data));
    }

    @Override
    public List<Device> getDevices(int ... modelNumber) {
        ArrayList<Device> result = new ArrayList<Device>();
        for (Device dev : mDeviceStore.getAllSingleDevices()) {
            //判断模型
            if (((SingleDevice)dev).isAnyModelSupported(modelNumber)) {
                result.add(dev);
            }
        }
        return result;
    }

    @Override
    public List<Device> getDevicesByShortName(String shortname) {
        ArrayList<Device> result = new ArrayList<Device>();
        for (Device dev : mDeviceStore.getAllSingleDevices()) {
            String getShortName =  ((SingleDevice)dev).getShortName().trim();
            //判断模型
            if (getShortName.equals(shortname)){
                result.add(dev);
            }
        }
        return result;
    }


    @Override
    public ArrayList<String> getModelsLabelSupported(int deviceId) {

        Device device =mDeviceStore.getDevice(deviceId);
        if (device instanceof SingleDevice) {
            return ((SingleDevice)device).getModelsLabelSupported();
        }
        return null;
    }


    @Override
    public List<Device> getGroups() {
        return mDeviceStore.getAllGroups();
    }

    @Override
    public void getDeviceData(DataListener listener) {
        this.mDataListener = listener;
        mService.setContinuousLeScanEnabled(true);
        DeviceInfoProtocol.requestDeviceInfo(mSendDeviceId);
    }

    public Handler getMeshHandler(){
        return mMeshHandler;
    }

    @Override
    public void startUITimeOut() {
        mMeshHandler.postDelayed(progressTimeOut, PROGRESS_DIALOG_TIME_MS);

    }

    @Override
    public void stopUITimeOut() {
        mMeshHandler.removeCallbacks(progressTimeOut);
    }

    @Override
    public void setContinuousScanning(boolean enabled) {
        mService.setContinuousLeScanEnabled(enabled);
    }

    @Override
    public void setTemperatureListener(TemperatureListener listener) {
        this.mTemperatureListener = listener;
    }

    @Override
    public void setUuid(String uuid) {
        this.mCurrentUuid = uuid;
    }

    @Override
    public String getUuid() {
        return this.mCurrentUuid;
    }

    @Override
    public void postRunnable(Runnable checkScanInfoRunnable) {
        getMeshHandler().post(checkScanInfoRunnable);
    }

    @Override
    public void removeRunnable(Runnable checkScanInfoRunnable) {
        getMeshHandler().removeCallbacks(checkScanInfoRunnable);
    }

    @Override
    public int getMaxTTLForMASP() {
        return MAX_TTL_MASP;
    }

    @Override
    public int getTTLForMCP() {
        return mService.getTTL();
    }

    @Override
    public void setTTLForMCP(int ttl) {
        // set ttl to the library
        mService.setTTL(ttl);
        Setting settings =mDeviceStore.getSetting();
        settings.setTTL(ttl);

        // save new settings with the new TTL value
        mDeviceStore.setSetting(settings, true);
    }

    /**
     * Notify device association has finished.
     * @param success
     */
    private void deviceAssociated(boolean success, String message) {
        // device associated so we need to clean the transaction id.
        mAssociationTransactionId = -1;

        if (mAssListener != null) {
            mAssListener.deviceAssociated(success, message);
        }
        removeNotificationFragment();
        if (success) {
            // Reload configuration fragment.
            mNavListener.refreshConfigFragment();
        }
    }

    private void notifyAssociationFragment(int progress) {
        showNotificationFragment(getString(R.string.association_progress) + progress + "%",
                getString(R.string.association_notify),
                SimpleNavigationListener.POSITION_ASSOCIATION, true, false);
    }

    public TemperatureStatus getTemperatureStatus() {
        return mTemperatureStatus.get(mSendDeviceId);
    }

    public byte[] getInitData(){
        return mInitData.get(mSendDeviceId);
    }

    @Override
    public byte[] getIndData(int indDeviceId) {
        return new byte[0];
    }

    public void activateAttentionMode(int uuidHash, boolean enabled) {

        // enable the new uuidHash.
        mService.setAttentionPreAssociation(uuidHash, enabled, ATTRACTION_DURATION_MS);

        // notify the user.
        if (enabled) {
            Toast.makeText(MainActivity.this, getString(R.string.attraction_enabled), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmReplacingDatabase(final String json) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_replacing_db)).setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                // read json and set to the database.
                                final boolean success = mDeviceStore.setConfigurationFromJson(json.toString(),mDeviceStore.getSetting().getNetworkKey());
                                runOnUiThread(new Runnable() {
                                    public void run() {

                                        // notify to the user.
                                        Toast.makeText(getApplicationContext(), success?getString(R.string.import_config_complete):getString(R.string.import_config_error), Toast.LENGTH_SHORT).show();

                                        // reload settings.
                                        SharedPreferences activityPrefs = getPreferences(Activity.MODE_PRIVATE);
                                        int lastIdUsed = activityPrefs.getInt(SETTING_LAST_ID, Setting.UKNOWN_ID);
                                        restoreSettings(lastIdUsed);

                                        // reload configuration fragment.
                                        mNavListener.refreshConfigFragment();
                                    }
                                });
                            }
                        }).start();

                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    //重连机制Runnable
    class MyTask extends TimerTask {

        @Override
        public void run() {
            boolean isOk = true;
            while(isOk) {
                Log.i("again", "MyTask AGAIN_IS_SUCESS = " + AGAIN_IS_SUCESS);
                if(!AGAIN_IS_SUCESS) {
                    isOpenTask = true;
                    Log.i("again", "进入重连任务！");
                    //启动服务连接蓝牙模块
                  /*  Intent bindIntent = new Intent(mContext, MeshService.class);
                    bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);*/
                    try {
                        Log.i("again", "睡眠等待4秒钟");
                        Thread.sleep(4000);
                        if (AGAIN_IS_SUCESS) {
                            Log.i("again", "连接成功不退出Activity");
                        } else {
                            Log.i("again", "还没连接成功再睡眠5秒钟");
                            Thread.sleep(5000);
                            if(AGAIN_IS_SUCESS){
                                Log.i("again", "连接成功不退出Actiivity");
                            } else {
                                unbindService(mServiceConnection);
                                Log.i("again", "退出Activity");
                                mMeshHandler.sendEmptyMessage(500);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    isOk = false;
                    isOpenTask = false;
                }
                Log.i("again", "退出Task");
            }
        }
    }



}
