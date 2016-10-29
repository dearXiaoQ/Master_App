package com.csr.masterapp.base.tab;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.csr.masterapp.R;
import com.csr.masterapp.base.MenuController;
import com.csr.masterapp.base.app.UserMenuController;
import com.csr.masterapp.base.app.WeixinMenuController;

import java.util.ArrayList;
import java.util.List;

;

/**
 * 项目名称：MasterApp
 * 类描述：场景页面控制器
 * 创建人：11177
 * 创建时间：2016/6/21 16:17
 * 修改人：11177
 * 修改时间：2016/6/21 16:17
 * 修改备注：
 */
public class appTabController extends Fragment{

    private FrameLayout mContentContainer;
    private FrameLayout mContainer;
    private List<MenuController> mMenuControllers;

    private Activity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initView();
    }

    private View initView() {

        View view = View.inflate(mActivity, R.layout.base_tab, null);

        mContentContainer = (FrameLayout) view.findViewById(R.id.tab_container_content);

        //初始化内容的view
        mContentContainer.addView(initContentView());

        return view;
    }

    protected View initContentView() {

        // app容器
        mContainer = new FrameLayout(mActivity);
        return mContainer;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //加载数据
        initData();

    }

    public void initData(){
        mMenuControllers = new ArrayList<MenuController>();

        //加实体数据
        mMenuControllers = new ArrayList<MenuController>();
        mMenuControllers.add(new UserMenuController(mActivity));
        mMenuControllers.add(new WeixinMenuController(mActivity));
        mMenuControllers.add(new UserMenuController(mActivity));
        mMenuControllers.add(new WeixinMenuController(mActivity));
        mMenuControllers.add(new UserMenuController(mActivity));
        mMenuControllers.add(new WeixinMenuController(mActivity));
        mMenuControllers.add(new UserMenuController(mActivity));
        mMenuControllers.add(new WeixinMenuController(mActivity));
        mMenuControllers.add(new UserMenuController(mActivity));
        mMenuControllers.add(new WeixinMenuController(mActivity));
        mMenuControllers.add(new UserMenuController(mActivity));
        mMenuControllers.add(new WeixinMenuController(mActivity));
        mMenuControllers.add(new UserMenuController(mActivity));
        mMenuControllers.add(new WeixinMenuController(mActivity));

    }

    public void switchMenu(int position){

        //mContainer.removeAllViews();

        MenuController menuController = mMenuControllers.get(position);

        //视图
        View rootView = menuController.getRootView();
        mContainer.addView(rootView);

        //数据
        menuController.initData();
    }
}
