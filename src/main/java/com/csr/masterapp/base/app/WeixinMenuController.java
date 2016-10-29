package com.csr.masterapp.base.app;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.csr.masterapp.R;
import com.csr.masterapp.base.MenuController;

/**
 * 项目名称：MasterApp
 * 类描述：
 * 创建人：11177
 * 创建时间：2016/6/23 8:26
 * 修改人：11177
 * 修改时间：2016/6/23 8:26
 * 修改备注：
 */
public class WeixinMenuController extends MenuController {

    private TextView tv;

    public WeixinMenuController(Context context) {
        super(context);
    }

    @Override
    protected View initView(Context context) {

        tv = new TextView(context);
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    @Override
    public void initData() {
        tv.setText(R.string.wechar_pager);
    }
}
