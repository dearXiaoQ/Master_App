package com.csr.masterapp;

import android.app.Activity;
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

import java.util.ArrayList;

public class LoginAndRegisterUI extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private final static long ANIMATION_DURATION = 2000;
    private final static long SCALE_ANIMATION_DURATION = 6000;

    private static final String TAG = "LoginAndRegisterUI";

    private ArrayList<User> allUsers;
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

    private Resources mResourts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去除状态栏
        setContentView(R.layout.login_and_register);

        findViewById(R.id.btn_skip).setOnClickListener(this);
        appWelcome = findViewById(R.id.rv_app_welcome);

        ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 1.05f, 1f, 1.05f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
        scaleAnimation.setDuration(SCALE_ANIMATION_DURATION);
        findViewById(R.id.bg_app_welcome).startAnimation(scaleAnimation);

        mResourts = this.getResources();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
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
        }, 3000);

        mDataBase = new DataBaseDataSource(LoginAndRegisterUI.this);
        allUsers = mDataBase.getAllUsers();

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

    public void autoLogin(){
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

                if (newUsername.equals("") || newPassword.equals("")) {
                    Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_and_password_is_not_null), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newUsername.length() < 6) {
                    Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.user_name_less_than_six_byte), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length() < 6) {
                    Toast.makeText(LoginAndRegisterUI.this, mResourts.getString(R.string.password_less_than_six_byte), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (allUsers != null) {
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
                break;
        }
    }

    private void verifyLogin(String username, String password) {
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
