
/******************************************************************************
 * Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.csr.masterapp.entities.DeviceDes;
import com.csr.masterapp.entities.DeviceStream;
import com.csr.masterapp.entities.DeviceType;
import com.csr.masterapp.entities.GroupDevice;
import com.csr.masterapp.entities.Setting;
import com.csr.masterapp.entities.SingleDevice;
import com.csr.masterapp.entities.User;
import com.csr.masterapp.scene.util.SceneModel;
import com.csr.masterapp.scene.util.ScenesListModel;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class DataBaseDataSource {

    // Log Tag
    private String TAG = "DataBaseDataSource";

    // Database fields
    private SQLiteDatabase db;
    private MeshSQLHelper dbHelper;

    public DataBaseDataSource(Context context) {
        dbHelper = new MeshSQLHelper(context);
    }

    /**
     * Open the database
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    /**
     * Close the database
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Create a setting entry or update if it already exists.
     *
     * @param setting
     * @return
     */
    public Setting createSetting(Setting setting) {
        Log.d(TAG, "Creating or updating (if it already exists) setting values.");
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.SETTINGS_COLUMN_KEY, setting.getNetworkKey());
        values.put(MeshSQLHelper.SETTINGS_COLUMN_NEXT_DEVICE_INDEX, setting.getLastDeviceIndex());
        values.put(MeshSQLHelper.SETTINGS_COLUMN_NEXT_GROUP_INDEX, setting.getLastGroupIndex());
        values.put(MeshSQLHelper.SETTINGS_COLUMN_AUTH_REQUIRED, setting.isAuthRequired());
        values.put(MeshSQLHelper.SETTINGS_COLUMN_TTL, setting.getTTL());

        long id;
        if (setting.getId() != Setting.UKNOWN_ID) {
            values.put(MeshSQLHelper.SETTINGS_COLUMN_ID, setting.getId());
            id = db.replace(MeshSQLHelper.TABLE_SETTINGS, null, values);
        } else {
            id = db.insert(MeshSQLHelper.TABLE_SETTINGS, null, values);
        }

        close();

        if (id == -1) {
            // error, return null;
            return null;
        } else {
            setting.setId((int) id);
            return setting;
        }
    }

    /**
     * Get single setting entry by id.
     *
     * @param id
     * @return
     */
    public Setting getSetting(int id) {
        String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_SETTINGS
                + " WHERE " + MeshSQLHelper.SETTINGS_COLUMN_ID + " = " + id;

        open();
        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null && c.moveToFirst()) {
            Setting setting = new Setting();
            setting.setId(c.getInt(c
                    .getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_ID)));
            setting.setNetworkKey(c.getString(c
                    .getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_KEY)));
            setting.setLastDeviceIndex(c.getInt(c
                    .getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_NEXT_DEVICE_INDEX)));
            setting.setLastGroupIndex(c.getInt(c
                    .getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_NEXT_GROUP_INDEX)));
            setting.setAuthRequired((c.getInt(c
                    .getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_AUTH_REQUIRED)) > 0));
            setting.setTTL(c.getInt(c
                    .getColumnIndex(MeshSQLHelper.SETTINGS_COLUMN_TTL)));
            close();
            return setting;
        } else {
            close();
            return null;
        }
    }

    /**
     * Get the list of SingleDevices stored in the database.
     *
     * @return
     */
    public ArrayList<SingleDevice> getAllSingleDevices() {
        open();
        db.beginTransaction();
        ArrayList<SingleDevice> devices = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_DEVICES;
        Cursor devicesCursor = db.rawQuery(selectQuery, null);
        while (devicesCursor.moveToNext()) {
            SingleDevice device = new SingleDevice(
                    devicesCursor.getInt(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_ID)),
                    devicesCursor.getString(devicesCursor.
                            getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_UUID)),
                    devicesCursor.getInt(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_HASH)),
                    devicesCursor.getString(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_NAME)),
                    devicesCursor.getString(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_SHORTNAME)),
                    devicesCursor.getLong(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_LOW)),
                    devicesCursor.getLong(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_HIGH)));
            device.setMinimumSupportedGroups(devicesCursor.getInt(devicesCursor
                    .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_GROUPS_SUPPORTED)));

            String query = "SELECT " + MeshSQLHelper.MODELS_COLUMN_GROUP_ID
                    + " FROM " + MeshSQLHelper.TABLE_MODELS + " WHERE "
                    + MeshSQLHelper.MODELS_COLUMN_DEVICE_ID + " ='"
                    + device.getDeviceId() + "'";
            Cursor groupsCursor = db.rawQuery(query, null);

            for (int i = 0; groupsCursor.moveToNext(); i++) {
                device.setGroupId(i, groupsCursor.getInt(groupsCursor
                        .getColumnIndex(MeshSQLHelper.MODELS_COLUMN_GROUP_ID)));
            }
            devices.add(device);
        }
        db.endTransaction();
        close();

        return devices;
    }

    /**
     * Get the list of SingleDevices stored in the database.
     *
     * @return
     */
    public Map<Integer ,SingleDevice> getAllDevicesMap() {
        open();
        HashMap<Integer ,SingleDevice> devices = new HashMap<>();
        String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_DEVICES;
        Cursor devicesCursor = db.rawQuery(selectQuery, null);
        int deviceId;
        while (devicesCursor.moveToNext()) {
            SingleDevice device = new SingleDevice(
                    deviceId = devicesCursor.getInt(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_ID)),
                    devicesCursor.getString(devicesCursor.
                            getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_UUID)),
                    devicesCursor.getInt(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_HASH)),
                    devicesCursor.getString(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_NAME)),
                    devicesCursor.getString(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_SHORTNAME)),
                    devicesCursor.getLong(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_LOW)),
                    devicesCursor.getLong(devicesCursor
                            .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_HIGH)));
            device.setMinimumSupportedGroups(devicesCursor.getInt(devicesCursor
                    .getColumnIndex(MeshSQLHelper.DEVICES_COLUMN_GROUPS_SUPPORTED)));

            devices.put(deviceId, device);
        }
        close();

        return devices;
    }

    /**
     * Get the list of SingleDevices stored in the database.
     *
     * @return
     */
    public ArrayList<DeviceType> getAllDeviceType() {
        open();
        ArrayList<DeviceType> types = new ArrayList<DeviceType>();
        String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_TYPES;
        Cursor typesCursor = db.rawQuery(selectQuery, null);
        while (typesCursor.moveToNext()) {
            DeviceType type = new DeviceType(
                    typesCursor.getString(typesCursor.getColumnIndex(MeshSQLHelper.TYPE_COLUMN_NAME)),
                    typesCursor.getString(typesCursor.getColumnIndex(MeshSQLHelper.TYPE_COLUMB_VERSION)));
            type.setId(typesCursor.getInt(typesCursor.getColumnIndex(MeshSQLHelper.TYPE_COLUMN_ID)));
            types.add(type);
        }
        close();

        return types;
    }

    public DeviceType createOrUpdateType(DeviceType type) {
        open();
        ContentValues values = new ContentValues();
        if(type.getId() != null){
            Log.d(TAG, "onSuccess createOrUpdateType: " + type.getId());
            values.put(MeshSQLHelper.TYPE_COLUMN_ID, type.getId());
        }
        Log.d(TAG, "onSuccess createOrUpdateType: null" + type.getId());
        values.put(MeshSQLHelper.TYPE_COLUMN_NAME, type.getShortname());
        values.put(MeshSQLHelper.TYPE_COLUMB_VERSION, type.getVersion());

        long id = db.replace(MeshSQLHelper.TABLE_TYPES, null, values);
        close();

        if (id == -1) {
            // error, return null;
            return null;
        } else {
            return type;
        }
    }

    /**
     * Get the list of Users in the database.
     *
     * @return
     */
    public ArrayList<User> getAllUsers() {
        open();
        ArrayList<User> users = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_USERS;
        Cursor usersCursor = db.rawQuery(selectQuery, null);
        while (usersCursor.moveToNext()) {
            User user = new User(
                    usersCursor.getString(usersCursor.getColumnIndex(MeshSQLHelper.USERS_COLUMN_USERNAME)),
                    usersCursor.getString(usersCursor.getColumnIndex(MeshSQLHelper.USERS_COLUMN_PHONE)),
                    usersCursor.getString(usersCursor.getColumnIndex(MeshSQLHelper.USERS_COLUMN_PASSWORD)),
                    usersCursor.getString(usersCursor.getColumnIndex(MeshSQLHelper.USERS_COLUMN_REGISTER_TIME)));
            user.setUserId(usersCursor.getInt(usersCursor.getColumnIndex(MeshSQLHelper.USERS_COLUMN_ID)));
            users.add(user);
        }
        close();
        return users;
    }

    /**
     * Get the list of SingleDevices stored in the database.
     *
     * @return
     */
    public ArrayList<GroupDevice> getAllGroupDevices() {
        open();
        ArrayList<GroupDevice> groups = new ArrayList<GroupDevice>();
        String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_GROUPS;
        Cursor groupsCursor = db.rawQuery(selectQuery, null);
        while (groupsCursor.moveToNext()) {
            GroupDevice group = new GroupDevice(
                    groupsCursor.getInt(groupsCursor.getColumnIndex(MeshSQLHelper.GROUPS_COLUMN_ID)),
                    groupsCursor.getString(groupsCursor.getColumnIndex(MeshSQLHelper.GROUPS_COLUMN_NAME)));
            groups.add(group);
        }
        close();
        return groups;
    }

    /**
     * Get the list of scenes in the database.
     *
     * @return
     */
    public ArrayList<SceneModel> getAllSecnes() {
        open();
        ArrayList<SceneModel> Scenes = new ArrayList<SceneModel>();

        String scenesQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_SCENES;
        Cursor scenesCursor = db.rawQuery(scenesQuery, null);

        while (scenesCursor.moveToNext()) {

            Gson gson = new Gson();
            String json = scenesCursor.getString(scenesCursor.getColumnIndex(MeshSQLHelper.SCENES_COLUMN_JSONINFO));
            ScenesListModel bean = gson.fromJson(json, ScenesListModel.class);
            //scenelist
            SceneModel scene = new SceneModel(
                    scenesCursor.getString(scenesCursor.getColumnIndex(MeshSQLHelper.SCENES_COLUMN_NAME)),
                    scenesCursor.getInt(scenesCursor.getColumnIndex(MeshSQLHelper.SCENES_COLUMN_STATUS)),
                    scenesCursor.getInt(scenesCursor.getColumnIndex(MeshSQLHelper.SCENES_COLUMN_MODE)),
                    scenesCursor.getInt(scenesCursor.getColumnIndex(MeshSQLHelper.SCENES_COLUMN_IS_SEND)),
                    bean.conditions, bean.tasks);
            int sceneId = scenesCursor.getInt(scenesCursor.getColumnIndex(MeshSQLHelper.SCENES_COLUMN_ID));
            if (scene != null) {
                scene.setSceneId(sceneId);
            }
            Scenes.add(scene);
        }

        close();
        return Scenes;
    }

    /**
     * Get the list of streams in the database.
     *
     * @return
     */
    public ArrayList<DeviceStream> getDeviceStream() {
        open();

        ArrayList<DeviceStream> streams = new ArrayList<DeviceStream>();
        String selectQuery = "SELECT  * FROM " + MeshSQLHelper.TABLE_DEVICE_STREAM;
        Cursor streamsCursor = db.rawQuery(selectQuery, null);
        while (streamsCursor.moveToNext()) {

            int streamId = streamsCursor.getInt(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_STREAM_ID));
            int data_type = streamsCursor.getInt(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_DATA_TYPE));

            DeviceStream stream = new DeviceStream(
                    streamId,
                    streamsCursor.getString(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_STREAM_DESCRIPTION)),
                    streamsCursor.getString(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_STREAM_NAME)),
                    streamsCursor.getString(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_DEVICE_SHORTNAME)),
                    streamsCursor.getInt(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_TYPE)),
                    data_type
            );

            switch (data_type) {
                case 0:
                    //枚举
                    String desquery = "SELECT  * FROM " + MeshSQLHelper.TABLE_DEVICE_DES + " WHERE "
                            + MeshSQLHelper.DEVICE_DES_COLUMN_STREAM_ID + " ='" + streamId + "'";
                    Cursor desCusor = db.rawQuery(desquery, null);

                    ArrayList<DeviceDes> Deslist = new ArrayList<DeviceDes>();
                    while (desCusor.moveToNext()) {
                        DeviceDes temp = new DeviceDes(
                                desCusor.getInt(desCusor.getColumnIndex(MeshSQLHelper.DEVICE_DES_COLUMN_ID)),
                                desCusor.getInt(desCusor.getColumnIndex(MeshSQLHelper.DEVICE_DES_COLUMN_STREAM_ID)),
                                desCusor.getString(desCusor.getColumnIndex(MeshSQLHelper.DEVICE_DES_COLUMN_KEY)),
                                desCusor.getInt(desCusor.getColumnIndex(MeshSQLHelper.DEVICE_DES_COLUMN_VALUE)),
                                desCusor.getString(desCusor.getColumnIndex(MeshSQLHelper.DEVICE_DES_COLUMN_COMPARISON_POT))
                        );
                        Deslist.add(temp);
                    }
                    stream.setManu_set(Deslist);
                    break;
                case 1:
                    //数值
                    stream.setMin_value(streamsCursor.getInt(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_MIN_VALUE)));
                    stream.setMax_value(streamsCursor.getInt(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_MAX_VALUE)));
                    stream.setIncrement(streamsCursor.getInt(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_INCREMENT)));
                    stream.setUnit(streamsCursor.getString(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_UNIT)));
                    stream.setUnit_symbol(streamsCursor.getString(streamsCursor.getColumnIndex(MeshSQLHelper.DEVICE_STREAM_COLUMN_UNIT_SYMBOL)));
                    break;
                default:
                    break;
            }
            streams.add(stream);
        }
        close();
        return streams;
    }

    /**
     * Create a User or update if it already exists in the database.
     *
     * @param user
     * @return
     */
    public User createOrUpdateUser(User user) {
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.USERS_COLUMN_USERNAME, user.getUserName());
        values.put(MeshSQLHelper.USERS_COLUMN_PHONE, user.getPhone());
        values.put(MeshSQLHelper.USERS_COLUMN_PASSWORD, user.getPassword());
        values.put(MeshSQLHelper.USERS_COLUMN_REGISTER_TIME, user.getRegisterTime());

        // insert row and close db
        long id = db.replace(MeshSQLHelper.TABLE_USERS, null, values);
        Log.d(TAG, "createOrUpdateGroup: " + id);

        Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + MeshSQLHelper.TABLE_USERS ,null);
        int last_insert_id = -1;
        if(cursor.moveToLast()){
            last_insert_id = cursor.getInt(0);
        }
        close();
        user.setUserId(last_insert_id);
        Log.d(TAG, "createOrUpdateGroup: " + last_insert_id);
        return user;
    }

    /**
     * Create a GroupDevice or update if it already exists in the database.
     *
     * @param group
     * @param settingsID
     * @return
     */
    public GroupDevice createOrUpdateGroup(GroupDevice group, int settingsID) {
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.GROUPS_COLUMN_ID, group.getDeviceId());
        values.put(MeshSQLHelper.GROUPS_COLUMN_NAME, group.getName());
        values.put(MeshSQLHelper.GROUPS_COLUMN_SETTINGS_ID, settingsID);

        // insert row and close db
        long id = db.replace(MeshSQLHelper.TABLE_GROUPS, null, values);
        close();

        if (id == -1) {
            // error, return null;
            return null;
        } else {
            group.setDeviceId((int) id);
            return group;
        }
    }

    /**
     * Create a SingleDevice or update if it already exists in the database.
     *
     * @param device
     * @param settingsID
     * @return
     */
    public boolean createOrUpdateSingleDevice(SingleDevice device, int settingsID) {
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.DEVICES_COLUMN_ID, device.getDeviceId());
        values.put(MeshSQLHelper.DEVICES_COLUMN_UUID, device.getUuid());
        values.put(MeshSQLHelper.DEVICES_COLUMN_HASH, device.getUuidHash());
        values.put(MeshSQLHelper.DEVICES_COLUMN_NAME, device.getName());
        values.put(MeshSQLHelper.DEVICES_COLUMN_SHORTNAME, device.getShortName());
        values.put(MeshSQLHelper.DEVICES_COLUMN_GROUPS_SUPPORTED,
                device.getMinimumSupportedGroups());
        values.put(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_LOW,
                device.getModelSupportBitmapLow());
        values.put(MeshSQLHelper.DEVICES_COLUMN_MODELSUPPORT_HIGH,
                device.getModelSupportBitmapHigh());
        values.put(MeshSQLHelper.DEVICES_COLUMN_SETTINGS_ID, settingsID);

        // insert row and close db
        long id = db.replace(MeshSQLHelper.TABLE_DEVICES, null, values);
        close();

        if (id == -1) {
            // error, return null;
            close();
            return false;
        } else {
            removeAllModels(device.getDeviceId());
            for (int i = 0; i < device.getGroupMembership().size(); i++) {
                createOrUpdateModel(device.getDeviceId(), device
                        .getGroupMembership().get(i).intValue());
            }
            close();
            return true;
        }
    }

    /**
     * Update the device name of an existing device of the data base.
     *
     * @param deviceId
     * @param name
     */
    public void updateDeviceName(int deviceId, String name) {
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.DEVICES_COLUMN_NAME, name);

        db.update(MeshSQLHelper.TABLE_DEVICES, values,
                MeshSQLHelper.DEVICES_COLUMN_ID + "=" + deviceId, null);
        close();

    }

    /**
     * Update the group name of an existing device of the group base.
     *
     * @param deviceId
     * @param name
     */
    public void updateGroupName(int deviceId, String name) {
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.DEVICES_COLUMN_NAME, name);

        db.update(MeshSQLHelper.TABLE_GROUPS, values,
                MeshSQLHelper.DEVICES_COLUMN_ID + "=" + deviceId, null);
        close();

    }


    /**
     * Remove all the models that a device has.
     *
     * @param deviceID
     */
    public void removeAllModels(int deviceID) {
        open();
        db.delete(MeshSQLHelper.TABLE_MODELS,
                MeshSQLHelper.MODELS_COLUMN_DEVICE_ID + "=" + deviceID, null);
        close();
    }

    /**
     * 创建场景
     */
    public int createOrUpdateScene(SceneModel scene) {
        open();
        ContentValues values = new ContentValues();
        if(scene.getSceneId() != null){
            values.put(MeshSQLHelper.SCENES_COLUMN_ID, scene.getSceneId());
        }
        if(scene.getAlarm_time() != null){
            values.put(MeshSQLHelper.COLUMN_ALARM_TIME, scene.getAlarm_time());
        }
        if(scene.getAlarm_days() != null){
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = null;
                oos = new ObjectOutputStream(bos);
                oos.writeObject(scene.getAlarm_days());
                byte[] buff = bos.toByteArray();

                values.put(MeshSQLHelper.COLUMN_ALARM_DAYS, buff);

            } catch (Exception e) {
            }

            values.put(MeshSQLHelper.SCENES_COLUMN_ID, scene.getSceneId());
        }
        values.put(MeshSQLHelper.SCENES_COLUMN_NAME, scene.getName());
        values.put(MeshSQLHelper.SCENES_COLUMN_IMAGES, scene.getImages());
        values.put(MeshSQLHelper.SCENES_COLUMN_STATUS, scene.getStatus());
        values.put(MeshSQLHelper.SCENES_COLUMN_MODE, scene.getMode());
        values.put(MeshSQLHelper.SCENES_COLUMN_IS_SEND, scene.getIsSend());

        String json = new Gson().toJson(new ScenesListModel(scene.getConditions(), scene.getTasks()));
        values.put(MeshSQLHelper.SCENES_COLUMN_JSONINFO, json);

        // insert row and close db
        db.replace(MeshSQLHelper.TABLE_SCENES, null, values);

        Cursor cursor = db.rawQuery("SELECT last_insert_rowid() FROM " + MeshSQLHelper.TABLE_SCENES ,null);
        int last_insert_id = -1;
        if(cursor.moveToLast()){
            last_insert_id = cursor.getInt(0);
        }
        close();
        return last_insert_id;
    }

    /**
     * 更新场景状态.
     *
     * @param sceneId 场景ID
     * @param status  状态值 0/1
     */
    public void updateSceneStatus(int sceneId, int status) {
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.SCENES_COLUMN_STATUS, status);
        db.update(MeshSQLHelper.TABLE_SCENES, values,
                MeshSQLHelper.SCENES_COLUMN_ID + "=" + sceneId, null);
        close();

    }

    /**
     * 批量删除场景
     */
    public Boolean removeScenes(TreeSet<Integer> sceneIds) {
        open();
        for (int sceneid : sceneIds) {
            db.delete(MeshSQLHelper.TABLE_SCENES, MeshSQLHelper.SCENES_COLUMN_ID + "=" + sceneid, null);
        }
        close();
        return sceneIds.size() > 0;
    }

    /**
     * 删除单个场景
     */
    public Boolean removeSingleScene(int sceneId) {
        open();
        int deleteRow = db.delete(MeshSQLHelper.TABLE_SCENES, MeshSQLHelper.SCENES_COLUMN_ID + "=" + sceneId, null);
        close();
        return deleteRow == 1;
    }

    /**
     * This method deletes the content of the settings', groups', devices' and models' tables.
     */
    public void cleanDatabase() {
        open();
        db.delete(MeshSQLHelper.TABLE_SETTINGS, null, null);
        db.delete(MeshSQLHelper.TABLE_GROUPS, null, null);
        db.delete(MeshSQLHelper.TABLE_DEVICES, null, null);
        db.delete(MeshSQLHelper.TABLE_MODELS, null, null);
        close();
    }

    /**
     * Create a model or update if it already exists in the database.
     *
     * @param deviceID
     * @param groupID
     */
    public void createOrUpdateModel(int deviceID, int groupID) {
        open();
        ContentValues values = new ContentValues();
        values.put(MeshSQLHelper.MODELS_COLUMN_DEVICE_ID, deviceID);
        values.put(MeshSQLHelper.MODELS_COLUMN_GROUP_ID, groupID);

        // insert row and close db
        long id = db.replace(MeshSQLHelper.TABLE_MODELS, null, values);

        close();
    }

    /**
     * Remove a SingleDevice from the database by device id.
     *
     * @param deviceId
     */
    public void removeSingleDevice(int deviceId) {
        open();
        db.delete(MeshSQLHelper.TABLE_DEVICES, MeshSQLHelper.DEVICES_COLUMN_ID
                + "=" + deviceId, null);
        close();
    }

    /**
     * Remove a GroupDevice from the database by device id.
     *
     * @param groupId
     */
    public void removeGroup(int groupId) {
        open();
        db.delete(MeshSQLHelper.TABLE_GROUPS, MeshSQLHelper.GROUPS_COLUMN_ID
                + "=" + groupId, null);
        close();
    }

}
