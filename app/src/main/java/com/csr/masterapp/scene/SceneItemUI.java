package com.csr.masterapp.scene;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.masterapp.R;
import com.csr.masterapp.database.DataBaseDataSource;
import com.csr.masterapp.entities.Alarm;
import com.csr.masterapp.entities.DeviceDes;
import com.csr.masterapp.entities.DeviceStream;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.masterapp.scene.util.SceneItemModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * 项目名称：MasterApp v3
 * 类描述：添加任务列表
 * 创建人：11177
 * 创建时间：2016/7/8 11:39
 * 修改人：11177
 * 修改时间：2016/7/8 11:39
 * 修改备注：
 */

public class SceneItemUI extends Activity implements AdapterView.OnItemClickListener {

    private HashMap<String, ArrayList<DeviceStream>> mStreams;
    private DataBaseDataSource mDataBase;

    private ListView mListView;

    private DeviceListAdapter deviceListAdapter;

    private ArrayList<SingleDevice> mSingleDevices;

    private static final String TAG = "SceneItemUI";

    public static final int RESULT_TIMER           = 1;
    public static final int RESULT_DELAY           = 2;

    private AlertDialog.Builder builder;
    private ArrayList<DeviceDes> manuSet;
    private AlertDialog dialog;
    private Integer mStreamType;
    private Integer mDataType;
    private TextView mTitle;
    private Integer mConditionMode;
    private ArrayList<DeviceStream> mCurrentStream;
    private int mPosition;

    private Resources mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        mDataBase = new DataBaseDataSource(this);
        mResources = this.getResources();
        initView();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void initView() {
        mTitle = ((TextView) findViewById(R.id.header_tv_title));
        mTitle.setText(R.string.device_linkage);
        findViewById(R.id.header_btn_ok).setVisibility(View.GONE);
        findViewById(R.id.header_iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mListView = (ListView) findViewById(R.id.device_list_view);
        mListView.setOnItemClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        mStreamType = intent.getIntExtra("type", -1);
        mPosition = intent.getIntExtra("position", -1);
        mConditionMode = intent.getIntExtra("mode", -1);//启动模式
        if (mStreamType == -1 || mStreamType == -1) {
            finish();
            return;
        }

        mSingleDevices = new ArrayList<SingleDevice>();

        switch (mStreamType){
            case 0:
                mTitle.setText(R.string.start_condition);
                SingleDevice Click = new SingleDevice(-1, "", 0 , getApplicationContext().getResources().getString(R.string.click_start), "", 0, 0);
                SingleDevice alarm = new SingleDevice(-1, "", 0 , getApplicationContext().getResources().getString(R.string.timer_start), "", 0, 0);
                mSingleDevices.add(Click);
                mSingleDevices.add(alarm);
                break;
            case 1:
                mTitle.setText(R.string.implement_task);
                SingleDevice delay = new SingleDevice(-1, "", 0 , getApplicationContext().getResources().getString(R.string.delay), "", 0, 0);
                mSingleDevices.add(delay);
                break;
        }

        mStreams = new HashMap<>();
        for (DeviceStream streams : mDataBase.getDeviceStream()) {
            if (Objects.equals(streams.getType(), mStreamType)) {
                if(mStreams.containsKey(streams.getShortname())){
                    mStreams.get(streams.getShortname()).add(streams);
                }else{
                    ArrayList<DeviceStream> deviceStreams = new ArrayList<>();
                    deviceStreams.add(streams);
                    mStreams.put(streams.getShortname(),deviceStreams);
                }

            }
        }

        for (SingleDevice devices : mDataBase.getAllSingleDevices()) {
            //获取设备基本信息
            if (mStreams.containsKey(devices.getShortName().trim())) {
                mSingleDevices.add(devices);
            }
        }

        //加载数据
        deviceListAdapter = new DeviceListAdapter();
        mListView.setAdapter(deviceListAdapter);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mSingleDevices == null) {
            return;
        }
        //选择定时、点击启动
        if(mStreamType == 0){
            if(position == 0){
                if(mConditionMode == 0) {
                    Toast.makeText(SceneItemUI.this, mResources.getString(R.string.it_man_in_development), Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Toast.makeText(SceneItemUI.this, mResources.getString(R.string.ban_click),Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if(position == 1){
                if(mConditionMode == 0){
                    Toast.makeText(SceneItemUI.this, mResources.getString(R.string.it_man_in_development), Toast.LENGTH_SHORT).show();
//                    Intent newTimerIntent = new Intent(SceneItemUI.this, TimerSetActivity.class);
//                    startActivityForResult(newTimerIntent,0);
                    return;
                }else{
                    Toast.makeText(SceneItemUI.this, R.string.ban_click,Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        //选择延时
        if(mStreamType == 1){
            if(position == 0){
                Toast.makeText(SceneItemUI.this, mResources.getString(R.string.it_man_in_development), Toast.LENGTH_SHORT).show();
                return;
//                startActivity(new Intent(SceneItemUI.this, DelaySetActivity.class));
            }
        }

        //判断当前已选状态
        if(mStreamType == 1 || mConditionMode == 1 || mConditionMode == 2 || mConditionMode == 0){
            String shortname = mSingleDevices.get(position).getShortName().trim();
            mCurrentStream = mStreams.get(shortname);

            final int mDeviceId = mSingleDevices.get(position).getDeviceId();
            final String mDeviceName = mSingleDevices.get(position).getName().trim();

            //弹出功能对话框
            builder = new AlertDialog.Builder(SceneItemUI.this);
            builder.setTitle(R.string.select_function);

            View itemDes = View.inflate(SceneItemUI.this, R.layout.ly_item_des, null);
            builder.setView(itemDes);
            ListView StreamListView = (ListView) itemDes.findViewById(R.id.des_list_view);
            final CurrentStreamListAdapter StreamListAdapter = new CurrentStreamListAdapter();
            StreamListView.setAdapter(StreamListAdapter);

            builder.create();
            dialog = builder.show();

            StreamListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    DeviceStream mCurrentDes = mCurrentStream.get(position);
                    mDataType = mCurrentStream.get(position).getData_type();
                    manuSet = mCurrentStream.get(position).getManu_set();
                    final String mStreamName = mCurrentStream.get(position).getStream_name();

                    //单位转换
                    String unit = mCurrentDes.getUnit() == null ? "":mCurrentDes.getUnit();
                    String unit_symbol = mCurrentDes.getUnit_symbol() == null ? "":mCurrentDes.getUnit_symbol();
                    if (unit.equals("") && unit_symbol.equals("")) {
                        unit = unit_symbol;
                    }

                    //不可控、数值类型
                    if (mStreamType == 0 && mDataType == 1) {
                        if (manuSet == null) {
                            manuSet = new ArrayList<DeviceDes>();
                            int max_value = mCurrentDes.getMax_value();
                            int min_value = mCurrentDes.getMin_value();
                            int increment = mCurrentDes.getIncrement();
                            int value;
                            for (int i = 0; i < (max_value - min_value + 1) / increment; i++) {
                                value = min_value + increment * i;
                                if (value > max_value) {
                                    return;
                                }
                                manuSet.add(new DeviceDes(i - 1, mCurrentDes.getStream_id(), mResources.getString(R.string.less_than) +  value + " " + unit, value, "<"));
                                manuSet.add(new DeviceDes(i - 1, mCurrentDes.getStream_id(), mResources.getString(R.string.more_than) + value + " " + unit, value, ">"));
                            }
                        }
                    }

                    //弹出对话框
                    builder = new AlertDialog.Builder(SceneItemUI.this);
                    builder.setTitle(mResources.getString(R.string.configu) + mCurrentStream.get(position).getStream_description() );

                    View itemDes = View.inflate(SceneItemUI.this, R.layout.ly_item_des, null);
                    builder.setView(itemDes);
                    ListView desListView = (ListView) itemDes.findViewById(R.id.des_list_view);
                    final DesListAdapter DesListAdapter = new DesListAdapter();
                    desListView.setAdapter(DesListAdapter);

                    builder.create();
                    dialog = builder.show();

                    desListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            dialog.dismiss();

                            if (manuSet != null) {
                                SceneItemModel sceneItem = new SceneItemModel(mDeviceId, mStreamName, manuSet.get(position).getKey(), manuSet.get(position).getValue());
                                sceneItem.setDeviceName(mDeviceName);
                                if (manuSet.get(position).getComparison_opt() != null ) {
                                    sceneItem.setComparison_opt(manuSet.get(position).getComparison_opt());
                                }
                                Intent intent = new Intent();
                                intent.putExtra("sceneItem", sceneItem);
                                if(mPosition != -1){
                                    intent.putExtra("position", mPosition);
                                }
                                SceneItemUI.this.setResult(mStreamType, intent);
                                SceneItemUI.this.finish();
                            }
                        }
                    });
                }
            });

        }else{
            Toast.makeText(SceneItemUI.this, mResources.getString(R.string.ban_click),Toast.LENGTH_SHORT).show();
        }
    }

    class DeviceListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mSingleDevices != null) {
                return mSingleDevices.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mSingleDevices != null) {
                return mSingleDevices.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(SceneItemUI.this, R.layout.item_device, null);
                convertView.setTag(holder);
                holder.deviceName = (TextView) convertView.findViewById(R.id.tv_device_name);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.deviceName.setText(mSingleDevices.get(position).getName());
            return convertView;
        }

    }

    class ViewHolder {
        TextView deviceName;
    }

    class CurrentStreamListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if ( mCurrentStream!= null) {
                return mCurrentStream.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mCurrentStream != null) {
                return mCurrentStream.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DesViewHolder holder = null;
            if (convertView == null) {
                holder = new DesViewHolder();
                convertView = View.inflate(SceneItemUI.this, R.layout.item_list, null);
                convertView.setTag(holder);
                holder. name = (TextView) convertView.findViewById(R.id.tv_list_name);
            } else {
                holder = (DesViewHolder) convertView.getTag();
            }

            holder. name.setText(mCurrentStream.get(position).getStream_description());
            return convertView;
        }
    }

    class DesListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (manuSet != null) {
                return manuSet.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (manuSet != null) {
                return manuSet.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            DesViewHolder holder = null;
            if (convertView == null) {
                holder = new DesViewHolder();
                convertView = View.inflate(SceneItemUI.this, R.layout.item_list, null);
                convertView.setTag(holder);
                holder. name = (TextView) convertView.findViewById(R.id.tv_list_name);
            } else {
                holder = (DesViewHolder) convertView.getTag();
            }

            holder. name.setText(manuSet.get(position).getKey());
            return convertView;
        }
    }

    class DesViewHolder {
        TextView name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            Toast.makeText(SceneItemUI.this, mResources.getString(R.string.data_is_empth), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent();
        switch (resultCode){
            case RESULT_TIMER:
                intent.putExtra("alarm", (Alarm) data.getSerializableExtra("alarm"));
                SceneItemUI.this.setResult(2, intent);
                SceneItemUI.this.finish();
                break;
            case RESULT_DELAY:
                int resMinute = data.getIntExtra("minute", 0);
                int resSecond = data.getIntExtra("second", 0);
                SceneItemModel sceneItem = new SceneItemModel(-1, "delay", resMinute +
                            mResources.getString(R.string.mm) + resSecond + mResources.getString(R.string.ss_later), 3000);
                intent.putExtra("sceneItem", sceneItem);
                SceneItemUI.this.setResult(mStreamType, intent);
                SceneItemUI.this.finish();
                break;
        }
    }
}
