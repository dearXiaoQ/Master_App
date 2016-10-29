package com.csr.masterapp.entities;

import java.io.Serializable;

/**
 * 项目名称：MasterApp v3
 * 类描述：参数值设置
 * 创建人：11177
 * 创建时间：2016/7/8 16:09
 * 修改人：11177
 * 修改时间：2016/7/8 16:09
 * 修改备注：
 */
public class DeviceDes implements Serializable {

    private Integer id;
    private Integer stream_id;//参数id
    private String key;
    private Integer value;
    private String comparison_opt;//比较符

    public DeviceDes(Integer id, Integer stream_id, String key, Integer value, String comparison_opt) {
        this.id = id;
        this.stream_id = stream_id;
        this.key = key;
        this.value = value;
        this.comparison_opt = comparison_opt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStream_id() {
        return stream_id;
    }

    public void setStream_id(Integer stream_id) {
        this.stream_id = stream_id;
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
}
