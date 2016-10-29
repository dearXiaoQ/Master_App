/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.device;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;

import com.csr.masterapp.DeviceAdapter;
import com.csr.masterapp.DeviceController;
import com.csr.masterapp.DevicesComparator;
import com.csr.masterapp.LightState;
import com.csr.masterapp.PowState;
import com.csr.masterapp.entities.Device;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.mesh.LightModelApi;
import com.csr.mesh.PowerModelApi.PowerState;
import com.csr.masterapp.DeviceState;

import java.util.Collections;
import java.util.List;

/**
 * 项目名称：MasterApp v3
 * 类描述：灯光控制页
 * 创建人：11177
 * 创建时间：2016/7/5 12:00
 * 修改人：11177
 * 修改时间：2016/7/5 12:00
 * 修改备注：
 */

public class LightControlFragment extends Fragment {
    public static final String TAG = "LightControlFragment";

    private View mRootView;
    private HSVCircle mColorWheel = null;
    private SeekBar mBrightSlider = null;
    private DeviceController mController;
    private DeviceAdapter mDeviceListAdapter;
    private Spinner mDeviceSpinner = null;
    private Switch mPowerSwitch = null;
    private WebView mWebView;

    private int mCurrentColor = Color.rgb(0, 0, 0);

    private boolean mEnableEvents = true;
    private boolean mEnablePowerSwitchEvent = true;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            // The last two arguments ensure LayoutParams are inflated properly.
            mRootView = inflater.inflate(com.csr.masterapp.R.layout.light_control_tab, container, false);

            mRootView.findViewById(com.csr.masterapp.R.id.header_tv_title).setVisibility(View.GONE);
            mRootView.findViewById(com.csr.masterapp.R.id.header_btn_ok).setVisibility(View.GONE);
            mRootView.findViewById(com.csr.masterapp.R.id.header_iv_back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFragmentManager().popBackStack();
                }
            });

            mColorWheel = (HSVCircle) mRootView.findViewById(com.csr.masterapp.R.id.colorWheel);
            mColorWheel.setOnTouchListener(colorWheelTouch);

            mBrightSlider = (SeekBar) mRootView.findViewById(com.csr.masterapp.R.id.seekBrightness);
            mBrightSlider.setOnSeekBarChangeListener(brightChange);

            mDeviceSpinner = (Spinner) mRootView.findViewById(com.csr.masterapp.R.id.spinnerLight);
            
            mPowerSwitch = (Switch) mRootView.findViewById(com.csr.masterapp.R.id.powerSwitch);
            mPowerSwitch.setOnCheckedChangeListener(powerChange);

            mWebView = (WebView) mRootView.findViewById(com.csr.masterapp.R.id.webview);
        }
        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mController = (DeviceController) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceController callback interface.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDeviceListAdapter == null) {
            mDeviceListAdapter = new DeviceAdapter(getActivity());
            mDeviceSpinner.setAdapter(mDeviceListAdapter);
            mDeviceSpinner.setOnItemSelectedListener(deviceSelect);
        }

        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptInterface(getActivity()), "LampObject");
        mWebView.loadUrl("file:///android_asset/index-lamp.html");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mColorWheel != null) {
            mColorWheel.onDestroyView();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mDeviceListAdapter.clear();        
    }

    @Override
    public void onResume() {
        super.onResume();

        loadDevices();

    }

    public class JavaScriptInterface {

        private Context mContext;
        public JavaScriptInterface(Context context) {
            mContext = context;
        }

        @JavascriptInterface
        public void controlLamp(int param) {
            boolean value = false;
            switch (param) {
                case 1://开
                    value = true;
                    break;
                case 2://关
                    value = false;
                    break;
            }
            mController.setLightPower(value ? PowerState.ON : PowerState.OFF);
        }
    }

    private void loadDevices() {
//    	List<Device> groups = mController.getGroups();

//        for (Device dev : groups) {
//        	if (dev.getDeviceId() == 0) {
//        		dev.setName(getString(R.string.all_lights));
//        	}
//            mDeviceListAdapter.addDevice(dev);
//        }
//        Device allDev = groups.get(0);
//        allDev.setName(getString(R.string.all_lights));
//        mDeviceListAdapter.addDevice(allDev);

        // Add individual lights already associated.
        // List<Device> lights = mController.getDevices(LightModelApi.MODEL_NUMBER);
        List<Device> lights = mController.getDevicesByShortName("Light");
        // sort devices list.
        Collections.sort(lights, new DevicesComparator());
        // add devices to adapter.
        for (Device dev : lights) {
            mDeviceListAdapter.addDevice(dev);
        }

        selectSpinnerDevice();
		
	}

	/**
     * Called when a colour is selected on the colour wheel.
     * 色彩
     */
    protected OnTouchListener colorWheelTouch = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {                
                int x = (int) event.getX();
                int y = (int) event.getY();

                HSVCircle circleView = (HSVCircle) v;

                int pixelColor = 0;
                try {
                    pixelColor = circleView.getPixelColorAt(x, y);
                }
                catch (IndexOutOfBoundsException e) {
                    return true;
                }

                // Don't use values from the background of the image (outside the wheel).
                if (Color.alpha(pixelColor) < 0xFF) {
                    return true;
                }

                // Force power button on.
                mEnablePowerSwitchEvent = false;
                mPowerSwitch.setChecked(true);
                // Save the new power state but don't send a message to the device.
                mController.setLocalLightPower(PowerState.ON);
                mEnablePowerSwitchEvent = true;
                
                // The cursor is a small circle drawn over the colour wheel image over the selected colour.
                // Set the cursor position and invalidate the imageView so that it is redrawn.
                mColorWheel.setCursor(x, y);
                mColorWheel.invalidate();

                // Extract the R,G,B from the colour.
                mCurrentColor = Color.rgb(Color.red(pixelColor), Color.green(pixelColor), Color.blue(pixelColor));
                mController.setLightColor(mCurrentColor, mBrightSlider.getProgress());
                return true;
            }
            else {
                return false;
            }
        }
    };

    /**
     * Called when the brightness slider changes position.
     * 亮度调节
     */
    protected OnSeekBarChangeListener brightChange = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // No behaviour.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // No behaviour.
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!mEnableEvents)
                return;
            
            // Force power button on.
            mEnablePowerSwitchEvent = false;
            mPowerSwitch.setChecked(true);
            // Save the new power state but don't send a message to the device.
            mController.setLocalLightPower(PowerState.ON);
            mEnablePowerSwitchEvent = true;
            
            // Set a new colour to send.
            mController.setLightColor(mCurrentColor, progress);
        }
    };

    /**
     * Called when power button is pressed.
     * 开、关
     */
    protected OnCheckedChangeListener powerChange = new OnCheckedChangeListener() {
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mEnablePowerSwitchEvent) {
                mController.setLightPower(isChecked ? PowerState.ON : PowerState.OFF);
            }
        }
    };
    
    /**
     * Event handler for when a new device is selected from the Spinner.
     * 
     * @param position
     *            Position within Spinner of selected device.
     */
    protected void deviceSelected(int position) {
        if (position == 0) {
            mController.setSelectedDeviceId(0);
            updateControls(0);
        }
        else {
            int deviceId = mDeviceListAdapter.getItemDeviceId(position);
            mController.setSelectedDeviceId(deviceId);
            updateControls(deviceId);
        }
    }
    
    /**
     * Update the UI with the state of the selected device, if its state is known.
     * 
     * @param selectedDeviceId
     *            The device id of the selected device.
     */
    protected void updateControls(int selectedDeviceId) {        
        Device dev = mController.getDevice(selectedDeviceId);
        
        if (dev != null) {
        	LightState state = (LightState)dev.getState(DeviceState.StateType.LIGHT);
        	PowState powState = (PowState)dev.getState(DeviceState.StateType.POWER);
            // Set UI to reflect the selected light's state if the state is valid.
            mEnableEvents = false;
            mEnablePowerSwitchEvent = false;
            mPowerSwitch.setChecked(powState.getPowerState() == PowerState.ON);
            mEnablePowerSwitchEvent = true;
            if (state.isStateKnown()) {
                int r = (int) state.getRed() & 0xFF;
                int g = (int) state.getGreen() & 0xFF;
                int b = (int) state.getBlue() & 0xFF;
                float[] hsv = new float[3];
                Color.RGBToHSV(r, g, b, hsv);
                mColorWheel.setCursor(hsv[0], hsv[1]);
                mBrightSlider.setProgress((int) (hsv[2] * 100.0f));
                mCurrentColor = Color.rgb(r, g, b);
                mColorWheel.invalidate();
            }
            else {
                mColorWheel.setCursor(0, 0);
                mColorWheel.invalidate();
            }
            mEnableEvents = true;
        }
        else {
            mEnableEvents = false;
            mBrightSlider.setProgress(100);
            mEnableEvents = true;
        }
    }

    protected void enableEvents(boolean enabled) {
        mEnableEvents = enabled;
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
     * Get the selected device id and set the spinner to it.
     */
    protected void selectSpinnerDevice() {
        int selectedDeviceId = mController.getSelectedDeviceId();        
        if (selectedDeviceId != Device.DEVICE_ID_UNKNOWN) {
    		Device dev = mController.getDevice(selectedDeviceId);
    		if (dev instanceof SingleDevice &&
    			((SingleDevice)dev).isModelSupported(LightModelApi.MODEL_NUMBER)) {
    			mDeviceSpinner.setSelection(mDeviceListAdapter.getDevicePosition(selectedDeviceId), true);
    		}
        }
        else {
            // No active device, so select the first device in the spinner if there is one.
            if (mDeviceListAdapter.getCount() > 0) {
                if (mDeviceSpinner.getSelectedItemPosition() == 0) {
                    // Make sure the event handler is called even if index zero was already selected.
                    updateControls(mDeviceListAdapter.getItemDeviceId(0));
                }
                else {
                    mDeviceSpinner.setSelection(0);
                }
            }
        }
    }
    
    /**
	 * Reload the displayed devices and groups.
	 */
	public void refreshUI() {
		mDeviceListAdapter.clear(); 
		loadDevices();
	}

}
