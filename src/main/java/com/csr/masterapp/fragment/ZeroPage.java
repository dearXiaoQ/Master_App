package com.csr.masterapp.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.masterapp.BaseFragment;
import com.csr.masterapp.R;
import com.csr.masterapp.adapter.PopListAdapter;
import com.csr.masterapp.adapter.WifiListAdapter;
import com.csr.masterapp.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mars on 2016/11/9.
 */
public class ZeroPage extends BaseFragment implements View.OnClickListener, ListView.OnItemClickListener{

    private TextView selectType;
    private ImageView wifiListIv;
    private EditText wifiUserEt, wifiPwdEt;
    private ImageView goback;
    private Button nextBtn;

    private final int  POP_DEVICE_LIST = 1;
    private final int  POP_WIFI_LIST   = 0;

    private static int POP_TYPE = 0;

    private Dialog builder = null;

    private View popView = null;
    private TextView popTitleTv;
    private ListView popListView;
    private List<String> modeData;

    private BaseAdapter popListAdater;

    private ArrayList<ScanResult> wifiList;

    /** 设备热点默认密码 */
    public static String SoftAP_PSW = "123456789";

    /** 设备热点默认前缀 */
    public static String SoftAP_Start = "XPG-GAgent";

    @Override
    protected View initView() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.zero_page, null);
        selectType = (TextView) view.findViewById(R.id.deviceTypeName);
        wifiListIv = (ImageView) view.findViewById(R.id.imgWiFiList);
        wifiUserEt = (EditText) view.findViewById(R.id.etSSID);
        wifiPwdEt  = (EditText) view.findViewById(R.id.etPsw);
        nextBtn    = (Button) view.findViewById(R.id.btnNext);
        wifiPwdEt.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        selectType.setOnClickListener(this);
        selectType.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        wifiListIv.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deviceTypeName:
                //弹出选择设备列表
                POP_TYPE = POP_DEVICE_LIST;
                popSelectList();
                Toast.makeText(mActivity, "你点击了选择模组", Toast.LENGTH_SHORT).show();

                break;

            case R.id.imgWiFiList:
                //弹出选择wifi列表
                POP_TYPE = POP_WIFI_LIST;
                popSelectList();
                Toast.makeText(mActivity, "你点击了选择WIFI", Toast.LENGTH_SHORT).show();
                break;

            case R.id.cbLaws:
                //密码可见按钮

                break;

            case R.id.btnNext:
                //下一步按钮
                jump();
                break;

        }
    }

    private void jump() {
        if(selectType.getText().toString().equals(mResources.getString(R.string.select_device_type))) {
            Toast.makeText(mActivity, mResources.getString(R.string.please_select_mode), Toast.LENGTH_SHORT).show();
            return;
        }

        if(wifiUserEt.getText().toString().trim().length() == 0) {
            Toast.makeText(mActivity, mResources.getString(R.string.please_select_wifi), Toast.LENGTH_SHORT).show();
        }

        if(wifiPwdEt.getText().toString().length() == 0) {
            Toast.makeText(mActivity, mResources.getString(R.string.Please_input_wifi_pwd), Toast.LENGTH_SHORT).show();
        }


    }


    private void popSelectList() {
        // if(popView == null) {
        popView     = LayoutInflater.from(mActivity).inflate(R.layout.alert_view, null);
        popTitleTv  = (TextView) popView.findViewById(R.id.titleTv);
        popListView = (ListView) popView.findViewById(R.id.pop_list);
        popListView.setOnItemClickListener(this);
        //初始化popList的数据
        modeData = Arrays.asList(mResources.getStringArray(R.array.mode));
        //   }
        initPopView();
    }

    //每次调用Wifi数据都要刷星
    private void initWifiData() {
        List<ScanResult> rsList = Utils.getCurrentWifiScanResult(mActivity);
        List<String> localList = new ArrayList<String>();
        localList.clear();
        wifiList = new ArrayList<ScanResult>();
        wifiList.clear();
        for (ScanResult sss : rsList) {

            if (sss.SSID.contains(SoftAP_Start)) {
            } else {
                if (localList.toString().contains(sss.SSID)) {
                } else {
                    localList.add(sss.SSID);
                    wifiList.add(sss);
                }
            }
        }

    }

    /**
     * 初始化弹出窗口数据
     */
    private void initPopView() {
        int popTitleStr = 0;
        switch (POP_TYPE) {
            case POP_DEVICE_LIST:
                popTitleStr = R.string.choose_mode_title;
                popListAdater = new PopListAdapter(mActivity, modeData);
                break;
            case POP_WIFI_LIST:
                initWifiData();
                popTitleStr = R.string.choose_wifi_list_title;
                popListAdater = new WifiListAdapter(wifiList, mActivity);
                break;
        }
        popTitleTv.setText(mResources.getString(popTitleStr));
        popListView.setAdapter(popListAdater);
        builder = new AlertDialog.Builder(mActivity).setView(popView).create();
        builder.setCancelable(true);
        builder.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (POP_TYPE) {
            case POP_DEVICE_LIST:
                String appendStr =  mResources.getString(R.string.select_device_type) + modeData.get(position);
                selectType.setText(appendStr);
                break;
            case POP_WIFI_LIST:
                ScanResult wifi = wifiList.get(position);
                wifiUserEt.setText(wifi.SSID);
                break;
        }
        builder.dismiss();
    }
}
