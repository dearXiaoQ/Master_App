package com.csr.masterapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * 项目名称：MasterApp
 * 类描述：
 * 创建人：11177
 * 创建时间：2016/6/17 17:04
 * 修改人：11177
 * 修改时间：2016/6/17 17:04
 * 修改备注：
 */
public class CacheUtils
{
    private final static String SP_NAME = "masterapp";
    private static SharedPreferences mPreferences;

    private static SharedPreferences getSp(Context context)
    {
        if (mPreferences == null)
        {
            mPreferences = context.getSharedPreferences(SP_NAME, context.MODE_PRIVATE);
        }

        return mPreferences;
    }

    /**
     * 通过SP获得boolean类型的数据，没有默认为false
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static boolean getBoolean(Context context, String key){
        SharedPreferences sp = getSp(context);
        return sp.getBoolean(key, false);
    }

    /**
     * 通过SP获得boolean类型的数据，没有默认为false
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static boolean getBoolean(Context context, String key, boolean defValue){
        SharedPreferences sp = getSp(context);
        return sp.getBoolean(key, defValue);
    }

    /**
     * 设置boolean的缓存数据
     *
     * @param context
     * @param key
     * 			：缓存对应的key
     * @param defValue
     * 			：缓存对应的值
     * @return
     */


    public static void setBoolean(Context context, String key, boolean defValue){
        SharedPreferences sp = getSp(context);
        SharedPreferences.Editor edit = sp.edit();//获得编辑器
        edit.putBoolean(key, defValue);
        edit.commit();
    }

    /**
     * 通过SP获得String类型的数据，没有默认为null
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static String getString(Context context, String key){
        SharedPreferences sp = getSp(context);
        return sp.getString(key, null);
    }

    /**
     * 通过SP获得String类型的数据，没有默认为null
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static String getString(Context context, String key, String defValue){
        SharedPreferences sp = getSp(context);
        return sp.getString(key, defValue);
    }

    /**
     * 设置String的缓存数据
     *
     * @param context
     * @param key
     * 			：缓存对应的key
     * @param defValue
     * 			：缓存对应的值
     * @return
     */


    public static void setString(Context context, String key, String defValue){
        SharedPreferences sp = getSp(context);
        Editor edit = sp.edit();//获得编辑器
        edit.putString(key, defValue);
        edit.commit();
    }


    /**
     * 通过SP获得Long类型的数据，没有默认为-1
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static Long getLong(Context context, String key){
        SharedPreferences sp = getSp(context);
        return sp.getLong(key, -1);
    }

    /**
     * 通过SP获得Long类型的数据，没有默认为-1
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static Long getLong(Context context, String key, long defValue){
        SharedPreferences sp = getSp(context);
        return sp.getLong(key, defValue);
    }

    /**
     * 设置Long的缓存数据
     *
     * @param context
     * @param key
     * 			：缓存对应的key
     * @param defValue
     * 			：缓存对应的值
     * @return
     */


    public static void setLong(Context context, String key, long defValue){
        SharedPreferences sp = getSp(context);
        Editor edit = sp.edit();//获得编辑器
        edit.putLong(key, defValue);
        edit.commit();
    }

    /**
     * 通过SP获得Int类型的数据，没有默认为-1
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static int getInt(Context context, String key){
        SharedPreferences sp = getSp(context);
        return sp.getInt(key, -1);
    }

    /**
     * 通过SP获得Int类型的数据，没有默认为-1
     *
     * @param context
     * 			：上下文
     * @param key
     * 			：存储的key
     * @return
     */
    public static int getInt(Context context, String key, int defValue){
        SharedPreferences sp = getSp(context);
        return sp.getInt(key, defValue);
    }

    /**
     * 设置Int的缓存数据
     *
     * @param context
     * @param key
     * 			：缓存对应的key
     * @param defValue
     * 			：缓存对应的值
     * @return
     */


    public static void setInt(Context context, String key, int defValue){
        SharedPreferences sp = getSp(context);
        Editor edit = sp.edit();//获得编辑器
        edit.putInt(key, defValue);
        edit.commit();
    }
}
