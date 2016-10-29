
/******************************************************************************
 * Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.scene;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.csr.masterapp.R;
import com.csr.masterapp.SceneController;
import com.csr.masterapp.WelcomeUI;
import com.csr.masterapp.database.DataBaseDataSource;
import com.csr.masterapp.entities.Alarm;
import com.csr.masterapp.entities.SingleDevice;
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
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Fragment used to configure devices. Handles assigning devices to groups, get firmware version, remove a device or
 * group, rename a device or group and add a new group. Contains two side by side CheckedListFragment fragments.
 */
public class SceneListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener, AdapterView.OnItemLongClickListener {
    private static final String TAG = "DeviceListFragment";

    private View mRootView;
    private DataBaseDataSource mDataBase;
    private ArrayList<SceneModel> mScenes;
    private ScenesItemListAdpter mScenesItemAdpter;     //mScenesItemAdpter
    private ScenesDetailListAdpter mScenesListAdpter;

    private ListView mListViewLeft;
    private TextView mSceneName;
    private CheckBox mSceneMsg;
    private Switch mSceneSwitch;

    private Boolean isEditScene = false;//是否修改了当前场景 false:没有修改  true:有修改，弹出提示
    private Boolean isEditAlarm = false;
    private Button mSaveScene;

    private SceneModel mCurrentScene;
    private int mSelect = 0;

    private SceneController mController;
    private CallbackActivity mCallback;
    private ListView mListViewScenes;
    private Alarm alarm;
    private boolean tempAddItem = false;//判断是否有场景项添加,有添加，拦截onResume
    private Resources mResources;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBase = new DataBaseDataSource(getActivity());
        mResources = this.getResources();
        initData();
        if (mScenes.size() > 0) {
            mCurrentScene = mScenes.get(mSelect);
        }
    }

    private void initData() {
        Log.i("sence", "进入initData");

        mScenes = mDataBase.getAllSecnes();
        Map<Integer, SingleDevice> mDevices = mDataBase.getAllDevicesMap();
        for (SceneModel scene : mScenes) {
            for (SceneItemModel sceneItem : scene.getConditions()) {
                sceneItem.setDeviceName(sceneItem.getDeviceId() > 0 ? (mDevices.get(sceneItem.getDeviceId()) == null?mResources.getString(R.string.device_not_exists): mDevices.get(sceneItem.getDeviceId()).getName()) : "定时");
            }
            for (SceneItemModel sceneItem : scene.getTasks()) {
                sceneItem.setDeviceName(mDevices.get(sceneItem.getDeviceId()) == null?mResources.getString(R.string.device_not_exists): mDevices.get(sceneItem.getDeviceId()).getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.scene_list_fragment, container, false);
        }
        if(mCurrentScene == null){
            mRootView.findViewById(R.id.rv_no_scene).setVisibility(View.VISIBLE);
        }else{
            mRootView.findViewById(R.id.rv_no_scene).setVisibility(View.GONE);
        }


        mListViewLeft = (ListView) mRootView.findViewById(R.id.scene_lv_left);

        mListViewLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("mListViewLeft", "mListViewLeft is onTouch");
                //回调函数
                mCallback.onCallbackActivity(true);
                return false;
            }
        });

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //回调函数
                mCallback.onCallbackActivity(true);
                Log.i("mListViewLeft", "mRootView is onTouch!");
                return false;
            }
        });

        mScenesItemAdpter = new ScenesItemListAdpter();
        mListViewLeft.setAdapter(mScenesItemAdpter);
        mListViewLeft.setOnItemClickListener(this);
        mListViewLeft.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);//多选模式
        mListViewLeft.setMultiChoiceModeListener(new AccountMultiChoiceModeListener());//多选监听

        mListViewScenes = (ListView) mRootView.findViewById(R.id.lv_list_scene);
        View headerView = mRootView.inflate(getActivity(), R.layout.scene_list_top_view, null);
        mListViewScenes.addHeaderView(headerView);
        mScenesListAdpter = new ScenesDetailListAdpter();
        mListViewScenes.setAdapter(mScenesListAdpter);
        mListViewScenes.setOnItemLongClickListener(this);
        mListViewScenes.setOnItemClickListener(this);


        mListViewScenes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("mListViewLeft", "mListViewScenes is onTouch");
                //回调函数
                mCallback.onCallbackActivity(true);
                return false;
            }
        });


        mSceneName = (TextView) headerView.findViewById(R.id.tv_scene_name);
        mSceneName.setOnClickListener(this);
        mSceneMsg = (CheckBox) headerView.findViewById(R.id.chk_msg);
        mSceneMsg.setOnCheckedChangeListener(this);
        mSceneSwitch = (Switch) mRootView.findViewById(R.id.switch_scene);
        mSceneSwitch.setOnCheckedChangeListener(this);
        mRootView.findViewById(R.id.tv_edit_name).setOnClickListener(this);
        mSaveScene = (Button) mRootView.findViewById(R.id.btn_save_scene);
        mSaveScene.setOnClickListener(this);

        if (mScenes.size() != 0) {
            mScenesListAdpter.changeDatas(mCurrentScene);
        }

        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mController = (SceneController) activity;

            mCallback = (CallbackActivity) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeviceController or OnHeadlineSelectedListener callback interface.");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        if ( mScenes.size() == 1) {
            if(!tempAddItem){
                mCurrentScene = mScenes.get(0);
                mScenesListAdpter.changeDatas(mCurrentScene);
            }else{
                tempAddItem = false;
            }
            mRootView.findViewById(R.id.rv_no_scene).setVisibility(View.GONE);
        }
        mScenesItemAdpter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_edit_name:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.modify_sence_name);

                final EditText input = new EditText(getActivity());
                input.setHint(mCurrentScene.getName());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
                builder.setView(input);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString().trim();
                        for (SceneModel scene : mScenes){
                            if(java.util.Objects.equals(scene.getName(), newName)){
                                Toast.makeText(getActivity(), R.string.sence_name_existence_pelase_retry, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        mCurrentScene.setName(newName);
                        isEditScene = true;
                        mScenesListAdpter.changeDatas(mCurrentScene);
                    }
                });
                final AlertDialog dialog = builder.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable text) {
                        if (text.length() <= 0) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        // No behaviour.
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // No behaviour.
                    }
                });

                return;
            case R.id.btn_save_scene:
                updateScenes(mCurrentScene, -1);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //初始setChecked时禁止触发onCheckedChanged监听器
        if (buttonView.isPressed()) {
            if (buttonView.equals(mSceneSwitch)) {
                if (mCurrentScene != null) {
                    int value = isChecked ? 1 : 0;
                    mCurrentScene.setStatus(value);
                    mDataBase.updateSceneStatus(mCurrentScene.getSceneId(), value);
                    mScenesItemAdpter.notifyDataSetChanged();
                    mController.getScenes();
                    Toast.makeText(getActivity(), isChecked ? mResources.getString(R.string.open_sence) : mResources.getString(R.string.close_sence), Toast.LENGTH_SHORT).show();
                }
            }
            if (buttonView.equals(mSceneMsg)) {
                isEditScene = true;
                mCurrentScene.setIsSend(isChecked ? 1 : 0);
                mScenesListAdpter.changeDatas(mCurrentScene);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
        if(parent == mListViewScenes){
            int size = mCurrentScene.getConditions().size();//条件的个数
            if ((position - 1) > size) {
                //增加任务
                Toast.makeText(getActivity(),position - 3 - size + "" , Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), SceneItemUI.class);
                intent.putExtra("type", 1);
                intent.putExtra("position", position - 3 - size);
                startActivityForResult(intent, 0);
            } else {
                //增加条件
                Intent intent = new Intent(getActivity(), SceneItemUI.class);
                intent.putExtra("type", 0);
                intent.putExtra("position", position - 2 + "");
                intent.putExtra("mode", mCurrentScene.getMode());
                startActivityForResult(intent, 0);
                Toast.makeText(getActivity(),position - 2 + "" , Toast.LENGTH_SHORT).show();
            }
            isEditScene = true;
            mScenesListAdpter.changeDatas(mCurrentScene);
        }

        if(parent == mListViewLeft) {
            if(position != mSelect) {
                if (isEditScene) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.exit_modify_is_modify);
                    builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelect = position;
                            updateScenes(mCurrentScene, position);
                        }
                    });
                    builder.setNeutralButton(R.string.not_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isEditScene = false;
                            isEditAlarm = false;
                            mSelect = position;
                            initData();
                            mCurrentScene = mScenes.get(position);
                            mScenesListAdpter.changeDatas(mCurrentScene);
                            mScenesItemAdpter.notifyDataSetChanged();
                        }
                    });
                    builder.create().show();
                } else {
                    mSelect = position;
                    mScenesItemAdpter.notifyDataSetChanged();
                    mCurrentScene = mScenes.get(position);
                    mScenesListAdpter.changeDatas(mCurrentScene);
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.menu_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int size = mCurrentScene.getConditions().size();//条件的个数
                if ((position - 1) > size) {
                    //删除任务
                    size = (size == 0 ? 1 : size);
                    mCurrentScene.getTasks().remove(position - 3 - size);
                } else {
                    //删除条件
                    if(mCurrentScene.getConditions().get(position - 2).getDeviceId() == -1){
                        mCurrentScene.setAlarm_time(null);
                        mCurrentScene.setAlarm_days(null);
                    }
                    mCurrentScene.getConditions().remove(position - 2);
                    if(mCurrentScene.getConditions().size() == 0){
                        mCurrentScene.setMode(0);
                    }
                }

                isEditScene = true;
                mScenesListAdpter.changeDatas(mCurrentScene);
            }
        });
        builder.create().show();
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        tempAddItem = true;
        if (data != null) {
            int position = data.getIntExtra("position", -1);//场景编辑的位置
            switch (resultCode) {
                case 0:
                    SceneItemModel sceneConditionItem = (SceneItemModel) data.getSerializableExtra("sceneItem");
                    mCurrentScene.setMode(1);
                    sceneConditionItem.setType(0);
                    if(position != -1){
                        mCurrentScene.getConditions().set(position,sceneConditionItem);
                    }else{
                        mCurrentScene.getConditions().add(sceneConditionItem);
                    }
                    break;
                case 1:
                    SceneItemModel sceneTaskItem = (SceneItemModel) data.getSerializableExtra("sceneItem");
                    sceneTaskItem.setType(1);
                    if(position != -1){
                        mCurrentScene.getTasks().set(position,sceneTaskItem);
                    }else{
                        mCurrentScene.getTasks().add(sceneTaskItem);
                    }
                    break;
                case 2:
                    isEditAlarm = true;
                    alarm = (Alarm) data.getSerializableExtra("alarm");
                    SceneItemModel alarmItem = new SceneItemModel(-1,"alarm",alarm.getDescription() +" " + alarm.getAlarmTimeString(),0);
                    alarmItem.setType(0);
                    mCurrentScene.setMode(4);
                    alarmItem.setDeviceName(mResources.getString(R.string.timer));
                    mCurrentScene.getConditions().add(alarmItem);
                    break;
            }
            isEditScene = true;
            mScenesListAdpter.changeDatas(mCurrentScene);
        }
    }

    /**
     * 多选菜单
     */
    private class AccountMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        private TreeSet<Integer> sceneIds;
        private ArrayList<Integer> alarmIds;

        public AccountMultiChoiceModeListener() {
            this.sceneIds = new TreeSet<Integer>();
            this.alarmIds = new ArrayList<Integer>();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.contextual_menu_accountactivity, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if(checked){
                sceneIds.add(mScenes.get(position).getSceneId());
                if(mScenes.get(position).getMode() == 4){ alarmIds.add(mScenes.get(position).getSceneId()); }
            }else{
                sceneIds.remove(mScenes.get(position).getSceneId());
                if(mScenes.get(position).getMode() == 4){ alarmIds.remove(mScenes.get(position).getSceneId()); }
            }
            mScenesItemAdpter.notifyDataSetChanged();
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_remove_scene:
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(mResources.getString(R.string.config_delete_Selected) + sceneIds.size() + mResources.getString(R.string.various_sence));
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeScenes(sceneIds);
                            mode.finish();
                        }
                    });
                    builder.setNegativeButton(R.string.no, null);
                    builder.create().show();
                    return true;
            }
            return false;
        }
    }

    class ScenesDetailListAdpter extends BaseAdapter {

        public static final int VIEW_TYPE_CONDITION = 0;

        public static final int VIEW_TYPE_TASK = 1;

        public static final int VIEW_TYPE_HEADER = 2;

        public static final int VIEW_TYPE_EMPTY = 3;

        private static final int VIEW_TYPE_COUNT = 4;

        private List<SceneItemModel> taskData;
        private List<SceneItemModel> conditionData;

        private final LayoutInflater mInflater;

        private List<TypeItem> items;

        public ScenesDetailListAdpter() {
            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        class TypeItem {
            int itemType;

            private TypeItem(int itemType) {
                this.itemType = itemType;
            }
        }

        class SceneTypeItem extends TypeItem {
            SceneItemModel sceneItem;

            public SceneTypeItem(int type, SceneItemModel sceneItem) {
                super(type);
                this.sceneItem = sceneItem;
            }
        }

        class EmptyTypeItem extends TypeItem {
            public EmptyTypeItem(int type) {
                super(type);
            }
        }

        class HeaderTypeItem extends TypeItem {
            String label;

            public HeaderTypeItem(String label) {
                super(VIEW_TYPE_HEADER);
                this.label = label;
            }
        }

        class ViewHolder {

            View itemView;

            public ViewHolder(View itemView) {
                if (itemView == null) {
                    throw new IllegalArgumentException("itemView can not be null!");
                }
                this.itemView = itemView;
            }
        }

        class SceneViewHolder extends ViewHolder {

            TextView device;
            TextView name;
            View root;

            public SceneViewHolder(View view) {
                super(view);
                device = (TextView) view.findViewById(R.id.tv_list_name);
                name = (TextView) view.findViewById(R.id.tv_list_sec_name);
                root = view.findViewById(R.id.listview_root);
            }
        }

        class SceneEmptyHolder extends ViewHolder {

            public SceneEmptyHolder(View view) {
                super(view);
            }
        }

        class HeaderViewHolder extends ViewHolder {

            TextView label;
            ImageView addScene;

            public HeaderViewHolder(View view) {
                super(view);
                label = (TextView) view.findViewById(R.id.label);
                addScene = (ImageView) view.findViewById(R.id.btn_add_scene);
            }
        }

        private List<TypeItem> generateItems() {
            List<TypeItem> items = new ArrayList<TypeItem>();
            items.add(new HeaderTypeItem(mResources.getString(R.string.start_condition)));
            if (conditionData.size() > 0) {
                for (SceneItemModel scene : conditionData) {
                    items.add(new SceneTypeItem(VIEW_TYPE_CONDITION, scene));
                }
            } else {
                items.add(new EmptyTypeItem(VIEW_TYPE_EMPTY));
            }
            items.add(new HeaderTypeItem(mResources.getString(R.string.implement_task)));
            if (taskData.size() > 0) {
                for (SceneItemModel scene : taskData) {
                    items.add(new SceneTypeItem(VIEW_TYPE_TASK, scene));
                }
            } else {
                items.add(new EmptyTypeItem(VIEW_TYPE_EMPTY));
            }
            return items;
        }

        public void changeDatas(SceneModel scene) {
            taskData = scene.getTasks();
            conditionData = scene.getConditions();
            mSceneName.setText(scene.getName());
            mSceneMsg.setChecked(scene.getIsSend() == 1);
            mSceneSwitch.setChecked(scene.getStatus() == 1);
            this.items = generateItems();
            mSaveScene.setVisibility(isEditScene ? View.VISIBLE : View.GONE);
            mScenesListAdpter.notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (items.get(position) != null) {
                return items.get(position).itemType;
            }
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        @Override
        public int getCount() {
            if (items != null) {
                return items.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (items != null && position > 0 && position < items.size()) {
                return items.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public SceneItemModel getSceneByPosition(int position) {
            if (items.get(position).itemType == VIEW_TYPE_HEADER || items.get(position).itemType == VIEW_TYPE_EMPTY) {
                return null;
            } else {
                SceneTypeItem sceneTypeItem = (SceneTypeItem) items.get(position);
                return sceneTypeItem.sceneItem;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TypeItem item = items.get(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                // 根据不同的viewType，初始化不同的布局
                switch (getItemViewType(position)) {
                    case VIEW_TYPE_HEADER:
                        viewHolder = new HeaderViewHolder(mInflater.inflate(
                                R.layout.scene_list_item_herder, null));
                        break;
                    case VIEW_TYPE_CONDITION:
                    case VIEW_TYPE_TASK:
                        viewHolder = new SceneViewHolder(mInflater.inflate(
                                R.layout.item_scene, null));
                        break;
                    case VIEW_TYPE_EMPTY:
                        viewHolder = new SceneEmptyHolder(mInflater.inflate(
                                R.layout.scene_list_item_empty, null));
                        break;
                    default:
                        throw new IllegalArgumentException("invalid view type : "
                                + getItemViewType(position));

                }
                // 缓存header与item视图
                convertView = viewHolder.itemView;
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // 根据初始化的不同布局，绑定数据
            if (viewHolder instanceof HeaderViewHolder) {
                ((HeaderViewHolder) viewHolder).label.setText(String.valueOf(((HeaderTypeItem) item).label));

                if (((HeaderTypeItem) item).label.equals(mResources.getString(R.string.start_condition))) {
                    ((HeaderViewHolder) viewHolder).addScene.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), SceneItemUI.class);
                            intent.putExtra("type", 0);
                            intent.putExtra("mode", mCurrentScene.getMode());
                            startActivityForResult(intent, 0);
                        }
                    });
                }

                if (((HeaderTypeItem) item).label.equals(mResources.getString(R.string.implement_task))) {
                    ((HeaderViewHolder) viewHolder).addScene.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), SceneItemUI.class);
                            intent.putExtra("type", 1);
                            startActivityForResult(intent, 0);
                        }
                    });
                }

            } else if (viewHolder instanceof SceneViewHolder) {
                ((SceneViewHolder) viewHolder).device.setText(((SceneTypeItem) item).sceneItem.getDeviceName());
                ((SceneViewHolder) viewHolder).name.setText(((SceneTypeItem) item).sceneItem.getKey());
            }

            return convertView;
        }

    }


    class ScenesItemListAdpter extends BaseAdapter {

        private int checkedBG;
        private int defaultBG;

        public ScenesItemListAdpter() {
            this.defaultBG = getResources().getColor(R.color.transparent);
            this.checkedBG = getResources().getColor(R.color.selected_color);
        }

        @Override
        public int getCount() {
            if (mScenes != null) {
                return mScenes.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mScenes != null) {
                return mScenes.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getActivity(), R.layout.item_list_scene, null);
                convertView.setTag(holder);

                holder.sceneName = (TextView) convertView.findViewById(R.id.tv_scene_name);
                holder.sceneStatu = (TextView) convertView.findViewById(R.id.tv_scene_statu);
                holder.sceneRoot = convertView.findViewById(R.id.listview_root);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.sceneName.setText(mScenes.get(position).getName());
            String status = null;
            switch (mScenes.get(position).getStatus()) {
                case 0:
                    status = mResources.getString(R.string.already_close);
                    break;
                case 1:
                    status = mResources.getString(R.string.already_open);
                    break;
                default:
                    return convertView;
            }
            holder.sceneStatu.setText(status);
            holder.sceneRoot.setBackgroundColor(defaultBG);
            if (mListViewLeft.getCheckedItemPositions().get(position)) {
                holder.sceneRoot.setBackgroundColor(checkedBG);
            }
            Log.d(TAG, "getView: " + mSelect);
            if(mSelect == position){
                convertView.setBackgroundColor(Color.parseColor("#FFCDCDCD"));
            }else{
                convertView.setBackgroundColor(defaultBG);
            }

            return convertView;
        }
    }

    class ViewHolder {
        TextView sceneName;
        TextView sceneStatu;
        View sceneRoot;
    }

    /**
     * 取消闹钟服务
     */
    private void CancelAlarmServiceBroadcastReciever(int id) {
        AlarmServiceBroadcastReciever reciever = new AlarmServiceBroadcastReciever();
        reciever.CancelAlarm(getActivity(),id);
        Log.d("场景取消了", "CancelAlarmServiceBroadcastReciever: " + id );
    }

    /**
     * 设置闹钟服务
     */
    private void CallAlarmServiceBroadcastReciever(Alarm alarm) {
        AlarmServiceBroadcastReciever reciever = new AlarmServiceBroadcastReciever();
        reciever.setAlarm(getActivity(), alarm);
    }

    /**
     * 保存修改的场景数据,上传服务器,刷新列表数据
     */
    private void updateScenes(final SceneModel scene,final int position) {

        if (scene.getTasks().size() == 0) {
            Toast.makeText(getActivity(), mResources.getString(R.string.please_add_task), Toast.LENGTH_SHORT).show();
            return;
        }
        if (scene.getConditions().size() == 0) {
            Toast.makeText(getActivity(), mResources.getString(R.string.please_add_condition), Toast.LENGTH_SHORT).show();
            return;
        }

        final String url = Constans.SCENE_EDIT_URL;
        HttpUtils utils = new HttpUtils();

        Gson gson = new Gson();
        RequestParams params = new RequestParams();
        params.addBodyParameter("scene",gson.toJson(scene));
        params.addBodyParameter("masterAppId", CacheUtils.getString(getActivity(), WelcomeUI.MASTER_APP_ID));


        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setMessage(mResources.getString(R.string.is_being_saved));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(false);
        progress.show();

        if(MenuFragment.NETWORK_ONLINE) {
            utils.configTimeout(5 * 1000);
            utils.configSoTimeout(5 * 1000);
            utils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    String result = responseInfo.result;
                    String errorCode = Utils.ParseJSON(result, "errorCode");
                    if (errorCode != null && errorCode.equals("0")) {
                        int lastInsertId = mDataBase.createOrUpdateScene(scene);
                        Log.d(TAG, "onSuccess: " + lastInsertId);
                        progress.dismiss();
                        mSaveScene.setVisibility(View.GONE);
                        isEditScene = false;
                        Toast.makeText(getActivity(), mResources.getString(R.string.successful_modification), Toast.LENGTH_SHORT).show();
                        initData();
                        if (position >= 0) {
                            mCurrentScene = mScenes.get(position);
                        }
                        mScenesListAdpter.changeDatas(mCurrentScene);
                        mScenesItemAdpter.notifyDataSetChanged();
                        mController.getScenes();
                    } else {
                        Toast.makeText(getActivity(), mResources.getString(R.string.failed_modification), Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Toast.makeText(getActivity(), mResources.getString(R.string.network_connection_failed), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
        } else {
            int lastInsertId = mDataBase.createOrUpdateScene(scene);
            progress.dismiss();
            mSaveScene.setVisibility(View.GONE);
            isEditScene = false;
            Toast.makeText(getActivity(), mResources.getString(R.string.successful_modification), Toast.LENGTH_SHORT).show();
            initData();
            if (position >= 0) {
                mCurrentScene = mScenes.get(position);
            }
            mScenesListAdpter.changeDatas(mCurrentScene);
            mScenesItemAdpter.notifyDataSetChanged();
            mController.getScenes();
        }

//        if (lastInsertId > 0) {
//            if(isEditScene){
//                CancelAlarmServiceBroadcastReciever(mCurrentScene.getSceneId());
//                CallAlarmServiceBroadcastReciever(alarm);
//                isEditScene = false;
//            }
//
//        }
    }

    /**
     * 批量删除场景
     * @param sceneIds
     */
    private void removeScenes(final TreeSet<Integer> sceneIds){

        final String url = Constans.SCENE_DELETE_URL;
        HttpUtils utils = new HttpUtils();

        Gson gson = new Gson();
        RequestParams params = new RequestParams();
        params.addBodyParameter("sceneId",gson.toJson(sceneIds));
        params.addBodyParameter("masterAppId",Utils.getMD5MacAddr(getActivity()));

        final ProgressDialog progress = new ProgressDialog(getActivity());
        progress.setMessage(mResources.getString(R.string.is_being_delete));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.setCancelable(true);
        progress.setCanceledOnTouchOutside(false);
        progress.show();


        Log.i("sence", "NETWORK_ONLINE = " + MenuFragment.NETWORK_ONLINE);
        if(MenuFragment.NETWORK_ONLINE) {

            utils.configTimeout(5 * 1000);
            utils.configSoTimeout(5 * 1000);
            utils.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    String result = responseInfo.result;
                    String errorCode = Utils.ParseJSON(result, "errorCode");
                    if (errorCode != null && errorCode.equals("0") && mDataBase.removeScenes(sceneIds)) {
                        //            //取消定时服务
                        //            if(alarmIds.size() != 0){
                        //                for(int alarmId : alarmIds){
                        //                    Log.d(TAG, "onClick: 取消场景" + alarmId);
                        //                    CancelAlarmServiceBroadcastReciever(alarmId);
                        //                }
                        //            }
                        Toast.makeText(getActivity(), mResources.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
                        mController.getScenes();
                        initData();
                        if (sceneIds.contains(mCurrentScene.getSceneId())) {
                            if (mScenes.size() > 0) {
                                mCurrentScene = mScenes.get(0);
                                mScenesListAdpter.changeDatas(mCurrentScene);
                            } else {
                                mRootView.findViewById(R.id.rv_no_scene).setVisibility(View.VISIBLE);
                            }
                        }
                        mScenesItemAdpter.notifyDataSetChanged();
                        progress.dismiss();
                    } else {
                        Toast.makeText(getActivity(), mResources.getString(R.string.modify_failed), Toast.LENGTH_SHORT).show();
                        progress.dismiss();
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    Toast.makeText(getActivity(), mResources.getString(R.string.network_connection_failed), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
        } else {
            Toast.makeText(getActivity(), mResources.getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
            Log.i("sence", "进入删除处理");
            mController.getScenes();
            initData();
            Log.i("sence", "initData已经结束！");
            Log.i("sence", "mCurrentScene = " + mCurrentScene.getSceneId());
            Log.i("sence", "sceneIds.contains(mCurrentScene.getSceneId()) = " + sceneIds.contains(mCurrentScene.getSceneId()));
            if (sceneIds.contains(mCurrentScene.getSceneId()) && mDataBase.removeScenes(sceneIds)) {
                if (mScenes.size() > 0) {
                    Log.i("sence", "mScenes.size() > 0");
                    // mCurrentScene = mScenes.get(0);
                    //   mScenesListAdpter.changeDatas(mCurrentScene);

                } else {

                    mRootView.findViewById(R.id.rv_no_scene).setVisibility(View.VISIBLE);
                }
            }


            Log.i("sence", "try{} catch(Exception){}");

            boolean isBreak = false;
            for(int i = 0; i<mScenes.size(); i++){
                if(mScenes.get(i).getSceneId() == mCurrentScene.getSceneId()) {
                    isBreak = true;
                    mScenes.remove(i);
                    if(mScenes.size() != 0) {
                        mCurrentScene = mScenes.get(0);
                    }
                    Log.i("sence", "mScenes1 = " + mScenes.size());
                    if(mScenes.size() == 0){
                        Log.i("sence", "mScenes2 = " + mScenes.size());
                        Log.i("sence", "显示未添加场景界面 mScenes= " + mScenes.size());
                        mRootView.findViewById(R.id.rv_no_scene).setVisibility(View.VISIBLE);
                    }
                    mScenesListAdpter.changeDatas(mCurrentScene);
                    break;
                }
                if(isBreak) break;
            }






            mScenesItemAdpter.notifyDataSetChanged();
            mScenesListAdpter.notifyDataSetChanged();
            //    Log.i("sence", "notifyDataSetChanged()");
            //      mScenesItemAdpter.notifyDataSetChanged();
            progress.dismiss();
        }

    }


    /**
     * 11468
     * 接口描述：与Activity通信
     */

    public interface CallbackActivity {

        void onCallbackActivity(boolean isTouch);

    }




}
