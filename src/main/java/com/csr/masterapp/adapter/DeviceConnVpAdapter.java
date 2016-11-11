package com.csr.masterapp.adapter;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;

import java.util.List;

/**
 * Created by mars on 2016/11/10.
 * builder: 11468
 * 机智云设备连接界面的viewPager适配器
 */
public class DeviceConnVpAdapter extends FragmentPagerAdapter{

    private Context mContext;
    private List<Fragment> mFragments;

    public DeviceConnVpAdapter(FragmentManager fm, Context mContext, List<Fragment> mFragments) {
        super(fm);
        this.mContext = mContext;
        this.mFragments = mFragments;
    }


    @Override
    public Fragment getItem(int i) {
        return mFragments.get(i);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
