package com.csr.masterapp.fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.csr.masterapp.BaseFragment;
import com.csr.masterapp.R;
import com.csr.masterapp.device.GizConnDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiConfigureMode;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.enumration.GizWifiGAgentType;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mars on 2016/11/12.
 * 机智云
 */
public class SecondPage extends BaseFragment{

    private GizConnDevice gizConnDevice;

    GizWifiSDKListener mListener = new GizWifiSDKListener() {
        //等待配置完成或超时，回调配置完成接口

        @Override
        public void didSetDeviceOnboarding(GizWifiErrorCode result, String mac, String did, String productKey) {
            if(result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                //配置成功
                Log.i("AirLink", "配置成功" + "mac = " + mac  + " did = " + did + " productKey = " + productKey);
            } else {
                //配置失败
                Log.i("AirLink", "配置失败" + result);
            }
        }
    };

    /**  */


    @Override
    protected View initView() {
        View view = LayoutInflater.from(mActivity).inflate(R.layout.second_page, null);
        gizConnDevice = (GizConnDevice) mActivity;
        //对接机智云
        connDevice();
        return view;
    }

    private void connDevice() {
        GizWifiSDK.sharedInstance().setListener(mListener);
        List<GizWifiGAgentType> types = new ArrayList<>();
        types.add(gizConnDevice.getDeviceType());
        GizWifiSDK.sharedInstance().setDeviceOnboarding(gizConnDevice.getWifiNmae(),
                gizConnDevice.getWifiPwd(), GizWifiConfigureMode.GizWifiAirLink, null, 60 ,types);
        /**
         GizGAgentMXCHIP,   //庆科
         GizGAgentHF,       //汉枫
         GizGAgentRTK,      //
         GizGAgentWM,       //
         GizGAgentESP,      //乐鑫
         GizGAgentQCA,      //高通
         GizGAgentTI,       //TI
         GizGAgentFSK,      //
         GizGAgentMXCHIP3,  //庆科V3
         GizGAgentBL,       //
         GizGAgentAtmelEE,  //AtmeLee
         GizGAgentOther;    //其他
         */
    }
}
