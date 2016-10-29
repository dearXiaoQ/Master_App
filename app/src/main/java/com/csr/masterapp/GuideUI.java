package com.csr.masterapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.csr.masterapp.utils.CacheUtils;
import com.csr.masterapp.utils.MD5Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称：MasterApp
 * 类描述：
 * 创建人：11177
 * 创建时间：2016/6/20 8:17
 * 修改人：11177
 * 修改时间：2016/6/20 8:17
 * 修改备注：
 */
public class GuideUI extends Activity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private ViewPager mPager;//页面中的ViewPager

    private List<ImageView> mPageDatas;//页面数据
    private Button mBtnStart;
    private LinearLayout mContainerPoint;//静态点容器

    private View mFocusPoint;//动态点
    private int mPointSpage;//两点间距离

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.guide);

        //初始化view
        initView();

        //初始化数据
        initData();
    }


    private void initView() {
        mPager = (ViewPager) findViewById(R.id.guide_pager);
        mBtnStart = (Button) findViewById(R.id.guide_btn_start);
        mContainerPoint = (LinearLayout) findViewById(R.id.guide_container_pointer);
        mFocusPoint = findViewById(R.id.guide_focus_pointer);

        mContainerPoint.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            //监听布局完成
            @Override
            public void onGlobalLayout() {
                mContainerPoint.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mPointSpage = mContainerPoint.getChildAt(1).getLeft() - mContainerPoint.getChildAt(0).getLeft();
            }
        });

        mBtnStart.setOnClickListener(this);
    }

    private void initData() {
        int[] imgRes = new int[]{
                R.drawable.guide_1,
                R.drawable.guide_2,
                R.drawable.guide_3,
        };

        ImageView iv;
        mPageDatas = new ArrayList<ImageView>();
        View point;

        for (int i = 0; i < imgRes.length; i++) {
            iv = new ImageView(this);
            iv.setImageResource(imgRes[i]);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);

            //添加到list中
            mPageDatas.add(iv);

            point = new View(this);
            point.setBackgroundResource(R.drawable.guide_point_normal);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            if (i != 0) {
                params.leftMargin = 20;
            }
            mContainerPoint.addView(point, params);
        }

        //给ViewPater设置数据
        mPager.setAdapter(new GuideAdapter());

        //监听ViewPager的滑动
        mPager.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //positionOffset:滑动的百分比
        //positionOffsetPixels:滑动的像素
        int leftMargin = (int) (mPointSpage * positionOffset + position * mPointSpage + 0.5f);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mFocusPoint.getLayoutParams();
        params.leftMargin = leftMargin;

        mFocusPoint.setLayoutParams(params);
    }

    @Override
    public void onPageSelected(int position) {
        //页面选中时回调,Button控制
        mBtnStart.setVisibility(position == mPageDatas.size() - 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        if(v == mBtnStart){
            clickStart();
        }
    }

    private void clickStart() {
        //页面跳转
        CacheUtils.setBoolean(this, WelcomeUI.KEY_FIRST_START, false);
        //保存Mac地址到本地缓存
        WifiManager wifi = (WifiManager) GuideUI.this.getSystemService(Context.WIFI_SERVICE);

        CacheUtils.setString(this, WelcomeUI.MASTER_APP_ID, MD5Utils.encodeByMD5(wifi.getConnectionInfo().getMacAddress()));

        Intent intent = new Intent(this, LoginAndRegisterUI.class);
        startActivity(intent);
        finish();
    }


    class GuideAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (mPageDatas != null) {
                return mPageDatas.size();
            }
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView iv = mPageDatas.get(position);
            container.addView(iv);
            return iv;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
