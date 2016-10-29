package com.csr.masterapp.adapter;

/**
 * Created by mars on 2016/10/28.
 */

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * 类    名：   DeviceVpAdapter
 * 描    述：设备主界面GridView数据适配器
 * 作    者：  聪（11468）
 * 时    间：  2016.9.29
 * 版    本：  MasterApp
 * 修 改 者：  强（11468）
 * 修改时间：  2016.10.28
 * 备    注:  转移位置
 */
public class DeviceVpAdapter extends PagerAdapter {

    private int mChildCount = 0;

 //   private LinearLayout containLL;

    private List<View> viewsList ;

    public DeviceVpAdapter(List<View> viewList){
        this.viewsList = viewList;
    }

 /*   public void refreshiView(List<View> viewList, LinearLayout containLL){
        this.viewsList = viewList;
        this.containLL = containLL;
        notifyDataSetChanged();
    }*/

  /*  @Override
    public void notifyDataSetChanged() {
        mChildCount = getCount();
        for(int i = 0; i < mChildCount; i++) {
            destroyItem(containLL, i, new Object());
        }
        super.notifyDataSetChanged();
    }*/

    @Override
    public int getItemPosition(Object object) {
        if ( mChildCount > 0) {
            mChildCount --;
            return POSITION_NONE;
        }

        return super.getItemPosition(object);

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)   {
        container.removeView(viewsList.get(position));//删除页卡
    }


    @Override
    public Object instantiateItem(ViewGroup container, final int position) {  //这个方法用来实例化页卡
        container.addView(viewsList.get(position), 0);//添加页卡
        return viewsList.get(position);
    }

    @Override
    public int getCount() {
        return  viewsList.size();//返回页卡的数量
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0==arg1;//官方提示这样写
    }
}
