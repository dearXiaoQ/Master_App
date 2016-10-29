package com.csr.masterapp.scene;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.masterapp.R;
import com.csr.masterapp.WelcomeUI;
import com.csr.masterapp.database.DataBaseDataSource;
import com.csr.masterapp.entities.Alarm;
import com.csr.masterapp.fragment.MenuFragment;
import com.csr.masterapp.receiver.AlarmServiceBroadcastReciever;
import com.csr.masterapp.scene.util.SceneItemModel;
import com.csr.masterapp.scene.util.SceneModel;
import com.csr.masterapp.utils.CacheUtils;
import com.csr.masterapp.utils.Constans;
import com.csr.masterapp.utils.Utils;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;

/**
 * 项目名称：MasterApp v3
 * 类描述：新建场景
 * 创建人：11177
 * 创建时间：2016/7/7 15:21
 * 修改人：11177
 * 修改时间：2016/7/7 15:21
 * 修改备注：
 */
public class CreateSceneUI extends Activity implements View.OnClickListener, TextWatcher, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemLongClickListener {

    private ArrayList<SceneItemModel> mConditions;
    private ArrayList<SceneItemModel> mTasks;

    private ListView mTaskListview;

    private ListView mConditionListview;
    private TaskItemListAdppter mTaskItemListAdppter;
    private ConditionItemListAdpter mConditionItemListAdpter;

    private Integer mode;//启动模式  1:任一条件 2:所有条件 3:点击启动 4:定时启动

    private static final String TAG = "CreateSceneUI";

    private Intent intentDeviceList;

    private Resources mResources;

    private EditText mSceneName;
    private Button mPositiveButton;
    private DataBaseDataSource mDataBase;
    private int mIsSend;
    private SceneModel mScene;
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_scene);
        mResources = this.getResources();
        mDataBase = new DataBaseDataSource(CreateSceneUI.this);
        mTasks = new ArrayList<SceneItemModel>();
        mConditions = new ArrayList<SceneItemModel>();
        mode = 0;
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
        ((TextView) findViewById(R.id.header_tv_title)).setText(R.string.new_add_sence);
        Button Save = (Button) findViewById(R.id.header_btn_ok);
        Save.setText(R.string.save);
        Save.setOnClickListener(this);

        findViewById(R.id.header_iv_back).setOnClickListener(this);
        findViewById(R.id.iv_add_task).setOnClickListener(this);
        findViewById(R.id.iv_add_condition).setOnClickListener(this);

        mTaskListview = (ListView) findViewById(R.id.lv_task);
        mConditionListview = (ListView) findViewById(R.id.lv_condition);
    }

    private void initData() {

        mConditionItemListAdpter = new ConditionItemListAdpter();
        mConditionListview.setAdapter(mConditionItemListAdpter);
        mConditionListview.setOnItemLongClickListener(this);

        mTaskItemListAdppter = new TaskItemListAdppter();
        mTaskListview.setAdapter(mTaskItemListAdppter);
        mTaskListview.setOnItemLongClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            switch (resultCode) {
                case 0:
                    //添加条件
                    mode = 1;
                    SceneItemModel sceneConditionItem = (SceneItemModel) data.getSerializableExtra("sceneItem");
                    sceneConditionItem.setType(0);
                    mConditions.add(sceneConditionItem);
                    findViewById(R.id.tv_no_condition).setVisibility(mConditions.size() > 0 ? View.GONE : View.VISIBLE);
                    mConditionItemListAdpter.notifyDataSetChanged();
                    break;
                case 1:
                    //添加任务
                    SceneItemModel sceneTaskItem = (SceneItemModel) data.getSerializableExtra("sceneItem");
                    sceneTaskItem.setType(1);
                    mTasks.add(sceneTaskItem);
                    findViewById(R.id.tv_no_task).setVisibility(mTasks.size() > 0 ? View.GONE : View.VISIBLE);
                    mTaskItemListAdppter.notifyDataSetChanged();
                    break;
                case 2:
                    mode = 4;
                    alarm = (Alarm) data.getSerializableExtra("alarm");
                    SceneItemModel alarmItem = new SceneItemModel(-1,"alarm",alarm.getDescription() +" " + alarm.getAlarmTimeString(),0);
                    alarmItem.setType(0);
                    alarmItem.setDeviceName(mResources.getString(R.string.timer));
                    mConditions.add(alarmItem);
                    findViewById(R.id.tv_no_condition).setVisibility(mConditions.size() > 0 ? View.GONE : View.VISIBLE);
                    mConditionItemListAdpter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                return;
            case R.id.header_iv_back:
                if (mTasks.size() != 0 || mConditions.size() != 0) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(CreateSceneUI.this);
                    builder.setMessage(R.string.exit_modify_is_modify);
                    builder.setPositiveButton(R.string.yes_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    builder.setNegativeButton(R.string.no, null);
                    builder.create().show();
                    return;
                }
                finish();
                return;
            case R.id.iv_add_condition:
                intentDeviceList = new Intent(CreateSceneUI.this, SceneItemUI.class);
                intentDeviceList.putExtra("type", 0);
                intentDeviceList.putExtra("mode",mode);
                startActivityForResult(intentDeviceList, 0);
                return;
            case R.id.iv_add_task:
                intentDeviceList = new Intent(CreateSceneUI.this, SceneItemUI.class);
                intentDeviceList.putExtra("type", 1);
                startActivityForResult(intentDeviceList, 0);
                return;
            case R.id.header_btn_ok:
                if (mTasks.size() == 0) {
                    Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.please_add_task), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mConditions.size() == 0) {
                    Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.please_add_condition), Toast.LENGTH_SHORT).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(CreateSceneUI.this);
                builder.setTitle(R.string.set_sence_name);
                View diaName = View.inflate(CreateSceneUI.this, R.layout.dialog_insert_name, null);
                mSceneName = (EditText) diaName.findViewById(R.id.scene_et_name);
                CheckBox checkBox = (CheckBox) diaName.findViewById(R.id.scene_chk_msg);
                checkBox.setOnCheckedChangeListener(this);
                builder.setView(diaName);
                builder.setNegativeButton(R.string.no, null);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        for (SceneModel scene : mDataBase.getAllSecnes()) {
                            if (java.util.Objects.equals(scene.getName(), mSceneName.getText().toString().trim())) {
                                Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.sence_name_existence_pelase_retry), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        //保存场景
                        mScene = new SceneModel(mSceneName.getText().toString().trim(), 1, mode, mIsSend, mConditions, mTasks);
                        final int lastInsertId = mDataBase.createOrUpdateScene(mScene);
                        if (lastInsertId < 1) {
                            Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.bulid_failed), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mScene.setSceneId(lastInsertId);

                        final String url = Constans.SCENE_ADD_URL;
                        HttpUtils utils = new HttpUtils();

                        Gson gson = new Gson();
                        RequestParams params = new RequestParams();
                        params.addBodyParameter("scene", gson.toJson(mScene));
                        params.addBodyParameter("masterAppId", CacheUtils.getString(CreateSceneUI.this, WelcomeUI.MASTER_APP_ID));

                        final ProgressDialog progress = new ProgressDialog(CreateSceneUI.this);
                        progress.setMessage(mResources.getString(R.string.being_created));
                        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progress.setIndeterminate(true);
                        progress.setCancelable(true);
                        progress.setCanceledOnTouchOutside(false);
                        progress.show();

                        if(MenuFragment.NETWORK_ONLINE){
                            utils.configTimeout(5 * 1000);
                            utils.configSoTimeout(5 * 1000);
                            utils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

                                @Override
                                public void onSuccess(ResponseInfo<String> responseInfo) {
                                    String result = responseInfo.result;
                                    String errorCode = Utils.ParseJSON(result, "errorCode");
                                    if (errorCode != null && errorCode.equals("0")) {
                                        if (mode == 4) {
                                            //设置定时
                                            alarm.setId(lastInsertId);
                                            CallAlarmServiceBroadcastReciever(alarm);
                                            mScene.setAlarm_time(alarm.getAlarmTimeString());
                                            mScene.setAlarm_days(alarm.getDays());
                                            mDataBase.createOrUpdateScene(mScene);
                                        }
                                        progress.dismiss();
                                        Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.build_success), Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.build_success), Toast.LENGTH_SHORT).show();
                                        progress.dismiss();
                                        mDataBase.removeSingleScene(lastInsertId);
                                    }
                                }

                                @Override
                                public void onFailure(HttpException error, String msg) {
                                    Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.network_connection_failed), Toast.LENGTH_SHORT).show();
                                    progress.dismiss();
                                    mDataBase.removeSingleScene(lastInsertId);
                                }
                            });

                        } else {
                            if (mode == 4) {
                                //设置定时
                                alarm.setId(lastInsertId);
                                CallAlarmServiceBroadcastReciever(alarm);
                                mScene.setAlarm_time(alarm.getAlarmTimeString());
                                mScene.setAlarm_days(alarm.getDays());
                                mDataBase.createOrUpdateScene(mScene);
                            }
                            progress.dismiss();
                            Toast.makeText(CreateSceneUI.this, mResources.getString(R.string.build_success), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

                mSceneName.addTextChangedListener(this);
                mPositiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                if (mSceneName.getText().toString().equals("")) {
                    mPositiveButton.setEnabled(false);
                }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mSceneName.getText().toString().equals("")) {
            mPositiveButton.setEnabled(false);
        } else {
            mPositiveButton.setEnabled(true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mIsSend = isChecked ? 1 : 0;
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(CreateSceneUI.this);
        builder.setPositiveButton(R.string.menu_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (parent.equals(mConditionListview)) {
                    mConditions.remove(position);
                    findViewById(R.id.tv_no_condition).setVisibility(mConditions.size() == 0 ? View.VISIBLE : View.GONE);
                    mConditionItemListAdpter.notifyDataSetChanged();
                    if(mConditions.size() == 0){
                        mode = 0;
                    }
                }
                if (parent.equals(mTaskListview)) {
                    mTasks.remove(position);
                    findViewById(R.id.tv_no_task).setVisibility(mTasks.size() == 0 ? View.VISIBLE : View.GONE);
                    mTaskItemListAdppter.notifyDataSetChanged();
                }
            }
        });
        builder.create().show();
        return false;
    }

    class TaskItemListAdppter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mTasks != null) {
                return mTasks.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mTasks != null) {
                return mTasks.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getBaseContext(), R.layout.item_scene, null);
                convertView.setTag(holder);

                holder.deviceName = (TextView) convertView.findViewById(R.id.tv_list_name);
                holder.sceneName = (TextView) convertView.findViewById(R.id.tv_list_sec_name);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.deviceName.setText(mTasks.get(position).getDeviceName());
            holder.sceneName.setText(mTasks.get(position).getKey());

            return convertView;
        }
    }

    class ConditionItemListAdpter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mConditions != null) {
                return mConditions.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mConditions != null) {
                return mConditions.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getBaseContext(), R.layout.item_scene, null);
                convertView.setTag(holder);

                holder.deviceName = (TextView) convertView.findViewById(R.id.tv_list_name);
                holder.sceneName = (TextView) convertView.findViewById(R.id.tv_list_sec_name);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.deviceName.setText(mConditions.get(position).getDeviceName());
            holder.sceneName.setText(mConditions.get(position).getKey());

            return convertView;
        }
    }

    class ViewHolder {
        TextView deviceName;
        TextView sceneName;
    }

    /**
     * 设置闹钟服务
     */
    private void CallAlarmServiceBroadcastReciever(Alarm alarm) {
        AlarmServiceBroadcastReciever reciever = new AlarmServiceBroadcastReciever();
        reciever.setAlarm(this, alarm);
    }

}
