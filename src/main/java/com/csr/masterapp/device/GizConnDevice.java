package com.csr.masterapp.device;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.adapter.DeviceConnVpAdapter;
import com.csr.masterapp.fragment.ZeroPage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mars on 2016/11/9.
 * builder 11468
 * describe: 使用机智云WifiSdk连接设备
 */
public class GizConnDevice extends FragmentActivity implements View.OnClickListener, ViewPager.OnPageChangeListener{

    private ImageView gobackIv;
    private TextView titleTv;
    private ViewPager deviceConnVp;

    private final int ZERO_PAGE   = 0;
    private final int FIRST_PAGE  = 1;
    private final int SECOND_PAGE = 2;
    private final int THIRD_PAGE  = 3;

    private PagerAdapter mPagerAdapter;
    private List<Fragment> mFragment;

    //GizWifiSDKListener
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.giz_conn_device);

        //初始化控件
        initView();

        //初始化数据
        initData();

    }

    private void initData() {
        mFragment = new  ArrayList();
        Fragment zeroFragment = new ZeroPage();
        mFragment.add(zeroFragment);
        mPagerAdapter = new DeviceConnVpAdapter(getSupportFragmentManager() ,this, mFragment);
        deviceConnVp.setAdapter(mPagerAdapter);
    }

    //初始化控件
    private void initView() {
        gobackIv = (ImageView) findViewById(R.id.backIv);
        titleTv = (TextView) findViewById(R.id.titleTv);
        titleTv.setText(R.string.add_device);
        deviceConnVp = (ViewPager) findViewById(R.id.device_conn_vp);

        gobackIv.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backIv :
                int currentIndex = deviceConnVp.getCurrentItem();
                  if (deviceConnVp.getCurrentItem() == 0) {
                        //弹出对话框询问是否退出设备连接
                  } else {
                      deviceConnVp.setCurrentItem(currentIndex--);
                  }
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {

    }

    @Override
    public void onPageScrollStateChanged(int currentItem) {
         int resourceId = 0;
        switch (currentItem) {
            case ZERO_PAGE :
                resourceId = R.string.add_device;
                break;
            case FIRST_PAGE :
                resourceId = R.string.config_device_can_conn;
                break;
            case SECOND_PAGE :
                resourceId = R.string.search_and_conn_device;
                break;
            case THIRD_PAGE :

                break;
        }
         titleTv.setText(resourceId);
    }
}
