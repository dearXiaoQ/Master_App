package com.csr.masterapp.entities;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 项目名称：MasterApp v3
 * 类描述：设备参数
 * 创建人：11177
 * 创建时间：2016/7/8 14:45
 * 修改人：11177
 * 修改时间：2016/7/8 14:45
 * 修改备注：
 */
public class DeviceStream implements Serializable {

    private Integer stream_id;//参数id
    private String stream_name;//参数名（英文、下划线，不可重复）
    private String stream_description;//参数描述（中文）
    private String shortname;//设备类型名
    private Integer type;//是否可控  0:否  1:是
    private Integer data_type;//数据类型  0:枚举  1:数值
    private Integer max_value;//最大值
    private Integer min_value;//最小值
    private Integer increment;//单位增量
    private ArrayList<DeviceDes> manu_set;//参数值
    private String unit;//单位
    private String unit_symbol;//单位值

    public DeviceStream(Integer stream_id, String stream_description , String stream_name, String shortname, Integer type, Integer data_type) {
        this.stream_id = stream_id;
        this.stream_description = stream_description;
        this.stream_name = stream_name;
        this.shortname = shortname;
        this.type = type;
        this.data_type = data_type;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getStream_description() {
        return stream_description;
    }

    public void setStream_description(String stream_description) {
        this.stream_description = stream_description;
    }

    public Integer getData_type() {
        return data_type;
    }

    public void setData_type(Integer data_type) {
        this.data_type = data_type;
    }

    public Integer getIncrement() {
        return increment;
    }

    public void setIncrement(Integer increment) {
        this.increment = increment;
    }

    public Integer getStream_id() {
        return stream_id;
    }

    public void setStream_id(Integer stream_id) {
        this.stream_id = stream_id;
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

    public ArrayList<DeviceDes> getManu_set() {
        return manu_set;
    }

    public void setManu_set(ArrayList<DeviceDes> manu_set) {
        this.manu_set = manu_set;
    }

    public Integer getMax_value() {
        return max_value;
    }

    public void setMax_value(Integer max_value) {
        this.max_value = max_value;
    }

    public Integer getMin_value() {
        return min_value;
    }

    public void setMin_value(Integer min_value) {
        this.min_value = min_value;
    }

    public String getUnit_symbol() {
        return unit_symbol;
    }

    public void setUnit_symbol(String unit_symbol) {
        this.unit_symbol = unit_symbol;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

}
