package com.csr.masterapp.interfaces;

/**
 * Created by mars on 2016/11/11.
 */
public interface FragmentControl {
    /**
     * 根据当前的标签，自动设置Fragment的页面
     */
    void autoNextFragment();

    /**
     * 回退到到上一步
     */
    void autoBackFragment();
}
