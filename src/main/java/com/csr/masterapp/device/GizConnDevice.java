package com.csr.masterapp.device;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.adapter.DeviceConnVpAdapter;
import com.csr.masterapp.fragment.FirstPage;
import com.csr.masterapp.fragment.SecondPage;
import com.csr.masterapp.fragment.ZeroPage;
import com.csr.masterapp.interfaces.FragmentControl;
import com.gizwits.gizwifisdk.enumration.GizWifiGAgentType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mars on 2016/11/9.
 * builder 11468
 * describe: 使用机智云WifiSdk连接设备
 */
public class GizConnDevice extends FragmentActivity implements View.OnTouchListener, View.OnClickListener, ViewPager.OnPageChangeListener, FragmentControl{
    /** 回退按钮 */
    private ImageView gobackIv;
    /** 标题栏文字 */
    private TextView titleTv;
    /** Fragment Contain */
    private ViewPager deviceConnVp;

    private final int ZERO_PAGE   = 0;
    private final int FIRST_PAGE  = 1;
    private final int SECOND_PAGE = 2;
    private final int THIRD_PAGE  = 3;

    /** 机智云wifi */
    private String wifiName;
    private String wifiPwd;

    /** 设备号  */
    private GizWifiGAgentType deviceType;

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
        FirstPage firstFragment = new FirstPage();
        SecondPage secondPageFragment = new SecondPage();
        mFragment.add(zeroFragment);
        mFragment.add(firstFragment);
        mFragment.add(secondPageFragment);
        mPagerAdapter = new DeviceConnVpAdapter(getSupportFragmentManager() ,this, mFragment);
        deviceConnVp.setAdapter(mPagerAdapter);
        deviceConnVp.setOnTouchListener(this);  //禁止掉触摸事件，使ViewPager不能滑动
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
                if (currentIndex == 0) {
                    //弹出对话框询问是否退出设备连接
                } else {
                    autoBackFragment();
                }
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int currentItem) {

    }

    @Override
    public void onPageScrollStateChanged(int currentItem) {

    }

    @Override
    public void autoNextFragment() {
        Log.i("nextBnt", "autoNextFragment()方法");
        int indexPage = deviceConnVp.getCurrentItem();
        indexPage ++;
        deviceConnVp.setCurrentItem(indexPage);
        setTitleText(indexPage);
    }

    @Override
    public void autoBackFragment() {
        int indexPage = deviceConnVp.getCurrentItem();
        if(indexPage > 0) {
            indexPage-- ;
            deviceConnVp.setCurrentItem(indexPage);
            setTitleText(indexPage);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }


    /** 设置标题栏文字 */
    public void setTitleText(int index) {
        int resourceId = 0;
        gobackIv.setVisibility(View.VISIBLE);
        switch (index) {
            case ZERO_PAGE :
                resourceId = R.string.add_device;
                break;
            case FIRST_PAGE :
                resourceId = R.string.config_device_can_conn;
                break;
            case SECOND_PAGE :
                gobackIv.setVisibility(View.GONE);
                resourceId = R.string.search_and_conn_device;

                break;
            case THIRD_PAGE :

                break;
        }
        titleTv.setText(resourceId);
    }

    public String getWifiNmae() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPwd() {
        return wifiPwd;
    }

    public void setWifiPwd(String wifiPwd) {
        this.wifiPwd = wifiPwd;
    }

    /** 设备类型 */
    public void setType(int selectPosition) {
        switch (selectPosition) {
            case 0 : //庆科
                deviceType = GizWifiGAgentType.GizGAgentMXCHIP;
                break;
            case 1 : //汉枫
                deviceType = GizWifiGAgentType.GizGAgentHF; //汉枫
                break;
            case 2 : //瑞昱
              //  deviceType = GizWifiGAgentType.GizGAgentHF; //汉枫
                break;
            case 3 : //联盛德
                //deviceType = GizWifiGAgentType
                break;
            case 4 : //乐鑫
                deviceType = GizWifiGAgentType.GizGAgentESP;   //乐鑫
                break;
            case 5 : //高通
                deviceType = GizWifiGAgentType.GizGAgentQCA;
                break;
            case 6 : //TI
                deviceType = GizWifiGAgentType.GizGAgentTI;
                break;
            case 7 : //语音天下
                //deviceType = GizWifiGAgentType.
                break;
            case 8 : //庆科V3
                deviceType = GizWifiGAgentType.GizGAgentMXCHIP3;
                break;
            case 9 : //古北
                //deviceType = GizWifiGAgentType.
                break;
            case 10 : //AtmelEE
                deviceType = GizWifiGAgentType.GizGAgentAtmelEE;
                break;
            case 11 :  //其他
                deviceType = GizWifiGAgentType.GizGAgentOther;
                break;
        }

    }
    /** 返回设备类型 */
    public GizWifiGAgentType getDeviceType() {
        return deviceType;
    }

}
