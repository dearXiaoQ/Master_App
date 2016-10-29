
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.device;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;

import com.csr.masterapp.adapter.DeviceAdapter;
import com.csr.masterapp.DeviceController;
import com.csr.masterapp.entities.Device;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.masterapp.interfaces.TemperatureListener;
import com.csr.mesh.SensorModelApi;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Fragment to show temperature control.
 *
 */
public class EcookpanFragment extends Fragment implements TemperatureListener{

    private final static String TAG = "EcookpanFragment";

    // UI elements
    private Spinner mDeviceSpinner;

    // Controllers
    private DeviceAdapter mDeviceListAdapter;
    private DeviceController mController;

    // Fragment variables
    DecimalFormat mDecimalFactor = new DecimalFormat("0.0");

    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(com.csr.masterapp.R.layout.ecookpan_fragment, container, false);

        rootView.findViewById(com.csr.masterapp.R.id.header_tv_title).setVisibility(View.GONE);
        rootView.findViewById(com.csr.masterapp.R.id.header_btn_ok).setVisibility(View.GONE);
        rootView.findViewById(com.csr.masterapp.R.id.header_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        mDeviceSpinner = (Spinner)rootView.findViewById(com.csr.masterapp.R.id.spinnerSensorGroup);
        mWebView = (WebView) rootView.findViewById(com.csr.masterapp.R.id.webview);

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
        if (mDeviceListAdapter == null) {
            mDeviceListAdapter = new DeviceAdapter(getActivity());
            mDeviceSpinner.setAdapter(mDeviceListAdapter);
            mDeviceSpinner.setOnItemSelectedListener(deviceSelect);
        }
        // enable continue scanning
        mController.setContinuousScanning(true);

        //webview
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.addJavascriptInterface(new JavaScriptInterface(getActivity()), "PanObject");
        mWebView.loadUrl("file:///android_asset/index-pan.html");
    }

//    private class JavaScriptInterface {
//        private Context mContext;
//        public JavaScriptInterface(Context context) {
//            mContext = context;
//        }
//
//        @JavascriptInterface
//        public String getTempData(){
//            TemperatureStatus temperatureStatus = mController.getTemperatureStatus();
//            if (temperatureStatus != null) {
//                if (temperatureStatus.getCurrentTemperature() != Double.MIN_VALUE) {
//                    Log.d(TAG, "getTempData: 接收到的温度数据是" + mDecimalFactor.format(temperatureStatus.getCurrentTemperature()) + "");
//                    return mDecimalFactor.format(temperatureStatus.getCurrentTemperature()) + "";
//                }
//            }
//            return null;
//        }
//    }

    private void setNewValue(double value, boolean request) {
        if (request) {
            mController.setDesiredTemperature((float) value);
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
        mDeviceListAdapter.clear();
    }

    @Override
    public void onResume() {
        super.onResume();

        List<Device> devices = mController.getDevicesByShortName("Ecookpan");
        for (Device dev : devices) {
            SingleDevice singleDevice = (SingleDevice)dev;
            mDeviceListAdapter.addDevice(singleDevice);
        }
        selectSpinnerDevice();
    }

    /**
     * Called when a new device is selected from the spinner.
     */
    private OnItemSelectedListener deviceSelect = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            deviceSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /**
     * Event handler for when a new device is selected from the Spinner.
     *
     * @param position
     *            Position within Spinner of selected device.
     */
    protected void deviceSelected(int position) {

        setNewValue(0xff00,true);//发送指令获取初始配置信息

        int deviceId = mDeviceListAdapter.getItemDeviceId(position);

        mController.setSelectedDeviceId(deviceId);
        mController.requestCurrentTemperature();
    }

    /**
     * Get the selected device id and set the spinner to it.
     */
    protected void selectSpinnerDevice() {

        int selectedDeviceId = mController.getSelectedDeviceId();
        if (selectedDeviceId != Device.DEVICE_ID_UNKNOWN) {
            Device dev = mController.getDevice(selectedDeviceId);
            if (dev instanceof SingleDevice &&  ((SingleDevice)dev).isModelSupported(SensorModelApi.MODEL_NUMBER)) {

                int position = 0;
                try {
                    position = mDeviceListAdapter.getDevicePosition(selectedDeviceId);
                } catch (Exception e){

                }
                mDeviceSpinner.setSelection(position, true);
            }
            else {
                if (mDeviceListAdapter.getCount() > 0) {
                    mDeviceSpinner.setSelection(0);
                }
            }
        }
        else {
            // No active device, so select the first device in the spinner if there is one.
            if (mDeviceListAdapter.getCount() > 0) {
                mDeviceSpinner.setSelection(0);
            }
        }

        setNewValue(0xff00,true);//发送指令获取初始配置信息
    }

    @Override
    public void confirmDesiredTemperature() {

    }

    @Override
    public void setCurrentTemperature(double celsius) {
        mWebView.loadUrl("javascript:getTempData(" + celsius + ")");
        Log.d(TAG, "setCurrentTemperature:--------------- " + celsius);
    }

    @Override
    public void setDesiredTemperature(double celsius) {

    }

    @Override
    public void setCurrentStatus(String data) {
    }

    @Override
    public void setDesiredData(byte[] data) {

    }

    @Override
    public void setIndData(byte[] data) {

    }

}
