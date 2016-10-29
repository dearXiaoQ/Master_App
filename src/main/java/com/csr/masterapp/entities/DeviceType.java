package com.csr.masterapp.entities;

import java.io.Serializable;

public class DeviceType implements Serializable {
    public Integer id;
    public String shortname;
    public String version;

    public DeviceType(String shortname, String version) {
        this.shortname = shortname;
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
