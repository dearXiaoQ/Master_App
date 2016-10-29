
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.Recipe;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.csr.masterapp.DeviceAdapter;
import com.csr.masterapp.DeviceController;
import com.csr.masterapp.MainActivity;
import com.csr.masterapp.R;
import com.csr.masterapp.entities.Device;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.masterapp.interfaces.TemperatureListener;
import com.csr.mesh.SensorModelApi;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Fragment to show temperature control.
 *
 */
public class RecipeControlFragment extends Fragment implements View.OnClickListener , TemperatureListener {

    // UI elements
    private Spinner mDeviceSpinner;
    private LinearLayout spLL;

    // Controllers
    private DeviceAdapter mIndDeviceListAdapter;
    private DeviceAdapter mTemDeviceListAdapter;

    private DeviceController mController;

    // Fragment variables
    DecimalFormat mDecimalFactor = new DecimalFormat("0.0");

    //使用BLE连接电磁炉
    private static boolean USE_BLE = true;

    private Spinner mIndDeviceSpinner;
    private Spinner mTemDeviceSpinner;

    private List<Device> tempDevices;
    private List<Device> indDevices;

    int tempDeviceId;
    int indDeviceId;

    private WebView mWebView;

    private static final String TAG = "RecipeControlFragment";

    private Button debugBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        tempDevices = mController.getDevicesByShortName("Ecookpan");
        indDevices  = mController.getDevicesByShortName("IndCooker");
        View rootView = inflater.inflate(R.layout.recipe_control_fragment, container, false);

        rootView.findViewById(R.id.header_tv_title).setVisibility(View.GONE);
        rootView.findViewById(R.id.header_btn_ok).setVisibility(View.GONE);
        rootView.findViewById(R.id.header_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.RECIPE_IND_SEND_DATA = false;
                getFragmentManager().popBackStack();
            }
        });
        mDeviceSpinner = (Spinner)rootView.findViewById(R.id.spinnerSensorGroup);
        mDeviceSpinner.setVisibility(View.GONE);
        mWebView = (WebView) rootView.findViewById(R.id.webview);

        mIndDeviceSpinner = (Spinner) rootView.findViewById(R.id.sp1);

        mTemDeviceSpinner = (Spinner) rootView.findViewById(R.id.sp2);

        mIndDeviceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                indDeviceId = indDevices.get(position).getDeviceId();
               /* mController.setSelectedDeviceId(IndDeviceId);*/
                Log.i("mDevice", "选择的电磁炉Id是 = " + indDeviceId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTemDeviceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tempDeviceId = tempDevices.get(position).getDeviceId();
                mController.setSelectedDeviceId(tempDeviceId);

                Log.i("mDevice", "你选择的EcookPan ID是 = "+ tempDeviceId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spLL              = (LinearLayout) rootView.findViewById(R.id.spLL);

        spLL.setVisibility(View.VISIBLE);

        rootView.findViewById(R.id.button1).setOnClickListener(this);
        rootView.findViewById(R.id.button2).setOnClickListener(this);
        rootView.findViewById(R.id.button3).setOnClickListener(this);
        rootView.findViewById(R.id.button4).setOnClickListener(this);
        rootView.findViewById(R.id.button5).setOnClickListener(this);
        rootView.findViewById(R.id.button6).setOnClickListener(this);
        rootView.findViewById(R.id.button7).setOnClickListener(this);
        rootView.findViewById(R.id.button8).setOnClickListener(this);


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
                if(rv_btn_group.getVisibility() == View.GONE){
                    rv_btn_group.setVisibility(View.VISIBLE);
                }else{
                    rv_btn_group.setVisibility(View.GONE);
                }
            }
        });

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
        if (mIndDeviceListAdapter == null) {
            // mDeviceListAdapter = new DeviceAdapter(getActivity(), 0);
            mIndDeviceListAdapter = new DeviceAdapter(getActivity());
            mIndDeviceSpinner.setAdapter(mIndDeviceListAdapter);
            // enable continue scanning
            mController.setContinuousScanning(true);
            //
            //
            // mController.setSelectedDeviceId(mDevice.getDeviceId());

            //   mDeviceSpinner.setAdapter(mDeviceListAdapter);
            //   mDeviceSpinner.setOnItemSelectedListener(deviceSelect);
        }

        if(mTemDeviceListAdapter == null) {
            mTemDeviceListAdapter = new DeviceAdapter(getActivity());
            mTemDeviceSpinner.setAdapter(mTemDeviceListAdapter);
        }

        // enable continue scanning
        mController.setContinuousScanning(true);
        mController.setSelectedDeviceId(tempDeviceId);

        //webview
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.addJavascriptInterface(new JavaScriptInterface(getActivity()), "RecipeObject");
        mWebView.loadUrl("file:///android_asset/index-recipesBeef.html");
        //  mWebView.loadUrl("file:///android_asset/index-recipesSoup.html");
    }

    private byte[] data = new byte[3];

    @Override
    public void onClick(View v) {
        switch(v.getId()){
        /*    case R.id.sendpower:
                new JavaScriptInterface(getActivity()).sendPower(800, true);
                break;
            case R.id.receivetemp:
                String tempData = new JavaScriptInterface(getActivity()).getTempData();
                Toast.makeText(getActivity().getBaseContext(),"接收的温度数据是：" + tempData, Toast.LENGTH_SHORT ).show();
                break;*/
            case R.id.button1:
                data[0] = 0x55;
                data[1] = 0x00;
                break;
            case R.id.button2:
                data[0] = (byte) 0xaa;
                data[1] = 0x00;
                break;
            case R.id.button3:
                data[0] = 0x55;
                data[1] = 0x00;
                break;
            case R.id.button4:
                data[0] = 0x55;
                data[1] = 0x01;
                break;
            case R.id.button5:
                data[0] = 0x55;
                data[1] = 0x02;
                break;
            case R.id.button6:
                data[0] = 0x55;
                data[1] = 0x03;
                break;
            case R.id.button7:
                data[0] = 0x55;
                data[1] = 0x04;
                break;
            case R.id.button8:
                data[0] = 0x55;
                data[1] = 0x05;
                break;
        }
        data[2] = 0x00;
        setNewValue(data,true);
    }

    private void setNewValue(byte[] data, boolean request) {
        if (request) {
            MainActivity.RECIPE_IND_SEND_DATA = true;
            mController.setIndDesireData(data, indDeviceId);
            Log.i("sendData", "发送的数据" + bytesToHexString(data));
        }
    }


    @Override
    public void confirmDesiredTemperature() {

    }

    @Override
    public void setCurrentTemperature(double celsius) {

    }

    @Override
    public void setDesiredTemperature(double celsius) {

    }

    @Override
    public void setCurrentStatus(String data) {

    }

    @Override
    public void setDesiredData(byte[] data) {
        Log.i("mDevice", "temp = " + bytesToHexString(data));
        mWebView.loadUrl("javascript:getTempData('"+ bytesToHexString(data) +"')");
    }

    @Override
    public void setIndData(byte[] data) {
        Log.i("mDevice", "Ind = " + bytesToHexString(data));
        mWebView.loadUrl("javascript:getIndData('"+ bytesToHexString(data) +"')");
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

    public class JavaScriptInterface {
        private Context mContext;
        public JavaScriptInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void setIndData(final byte[] data) {
            if(!USE_BLE) {  //此处改为BLE通0信
                final String url = "http://192.168.1.222/hotPotControl/powerControl";
                HttpUtils utils = new HttpUtils();
                RequestParams params = new RequestParams();

                JSONObject obj = new JSONObject();
                try {
                   // obj.put("powerSw", onoff);
                  //  obj.put("power", power);

                    params.setBodyEntity(new StringEntity(obj.toString(), "UTF-8"));
                } catch (Exception e) {
                }

                utils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        //场景 ecookpan -》 开抽烟机
                     /*   if (onoff == true) {
                            mController.setDesiredTemperature(0x000f);
                        } else {
                            mController.setDesiredTemperature(0x0000);
                        }*/

                        Log.d(TAG, "onSuccess: 发送的电磁炉功率是" + data);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {

                    }
                });
            } else {
                //将功率发发送到电磁炉
                /* mController.setDesiredData(data);
                Log.i("发送的数据" + bytesToHexString(data));*/

                //开抽油烟机
                MainActivity.RECIPE_IND_SEND_DATA = true;
                mController.setIndDesireData(data, indDeviceId);
                Log.i("sendData", "发送的数据" + bytesToHexString(data));
            }
        }

        @JavascriptInterface
        public String getTempData(){
            Log.i("mDevice", "调用了  getTempData()");
            byte[] receicveData = mController.getInitData();
            if(receicveData != null){
                short receiveValue = (short) (((receicveData[0] << 8) | receicveData[1] & 0xff));
                Log.d("", "getTempData: " + receiveValue);
                return Short.toString(receiveValue);
            }
            return null;
        }

        @JavascriptInterface
        public String getIndData() {
            Log.i("mDevice", "调用了  getIndData()");
            byte[] receicveData = mController.getIndData(indDeviceId);
            if(receicveData != null){
                short receiveValue = (short) (((receicveData[0] << 8) | receicveData[1] & 0xff));
                Log.d("", "getIndData: " + receiveValue);
                return Short.toString(receiveValue);
            }
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mWebView != null) {
            try {
                mWebView.getSettings().setJavaScriptEnabled(false);
                mWebView.removeAllViews();
                mWebView.destroy();
            } catch (Exception e) {
            }
        }

        // disable continue scanning
        mController.setContinuousScanning(false);
        mController.setTemperatureListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        // mDeviceListAdapter.clear();
        MainActivity.RECIPE_IND_SEND_DATA = false;
        mIndDeviceListAdapter.clear();
        mTemDeviceListAdapter.clear();
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface(getActivity());
        if (mWebView != null) {
            try {
                mWebView.getSettings().setJavaScriptEnabled(false);
                mWebView.removeAllViews();
                mWebView.destroy();
            } catch (Exception e) {

            }
        }
        javaScriptInterface.setIndData(new byte[]{(byte) 0xaa, 0, 0});
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        tempDevices = mController.getDevicesByShortName("Ecookpan");
        indDevices  = mController.getDevicesByShortName("IndCooker");
        Log.i("mDevice", "onResume");
        for (Device dev: tempDevices) {
            Log.i("mDevice", "device = " + dev.toString());
        }
        for (Device dev : tempDevices) {
            mTemDeviceListAdapter.addDevice(dev);
            // mDeviceListAdapter.addDevice(singleDevice);
        }

        for (Device dev : indDevices) {
            mIndDeviceListAdapter.addDevice(dev);
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
        // int deviceId = mDeviceListAdapter.getItemDeviceId(position);

        //    mController.setSelectedDeviceId(deviceId);
        //    mController.requestCurrentTemperature();
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
                    //    position = mDeviceListAdapter.getDevicePosition(selectedDeviceId);
                } catch (Exception e){}
                mDeviceSpinner.setSelection(position, true);
            }
            else {
                //  if (mDeviceListAdapter.getCount() > 0) {
                //        mDeviceSpinner.setSelection(0);
                //    }
            }
        }
        else {
            // No active device, so select the first device in the spinner if there is one.
          /*  if (mDeviceListAdapter.getCount() > 0) {
                mDeviceSpinner.setSelection(0);
            }*/
        }
    }
}
