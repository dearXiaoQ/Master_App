package com.csr.masterapp;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 项目名称：MasterApp
 * 类描述：基类
 * 创建人：11177
 * 创建时间：2016/6/21 10:15
 * 修改人：11177
 * 修改时间：2016/6/21 10:15
 * 修改备注：
 */
public abstract class BaseFragment extends Fragment {

    protected Activity mActivity;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        return initView();
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //加载数据
        initData();

    }

    protected abstract View initView();

    protected void initData(){}
}
