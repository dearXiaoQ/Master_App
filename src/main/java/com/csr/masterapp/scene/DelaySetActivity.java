package com.csr.masterapp.scene;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.entities.Alarm;
import com.csr.masterapp.weight.ArrayWheelAdapter;
import com.csr.masterapp.weight.WheelView;

public class DelaySetActivity extends Activity implements View.OnClickListener {

    private Alarm alarm;
    private String[] minutes = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20","21", "22", "23", "24", "25",
            "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
            "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};

    private String[] seconds = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20","21", "22", "23", "24", "25",
            "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
            "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};

    private WheelView hWheelveiew;
    private WheelView mWheelveiew;

    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scene_timer_set);
        mResources = this.getResources();
        ((TextView) findViewById(R.id.header_tv_title)).setText(R.string.delay);
        ((TextView) findViewById(R.id.header_btn_ok)).setText(R.string.save);
        findViewById(R.id.header_btn_ok).setOnClickListener(this);
        findViewById(R.id.rv_timer_repeat).setOnClickListener(this);
        findViewById(R.id.header_iv_back).setOnClickListener(this);

        hWheelveiew = (WheelView) findViewById(R.id.wheel_timing_hour);
        hWheelveiew.setTEXT_SIZE(30);
        hWheelveiew.setADDITIONAL_ITEM_HEIGHT(60);
        hWheelveiew.setADDITIONAL_ITEMS_SPACE(5);
        hWheelveiew.setAdapter(new ArrayWheelAdapter<String>(minutes));
        hWheelveiew.setCyclic(true);
        hWheelveiew.setLabel(mResources.getString(R.string.mm));
        // 初始化时显示的数据
        hWheelveiew.setCurrentItem(0);

        mWheelveiew = (WheelView) findViewById(R.id.wheel_timing_minute);
        mWheelveiew.setTEXT_SIZE(30);
        mWheelveiew.setADDITIONAL_ITEM_HEIGHT(60);
        mWheelveiew.setADDITIONAL_ITEMS_SPACE(5);
        mWheelveiew.setAdapter(new ArrayWheelAdapter<String>(seconds));
        mWheelveiew.setCyclic(true);
        mWheelveiew.setLabel(mResources.getString(R.string.ss));
        mWheelveiew.setCurrentItem(0);

        alarm = new Alarm();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.header_iv_back:
                finish();
                break;
            case R.id.header_btn_ok:
//                final Calendar newAlarmTime = Calendar.getInstance();
//                newAlarmTime.set(Calendar.MINUTE, Integer.parseInt());
//                newAlarmTime.set(Calendar.SECOND, Integer.parseInt(seconds[mWheelveiew.getCurrentItem()]));
                Intent intent = new Intent();
                intent.putExtra("minute", Integer.parseInt(minutes[mWheelveiew.getCurrentItem()]));
                intent.putExtra("second", Integer.parseInt(seconds[mWheelveiew.getCurrentItem()]));
                DelaySetActivity.this.setResult(SceneItemUI.RESULT_DELAY, intent);
                DelaySetActivity.this.finish();
                break;
        }
    }
}


