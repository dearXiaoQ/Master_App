package com.csr.masterapp.weight;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 项目名称：MasterApp
 * 类描述：不可滑动的ViewPager
 * 创建人：11177
 * 创建时间：2016/6/21 17:46
 * 修改人：11177
 * 修改时间：2016/6/21 17:46
 * 修改备注：
 */
public class NoScroViewPager extends ViewPager {
    public NoScroViewPager(Context context) {
        super(context);
    }

    public NoScroViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //不拦截
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //不消费
        return false;
    }
}
