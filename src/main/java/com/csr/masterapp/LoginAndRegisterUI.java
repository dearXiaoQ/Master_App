package com.csr.masterapp;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.csr.masterapp.database.DataBaseDataSource;
import com.csr.masterapp.entities.User;
import com.csr.masterapp.utils.CacheUtils;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizUserAccountType;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.List;

public class LoginAndRegisterUI extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private final static long ANIMATION_DURATION = 2000;
    private final static long SCALE_ANIMATION_DURATION = 6000;

    private static final String TAG = "LoginAndRegisterUI";

    private List<User> allUsers = null;
    private EditText mUserName;
    private EditText mPassword;
    private EditText mNewUserName;
    private EditText mNewPassword;
    private DataBaseDataSource mDataBase;
    private View rvLogin;
    private View rvRegister;
    private Boolean isAutoLogin = false;
    private View appWelcome;
    private Boolean isVisible = true;

    private String logUser = "";
    private String logPwd  = "";

    private Resources mResourts;
    private ProgressDialog dialog;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去除状态栏
        setContentView(R.layout.login_and_register);

        mResourts = this.getResources();

        Handler handler = new Handler();
        handler.postDelayed(new AnimRunnable(), 3000);

        //初始化控件
        initView();

        //设置动画
        setAnim();

    }

    //初始化控件和数据库
    private void initView() {

        findViewById(R.id.btn_skip).setOnClickListener(this);
        appWelcome = findViewById(R.id.rv_app_welcome);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1.05f, 1f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        scaleAnimation.setDuration(SCALE_ANIMATION_DURATION);
        findViewById(R.id.bg_app_welcome).startAnimation(scaleAnimation);

        new Thread(){
            @Override
            public void run() {
                //加载数据库的所有用户
                mDataBase = new DataBaseDataSource(LoginAndRegisterUI.this);
                allUsers = mDataBase.getAllUsers();
            }
        }.start();


        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);
        rvLogin = findViewById(R.id.rv_login);
        rvRegister = findViewById(R.id.rv_register);
        findViewById(R.id.register).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);

        mUserName = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mNewUserName = (EditText) findViewById(R.id.new_username);
        mNewPassword = (EditText) findViewById(R.id.new_password);
        ((CheckBox) findViewById(R.id.is_login)).setOnCheckedChangeListener(this);

        Intent resultIntent = getIntent();
        if (resultIntent != null) {
            String resultUserName = resultIntent.getStringExtra("resultUserName");
            String resultPassword = resultIntent.getStringExtra("resultPassword");
            if (resultUserName != null && resultPassword != null) {
                mUserName.setText(resultUserName);
                mPassword.setText(resultPassword);
            }
        }

    }


    //设置动画
    private void setAnim() {
        //透明动画
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        alphaAnimation.setDuration(ANIMATION_DURATION);

        //位移动画
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0f);
        translateAnimation.setDuration(ANIMATION_DURATION);

        //动画集合
        AnimationSet set = new AnimationSet(false);
        set.addAnimation(alphaAnimation);
        set.addAnimation(translateAnimation);

        findViewById(R.id.ic_logo).startAnimation(set);
    }


    public void autoLogin() {
        String autoLoginUserName = CacheUtils.getString(LoginAndRegisterUI.this, "autoLoginUserName");
        String autoLoginPassword = CacheUtils.getString(LoginAndRegisterUI.this, "autoLoginPassword");
        if (autoLoginUserName != null && autoLoginPassword != null) {
            Log.d(TAG, "onCreate: " + autoLoginUserName + "--" + autoLoginPassword);
            verifyLogin(autoLoginUserName, autoLoginPassword);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_skip:
                //跳过展示页
                appWelcome.setVisibility(View.GONE);
                appWelcome.clearAnimation();
                autoLogin();
                isVisible = false;
                break;
            case R.id.register:
                //切换注册界面
                rvRegister.setVisibility(View.VISIBLE);
                rvLogin.setVisibility(View.GONE);
                break;
            case R.id.login:
                //切换登录界面
                rvRegister.setVisibility(View.GONE);
                rvLogin.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_login:
                //登录
                String username = mUserName.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                verifyLogin(username, password);

                break;
            case R.id.btn_register:
                //注册
                String newUsername = mNewUserName.getText().toString().trim();
                String newPassword = mNewPassword.getText().toString().trim();

                userRegister(newUsername, newPassword);

                break;
        }
    }

    /**
     * 登录方法
     * @param username
     * @param password
     */
    private void verifyLogin(String username, String password) {
        //本地登录
     //  loginToDB(username, password);

        //登录到机智云
        loginToGiz(username, password);
    }

    /**
     * 注册方法
     */
    private void userRegister(String newUsername, String newPassword) {

        //注册本地数据库
      //  registerToDB(newUsername, newPassword);

        //注册到机智云
        registerToGizYun(newUsername, newPassword);
    }

    //注册到机智云
    private void registerToGizYun(String newUsername, String newPassword) {
        GizWifiSDK.sharedInstance().setListener(mListener);
        GizWifiSDK.sharedInstance().registerUser(newUsername, newPassword, null, GizUserAccountType.GizUserNormal);
        //使用静态方式创建并显示，这种进度条只能是圆形条,这里最后一个参数boolean cancelable 设置是否进度条是可以取消的
        dialog = ProgressDialog.show(this, mResourts.getString(R.string.prompt),
                mResourts.getString(R.string.register_ing), false, true);
    }

    //离线注册到本地数据库
    private void registerToDB(String newUsername, String newPassword) {
        if (newUsername.equals("") || newPassword.equals("")) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_and_password_is_not_null), Toast.LENGTH_SHORT).show();
            return;
        } else if (newUsername.length() < 6) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_less_than_six_byte), Toast.LENGTH_SHORT).show();
            return;
        } else if (newPassword.length() < 6) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.password_less_than_six_byte), Toast.LENGTH_SHORT).show();
            return;
        } else if (allUsers != null) {
            for (User user : allUsers) {
                if (user.getUserName().equals(newUsername)) {
                    Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_alrealy_exists), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        User result = mDataBase.createOrUpdateUser(new User(newUsername, "12345678912", newPassword, String.valueOf(System.currentTimeMillis())));
        if (result != null && result.getUserId() != 0) {

            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.register_success_ready_into), Toast.LENGTH_SHORT).show();

            startActivity(new Intent(LoginAndRegisterUI.this, WelcomeUI.class));
            finish();
        }
    }

    //登录到机智云
    private void loginToGiz(String username,String password) {
        GizWifiSDK.sharedInstance().setListener(mListener);
        if( checkLoginInfo(username, password) ) {
            GizWifiSDK.sharedInstance().userLogin(username, password);
            logUser = username;
            logPwd  = password;
            if (isAutoLogin) {
                CacheUtils.setString(LoginAndRegisterUI.this, "autoLoginUserName", username);
                CacheUtils.setString(LoginAndRegisterUI.this, "autoLoginPassword", password);
            }

            CacheUtils.setInt(LoginAndRegisterUI.this, "userId", 2);    //2是强行填进去的
            CacheUtils.setString(LoginAndRegisterUI.this, "userName", username);
            dialog = ProgressDialog.show(this, mResourts.getString(R.string.prompt),
                    mResourts.getString(R.string.login), false, true);
            dialog.show();
        }
    }

    //检验登录信息的合法性
    private boolean checkLoginInfo(String userNmae, String password) {
        if (userNmae.equals("") || password.equals("")) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_and_password_is_not_null), Toast.LENGTH_SHORT).show();
            return false;
        }
        return  true;
    }

    //离线登陆
    private void loginToDB(String username, String password) {
        if (username.equals("") || password.equals("")) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_and_password_is_not_null), Toast.LENGTH_SHORT).show();
            return;
        }
        if (username.length() < 6) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_less_than_six_byte), Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.password_less_than_six_byte), Toast.LENGTH_SHORT).show();
            return;
        }
        if (allUsers == null) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_account_is_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        User getUser = null;
        for (User user : allUsers) {
            if (user.getUserName().equals(username)) {
                getUser = user;
                Log.d(TAG, "verifyLogin: " + getUser + "--" + user.getUserName() + "---" + username);
                break;
            }
        }
        if (getUser == null) {
            Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_account_is_empty), Toast.LENGTH_SHORT).show();
        } else {
            if (getUser.getPassword().equals(password)) {
                Log.d(TAG, "verifyLogin: " + isAutoLogin + getUser.getUserName());
                if (isAutoLogin) {
                    CacheUtils.setString(LoginAndRegisterUI.this, "autoLoginUserName", getUser.getUserName());
                    CacheUtils.setString(LoginAndRegisterUI.this, "autoLoginPassword", getUser.getPassword());
                }

                CacheUtils.setInt(LoginAndRegisterUI.this, "userId", getUser.getUserId());
                CacheUtils.setString(LoginAndRegisterUI.this, "userName", getUser.getUserName());
                Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginAndRegisterUI.this, WelcomeUI.class));
                finish();
            } else {
                Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.password_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //动画的Runnable
    class AnimRunnable implements Runnable {

        @Override
        public void run() {
            if (isVisible) {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1f, 0f);
                alphaAnimation.setDuration(ANIMATION_DURATION);
                appWelcome.startAnimation(alphaAnimation);
                //动画结束后回馈动作
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        appWelcome.setVisibility(View.GONE);
                        autoLogin();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }
    }

    //机智云SDK回调
    //实现回调
    GizWifiSDKListener mListener = new GizWifiSDKListener() {
        @Override
        public void didRegisterUser(GizWifiErrorCode result, String uid, String token) {
            // 取消进读条
            dialogCancel(dialog);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                //注册成功，获取到uid和token
                Log.i("GizWifiSDK", "register Success" + " token " + token + " uid" + uid);
                Toast.makeText(getApplicationContext(), "注册成功", Toast.LENGTH_LONG).show();
                //切换到登录模式
                rvRegister.setVisibility(View.GONE);
                rvLogin.setVisibility(View.VISIBLE);
            } else {
                //注册失败，弹出错误信息
                Log.i("GizWifiSDK", "register faile" + " errorMessage = " + result);
                Toast.makeText(getApplicationContext(), "注册失败：" + result, Toast.LENGTH_LONG).show();
            }
        }

        //隐藏进度条
        private void dialogCancel(Dialog dialog) {
            if (dialog != null && dialog.isShowing()) {
                dialog.cancel();
            }
        }

        @Override
        public  void didUserLogin(GizWifiErrorCode result, String uid,  String token) {
            // 取消进度条
            dialogCancel(dialog);
            if(result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 登录成功，获取到uid和token
                Log.i("GizWifiSDK", "login successs" + " result = " + result + " uid = " + uid + " token = " + token);
                Toast.makeText(getApplicationContext(), mResourts.getString(R.string.login_success), Toast.LENGTH_LONG).show();
                //跳转到主页面
                jump(uid, token);
            } else {
                // 登录失败
                Log.i("GizWifiSDK", "login faile" + result);
                Toast.makeText(getApplicationContext(), mResourts.getString(R.string.login_faile), Toast.LENGTH_LONG);
            }
        }

        //跳转
        private void jump(String uuid, String token) {
            if (isAutoLogin) {  //记住密码功能
                CacheUtils.setString(LoginAndRegisterUI.this, "autoLoginUserName", logUser);
                CacheUtils.setString(LoginAndRegisterUI.this, "autoLoginPassword", logPwd);
            }
            Bundle bundle = new Bundle();
            Intent intent  = new Intent(LoginAndRegisterUI.this, WelcomeUI.class);
            User loginUser = new User(logUser, uuid, token);
            bundle.putSerializable("loginInfo", loginUser);
            intent.putExtra("loginInfo", bundle);
            startActivity(intent);
            finish();
            //  CacheUtils.setString();

        }
    };

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        isAutoLogin = isChecked;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            if (MainActivity.mMainActivity != null) {
                MainActivity.mMainActivity.finish();
                WelcomeUI.mWelcomeUI.finish();
            }
        }
        return false;
    }
}
