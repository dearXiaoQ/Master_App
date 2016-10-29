package com.csr.masterapp.base;

import android.content.Context;
import android.view.View;

/**
 * 项目名称：MasterApp
 * 类描述：tab页面对应的控制器
 * 创建人：11177
 * 创建时间：2016/6/21 15:55
 * 修改人：11177
 * 修改时间：2016/6/21 15:55
 * 修改备注：
 */
public abstract class MenuController {

    protected Context mContext;
    protected View mRootView;

    public MenuController(Context context) {
        this.mContext = context;
        mRootView = initView(context);
    }

    /**
     * 初始化视图
     *
     * @return
     */
    protected abstract View initView(Context context);

    /**
     * 获得根视图
     */
    public View getRootView(){
        return mRootView;
    }

    /**
     * 获取上下文
     *
     * @return
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * 初始化数据
     */
    public void initData() {

    }

}
