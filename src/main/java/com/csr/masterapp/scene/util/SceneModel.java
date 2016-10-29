package com.csr.masterapp.scene.util;

import java.util.List;

/**
 * 项目名称：MasterApp v3
 * 类描述：场景模型
 * 创建人：11177
 * 创建时间：2016/7/5 10:17
 * 修改人：11177
 * 修改时间：2016/7/5 10:17
 * 修改备注：
 */

public class SceneModel {

    private Integer sceneId;
    private String name;
    private String images;
    private Integer status;//状态
    private Integer mode;//或者、并且
    private Integer isSend;//是否发送到手机 0 否  1 是
    private String alarm_time;
    private int[] alarm_days;

    private List<SceneItemModel> conditions;
    private List<SceneItemModel> tasks;

    public SceneModel(String name, Integer status, Integer mode, Integer isSend , List<SceneItemModel> conditions , List<SceneItemModel> tasks) {
        this.name = name;
        this.status = status;
        this.mode = mode;
        this.isSend = isSend;
        this.conditions = conditions;
        this.tasks = tasks;
    }

    public Integer getSceneId() {
        return sceneId;
    }

    public void setSceneId(Integer sceneId) {
        this.sceneId = sceneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public Integer getIsSend() {
        return isSend;
    }

    public void setIsSend(Integer isSend) {
        this.isSend = isSend;
    }

    public String getAlarm_time() {
        return alarm_time;
    }

    public void setAlarm_time(String alarm_time) {
        this.alarm_time = alarm_time;
    }

    public int[] getAlarm_days() {
        return alarm_days;
    }

    public void setAlarm_days(int[] alarm_days) {
        this.alarm_days = alarm_days;
    }

    public List<SceneItemModel> getConditions() {
        return conditions;
    }

    public void setConditions(List<SceneItemModel> conditions) {
        this.conditions = conditions;
    }

    public List<SceneItemModel> getTasks() {
        return tasks;
    }

    public void setTasks(List<SceneItemModel> tasks) {
        this.tasks = tasks;
    }


}