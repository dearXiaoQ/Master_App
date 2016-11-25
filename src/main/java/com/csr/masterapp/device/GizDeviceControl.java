package com.csr.masterapp.device;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.csr.masterapp.R;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;

import java.util.concurrent.ConcurrentHashMap;

public class GizDeviceControl extends Activity implements View.OnClickListener{

    /** 发送指令按钮 */
    private Button sendDataBtn;
    /** Handler */
    private Handler mHandelr;
    /** 传送过来的device */
    GizWifiDevice mDevice = null;
    /** 发送控制指令 */
    private static final int SEND_DATA_TO_DEVICE = 10;

    /** 机智云回调 */
    GizWifiDeviceListener mListener = new GizWifiDeviceListener(){
        /**  */
        @Override
        public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 如果App不使用sn，此处不需要判断sn
                Log.i("Gizdevice", "操作成功 result = " + result + "  " + dataMap.toString()) ;

            } else {
                Log.i("Gizdevice", "操作失败 result = " + result) ;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giz_device_control);

        /** 初始控件 */
        initView();

        /** setHandler */
        setHandler();

        /** 初始化数据 */
        setData();

    }

    private void setData() {
        Intent intent = this.getIntent();
        mDevice =  intent.getParcelableExtra("device");
        Log.i("GizDevice", "mDevice = " + mDevice + toString());
        mDevice.setListener(mListener);
    }

    /** setHandler */
    private void setHandler() {
        mHandelr = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case SEND_DATA_TO_DEVICE:
                        Log.i("Gizdevice", "已经发送指令");
                        // 订阅设备并变为可控状态后，执行开灯动作
                        int sn = 0; // 如果App不使用sn，此处可写成 int sn = 0;
                        ConcurrentHashMap<String, Object> command = new ConcurrentHashMap<String, Object> ();
                        command.put("PowerCtrl", true);
                        mDevice.write(command, 0);
                        break;
                }
            }
        };
    }

    private void initView() {
        sendDataBtn = (Button) findViewById(R.id.sendDataBtn);
        sendDataBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendDataBtn:
                mHandelr.sendEmptyMessage(SEND_DATA_TO_DEVICE);
                break;
        }
    }
}
