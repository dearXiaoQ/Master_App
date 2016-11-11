
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.entities;

import com.csr.masterapp.DeviceState;

import java.io.Serializable;


public class User implements Serializable {

	private int userId;
	private String userName;
    private String phone;
    private String password;
    private String registerTime;

    //以下变量专用于机智云登录
    private String uuid;
    private String token;

    /**
     * 机智云普通登录模式
     * @param userName  账号
     * @param uuid      设备uuid
     * @param token     机智云生成的token
     */
    public User(String userName, String uuid, String token) {
        this.userName = userName;
        this.uuid     = uuid;
        this.token    = token;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * 旧版的本地登录模式
     * @param userName      用户名
     * @param phone
     * @param password      密码
     * @param registerTime  注册时间
     */
    public User(String userName, String phone, String password, String registerTime) {
        this.userName = userName;
        this.phone = phone;
        this.password = password;
        this.registerTime = registerTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRegisterTime() {
        return registerTime;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", phone='" + phone + '\'' +
                ", password='" + password + '\'' +
                ", registerTime='" + registerTime + '\'' +
                ", uuid='" + uuid + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }
}
