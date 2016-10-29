package com.csr.masterapp.utils;

/**
 * Created by mars on 2016/10/26.
 * 对扫描设备的结果进行排序
 */
public class ScanInfo implements Comparable<ScanInfo>{
    public String name;
    public String address;
    public int rssi;

    public ScanInfo(String name, String address, int rssi) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
    }

    @Override
    public int compareTo(ScanInfo another) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (rssi == another.rssi) return EQUAL;
        if (this.rssi < another.rssi) return AFTER;
        if (this.rssi > another.rssi) return BEFORE;

        return EQUAL;
    }
}
