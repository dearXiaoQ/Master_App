package com.csr.masterapp.scene.util;

import java.io.Serializable;

/**
 * 项目名称：MasterApp v3
 * 类描述：场景模型
 * 创建人：11177
 * 创建时间：2016/7/5 10:17
 * 修改人：11177
 * 修改时间：2016/7/5 10:17
 * 修改备注：
 */

public class SceneItemModel implements Serializable {

    private Integer id;//
    private Integer deviceId;
    private String deviceName;
    private String stream_name;
    private Integer type;//0:条件 1:任务
    private String key;
    private Integer value;
    private String comparison_opt;//
    private String images;

    public SceneItemModel(Integer deviceId,String stream_name, String key, Integer value) {
        this.deviceId = deviceId;
        this.stream_name = stream_name;
        this.key = key;
        this.value = value;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Integer deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getStream_name() {
        return stream_name;
    }

    public void setStream_name(String stream_name) {
        this.stream_name = stream_name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getComparison_opt() {
        return comparison_opt;
    }

    public void setComparison_opt(String comparison_opt) {
        this.comparison_opt = comparison_opt;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    
}