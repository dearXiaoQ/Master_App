package com.csr.masterapp.utils;

import android.content.Context;
import android.content.res.Resources;

import com.csr.masterapp.MainActivity;

/**
 * 项目名称：MasterApp
 * 类描述：提供UI操作工具类
 * 创建人：11177
 * 创建时间：2016/6/22 14:41
 * 修改人：11177
 * 修改时间：2016/6/22 14:41
 * 修改备注：
 */
public class UIUtils {

    /**
     * 获取上下文
     * @return
     */
    public static Context getContext(){
        return MainActivity.getContext();
    }

    public static Resources getResources(){
        return getContext().getResources();
    }

    public static String getString(int resId){
        return getResources().getString(resId);
    }

    public static String[] getStringArray(int resId){
        return getResources().getStringArray(resId);
    }

    public static String getPackageName(){
        return getContext().getPackageName();
    }
}
