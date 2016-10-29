package com.csr.masterapp.scene;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.entities.Alarm;
import com.csr.masterapp.weight.ArrayWheelAdapter;
import com.csr.masterapp.weight.WheelView;

import java.util.Calendar;

public class TimerSetActivity extends Activity implements View.OnClickListener {

    private Resources mResource;

    private Alarm alarm;
    private String[] hours = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20","21", "22", "23", "24"};
    private String[] minutes = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20","21", "22", "23", "24", "25",
            "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
            "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};

    private WheelView hWheelveiew;
    private WheelView mWheelveiew;
    private TextView mTvRepeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scene_timer_set);
        mResource = this.getResources();
        ((TextView) findViewById(R.id.header_tv_title)).setText(R.string.timer);
        ((TextView) findViewById(R.id.header_btn_ok)).setText(R.string.save);
        findViewById(R.id.header_btn_ok).setOnClickListener(this);
        findViewById(R.id.rv_timer_repeat).setOnClickListener(this);
        findViewById(R.id.header_iv_back).setOnClickListener(this);
        mTvRepeat = (TextView) findViewById(R.id.repeat_content);
        mTvRepeat.setOnClickListener(this);

        hWheelveiew = (WheelView) findViewById(R.id.wheel_timing_hour);
        hWheelveiew.setTEXT_SIZE(30);
        hWheelveiew.setADDITIONAL_ITEM_HEIGHT(60);
        hWheelveiew.setADDITIONAL_ITEMS_SPACE(5);
        hWheelveiew.setAdapter(new ArrayWheelAdapter<String>(hours));
        hWheelveiew.setCyclic(true);
        hWheelveiew.setLabel(mResource.getString(R.string.hour));
        // 初始化时显示的数据
        hWheelveiew.setCurrentItem(0);

        mWheelveiew = (WheelView) findViewById(R.id.wheel_timing_minute);
        mWheelveiew.setTEXT_SIZE(30);
        mWheelveiew.setADDITIONAL_ITEM_HEIGHT(60);
        mWheelveiew.setADDITIONAL_ITEMS_SPACE(5);
        mWheelveiew.setAdapter(new ArrayWheelAdapter<String>(minutes));
        mWheelveiew.setCyclic(true);
        mWheelveiew.setLabel(mResource.getString(R.string.minute));

        // 初始化时显示的数据
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
                final Calendar newAlarmTime = Calendar.getInstance();
                newAlarmTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours[hWheelveiew.getCurrentItem()]));
                newAlarmTime.set(Calendar.MINUTE, Integer.parseInt(minutes[mWheelveiew.getCurrentItem()]));
                alarm.setAlarmTime(newAlarmTime);

                Intent intent = new Intent();
                intent.putExtra("alarm", alarm);
                TimerSetActivity.this.setResult(SceneItemUI.RESULT_TIMER, intent);
                TimerSetActivity.this.finish();
                break;
            case R.id.rv_timer_repeat:
                AlertDialog.Builder builder = new AlertDialog.Builder(TimerSetActivity.this);
                builder.setItems(new String[]{mResource.getString(R.string.only_implement_one),
                        mResource.getString(R.string.every_day),
                        mResource.getString(R.string.working_day),
                        mResource.getString(R.string.custom)}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which){
                            case 0:
                                alarm.removeAllDay();
                                mTvRepeat.setText(alarm.getDescription());
                                break;
                            case 1:
                                alarm.addAllDay();
                                mTvRepeat.setText(alarm.getDescription());
                                break;
                            case 2:
                                alarm.workDay();
                                mTvRepeat.setText(alarm.getDescription());
                                break;
                            case 3:
                                AlertDialog.Builder defineBuilder = new AlertDialog.Builder(TimerSetActivity.this);
                                View view = View.inflate(TimerSetActivity.this, R.layout.item_define_date_list, null);
                                SetWeekButton((CheckBox) view.findViewById(R.id.chk_sunday), Calendar.SUNDAY);
                                SetWeekButton((CheckBox) view.findViewById(R.id.chk_monday), Calendar.MONDAY);
                                SetWeekButton((CheckBox) view.findViewById(R.id.chk_tuesday), Calendar.TUESDAY);
                                SetWeekButton((CheckBox) view.findViewById(R.id.chk_webnesday), Calendar.WEDNESDAY);
                                SetWeekButton((CheckBox) view.findViewById(R.id.chk_thursday), Calendar.THURSDAY);
                                SetWeekButton((CheckBox) view.findViewById(R.id.chk_friday), Calendar.FRIDAY);
                                SetWeekButton((CheckBox) view.findViewById(R.id.chk_saturday), Calendar.SATURDAY);
                                defineBuilder.setView(view);
                                defineBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (alarm.getDays() != null && alarm.getDays().length > 0){
                                            mTvRepeat.setText(alarm.getRepeatDaysString());
                                        }
                                    }
                                });
                                AlertDialog defineDialog = defineBuilder.create();
                                defineDialog.show();
                                break;
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
    }

    private void SetWeekButton(CheckBox checkbox, final int dayOfWeek){
        final CheckBox week = checkbox;
        if (week != null) {
            week.setChecked(alarm.IsRepeat(dayOfWeek));
            week.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        alarm.addDay(dayOfWeek);
                    }else{
                        alarm.removeDay(dayOfWeek);
                    }
                }
            });
        }

    }
}


