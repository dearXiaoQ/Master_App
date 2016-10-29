package com.csr.masterapp.base.tab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.csr.masterapp.base.TabController;

/**
 * 项目名称：MasterApp
 * 类描述：场景页面控制器
 * 创建人：11177
 * 创建时间：2016/6/21 16:17
 * 修改人：11177
 * 修改时间：2016/6/21 16:17
 * 修改备注：
 */
@SuppressLint("ValidFragment")
public class sceneTabController extends TabController {

    public sceneTabController(Context context) {
        super(context);
    }

    @Override
    protected View initContentView(Context context) {

        TextView tv = new TextView(context);

        tv.setText("场景页面");
        tv.setGravity(Gravity.CENTER);

        return tv;
    }

    @Override
    public void initData() {

    }
}