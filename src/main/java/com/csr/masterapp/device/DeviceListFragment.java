
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.device;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.masterapp.CheckedListItem;
import com.csr.masterapp.DeviceController;
import com.csr.masterapp.R;
import com.csr.masterapp.WelcomeUI;
import com.csr.masterapp.adapter.DeviceVpAdapter;
import com.csr.masterapp.database.DataBaseDataSource;
import com.csr.masterapp.entities.Device;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.masterapp.fragment.MenuFragment;
import com.csr.masterapp.interfaces.DataListener;
import com.csr.masterapp.interfaces.GroupListener;
import com.csr.masterapp.interfaces.InfoListener;
import com.csr.masterapp.interfaces.RemovedListener;
import com.csr.masterapp.utils.CacheUtils;
import com.csr.masterapp.utils.Constans;
import com.csr.masterapp.utils.Utils;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Fragment used to configure devices. Handles assigning devices to groups, get firmware version, remove a device or
 * group, rename a device or group and add a new group. Contains two side by side CheckedListFragment fragments.
 *
 */
public class DeviceListFragment extends Fragment implements GroupListener,InfoListener, RemovedListener, DataListener, CheckedListFragment.ItemListener {

    /**
     * 记录设备列表页面的位置
     */
    public static  int DEVICE_LIST_POSITION = 0;

    private static final String TAG = "DeviceListFragment";

    private static int DEVICE_SHOW_IMAGE_POSITION = 0;

    private static final int STATE_NORMAL = 0;
    private static final int STATE_EDIT_GROUP = 1;
    private static final int STATE_EDIT_DEVICE = 2;

    private ArrayList<Device> dat_list = new ArrayList<Device>();
    private ArrayList<SingleDevice> data_list = new ArrayList<>();

    private int deviceCount ;  //设备数量

    // Constants for application level device info protocol that is sent over data stream.
    private static final int DEVICE_INFO_OFFSET_LENGTH = 1;
    private static final int DEVICE_INFO_OFFSET_DATA = 2;
    private static final int DEVICE_INFO_HEADER_LENGTH = DEVICE_INFO_OFFSET_DATA;
    private static final int CSR_DEVICE_INFO_RSP = 0x02;

    public static final int UNKNOWN_BATTERY_LEVEL = -1;
    public static final int UNKNOWN_BATTERY_STATE = -1;

    private ProgressDialog mProgress;

    protected static final int MAX_DEVICE_NAME_LENGTH = 20;

    private DeviceController mController;
    private CheckedListFragment mDeviceFragment;
    private SingleDevice device = null;
    private static final int MAX_TTL = 128 - 1;


    private View mRootView;
    private AlertDialog mDeviceInfoAlert = null;
    private String mDeviceInfoData = null;
    private Device mSelectedDevice = null;
    // Id of device or group with attention enabled.
    private int mAttentionId;
    private int mState = STATE_NORMAL;

    // Used when editing group membership of a single selected device.
    private GroupList mEditGroupList = null;
    // Used when a group is selected; add each modified list of groups to this list.
    private SparseArray<GroupList> mModifiedGroups = new SparseArray<GroupList>();

    private Fragment mMainFragment;

    private GridView mGridView;

    private ViewPager deviceVP;

    private LinearLayout containLL;

    private ArrayList<View> viewsList = null;

    private DeviceVpAdapter deviceVpAdapter;    //ViewPager Adapter

    private  int displayWidth;

    private int  widthSub;      //滑动判断阈值

    private boolean isFirst = true;

    // Database fields
    private DataBaseDataSource mDataBase;

    public FragmentManager mFragmentManager;

    private Resources mResources;

    //初始化图片数组
    private int[] imgId = {R.drawable.ico_ecookpan, R.drawable.ico_rhood,
            R.drawable.ico_lamp, R.drawable.ico_pirsensor,
            R.drawable.ico_lsensor, R.drawable.ico_ssensor};

    /**
     * 存储图标
     */
    private static HashMap<String ,Integer> pictureMap;


    //初始化文字数组
  //  private String[] data = {"Ecookpan", "油烟机", "灯", "人体传感器", "光线传感器", "烟雾传感器"};

    private String associateUUID;

    private int VpHeight = 0;
    private int VpWidth  = 0;


    private boolean left = false;
    private boolean right = false;
    private boolean isScrolling = false;
    private float lastValue = -1;
    // private ChangeViewCallback changeViewCallback = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            Log.d(TAG, "onAttach: 1");
            mController = (DeviceController) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceController callback interface.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainFragment = this;
        mResources = getActivity().getResources();
        // show the actionBar menu.
        setHasOptionsMenu(true);
        mDataBase = new DataBaseDataSource(getActivity());
        data_list = mDataBase.getAllSingleDevices();

        pictureMap = new HashMap<>();

        //Ecookpan  RHood Light
        pictureMap.put("Ecookpan", R.drawable.ico_ecookpan);
        pictureMap.put("RHood", R.drawable.ico_rhood);
        pictureMap.put("Light", R.drawable.ico_lamp);
        pictureMap.put("IndCooker", R.drawable.ico_indcooker);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.device_list_fragment, container, false);
        }
        //实例化数据库，从数据库抽取搜索出来的设备列表

        mProgress = new ProgressDialog(getActivity());
        mDeviceFragment = new CheckedListFragment();
        //初始化搜寻控件ID
        containLL = (LinearLayout) mRootView.findViewById(R.id.containLL);
        deviceVP = (ViewPager) mRootView.findViewById(R.id.deviceVP);
        deviceVP.setOffscreenPageLimit(3);  //viewPager缓存数量
        deviceVP.setPageMargin(5);         //设置页面的间距


        //先新建Adapter需要的View
        initAdapterView();

        deviceVpAdapter = new DeviceVpAdapter(viewsList);
        deviceVP.setAdapter(deviceVpAdapter);
        if(deviceCount > 2 && DEVICE_LIST_POSITION == 0 && DEVICE_SHOW_IMAGE_POSITION == 0) {
            deviceVP.setCurrentItem(1);
        } else {
            deviceVP.setCurrentItem(DEVICE_SHOW_IMAGE_POSITION);
        }


        deviceVP.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float arg2, int positionOffsetPixels) {
                //初始化（只会调用一次）
                initView();
                if(deviceCount > 0) {
                    ImageView currentView = (ImageView) viewsList.get(deviceVP.getCurrentItem()).findViewById(R.id.iv_device_ico);
                    TextView currentTv = (TextView) viewsList.get(deviceVP.getCurrentItem()).findViewById(R.id.tv_device_name);

                    if (lastValue > arg2 && isScrolling) {
                        // 递减，向右侧滑动 1 ->0
                        right = true;
                        left = false;
                        if (arg2 > 0.5) {
                            currentView.setLayoutParams(new RelativeLayout.LayoutParams((int) (VpHeight * arg2),
                                    (int) (VpWidth * arg2)));
                            currentView.setAlpha(arg2);
                            currentTv.setAlpha(arg2);

                        }
                    } else if (lastValue < arg2 && isScrolling) {
                        // 递增，向左侧滑动 0 -> 1
                        right = false;
                        left = true;
                        if (arg2 < 0.5) {
                            currentView.setLayoutParams(new RelativeLayout.LayoutParams((int) (VpHeight * (1 - arg2)),
                                    (int) (VpWidth * (1 - arg2))));
                            currentView.setAlpha(1 - arg2);
                            currentTv.setAlpha(1 - arg2);

                        }
                    } else if (lastValue == arg2) {//滑动过程中暂停下来
                        isScrolling = false;
                    } else if (left) {
                        //向左滑动复原  1 -> 0    扩大
                        if (arg2 != 1.0 && arg2 < 0.5) {
                            currentView.setLayoutParams(new RelativeLayout.LayoutParams((int) (VpHeight * (1 - arg2)),
                                    (int) (VpWidth * (1 - arg2))));
                            currentView.setAlpha(1 - arg2);
                            currentTv.setAlpha(1 - arg2);
                        } else {
                            currentView.setLayoutParams(new RelativeLayout.LayoutParams((int) (VpHeight * (arg2)),
                                    (int) (VpWidth * (arg2))));
                            currentView.setAlpha((float) 1.0);
                            currentTv.setAlpha((float) 1.0);
                        }
                    } else if (right) {
                        //向右滑动复原
                        if (arg2 != 1.0 && arg2 > 0.5) {
                            currentView.setLayoutParams(new RelativeLayout.LayoutParams((int) (VpHeight * (arg2)),
                                    (int) (VpWidth * (arg2))));
                            currentView.setAlpha(arg2);
                            currentTv.setAlpha(arg2);
                        } else {
                            currentView.setLayoutParams(new RelativeLayout.LayoutParams(VpHeight, VpWidth));
                            currentView.setAlpha((float) 1.0);
                            currentTv.setAlpha((float) 1.0);
                        }
                    }

                    lastValue = arg2;
                }
            }


            @Override
            public void onPageSelected(int arg0) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                if (arg0 == 1) {
                    isScrolling = true;
                } else {
                    isScrolling = false;
                }
                if (arg0 == 2) {

                }
                SetParamas();
            }
        });


        return  mRootView;
    }


    private void initView() {
        if (isFirst) {
            isFirst = false;
            //初始化控件参数
            SetParamas();
        }
    }



    private void SetParamas(){
        VpHeight = deviceVP.getHeight();
        VpWidth  = deviceVP.getWidth();
        DEVICE_SHOW_IMAGE_POSITION = deviceVP.getCurrentItem();
        for (int i = 0; i < deviceCount; i++) {
            ImageView currentView = (ImageView) viewsList.get(i).findViewById(R.id.iv_device_ico);
            TextView currentTv = (TextView) viewsList.get(i).findViewById(R.id.tv_device_name);
            if (i != DEVICE_SHOW_IMAGE_POSITION) {
                RelativeLayout.LayoutParams IvParams = new RelativeLayout.LayoutParams(VpHeight / 2, VpWidth / 2);
                //setMargins(int left, int top, int right, int bottom)
                if( i < DEVICE_SHOW_IMAGE_POSITION) {
                    IvParams.setMargins(VpHeight / 3, VpHeight / 7, 0, 0);
                } else {
                    IvParams.setMargins(0, VpHeight / 7, VpHeight / 3, 0);
                }
                currentView.setLayoutParams(IvParams);
                currentTv.setVisibility(View.GONE);
                currentView.setAlpha((float)0.5);
            } else {
                RelativeLayout.LayoutParams IvParams = new RelativeLayout.LayoutParams(VpHeight, VpWidth);
                currentTv.setVisibility(View.VISIBLE);
                currentView.setLayoutParams(IvParams);
                currentView.setAlpha((float)1.0);
                currentTv.setAlpha((float)1.0);
            }
        }
    }



    private void initAdapterView() {
        deviceCount = data_list.size();
        viewsList = new ArrayList();
        View view;
        TextView tv;
        ImageView iv;
        // BitmapUtils mBitmapUtils = new BitmapUtils(getActivity());

        for (SingleDevice singleDev : data_list) {
            Log.i("mDevices", "singleDev = " + singleDev.toString());
        }

        for(int i = 0; i < deviceCount; i++) {
            view = LayoutInflater.from(getActivity()).inflate(R.layout.item_gv_device, null);
            tv = (TextView) view.findViewById(R.id.tv_device_name);
            iv = (ImageView) view.findViewById(R.id.iv_device_ico);

            SingleDevice dev = data_list.get(i);
            tv.setText(dev.getName());
            String shortname = dev.getShortName().trim();

            //判断获取的名字进行图片匹配
            switch (shortname) {
                case "Ecookpan":
                    // 设置网络图片
                    iv.setImageResource(pictureMap.get("Ecookpan"));
                    // mBitmapUtils.display(iv, "/sdcard/master/tpl/Ecookpan/images/index.png");
                    break;
                case "RHood":
                    iv.setImageResource(pictureMap.get("RHood"));
                    // mBitmapUtils.display(iv, "/sdcard/master/tpl/RHood/images/index.png");

                    break;
                case "Light":
                    iv.setImageResource(pictureMap.get("Light"));
              /*      mBitmapUtils.display(iv, "/sdcard/master/tpl/Light/images/index.png");
                    iv.setImageResource(R.drawable.baidu);*/
                    break;
                case "IndCooker" :
                    iv.setImageResource(pictureMap.get("IndCooker"));
                    break;
            }

            iv.setOnCreateContextMenuListener(new ViewPager.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add(0, 0, 0, mResources.getString(R.string.rename));
                    menu.add(0, 1, 1, mResources.getString(R.string.delete_device));
                    menu.add(0, 2, 2, mResources.getString(R.string.device_info));
                    menu.add(0, 3, 3, mResources.getString(R.string.firnware_info));
                    //   menu.add(0, 4, 4, mResources.getString(R.string.request_data));
                    //  menu.add(0, 5, 5, mResources.getString(R.string.request_config));
                }
            });


            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jumpFragment();
                }

            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    jumpFragment();
                }
            });

            viewsList.add(view);

        }

    }


    /**
     * 方法名称：设备功能菜单事件触发，服务器交互
     * 说明：  点击设备列表弹出的功能菜单事件的实现
     * 创建者：  聪  11437
     *
     * @param Item 菜单项
     */
    @Override
    public boolean onContextItemSelected(MenuItem Item) {       //Item
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) Item.getMenuInfo();
        //     Log.i("positionOffset", "info.position = " + info.position);
        final SingleDevice dev = data_list.get(deviceVP.getCurrentItem());
        //HTTP通信
        HttpUtils utils = new HttpUtils();
        RequestParams params = new RequestParams();
        switch (Item.getItemId()) {
            /**
             * 设备重新命名
             */
            case 0:
                rename(dev);
                return true;
            /**
             * 设备删除解绑
             */
            case 1:
                final String tag = mMainFragment.getTag();
                if(MenuFragment.NETWORK_ONLINE) {
                    String masterAppId = CacheUtils.getString(getActivity(), WelcomeUI.MASTER_APP_ID);
                    String uuid = dev.getUuid();
                    params.addBodyParameter("uuid", uuid);
                    params.addBodyParameter("masterAppId", masterAppId);
                    utils.configCurrentHttpCacheExpiry(1000 * 5);
                    utils.send(HttpRequest.HttpMethod.POST, Constans.DEVICE_UNBIND_MASTERAPP, params, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Log.d(TAG, "Remove device json return : " + responseInfo.result);
                            int errorCode = Integer.parseInt(Utils.ParseJSON(responseInfo.result, "errorCode"));
                            if (errorCode == 0) {
                                itemRemove(tag, dev.getDeviceId());
                            }
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Toast.makeText(getActivity(), mResources.getString(R.string.network_connection_failed), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    itemRemove(tag, dev.getDeviceId());
                }
                return true;
            case 2:
                itemInfo(this.getTag(), dev.getDeviceId());
                return true;
            case 3:
                itemVersion(this.getTag(), dev.getDeviceId());
                return true;
            case 4:
                itemGetData(this.getTag(), dev.getDeviceId());
                return true;
            case 5:
                itemRequestModels(this.getTag(), dev.getDeviceId());
                return true;
        }
        return super.onContextItemSelected(Item);
    }

    @Override
    public void onItemContextMenuClick(int menuGroupId, int position, int menuId) {
        if (menuGroupId == R.id.device_menu_group) {
            mDeviceFragment.handleContextMenu(position, menuId);
        }
    }


    /**
     * 方法名称：重命名设备
     * 说明：  把已添加的设备进行重命名
     * 创建者：  聪  11437
     * 修改者：
     * 备 注：
     * @param device : 获取重命名设备
     */
    private void rename(final SingleDevice device) {
        //
        final HttpUtils utils = new HttpUtils();
        final RequestParams params = new RequestParams();
        //
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mResources.getString(R.string.input_new_device_name));
        final EditText input = new EditText(getActivity());
        input.setHint(device.getName());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DEVICE_NAME_LENGTH)});
        builder.setView(input);
        builder.setPositiveButton(getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String newName = input.getText().toString();
                for (Device dev : data_list){
                    if(newName.equals(dev.getName())){
                        Toast.makeText(getActivity(), mResources.getString(R.string.device_name_is_exists_pelase_retry),Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Log.d(TAG, "DeviceId: " + device.getDeviceId());
                if(MenuFragment.NETWORK_ONLINE) {
                    params.addBodyParameter("uuid", device.getUuid());
                    params.addBodyParameter("devName", newName);
                    utils.configCurrentHttpCacheExpiry(1000 * 5);
                    utils.send(HttpRequest.HttpMethod.POST, Constans.DEVICE_RENAME_MASTERAPP, params, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Log.d(TAG, "Json : " + responseInfo.result);
                            int errorCode = Integer.parseInt(Utils.ParseJSON(responseInfo.result, "errorCode"));
                            if (errorCode == 0) {
                                Toast.makeText(getActivity(), mResources.getString(R.string.successful_modification), Toast.LENGTH_SHORT).show();
                                itemRename(getTag(), device.getDeviceId(), newName);
                                device.setName(newName);
                                refrashGridView();
                            } else {
                                Toast.makeText(getActivity(), mResources.getString(R.string.data_error), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(HttpException e, String s) {
                            Toast.makeText(getActivity(), mResources.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    itemRename(getTag(), device.getDeviceId(), newName);
                    device.setName(newName);
                    refrashGridView();
                }
            }
        });
        builder.setNegativeButton(getActivity().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable text) {
                if (text.length() <= 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No behaviour.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No behaviour.
            }
        });

    }


    /**
     * 方法名称：  jumpFragment
     * 说   明：  点击图标，跳转到设备详细页
     */
    private void jumpFragment() {
        SingleDevice dev = (SingleDevice) mController.getDevices().get(DEVICE_SHOW_IMAGE_POSITION);
        //对相应的设备进行页面处理，跳转
        mMainFragment = new DeviceControlFragment(dev);
        FragmentManager Manager = getActivity().getSupportFragmentManager();
        Manager.beginTransaction().addToBackStack(null).replace(R.id.main_container_content, mMainFragment).commit();
    }

    /**
     * 方法名称：  hideProgress
     * 说   明：  隐藏等待进度
     */
    private void hideProgress() {
        mProgress.dismiss();
    }

    /**
     * Show a modal progress dialogue until hideProgress is called.
     *
     * @param string The message to display in the dialogue.
     */
    private void showProgress(String string) {
        mProgress.setMessage(string);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgress.setIndeterminate(true);
        mProgress.setCancelable(false);
        mProgress.show();
    }

    /**
     * 方法名称：  confirmRemove
     * 说   明：  删除设备确认
     */
    private void confirmRemove(final int deviceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mResources.getString(R.string.remove_device)).setPositiveButton(getActivity().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Perform remove operation.
                mController.setSelectedDeviceId(deviceId);
                mController.removeDevice((RemovedListener) mMainFragment);
                if (deviceId >= Device.DEVICE_ADDR_BASE) {
                    // Removing groups is instant so no need for progress.
                    showProgress(mResources.getString(R.string.device_removeing));
                }
            }
        }).setNegativeButton(getActivity().getString(R.string.no), null);
        AlertDialog alert = builder.create();
        alert.show();

    }

    /**
     * 方法名称：  confirmRemoveLocal
     * 说   明：  强制删除设备确认
     */
    private void confirmRemoveLocal(final int deviceId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getString(R.string.confirm_remove_local)).setCancelable(false)
                .setPositiveButton(getActivity().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Perform remove operation.
                        mController.setSelectedDeviceId(deviceId);
                        mController.removeDeviceLocally((RemovedListener) mMainFragment);

                        // refreshList
                        refrashGridView();

                    }
                }).setNegativeButton(getActivity().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Start the timeout which manages if the progress bar dialog should be closed and not block the UI anymore.
     */
    private void startUITimeOut() {
        mController.startUITimeOut();
    }

    /**
     * Cancel the timeout which manages if the progress bar dialog should be closed and not block the UI anymore.
     */
    private void cancelUITimeOut() {
        mController.stopUITimeOut();
    }

    @Override
    public void dataReceived(int deviceId, byte[] data) {
        cancelUITimeOut();
        hideProgress();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getDeviceInfoString(data))
                .setCancelable(false)
                .setPositiveButton(getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void UITimeout() {
        hideProgress();
        Toast.makeText(getActivity(), "The operation has timed out", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void dataGroupReceived(int deviceId) {
        hideProgress();
        cancelUITimeOut();

        mDeviceInfoData += System.getProperty("line.separator") + deviceId;

        String message = mDeviceInfoData;
        if (mDeviceInfoAlert == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            mDeviceInfoAlert = builder.create();
            mDeviceInfoAlert.show();
        } else {
            mDeviceInfoAlert.hide();
            mDeviceInfoAlert.setMessage(mDeviceInfoData);
            mDeviceInfoAlert.show();
        }

    }

    @Override
    public void groupsUpdated(int deviceId, boolean success, String msg) {
        boolean done = false;
        if (success) {
            ((SingleDevice) mSelectedDevice).setGroupIds(mEditGroupList.groupMembership);
            if (mState == STATE_EDIT_DEVICE || mModifiedGroups.size() == 0) {
                done = true;
            } else {
                // If no update was sent then we are done, so a message will be displayed.
                done = !updateNextDeviceInGroup();
            }
        } else {
            // The update failed, so don't try and update any more devices.
            done = true;
        }
        if (done) {
            if (msg != null) {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            }
            resetUI();
            hideProgress();
        }
    }

    @Override
    public void onFirmwareVersion(int deviceId, int major, int minor, boolean success) {
        hideProgress();
        if (success) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(getActivity().getString(R.string.firmware_version_is) + " " + major + "." + minor).setCancelable(false)
                    .setPositiveButton(getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.firmware_get_fail), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeviceInfoReceived(byte[] vid, byte[] pid, byte[] version, int batteryPercent, int batteryState, int deviceId, boolean success) {
        hideProgress();
        if (success) {
            ArrayList<String> modelsLabel = mController.getModelsLabelSupported(deviceId);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.menu_device_info);
            String modelsListText = "";
            // making fancy the way we shows the list of models supported.
            if (modelsLabel == null || modelsLabel.size() == 0) {
                modelsListText = getActivity().getString(R.string.not_support_any_model);
            } else {
                for (int i = 0; i < modelsLabel.size(); i++) {
                    modelsListText += "- " + modelsLabel.get(i) + System.getProperty("line.separator");
                }

            }
            modelsListText += System.getProperty("line.separator") + "Vendor ID: 0x" + Utils.hexString(vid).toUpperCase();
            modelsListText += System.getProperty("line.separator") + "Product ID: 0x" + Utils.hexString(pid).toUpperCase();
            modelsListText += System.getProperty("line.separator") + "Version number: " + String.format("%02d", version[0]) + "." + String.format("%02d", version[1]) + "." + String.format("%02d", version[2]) + "." + String.format("%02d", version[3]);

            if (batteryPercent != UNKNOWN_BATTERY_LEVEL) {
                modelsListText += System.getProperty("line.separator");
                modelsListText += getString(R.string.battery_level) + ": " + batteryPercent + "%";
            }
            if (batteryState != UNKNOWN_BATTERY_STATE) {
                modelsListText += System.getProperty("line.separator");
                modelsListText += getString(R.string.battery_state) + ": " + getResources().getStringArray(R.array.battery_states)[batteryState];
            }
            builder.setMessage(modelsListText).setCancelable(false)
                    .setPositiveButton(getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else if (getActivity() != null) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.configuration_get_fail), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDeviceConfigReceived(boolean success) {
        hideProgress();
        if (success) {
            Toast.makeText(getActivity(), "Device configuration updated.", Toast.LENGTH_SHORT).show();
            resetDeviceList();
        }

    }

    @Override
    public void onItemSelected(String fragmentTag, Device device) {
        resetUI();
    }

    @Override
    public void onItemCheckStatusChanged(String fragmentTag, int deviceId, boolean checked) {
        resetUI();
    }

    @Override
    public void itemRename(String fragmentTag, int deviceId, String name) {
        mController.setDeviceName(deviceId, name);
    }

    @Override
    public void itemRemove(String fragmentTag, int deviceId) {
        confirmRemove(deviceId);
    }

    @Override
    public void itemInfo(String fragmentTag, int deviceId) {
        showProgress(getActivity().getString(R.string.waiting_for_response));
        mController.setSelectedDeviceId(deviceId);
        mController.getFwVersion((InfoListener) mMainFragment);
    }

    @Override
    public void itemVersion(String fragmentTag, int deviceId) {
        showProgress(getActivity().getString(R.string.waiting_for_response));
        mController.setSelectedDeviceId(deviceId);
        mController.getVID_PID_VERSION((InfoListener) mMainFragment);
    }

    @Override
    public void itemGetData(String fragmentTag, int deviceId) {
        showProgress(getActivity().getString(R.string.waiting_for_response));
        startUITimeOut();
        mController.setSelectedDeviceId(deviceId);
        mController.getDeviceData((DataListener) mMainFragment);
        mDeviceInfoData = "Data received from these devices:";
        mDeviceInfoAlert = null;
    }

    @Override
    public void itemRequestModels(String fragmentTag, int deviceId) {
        showProgress(getActivity().getString(R.string.waiting_for_response));
        mController.setSelectedDeviceId(deviceId);
        mController.requestModelsSupported((InfoListener) mMainFragment);
    }

    @Override
    public void onDeviceRemoved(int deviceId, boolean success) {
        hideProgress();
        if (success) {
            refrashGridView();
        } else {
            confirmRemoveLocal(deviceId);
        }
    }


    /**
     * 方法名称：刷新GridView数据
     * 说明：  对已删除的设备列表进行数据刷新
     * 创建者：  聪  11437
     * 修改者：  强  11468
     * 备 注：   GridView改为ViewPager
     */
    private void refrashGridView() {
        Log.d("DEVICE_LIST_POSITION", "DEVICE_LIST_POSITION =  " + DEVICE_LIST_POSITION);
        DEVICE_LIST_POSITION = deviceVP.getCurrentItem();
        //强制重绘新页面
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("").
                replace(R.id.listcontainer, new DeviceListFragment(),"devices").commit();
    }

    /**
     * Sort items in the ListView alphabetically.
     */
  /*  private void sortItems() {
        Collections.sort(dat_list, new DevicesComparator());
    }*/

    private class GroupList {

        public int deviceId;
        public ArrayList<Integer> groupMembership = new ArrayList<Integer>();

        public GroupList(int deviceId) {
            this.deviceId = deviceId;
        }

    }

    /**
     * Send the group update for the next device in the modified group if there is one.
     * This method will only send one group update, and will be called again when that group update completes.
     *
     * @return True if an update was sent, false if there were no more updates to send.
     */
    private boolean updateNextDeviceInGroup() {
        boolean sentUpdate = false;
        if (mState == STATE_EDIT_GROUP) {
            if (mModifiedGroups.size() > 0) {
                // Loop until a group update is sent.
                do {
                    mEditGroupList = mModifiedGroups.valueAt(0);
                    CheckedListItem device = mDeviceFragment.getItem(mEditGroupList.deviceId);
                    if (device == null) {
                        break;
                    }
                    mSelectedDevice = device.getDevice();
                    mModifiedGroups.removeAt(0);
                    sentUpdate = sendGroupUpdate();
                } while (!sentUpdate && mModifiedGroups.size() > 0);
            }
        }
        return sentUpdate;
    }

    /**
     * Send a group update to the remote device. The device with the edited groups is contained in mEditDevice.
     *
     * @return True if the update was sent, false if there were no changes to send.
     */
    private boolean sendGroupUpdate() {
        List<Integer> existingGroupMembership =
                ((SingleDevice) mDeviceFragment.getItem(mEditGroupList.deviceId).getDevice()).getGroupMembership();
        if (existingGroupMembership.size() == mEditGroupList.groupMembership.size()) {
            // If the lists are the same size then calculate the intersection of the set
            // of existing groups for the device and the set of groups we want to change it to.
            // If the intersection is an empty set then there is nothing to do.

            // Number of devices that are in both lists.
            int matching = 0;
            for (int deviceId : existingGroupMembership) {
                if (mEditGroupList.groupMembership.contains(deviceId)) {
                    matching++;
                }
            }
            if (matching == mEditGroupList.groupMembership.size()) {
                return false;
            }
        }
        // If there are devices that weren't in both lists then perform the update.
        showProgress(getActivity().getString(R.string.group_update));
        mController.setSelectedDeviceId(mEditGroupList.deviceId);
        mController.setDeviceGroups(mEditGroupList.groupMembership, (GroupListener) mMainFragment);
        return true;
    }

    /**
     * Process the data in the application level device info message sent over data stream model.
     *
     * @param data The data received from the remote device. Should be a CSR_DEVICE_INFO_RSP message.
     * @return The message contained in the CSR_DEVICE_INFO_RSP message.
     */
    private String getDeviceInfoString(byte[] data) {
        // Default message is failure.
        String message = "Failed to get data";
        if (data != null && data.length > DEVICE_INFO_HEADER_LENGTH) {
            int length = data[DEVICE_INFO_OFFSET_LENGTH];
            if (length > 0 && data.length >= DEVICE_INFO_HEADER_LENGTH + length) {
                message = new String(data, DEVICE_INFO_OFFSET_DATA, length);
            }
        }
        return message;
    }

    private void resetDeviceList() {
        // clear items
//        mGroupFragment.clearItems();
        mDeviceFragment.clearItems();

        // add items
//        mGroupFragment.addItems(mController.getGroups());
        mDeviceFragment.addItems(mController.getDevices());
        if (mDeviceFragment.getDevices().size() == 0) {
            mDeviceFragment.setClickEnabled(false);
        }
    }

    /**
     * Reset user interface to initial state and set state variable to indicate groups are not being edited.
     */
    private void resetUI() {
        mDeviceFragment.setContextMenuEnabled(true);
        mDeviceFragment.setCheckBoxesVisible(false);
        mDeviceFragment.selectNone();
        mDeviceFragment.clearSelection();
        disableDeviceAttention();
        mState = STATE_NORMAL;
    }


    private void disableDeviceAttention() {
        // Send message to clear attention state
        if (mAttentionId > 0) {
            mController.setSelectedDeviceId(mAttentionId);
            mController.setAttentionEnabled(false);
            mAttentionId = 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resetDeviceList();

    }

    @Override
    public void onStop() {
        super.onStop();
        disableDeviceAttention();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}
