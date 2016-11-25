package com.csr.masterapp.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.csr.masterapp.BaseFragment;
import com.csr.masterapp.R;
import com.csr.masterapp.interfaces.FragmentControl;

/**
 * Created by mars on 2016/11/11.
 */
public class FirstPage extends BaseFragment implements View.OnClickListener, CheckBox.OnCheckedChangeListener{
    /** 演示视频  */
    private ImageView videoIv;
    /**  */
    private CheckBox  checkBox;
    /** 下一步按钮 */
    private Button    nextBtnl;
    /** Fragment跳转控制 */
    private FragmentControl mFragmentControl;

    @Override
    protected View initView() {
        if(mActivity instanceof FragmentControl) {
            mFragmentControl = (FragmentControl) mActivity;
        }
        View view = LayoutInflater.from(mActivity).inflate(R.layout.first_page, null);
        videoIv   = (ImageView) view.findViewById(R.id.playIv);
        checkBox  = (CheckBox) view.findViewById(R.id.cbSelect);
        nextBtnl  = (Button) view.findViewById(R.id.btnNext);
        videoIv.setBackgroundResource(R.drawable.mymyshoft);
        checkBox.setOnCheckedChangeListener(this);
        nextBtnl.setOnClickListener(this);
        nextBtnl.setClickable(false);
        nextBtnl.setBackgroundResource(R.drawable.btn_next_shape_gray);
        
        return  view;
    }


    @Override
    public void onClick(View v) {
        mFragmentControl.autoNextFragment();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            nextBtnl.setClickable(isChecked);
            nextBtnl.setBackgroundResource(R.drawable.img_ba_shape);
        } else {
            nextBtnl.setClickable(isChecked);
            nextBtnl.setBackgroundResource(R.drawable.btn_next_shape_gray);

        }
    }
}
