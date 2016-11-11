
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.database;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.csr.masterapp.R;
import com.csr.masterapp.WelcomeUI;

import java.io.File;

/**
 * 项目名称：MasterApp v3
 * 类描述：数据库操作类
 * 创建人：11177
 * 创建时间：2016/7/5 12:00
 * 修改人：11177
 * 修改时间：2016/7/5 12:00
 * 修改备注：MeshSQLHelper class contains all the methods to perform database operations like opening connection,
 * closing connection, insert, update, read, delete and other things.
 */

public class MeshSQLHelper extends SQLiteOpenHelper {

	  // Logcat tag
      private static final String TAG = "MeshSQLHelper";
      
      // Database version and name
      private static final int DATABASE_VERSION = 1;
      private static final String DATABASE_NAME = "mesh.db";
      
      // Database 数据库表 table names
      static final String TABLE_SETTINGS = "settings";
	  static final String TABLE_USERS = "users";
      static final String TABLE_DEVICES = "devices";
	  static final String TABLE_TYPES = "types";
      static final String TABLE_GROUPS = "groups";
      static final String TABLE_MODELS = "models";
	  static final String TABLE_SCENES = "scenes";
	  static final String TABLE_DEVICE_STREAM = "device_stream";
	  static final String TABLE_DEVICE_DES = "device_des";

	  // Users Table 用户表  - Columns names
	  public static final String USERS_COLUMN_ID = "id";//用户id
	  public static final String USERS_COLUMN_USERNAME = "username";//用户名称
	  public static final String USERS_COLUMN_PHONE = "phone";//用户手机
	  public static final String USERS_COLUMN_PASSWORD = "password";//用户密码
	  public static final String USERS_COLUMN_REGISTER_TIME = "register_time";//注册时间
      
      // Settings Table 设置表 - Columns names
      public static final String SETTINGS_COLUMN_ID = "id";
	  public static final String SETTINGS_COLUMN_KEY = "networkKey";
	  public static final String SETTINGS_COLUMN_AUTH_REQUIRED = "authRequired";
	  public static final String SETTINGS_COLUMN_NEXT_DEVICE_INDEX = "nextDeviceIndex";
	  public static final String SETTINGS_COLUMN_NEXT_GROUP_INDEX = "nextGroupIndex";
	  public static final String SETTINGS_COLUMN_TTL = "ttl";
	  
	  // Devices Table 设备表 - Columns names
	  public static final String DEVICES_COLUMN_ID = "id";
	  public static final String DEVICES_COLUMN_UUID = "uuid";
	  public static final String DEVICES_COLUMN_HASH = "hash";
	  public static final String DEVICES_COLUMN_NAME = "name";
	  public static final String DEVICES_COLUMN_SHORTNAME = "shortname";
	  public static final String DEVICES_COLUMN_GROUPS_SUPPORTED = "groupsSupported";
	  public static final String DEVICES_COLUMN_MODELSUPPORT_LOW = "modelSupportL";
	  public static final String DEVICES_COLUMN_MODELSUPPORT_HIGH = "modelSupportH";
	  public static final String DEVICES_COLUMN_SETTINGS_ID = "settingsID";

	  // Types Table 设备类型表 - Columns names
	  public static final String TYPE_COLUMN_ID = "id";
	  public static final String TYPE_COLUMN_NAME = "shortname";
	  public static final String TYPE_COLUMB_VERSION = "version";
	  
	  // Groups Table 设备组表 - Columns names
	  public static final String GROUPS_COLUMN_ID = "id";
	  public static final String GROUPS_COLUMN_NAME = "name";
	  public static final String GROUPS_COLUMN_SETTINGS_ID = "settingsID";
	  
	  // Models Table - Columns names
	  public static final String MODELS_COLUMN_DEVICE_ID = "deviceID";
	  public static final String MODELS_COLUMN_GROUP_ID = "groupID";

	  // scenes Table 场景列表 - Columns names
	  public static final String SCENES_COLUMN_ID = "id";
	  public static final String SCENES_COLUMN_NAME = "name";
	  public static final String SCENES_COLUMN_IMAGES = "images";
	  public static final String SCENES_COLUMN_STATUS = "status";
	  public static final String SCENES_COLUMN_MODE = "mode";//启动模式  1:任一条件 2:所有条件 3:点击启动 4:定时启动
	  public static final String COLUMN_ALARM_TIME = "alarm_time";//时间
	  public static final String COLUMN_ALARM_DAYS = "alarm_days";//天
	  public static final String SCENES_COLUMN_IS_SEND = "isSend";
	  public static final String SCENES_COLUMN_JSONINFO = "json";

	  // device_stream Table 设备参数列表 - Columns names
	  public static final String DEVICE_STREAM_COLUMN_STREAM_ID = "stream_id";
	  public static final String DEVICE_STREAM_COLUMN_STREAM_NAME = "stream_name";//由字母、数字或下划线组成，不能重复
	  public static final String DEVICE_STREAM_COLUMN_STREAM_DESCRIPTION = "stream_description";//中文名称
	  public static final String DEVICE_STREAM_COLUMN_DEVICE_SHORTNAME = "short_name";
	  public static final String DEVICE_STREAM_COLUMN_TYPE = "type";//是否可控  0:否  1:是
	  public static final String DEVICE_STREAM_COLUMN_DATA_TYPE = "data_type";//数据类型 0:枚举  1:数值
	  public static final String DEVICE_STREAM_COLUMN_MIN_VALUE = "min_value";//最小值
	  public static final String DEVICE_STREAM_COLUMN_MAX_VALUE = "max_value";//最大值
	  public static final String DEVICE_STREAM_COLUMN_INCREMENT = "increment";//单位增量
	  public static final String DEVICE_STREAM_COLUMN_UNIT = "unit";//单位
	  public static final String DEVICE_STREAM_COLUMN_UNIT_SYMBOL = "unit_symbol";//单位标志

	  // device_des Table 设备参数设置项列表 - Columns names
	  public static final String DEVICE_DES_COLUMN_ID = "id";
	  public static final String DEVICE_DES_COLUMN_STREAM_ID = "stream_id";
	  public static final String DEVICE_DES_COLUMN_KEY = "key";
	  public static final String DEVICE_DES_COLUMN_VALUE = "value";
	  public static final String DEVICE_DES_COLUMN_COMPARISON_POT  = "comparison_opt";

	  // Settings table create statement
	  private static final String CREATE_TABLE_SETTINGS = "CREATE TABLE " + TABLE_SETTINGS + 
				 "(" + SETTINGS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + SETTINGS_COLUMN_AUTH_REQUIRED +" BOOLEAN,"+ SETTINGS_COLUMN_KEY + " TEXT," +
				 SETTINGS_COLUMN_NEXT_DEVICE_INDEX + " INTEGER," + SETTINGS_COLUMN_NEXT_GROUP_INDEX + " INTEGER," + SETTINGS_COLUMN_TTL + " INTEGER" + ")";

	  // Users table create statement
	  private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS +
				"(" + USERS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + USERS_COLUMN_USERNAME +" TEXT,"+ USERS_COLUMN_PHONE + " TEXT," +
			  USERS_COLUMN_PASSWORD + " TEXT," + USERS_COLUMN_REGISTER_TIME + " TEXT" + ")";


	// Devices table create statement
	  private static final String CREATE_TABLE_DEVICES = "CREATE TABLE " + TABLE_DEVICES + 
			 "(" + DEVICES_COLUMN_ID + " INTEGER PRIMARY KEY," +  DEVICES_COLUMN_UUID + " INTEGER," + DEVICES_COLUMN_HASH
	            + " INTEGER," + DEVICES_COLUMN_NAME + " TEXT," + DEVICES_COLUMN_SHORTNAME + " TEXT," + DEVICES_COLUMN_GROUPS_SUPPORTED + " INTEGER," + DEVICES_COLUMN_SETTINGS_ID + " INTEGER," + DEVICES_COLUMN_MODELSUPPORT_LOW + " INTEGER," + DEVICES_COLUMN_MODELSUPPORT_HIGH + " INTEGER" + ")";

	  // Type table create statement
	  private static final String CREATE_TABLE_TYPES = "CREATE TABLE " + TABLE_TYPES +
			"(" + TYPE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +  TYPE_COLUMN_NAME + " TEXT," +  TYPE_COLUMB_VERSION + " TEXT" + ")";


	  // Models table create statement
	  private static final String CREATE_TABLE_MODELS = "CREATE TABLE " + TABLE_MODELS + 
				 "(" + MODELS_COLUMN_DEVICE_ID + " INTEGER NOT NULL," + MODELS_COLUMN_GROUP_ID + " INTEGER NOT NULL," + "PRIMARY KEY (" + MODELS_COLUMN_DEVICE_ID + "," + MODELS_COLUMN_GROUP_ID + ")" + ")";
		  
	  // Groups table create statement
	  private static final String CREATE_TABLE_GROUPS = "CREATE TABLE " + TABLE_GROUPS + 
				 "(" + GROUPS_COLUMN_ID + " INTEGER PRIMARY KEY," + GROUPS_COLUMN_NAME  + " TEXT," +  GROUPS_COLUMN_SETTINGS_ID + " INTEGER" + ")";

	  // Scenes table create statement
	  private static final String CREATE_TABLE_SCENES = "CREATE TABLE " + TABLE_SCENES +
				"(" + SCENES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + SCENES_COLUMN_NAME  + " TEXT," +  SCENES_COLUMN_IMAGES + " TEXT," + SCENES_COLUMN_STATUS + " INTEGER," + SCENES_COLUMN_MODE + " INTEGER," + COLUMN_ALARM_TIME + " TEXT," + COLUMN_ALARM_DAYS + " BLOB," + SCENES_COLUMN_IS_SEND + " INTEGER,"  + SCENES_COLUMN_JSONINFO + " TEXT" + ")";

	  // device_stream Table - Columns names
	  private static final String CREATE_TABLE_DEVICE_STREAM = "CREATE TABLE " + TABLE_DEVICE_STREAM +
			"(" + DEVICE_STREAM_COLUMN_STREAM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + DEVICE_STREAM_COLUMN_STREAM_NAME + " TEXT," + DEVICE_STREAM_COLUMN_STREAM_DESCRIPTION + " TEXT," + DEVICE_STREAM_COLUMN_DEVICE_SHORTNAME + " TEXT," + DEVICE_STREAM_COLUMN_TYPE  + " INTEGER," + DEVICE_STREAM_COLUMN_DATA_TYPE + " INTEGER," + DEVICE_STREAM_COLUMN_MIN_VALUE + " INTEGER," + DEVICE_STREAM_COLUMN_MAX_VALUE  + " INTEGER," + DEVICE_STREAM_COLUMN_INCREMENT  + " INTEGER,"+ DEVICE_STREAM_COLUMN_UNIT + " TEXT," +DEVICE_STREAM_COLUMN_UNIT_SYMBOL + " TEXT" + ")";

	  // device_des Table - Columns names
	  private static final String CREATE_TABLE_DEVICE_DES = "CREATE TABLE " + TABLE_DEVICE_DES +
			"(" + DEVICE_DES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + DEVICE_DES_COLUMN_STREAM_ID + " INTEGER," + DEVICE_DES_COLUMN_KEY + " TEXT," + DEVICE_DES_COLUMN_VALUE  + " TEXT," + DEVICE_DES_COLUMN_COMPARISON_POT   + " TEXT" + ")";


	  public MeshSQLHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	@Override
	public void onCreate(SQLiteDatabase database) {

		Log.w(TAG, "Creating database...");
		String switchStr = WelcomeUI.GLOBAL_CONTEXT.getResources().getString(R.string.switch_);
		String windSpeed = WelcomeUI.GLOBAL_CONTEXT.getResources().getString(R.string.wind_speed);
		String lighting  = WelcomeUI.GLOBAL_CONTEXT.getResources().getString(R.string.lighting);
		// creating required tables
		database.execSQL(CREATE_TABLE_SETTINGS);
		database.execSQL(CREATE_TABLE_USERS);
		database.execSQL(CREATE_TABLE_DEVICES);
		database.execSQL(CREATE_TABLE_TYPES);
		database.execSQL(CREATE_TABLE_MODELS);
		database.execSQL(CREATE_TABLE_GROUPS);
		database.execSQL(CREATE_TABLE_SCENES);
		database.execSQL(CREATE_TABLE_DEVICE_STREAM);
		database.execSQL(CREATE_TABLE_DEVICE_DES);
		database.execSQL("INSERT INTO device_stream (stream_id, stream_name, stream_description, short_name, type, data_type) " + "VALUES ('1', 'power', '" + switchStr + "', 'Light', 1, 0);");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('1', 'open', '1');");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('1', 'close', '0');");
		database.execSQL("INSERT INTO device_stream (stream_id, stream_name, stream_description, short_name, type, data_type) " + "VALUES ('2', 'power', '" + switchStr +  "', 'RHood', 1, 0);");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('2', 'open', '1');");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('2', 'close', '0');");
		database.execSQL("INSERT INTO device_stream (stream_id, stream_name, stream_description, short_name, type, data_type) " + "VALUES ('3', 'mark', '" + windSpeed + "', 'RHood', 1, 0);");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('3', 'fast wind', '1');");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('3', 'slow wind', '0');");
		database.execSQL("INSERT INTO device_stream (stream_id, stream_name, stream_description, short_name, type, data_type) " + "VALUES ('4', 'light', '" + lighting + "', 'RHood', 1, 0);");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('4', 'open light', '1');");
		database.execSQL("INSERT INTO device_des (stream_id, key, value) " + "VALUES ('4', 'close light', '0');");

		database.execSQL("INSERT INTO device_stream (stream_id, stream_name, stream_description, short_name, type, data_type, min_value, max_value, increment, unit, unit_symbol) " + "VALUES ('5', 'temperature', 'Temperature sensor', 'Ecookpan', '0', '1', '1', '200', '2', '℃', '℃');");
}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");

		// on upgrade drop older tables
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_TYPES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODELS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUPS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCENES);
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_DEVICE_STREAM);
		db.execSQL("DROP TABLE IF EXISTS " + CREATE_TABLE_DEVICE_DES);
		File file = new File("");

		// create new tables
		onCreate(db);
	}

	} 
