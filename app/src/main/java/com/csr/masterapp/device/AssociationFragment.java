
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.masterapp.DeviceController;
import com.csr.masterapp.MainActivity;
import com.csr.masterapp.R;
import com.csr.masterapp.WelcomeUI;
import com.csr.masterapp.database.DataBaseDataSource;
import com.csr.masterapp.fragment.MenuFragment;
import com.csr.masterapp.interfaces.AssociationListener;
import com.csr.masterapp.interfaces.AssociationStartedListener;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
/**
 * Fragment used to discover and associate devices. Devices can be associated by tapping them or by pressing the QR code
 * button and scanning a QR code that will provide the UUID and authorisation code. If authorisation has been enabled on
 * the security settings screen then when a UUID is tapped the short code is prompted for.
 *
 */
public class AssociationFragment extends Fragment implements AssociationListener, AssociationStartedListener, OnItemClickListener {
    private String TAG = "Association Fragment";
    private Context mContext = getActivity();

    static String TAG_PROGRESS = "AssociationProgress";

    private Resources mResources;

    private AlertDialog.Builder builder;

    private static final int MAX_SHORT_CODE_LENGTH = 24;
    private DeviceController mController;

    private Handler mHandler;
    private ArrayList<ScanInfo> mNewDevices = new ArrayList<ScanInfo>();
    private HashMap<Integer, Appearance> mAppearances = new HashMap<Integer, Appearance>();

    // Time to wait until check if the info of the devices is valid.
    // This controls how quickly devices are removed from the list if no longer seen.
    private static final int CHECKING_SCAN_INFO_TIME_MS = (15 * 1000);

    private static final int MAX_TTL = 128 - 1;

    private UuidResultsAdapter resultsAdapter;
    private ProgressDialog mProgress = null;

    private CheckBox mSortCheckBox;

    private Button mBtnReScan = null;
    //    private AssociationFragment mFragment;
    private View mProgressBar;
    private ScanInfo info;
    private DataBaseDataSource mDataBase;
    private int[] imgId = {R.drawable.ico_ecookpan, R.drawable.ico_rhood,
            R.drawable.ico_lamp, R.drawable.ico_pirsensor,
            R.drawable.ico_lsensor, R.drawable.ico_ssensor,};
    private ArrayList<ScanInfo> mTempDevices = new ArrayList<ScanInfo>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCheckingScanInfo();
        mResources = getActivity().getResources();
        mDataBase = new DataBaseDataSource(getActivity());

    }

    /**
     * Start checking if the list of devices we are displaying contains a valid info or should be removed from the list.
     */
    private void startCheckingScanInfo() {
        mController.postRunnable(checkScanInfoRunnable);
    }

    /**
     * Stop checking if the list of devices we are displaying contains a valid info or should be removed from the list.
     */
    private void stopCheckingScanInfo() {
        mController.removeRunnable(checkScanInfoRunnable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        mHandler = ((MainActivity) getActivity()).getMeshHandler();
        View view = inflater.inflate(R.layout.association_fragment, container, false);
       ((TextView) view.findViewById(R.id.header_tv_title)).setText(R.string.select_device_Model);
        view.findViewById(R.id.header_btn_ok).setVisibility(View.GONE);
        //返回键
        view.findViewById(R.id.header_iv_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        mProgressBar = view.findViewById(R.id.association_progressBar);
        resultsAdapter = new UuidResultsAdapter(getActivity(), mNewDevices);

        ListView listView = (ListView) view.findViewById(R.id.device_list);
        listView.setAdapter(resultsAdapter);
        //长按
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int hash = mNewDevices.get(position).uuidHash;
                mController.activateAttentionMode(hash, true);
                return true;
            }
        });
        //短按
        listView.setOnItemClickListener(this);


        mSortCheckBox = (CheckBox) view.findViewById(R.id.sort_check);
        mSortCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSortCheckBox.isChecked()) {
                    resultsAdapter.notifyDataSetChanged();
                }
            }
        });


        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        /**
         * 搜索设备列表点击事件，xUtils框架进行HTTP通信：POST方式与服务器通信
         */
        //用户鉴权码输入框图
        builder = new AlertDialog.Builder(getActivity()).setTitle(R.string.please_input_device_password);
        final EditText code = new EditText(getActivity());
        code.setKeyListener(new DigitsKeyListener(false, true));
        builder.setIcon(android.R.drawable.ic_dialog_info).setView(code);

        info = mNewDevices.get(position);
        info.uuid = mNewDevices.get(position).uuid;

        builder.setPositiveButton(getActivity().getResources().getString(R.string.submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                showProgress(mResources.getString(R.string.authentication_password),ProgressDialog.STYLE_SPINNER);
                if(MenuFragment.NETWORK_ONLINE) {   //在线状态，通过网络雁阵个
                    String masterAppId = CacheUtils.getString(getActivity(), WelcomeUI.MASTER_APP_ID);
                    //获取鉴权码
                    final HttpUtils utils = new HttpUtils();
                    final RequestParams params = new RequestParams();
                    Log.d(TAG, "uuid : " + info.uuid);
                    Log.d(TAG, "masterAppId : " + masterAppId);
                    params.addBodyParameter("uuid", info.uuid);
                    params.addBodyParameter("deviceId", "");
                    params.addBodyParameter("masterAppId", masterAppId);
                    mController.setUuid(info.uuid);

                    utils.configCurrentHttpCacheExpiry(1000 * 5);
                    utils.send(HttpRequest.HttpMethod.POST, Constans.DEVICE_BASE_INFO, params, new RequestCallBack<String>() {
                        @Override
                        public void onSuccess(ResponseInfo<String> responseInfo) {
                            Log.d(TAG, "Json return : " + responseInfo.result);
                            int errorCode = Integer.parseInt(Utils.ParseJSON(responseInfo.result, "errorCode"));
                            Log.d(TAG, "errorCode : " + errorCode);
                            if (errorCode == 0) {
                                String res = Utils.ParseJSON(responseInfo.result, "res");
                                String checknum = Utils.ParseJSON(res, "check");
                                String num = code.getText().toString().trim();
                                if (checknum != null && num.equals(checknum)) {
                                    Toast.makeText(getActivity(), mResources.getString(R.string.authentication_success), Toast.LENGTH_SHORT).show();
                                    //鉴权成功，添加设备到主界面
                                    Appearance shortName = mNewDevices.get(position).getAppearance();
                                    if (shortName != null) {
                                        int hash = mNewDevices.get(position).uuidHash;
                                        if (mController.isAuthRequired()) {
                                            //认证
                                            associateShortCode(hash, position);
                                        } else {
                                            //无需认证
                                            mController.associateDevice(hash, null);
                                            mNewDevices.remove(position);
                                            resultsAdapter.notifyDataSetChanged();
                                        }
                                    }

                                } else {
                                    Toast.makeText(getActivity(), mResources.getString(R.string.password_error), Toast.LENGTH_SHORT).show();
                                }
                                hideProgress();
                            } else {
                                Toast.makeText(getActivity(), mResources.getString(R.string.get_data_error), Toast.LENGTH_SHORT).show();
                                hideProgress();

                            }
                        }

                        @Override
                        public void onFailure(HttpException error, String msg) {
                            Toast.makeText(getActivity(), mResources.getString(R.string.network_connection_failed), Toast.LENGTH_SHORT).show();
                            hideProgress();
                        }
                    });
                }else { //离线状态

                    if(code.getText().toString().trim().equals("1111")) {
                        Toast.makeText(getActivity(), mResources.getString(R.string.authentication_success), Toast.LENGTH_SHORT).show();
                        //添加设备到主界面
                        Appearance shortName = mNewDevices.get(position).getAppearance();
                        Log.i("shortName", "离线模式  shortName = " + shortName);
                        //鉴权成功，添加设备到主界面
                        if (shortName != null) {
                            int hash = mNewDevices.get(position).uuidHash;
                           /* if (mController.isAuthRequired()) {
                                //认证
                                associateShortCode(hash, position);
                            } else {*/
                                //无需认证
                                mController.associateDevice(hash, null);
                                mNewDevices.remove(position);
                                resultsAdapter.notifyDataSetChanged();
                    //        }
                        }
                    } else {
                        Toast.makeText(getActivity(), mResources.getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                    }
                    //隐藏进度条
                    hideProgress();
                } }
        });
        builder.create().show();
    }


    @Override
    public void onResume() {
        super.onResume();
        mController.initAssociaction(true, this);
        startCheckingScanInfo();
        hideProgress();
    }

    @Override
    public void onStop() {
        super.onStop();
        mController.initAssociaction(false, null);
        mProgressBar.setVisibility(View.GONE);
        stopCheckingScanInfo();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mController = (DeviceController) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceController callback interface.");
        }
    }

    @Override
    public void newUuid(UUID uuid, int uuidHash, int rssi, int ttl) {
        boolean existing = false;
        for (ScanInfo info : mNewDevices) {
            if (info.uuid.equalsIgnoreCase(uuid.toString())) {
                info.rssi = rssi;
                info.ttl = ttl;
                // check if we already have appearance info according with the uuidHash
                if (mAppearances.containsKey(uuidHash)) {
                    info.setAppearance(mAppearances.get(uuidHash));
                }
                info.updated();
                resultsAdapter.notifyDataSetChanged();

                existing = true;
                break;
            }
        }
        if (!existing) {
            ScanInfo info = new ScanInfo(uuid.toString().toUpperCase(), rssi, uuidHash, ttl);
            // check if we already have appearance info according with the uuidHash
            if (mAppearances.containsKey(uuidHash)) {
                info.setAppearance(mAppearances.get(uuidHash));
            }
//            mNewDevices.add(info);
            mTempDevices.add(info);
            resultsAdapter.notifyDataSetChanged();

        }
    }

    @Override
    public void newAppearance(int uuidHash, byte[] appearance, String shortName) {

        for (ScanInfo info : mTempDevices) {
            if (info.uuidHash == uuidHash && info.appearance == null) {
                info.setAppearance(new Appearance(appearance, shortName));
                info.updated();

                mNewDevices.add(info);
                resultsAdapter.notifyDataSetChanged();
                break;
            }
        }

        mAppearances.put(uuidHash, new Appearance(appearance, shortName));

    }

    /**
     * Show modal progress dialogue whilst associating a device.
     * 搜索进度会话框
     *
     * @param str   显示的进度条字符串
     * @param style 显示进度条的风格   ProgressDialog.STYLE_SPINNER or ProgressDialog.STYLE_HORIZONTAL
     */
    private void showProgress(String str, int style) {
        //getActivity().setProgressBarIndeterminateVisibility(false);
        if (mProgress == null) {
            mProgress = new ProgressDialog(getActivity());
        }
        mProgress.setMessage(str);
        mProgress.setProgressStyle(style);
        mProgress.setIndeterminate(true);  //搜索完成
        mProgress.setCancelable(true);    //设置为false，按返回键不能退出。默认为true。
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

    }

    /**
     * Hide the progress dialogue when association is finished.
     */
    private void hideProgress() {
        if (mProgress != null) {
            mProgress.dismiss();
            mProgress = null;
        }
        mProgressBar.setVisibility(View.INVISIBLE);
        //getActivity().setProgressBarIndeterminateVisibility(true);
    }


    /**
     * Associate a device after first prompting for a short code.
     *
     * @param hash     The 31-bit UUID hash of the device to associate.
     * @param position Position of device in ListView.
     */
    private void associateShortCode(final int hash, final int position) {
        final AlertDialog.Builder shortCodeDialog = new AlertDialog.Builder(getActivity());
        shortCodeDialog.setTitle(getActivity().getString(R.string.short_code_prompt));
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_SHORT_CODE_LENGTH)});
        shortCodeDialog.setView(input);

        shortCodeDialog.setPositiveButton(mResources.getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (mController.associateDevice(hash & 0x7FFFFFFF, input.getText().toString())) {
                        mNewDevices.remove(position);
                        resultsAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(getActivity(), getActivity().getString(R.string.short_code_match_fail), Toast.LENGTH_LONG).show();
                    }
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.shortcode_invalid), Toast.LENGTH_LONG).show();
                }
            }
        });
        shortCodeDialog.setNegativeButton(mResources.getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog dialog = shortCodeDialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private boolean deletingHyphen;
            private int hyphenStart;
            private boolean deletingBackward;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing.
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting)
                    return;

                // Make sure user is deleting one char, without a selection
                final int selStart = Selection.getSelectionStart(s);
                final int selEnd = Selection.getSelectionEnd(s);
                if (s.length() > 1 // Can delete another character
                        && count == 1 // Deleting only one character
                        && after == 0 // Deleting
                        && s.charAt(start) == '-' // a hyphen
                        && selStart == selEnd) { // no selection
                    deletingHyphen = true;
                    hyphenStart = start;
                    // Check if the user is deleting forward or backward
                    if (selStart == start + 1) {
                        deletingBackward = true;
                    } else {
                        deletingBackward = false;
                    }
                } else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void afterTextChanged(Editable text) {
                if (isFormatting)
                    return;

                isFormatting = true;

                // If deleting hyphen, also delete character before or after it
                if (deletingHyphen && hyphenStart > 0) {
                    if (deletingBackward) {
                        if (hyphenStart - 1 < text.length()) {
                            text.delete(hyphenStart - 1, hyphenStart);
                        }
                    } else if (hyphenStart < text.length()) {
                        text.delete(hyphenStart, hyphenStart + 1);
                    }
                }
                if ((text.length() + 1) % 5 == 0) {
                    text.append("-");
                }

                isFormatting = false;

                if (text.length() < MAX_SHORT_CODE_LENGTH) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

    }

    @Override
    public void deviceAssociated(boolean success, String message) {
        hideProgress();
        // notify the user.
        if (message != null)
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void associationStarted() {
        // Association was triggered by MainActivity, so display progress dialogue.
        showProgress(mResources.getString(R.string.associating), ProgressDialog.STYLE_SPINNER);
    }

    private class UuidResultsAdapter extends BaseAdapter {
        private Activity activity;
        private ArrayList<ScanInfo> data;

        private LayoutInflater inflater = null;

        public UuidResultsAdapter(Activity a, ArrayList<ScanInfo> object) {
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


        //重写getview方法，提高代码读入效率
        @SuppressLint({"SetTextI18n", "InflateParams"})
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TextView nameText;
            final TextView rssiText;
            final TextView appearanceText;
            final Button mReScan = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.uuid_list_row, null);
                nameText = (TextView) convertView.findViewById(R.id.ass_uuid);
                rssiText = (TextView) convertView.findViewById(R.id.ass_rssi);
                appearanceText = (TextView) convertView.findViewById(R.id.apperance_text);
                convertView.setTag(new ViewHolder(nameText, rssiText, appearanceText, mReScan));
            } else {
                ViewHolder viewHolder = (ViewHolder) convertView.getTag();
                nameText = viewHolder.uuid;
                rssiText = viewHolder.rssi;
                appearanceText = viewHolder.appearanceText;
            }

            //信号强度、距离处理
            ScanInfo info = data.get(position);
            nameText.setText(info.uuid);


            int hops = MAX_TTL - info.ttl;
            if (hops == 0) {
                rssiText.setText(String.valueOf(info.rssi) + "dBm");
            } else if (hops == 1) {
                rssiText.setText(String.valueOf(info.rssi) + "dBm " + hops + " hop");
            } else {
                rssiText.setText(String.valueOf(info.rssi) + "dBm " + hops + " hops");
            }

//            appearanceText.setText(info.getAppearance() != null ? info.getAppearance().mShortName : "1");
//            if (info.getAppearance() != null && info.getAppearance().mShortName != null) {
//                appearanceText.setText(info.getAppearance().mShortName);
//            }

            if (info.getAppearance() != null) {
                appearanceText.setText(info.getAppearance().mShortName);
            }

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            // Sort in ascending order by hops, then RSSI.
            if (mSortCheckBox.isChecked()) {
                Collections.sort(data);
            }

            super.notifyDataSetChanged();

        }


    }

    private class Appearance {
        private byte[] mAppearanceCode;
        private String mShortName;

        public Appearance(byte[] appearanceCode, String shortName) {
            setAppearanceCode(appearanceCode);
            setShortName(shortName);
        }

        public String getShortName() {
            return mShortName;
        }

        public void setShortName(String mShortName) {
            this.mShortName = mShortName;
        }

        public byte[] getAppearanceCode() {
            return mAppearanceCode;
        }

        public void setAppearanceCode(byte[] mAppearanceCode) {
            this.mAppearanceCode = mAppearanceCode;
        }
    }

    private class ScanInfo implements Comparable<ScanInfo> {

        private static final long TIME_SCAN_INFO_VALID_MS = 15 * 1000;

        public String uuid;
        public int rssi;
        public int uuidHash;
        public long timeStamp;
        public int ttl;
        public Appearance appearance;

        public ScanInfo(String uuid, int rssi, int uuidHash, int ttl) {
            this.uuid = uuid;
            this.rssi = rssi;
            this.uuidHash = uuidHash;
            this.ttl = ttl;
            updated();
        }

        public void updated() {
            this.timeStamp = System.currentTimeMillis();
        }

        @Override
        public int compareTo(ScanInfo info) {
            final int LESS_THAN = -1;
            final int GREATER_THAN = 1;
            final int EQUAL = 0;

            // Compare to is used for sorting the list in ascending order.
            // Smaller number of hops (highest TTL) should be at the top of the list.
            // For items with the same TTL, largest signal strength (highest RSSI) should be at the top of the list.
            if (this.ttl > info.ttl) {
                return LESS_THAN;
            } else if (this.ttl < info.ttl) {
                return GREATER_THAN;
            } else if (this.rssi > info.rssi) {
                return LESS_THAN;
            } else if (this.rssi < info.rssi) {
                return GREATER_THAN;
            } else {
                return EQUAL;
            }
        }

        public Appearance getAppearance() {
            return appearance;
        }


        /**
         * This method check if the timeStamp of the last update is still valid or not (time<TIME_SCANINFO_VALID).
         *
         * @return true if the info is still valid
         */
        public boolean isInfoValid() {
            return ((System.currentTimeMillis() - this.timeStamp) < TIME_SCAN_INFO_VALID_MS);
        }

        public void setAppearance(Appearance newAppearance) {
            appearance = newAppearance;
        }
    }

    private static class ViewHolder {
        public final TextView uuid;
        public final TextView rssi;
        public final TextView appearanceText;

        public final Button mReScan;

        public ViewHolder(TextView uuid, TextView rssi, TextView appearanceText, Button mReScan) {
            this.uuid = uuid;
            this.rssi = rssi;
            this.appearanceText = appearanceText;
            this.mReScan = mReScan;
        }
    }

    @Override
    public void associationProgress(final int progress, final String message) {

        // different cases that we should avoid to set the progress
        if (progress < 0 || progress > 100 || mProgress == null || !mProgress.isShowing()) {
            return;
        }
        Log.d(TAG_PROGRESS, message);
        // run in the UI thread
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mProgress.setProgress(progress);
                // we don't change the message anymore
                //mProgress.setMessage(message);
            }
        });
    }

    private Runnable checkScanInfoRunnable = new Runnable() {

        @Override
        public void run() {
            Iterator<ScanInfo> it = mNewDevices.iterator();
            ScanInfo info = null;
            while (it.hasNext()) {
                info = it.next();
                if (!info.isInfoValid()) {
                    it.remove();
                }
            }

            hideProgress();
            mBtnReScan = (Button) getView().findViewById(R.id.btn_rescan);
            mBtnReScan.setText(R.string.scan_device);
            mBtnReScan.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ReScanDevice(true);
                }
            });


        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        ReScanDevice(false);
        mController.removeRunnable(checkScanInfoRunnable);
    }

    /**
     * 名  称：搜寻设备
     * 类  别：新添加方法
     * 说  明：每当搜寻不到的设备，点击此按钮触发重新搜寻设备的方法
     * 创建者：聪  11437
     */
    public void ReScanDevice(boolean enabled) {
        //PrograssBar设置为可视
        mProgressBar.setVisibility(View.VISIBLE);
        //设置handler延时搜索，CHECKING_SCAN_INFO_TIME_MS为线程延时时间（单位：ms）
        mHandler.postDelayed(checkScanInfoRunnable, CHECKING_SCAN_INFO_TIME_MS);
        //清除列表上搜索出来的设备
        mNewDevices.clear();
        mAppearances.clear();
        //调用蓝牙csrMesh服务，搜索设备
        mController.discoverDevices(enabled);
        //刷新列表
        resultsAdapter.notifyDataSetChanged();

        mBtnReScan.setText(mResources.getString(R.string.scanning));

    }
}
