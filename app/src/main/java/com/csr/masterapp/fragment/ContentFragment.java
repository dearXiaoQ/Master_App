package com.csr.masterapp.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.csr.masterapp.BaseFragment;
import com.csr.masterapp.MainActivity;
import com.csr.masterapp.R;
import com.csr.masterapp.Recipe.RecipeListFragment;
import com.csr.masterapp.base.tab.appTabController;
import com.csr.masterapp.device.AssociationFragment;
import com.csr.masterapp.device.DeviceListFragment;
import com.csr.masterapp.device.GroupAssignFragment;
import com.csr.masterapp.device.LightControlFragment;
import com.csr.masterapp.scene.CreateSceneUI;
import com.csr.masterapp.scene.SceneListFragment;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;


/**
 * 项目名称：MasterApp
 * 类描述：主页的内容
 * 创建人：11177
 * 创建时间：2016/6/21 10:12
 * 修改时间：2016/6/21 10:12
 * 修改备注：增加浮动按钮
 *
 */
public class ContentFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener, View.OnClickListener{

    private int currentPosition = 0;
    private int mSavedPosition = POSITION_INVALID;

    private static final int ANIMATION_DURAION = 250;

    private Fragment mCurrentFragment;

    private boolean upDateAnim = false;

    private boolean senceAnimIsOk = true;

    private boolean isSenceFragment = true;

    private boolean btnHideAnim = false;

    private boolean menuAnimIsOk = true;

    private boolean isMnuFragment = true;

    public static final int POSITION_INVALID = -1;
    public static final int POSITION_LIGHT_CONTROL = 0;
    public static final int POSITION_TEMP_CONTROL = 1;
    public static final int POSITION_ASSOCIATION = 2;
    public static final int POSITION_GROUP_CONFIG = 3;
    public static final int POSITION_NETWORK_SETTINGS = 4;
    public static final int POSITION_ABOUT = 5;
    public static final int POSITION_MAX = POSITION_ASSOCIATION;

    public static int POSITION = 1;

    private static final String TAG ="ContentFragment";

    @ViewInject(R.id.Content_rg)
    public RadioGroup mRadioGroup;

    //private List<TabController> mPagerDatas;
    private int mCurrentTab;

    //顶部Btn显示控制
    @ViewInject(R.id.tab_rlyt_device)
    private RelativeLayout mButtonDevice;

    @ViewInject(R.id.tab_rlyt_scene)
    private RelativeLayout mButtonScene;


    @ViewInject(R.id.tab_rlyt_recipe)
    private RelativeLayout mButtonRecipe;

    //获取顶部Btn
    @ViewInject(R.id.device_btn_add)
    private Button mButtonAddDevice;

    @ViewInject(R.id.scene_btn_log)
    private Button mButtonDeviceLog;

    @ViewInject(R.id.scene_btn_add)
    private Button mButtonAddScene;

    @ViewInject(R.id.scene_btn_log)
    private Button mButtonSceneLog;

    @ViewInject(R.id.floatIB)
    private ImageButton mFloatIB;

    @ViewInject(R.id.recipe_btn_download)
    private Button mButtonDownload;

    @ViewInject(R.id.recipe_btn_collect)
    private Button mButtonCollect;

    @ViewInject(R.id.recipe_btn_select)
    private Button mButtonselect;

    public FragmentManager mFragmentManager;
    private appTabController mAppTabController;

    @ViewInject(R.id.listLL)
    private RelativeLayout containLL;

    private boolean mEnableNavigation;

    @Override
    protected View initView() {

        View view = View.inflate(mActivity, R.layout.content, null);

        ViewUtils.inject(this, view);

        //RadioGroup选中的监听
        mRadioGroup.setOnCheckedChangeListener(this);
        ((RadioButton) mRadioGroup.findViewById(R.id.content_rb_device)).setChecked(true);


        containLL.setOnClickListener(this);

        return view;
    }

    @Override
    protected void initData() {

        //头部选项
        mButtonAddScene.setOnClickListener(this);
        mButtonDeviceLog.setOnClickListener(this);
        mButtonAddDevice.setOnClickListener(this);
        mButtonSceneLog.setOnClickListener(this);


        mFloatIB.setOnClickListener(this);
    }

    public void setNavigationEnabled(boolean enabled) {
        mEnableNavigation = enabled;
    }

    /**
     * 1.Radiogroup本身
     * 2.某一个选中的RadioButton的id
     */

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        mCurrentFragment = null;
        mCurrentTab = -1;
        upDateAnim = false;
        btnHideAnim = false;
        switch (checkedId) {
            case R.id.content_rb_device:
                MainActivity.RECIPE_IND_SEND_DATA = false;
                isSenceFragment = false;
                isMnuFragment = false;
                mCurrentFragment = new DeviceListFragment();
                mCurrentTab = POSITION_LIGHT_CONTROL;
                mButtonDevice.setVisibility(View.VISIBLE);
                mButtonRecipe.setVisibility(View.GONE);
                mButtonScene.setVisibility(View.GONE);
                //场景Fragment按钮
                mButtonAddScene.setVisibility(View.GONE);
                mButtonSceneLog.setVisibility(View.GONE);
                mButtonselect.setVisibility(View.GONE);
                mButtonCollect.setVisibility(View.GONE);
                mButtonDownload.setVisibility(View.GONE);
                //显示浮动按钮
                mFloatIB.setVisibility(View.VISIBLE);
                POSITION = 1;
                break;
            case R.id.content_rb_scene:
                mButtonselect.setVisibility(View.GONE);
                mButtonCollect.setVisibility(View.GONE);
                mButtonDownload.setVisibility(View.GONE);
                isSenceFragment = true;
                isMnuFragment = false;
                senceAnimIsOk = true;
                mCurrentFragment = new SceneListFragment();
                mButtonDevice.setVisibility(View.GONE);
                mButtonRecipe.setVisibility(View.GONE);
                mButtonScene.setVisibility(View.VISIBLE);
                mCurrentTab = POSITION_TEMP_CONTROL;

                //显示浮动按钮
                mFloatIB.setVisibility(View.VISIBLE);
                POSITION = 2;
                break;
            case R.id.content_rb_recipe:
                isSenceFragment = false;
                menuAnimIsOk = true;
                isMnuFragment = true;
                mCurrentFragment = new RecipeListFragment();
                mButtonDevice.setVisibility(View.GONE);
                mButtonRecipe.setVisibility(View.VISIBLE);
                mButtonScene.setVisibility(View.GONE);
                mCurrentTab = POSITION_ASSOCIATION;
                mFloatIB.setVisibility(View.VISIBLE);
                mButtonAddScene.setVisibility(View.GONE);
                mButtonSceneLog.setVisibility(View.GONE);
                POSITION = 3;
                break;
        }

        if (mCurrentFragment != null) {
            mFragmentManager = getActivity().getSupportFragmentManager();
            mFragmentManager.beginTransaction().addToBackStack(null).replace(R.id.listcontainer, mCurrentFragment,"devices").commit();
        }

        currentPosition = mCurrentTab;

    }

    public void switchMenu(int position) {

        if(mAppTabController != null){

            mButtonDevice.setVisibility(View.GONE);
            mButtonScene.setVisibility(View.GONE);
            mButtonRecipe.setVisibility(View.GONE);

            mAppTabController = new appTabController();

            mFragmentManager = getActivity().getSupportFragmentManager();
            mFragmentManager.beginTransaction().addToBackStack(null).replace(R.id.listcontainer, mAppTabController).commit();

            mAppTabController.switchMenu(position);
        }
    }

    public int getCurrentPosition(){
        return currentPosition;
    }

    /**
     * Saves which fragment is currently selected. Multiple calls will overwrite the last saved position.
     */
    public void savePosition() {
        mSavedPosition = currentPosition;
    }

    /**
     * Saves selected fragment position. Multiple calls will overwrite the last saved position.
     */
    public void savePosition(int position) {
        if (mSavedPosition > POSITION_MAX) {
            throw new IllegalArgumentException("Position is out of range.");
        }
        mSavedPosition = position;
    }

    /**
     * Restores the last saved selected fragment position.
     */
    public void restorePosition() {
        if (mSavedPosition == POSITION_INVALID) {
            throw new IllegalStateException("Position has not been previously saved.");
        }
//        if (mPager != null) {
//            mPager.setCurrentItem(mSavedPosition);
//            //bar.setSelectedNavigationItem(mSavedPosition);
//        }
    }

    /**
     * Refresh the configuration fragment if this is currently active.
     */
    public void refreshConfigFragment() {
        if (mCurrentFragment instanceof GroupAssignFragment) {
            GroupAssignFragment groupFragment = (GroupAssignFragment) mCurrentFragment;
            groupFragment.refreshUI();
        }
        else if (mCurrentFragment instanceof LightControlFragment) {
            LightControlFragment lightFragment = (LightControlFragment) mCurrentFragment;
            lightFragment.refreshUI();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.scene_btn_log:
                return;
            case R.id.scene_btn_add:
                Intent intent = new Intent(mActivity, CreateSceneUI.class);
                startActivity(intent);
                return;
            case R.id.device_btn_log:
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container_content, new GroupAssignFragment()).commit();
                return;
            case R.id.device_btn_add:
                //     getActivity().entManager().beginTransaction().addToBackStack("").replace(R.id.main_container_content, new AssociationFragment()).commit();
                return;

            case R.id.floatIB:
                //  Toast.makeText(getActivity(), "你按压了浮动按钮" , Toast.LENGTH_SHORT ).show();zzz
                if(POSITION == 2) {
                    //按压弹出按钮
                    //场景Fragment按钮
                  //  mButtonSceneLog.setVisibility(View.VISIBLE);
                    //设置按钮弹出动画
                    setTranslateAnimation();
                } else if(POSITION == 1){
                    getActivity().getSupportFragmentManager().beginTransaction().addToBackStack("").replace(R.id.main_container_content, new AssociationFragment()).commit();
                } else {
                    //菜谱按钮弹出动画
                    setMenuAnimation();
                }
                break;

        }
    }

    /**
     * 11648
     * 方法描述：菜谱按钮弹出动画
     */
    private void setMenuAnimation() {
        if(menuAnimIsOk && isMnuFragment) {
            Log.i("Menu", "动画开始");
            //筛选按钮动画
            //setCollectAnim
            setSelectAnim();
            //收藏按钮动画
            setCollectAnim();
            //下载按钮动画
            setDownloadAnim();
            menuAnimIsOk = false;
        }
    }

    private void setDownloadAnim() {
        AnimationSet mAnimationSet = new AnimationSet(true);
        //float fromXDelta, float toXDelta, float fromYDelta, float toYDelta
        Animation mAlpha =  new AlphaAnimation(0.1f, 1.0f);
        Animation mTransalte = new TranslateAnimation(140, 0, 0, 0);
        mAnimationSet.addAnimation(mAlpha);
        mAnimationSet.addAnimation(mTransalte);
        mAnimationSet.setDuration(ANIMATION_DURAION);
        mButtonDownload.startAnimation(mAnimationSet);
        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mButtonDownload.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btnHideAnim = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //收藏按钮动画
    private void setCollectAnim() {
        AnimationSet mAnimationSet = new AnimationSet(true);
        //float fromXDelta, float toXDelta, float fromYDelta, float toYDelta
        Animation mAlpha =  new AlphaAnimation(0.1f, 1.0f);
        //Animation mTransalte = new TranslateAnimation(135, 0, 120, 0);
        Animation mTransalte = new TranslateAnimation(100, 0, 85, 0);
        mAnimationSet.addAnimation(mAlpha);
        mAnimationSet.addAnimation(mTransalte);
        mAnimationSet.setDuration(ANIMATION_DURAION);
        mButtonCollect.startAnimation(mAnimationSet);
        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mButtonCollect.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    //筛选按钮动画
    private void setSelectAnim() {
        AnimationSet mAnimationSet = new AnimationSet(true);
        //float fromXDelta, float toXDelta, float fromYDelta, float toYDelta
        Animation mAlpha =  new AlphaAnimation(0.1f, 1.0f);
       // Animation mTransalte = new TranslateAnimation(0, 0, 175, 0);
        Animation mTransalte = new TranslateAnimation(0, 0, 140, 0);
        mAnimationSet.addAnimation(mAlpha);
        mAnimationSet.addAnimation(mTransalte);
        mAnimationSet.setDuration(ANIMATION_DURAION);
        mButtonselect.startAnimation(mAnimationSet);
        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mButtonselect.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 11468
     * 方法描述：场景界面按钮弹出动画
     * 2016.10.7  15:10
     */
    private void setTranslateAnimation() {
        //定义动画集,指定所用动画使用同一个插值器
    //    Log.i("Animation", "setTranslateAnimation animIsOk = " + animIsOk);
        if(senceAnimIsOk && isSenceFragment) {
            Log.i("Animation", "开始弹出动画");
            //添加场景按钮动画
            setAddsenceBtnAnim();
            //操作日志按钮动画
            setOperationLogAnim();
            senceAnimIsOk = false;
        }
    }

    //操作日志按钮动画
    private void setOperationLogAnim() {
        AnimationSet mAnimationSet = new AnimationSet(true);
        Animation mAlpha =  new AlphaAnimation(0.1f, 1.0f);
        Animation mTransalte = new TranslateAnimation(0, 0, 140, 0);
        //Animation mTransalte = new TranslateAnimation(0, 0, 175, 0);
        mAnimationSet.addAnimation(mAlpha);
        mAnimationSet.addAnimation(mTransalte);
        mAnimationSet.setDuration(ANIMATION_DURAION);
        mButtonSceneLog.startAnimation(mAnimationSet);
        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mButtonSceneLog.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //添加场景按钮动画
    private void setAddsenceBtnAnim() {
        AnimationSet mAnimationSet = new AnimationSet(true);
       // Animation mTramslate = new TranslateAnimation(190, 0, 0, 0);
        Animation mTramslate = new TranslateAnimation(140, 0, 0, 0);
        Animation mAlpha =  new AlphaAnimation(0.1f, 1.0f);
        mAnimationSet.addAnimation(mAlpha);
        mAnimationSet.addAnimation(mTramslate);
        mAnimationSet.setDuration(ANIMATION_DURAION);
        mButtonAddScene.startAnimation(mAnimationSet);
        mAnimationSet.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                mButtonAddScene.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                upDateAnim = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void upDateFloatBtn(){
        if(upDateAnim) {
            //设置"添加按钮的隐藏动画"
            setAddSenceBtnGobackAnim();

            //添加日志按钮的隐藏动画
            setLogSenceBtnGobackAnim();
            upDateAnim = false;
        }
    }

    private void setLogSenceBtnGobackAnim() {
        AnimationSet animSet = new AnimationSet(true);
        Animation mAlpha = new AlphaAnimation(1.0f, 0);
        // Animation mTram  = new TranslateAnimation(0, 0, 0, 175);
        Animation mTram  = new TranslateAnimation(0, 0, 0, 140);
        animSet.addAnimation(mTram);
        animSet.addAnimation(mAlpha);
        animSet.setDuration(ANIMATION_DURAION);
        mButtonSceneLog.startAnimation(animSet);
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButtonSceneLog.setVisibility(View.GONE);
                senceAnimIsOk   = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

    }


    //场景按钮的回收动画
    private void setAddSenceBtnGobackAnim() {
        AnimationSet animSet = new AnimationSet(true);
        Animation mAlpha = new AlphaAnimation(1.0f, 0);
        //   Animation mTram  = new TranslateAnimation(0, 190, 0, 0);
        Animation mTram  = new TranslateAnimation(0, 140, 0, 0);
        animSet.addAnimation(mTram);
        animSet.addAnimation(mAlpha);
        animSet.setDuration(ANIMATION_DURAION);
        mButtonAddScene.startAnimation(animSet);
        animSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButtonAddScene.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }



    //菜谱按钮隐藏方法
    public void hideMenuBtn() {
        Log.i("hideMenuButton", "按钮动画开始");

        if(btnHideAnim) {
            //隐藏筛选按钮动画
            setSelectBtnHideAnim();
            //隐藏收藏按钮动画
            setCollectBtnHideAnim();
            //隐藏下载按钮动画
            setDownloadBtnHideAnim();
            btnHideAnim = false;
        }


    }

    //筛选按钮隐藏动画
    private void setSelectBtnHideAnim() {
        AnimationSet mAnimSet = new AnimationSet(true);
        //0, 0, 175, 0
        //ranslateAnimation mTransalte = new TranslateAnimation(0, 0, 0, 175);
        TranslateAnimation mTransalte = new TranslateAnimation(0, 0, 0, 140);
        AlphaAnimation  mAalpha = new AlphaAnimation(1.0f, 0);
        mAnimSet.addAnimation(mTransalte);
        mAnimSet.addAnimation(mAalpha);
        mAnimSet.setDuration(ANIMATION_DURAION);
        mButtonselect.startAnimation(mAnimSet);
        mAnimSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButtonselect.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //收藏按钮隐藏动画
    private void setCollectBtnHideAnim() {
        AnimationSet mAnimSet = new AnimationSet(true);
        //TranslateAnimation mTranslate = new TranslateAnimation(0, 135, 0, 120);
        TranslateAnimation mTranslate = new TranslateAnimation(0, 100, 0, 85);
        AlphaAnimation mAlpha = new AlphaAnimation(1.0f, 0);
        mAnimSet.addAnimation(mTranslate);
        mAnimSet.addAnimation(mAlpha);
        mAnimSet.setDuration(ANIMATION_DURAION);
        mButtonCollect.startAnimation(mAnimSet);
        mAnimSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                    mButtonCollect.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //下载按钮隐藏动画
    private void setDownloadBtnHideAnim() {
        AnimationSet mAnimSet = new AnimationSet(true);
        //TranslateAnimation mTranslate = new TranslateAnimation(0, 190, 0, 0);
        TranslateAnimation mTranslate = new TranslateAnimation(0, 140, 0, 0);
        AlphaAnimation mAlpha = new AlphaAnimation(1.0f, 0);
        mAnimSet.addAnimation(mTranslate);
        mAnimSet.addAnimation(mAlpha);
        mAnimSet.setDuration(ANIMATION_DURAION);
        mButtonDownload.startAnimation(mAnimSet);
        mAnimSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButtonDownload.setVisibility(View.GONE);
                menuAnimIsOk =true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


}










