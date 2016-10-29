package com.csr.masterapp.base;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.csr.masterapp.R;

/**
 * 项目名称：MasterApp
 * 类描述：tab页面对应的控制器
 * 创建人：11177
 * 创建时间：2016/6/21 15:55
 * 修改人：11177
 * 修改时间：2016/6/21 15:55
 * 修改备注：
 */
public abstract class TabController extends Fragment{

    protected Context mContext;
    protected View mRootView;

    protected FrameLayout mContentContainer;

    protected Button mAddDevice;

    public TabController(Context context) {
        this.mContext = context;
        mRootView = initView(context);
    }

    /**
     * 初始化视图
     *
     * @return
     */
    protected View initView(Context context) {

        View view = View.inflate(mContext, R.layout.base_tab, null);

        mContentContainer = (FrameLayout) view.findViewById(R.id.tab_container_content);

        //初始化内容的view
        mContentContainer.addView(initContentView(context));

        return view;
    }

    /**
     * 初始化内容的view
     * @return
     */
    protected abstract View initContentView(Context context);

    public View getRootView() {
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

    /**
     * 切换菜单
     * @param position
     */
    public void switchMenu(int position){


    }
}
