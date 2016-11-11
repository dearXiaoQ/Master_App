
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.device;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.csr.masterapp.DeviceController;
import com.csr.masterapp.MainActivity;
import com.csr.masterapp.R;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.masterapp.interfaces.TemperatureListener;

import java.util.Locale;


/**
 * Fragment to show device control.
 *
 */
@SuppressLint("ValidFragment")
public class DeviceControlFragment extends Fragment implements TemperatureListener, View.OnClickListener {

    private final static String TAG = "DevicesControlFragment";

    // Controllers
    private DeviceController mController;

    private WebView mWebView;
    private SingleDevice mDevice;
    private byte[] data = new byte[4];
    private TextView logInfo;

    private Button debugBtn;

    public DeviceControlFragment(SingleDevice device) {
        this.mDevice = device;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.device_control_fragment, container, false);

        ((TextView)rootView.findViewById(R.id.header_tv_title)).setText(mDevice.getName());
        rootView.findViewById(R.id.header_btn_ok).setVisibility(View.GONE);
        rootView.findViewById(R.id.header_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        rootView.findViewById(R.id.button1).setOnClickListener(this);
        rootView.findViewById(R.id.button2).setOnClickListener(this);
        rootView.findViewById(R.id.button3).setOnClickListener(this);
        rootView.findViewById(R.id.button4).setOnClickListener(this);
        rootView.findViewById(R.id.button5).setOnClickListener(this);
        rootView.findViewById(R.id.button6).setOnClickListener(this);
        rootView.findViewById(R.id.button7).setOnClickListener(this);
        rootView.findViewById(R.id.button8).setOnClickListener(this);

        logInfo = (TextView) rootView.findViewById(R.id.log_info);
        final View rv_btn_group = rootView.findViewById(R.id.rv_btn_group);

        debugBtn = (Button) rootView.findViewById(R.id.btn_test);

        if(MainActivity.DEBUG_MODLE) {
            debugBtn.setVisibility(View.VISIBLE);
        } else {
            debugBtn.setVisibility(View.GONE);
        }

        debugBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(logInfo.getVisibility() == View.GONE || rv_btn_group.getVisibility() == View.GONE){
                    logInfo.setVisibility(View.VISIBLE);
                    rv_btn_group.setVisibility(View.VISIBLE);
                }else{
                    logInfo.setVisibility(View.GONE);
                    rv_btn_group.setVisibility(View.GONE);
                }
            }
        });

        mWebView = (WebView) rootView.findViewById(R.id.webview);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mController = (DeviceController) activity;
            mController.setTemperatureListener(this);
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceController callback interface.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        // enable continue scanning
        mController.setContinuousScanning(true);
        mController.setSelectedDeviceId(mDevice.getDeviceId());

        //webview
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptInterface(getActivity()), "jsObject");
        mWebView.loadUrl("file:///mnt/sdcard/master/tpl/" + mDevice.getShortName().trim() + "/index.html");

    }

    public void confirmDesiredTemperature() {

    }

    public void setCurrentTemperature(double celsius) {


    }

    public void setDesiredTemperature(double celsius) {

    }

    @Override
    public void setCurrentStatus(String data) {

    }

    @Override
    public void setDesiredData(byte[] data) {
//        Toast.makeText(getActivity(),"接收的数据" + bytesToHexString(data),Toast.LENGTH_SHORT).show();
        mWebView.loadUrl("javascript:getData('"+ bytesToHexString(data) +"')");
    }

    @Override
    public void setIndData(byte[] data) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button1:

                data[0] = 0x01;
                data[1] = -1;
                data[2] = -1;
                data[3] = -1;
                break;
            case R.id.button2:
                data[0] = 0x00;
                data[1] = -1;
                data[2] = -1;
                data[3] = -1;
                break;
            case R.id.button3:
                data[0] = -1;
                data[1] = 0x01;
                data[2] = -1;
                data[3] = -1;
                break;
            case R.id.button4:
                data[0] = -1;
                data[1] = 0x00;
                data[2] = -1;
                data[3] = -1;
                break;
            case R.id.button5:
                data[0] = -1;
                data[1] = -1;
                data[2] = 0x01;
                data[3] = -1;
                break;
            case R.id.button6:
                data[0] = -1;
                data[1] = -1;
                data[2] = 0x00;
                data[3] = -1;
            case R.id.button7:
                data[0] = -1;
                data[1] = -1;
                data[2] = -11;
                data[3] = 0x01;
            case R.id.button8:
                data[0] = -1;
                data[1] = -1;
                data[2] = -1;
                data[3] = 0x00;
                break;
        }
        setNewValue(data,true);
    }

    private class JavaScriptInterface {
        private Context mContext;
        public JavaScriptInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void setData(byte[] data) {
            setNewValue(data,true);
        }

        @JavascriptInterface
        public String getLang(){    //网页语言适配
                return Locale.getDefault().getLanguage();
        }

    }

    private void setNewValue(byte[] data, boolean request) {
        if (request) {
            mController.setDesiredData(data);
            try {
                //  logInfo.setText("发送的数据" + bytesToHexString(data));
                Log.i("sendData", "发送的数据" + bytesToHexString(data));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // disable continue scanning
        mController.setContinuousScanning(false);
        mController.setTemperatureListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
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
            if(i != src.length - 1){
                stringBuilder.append(",");
            }
        }
        return stringBuilder.toString();
    }

    public static boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_BACK) {
            Log.d("GameFragmet事件", "OK");
        }
        return true;
    }

}
