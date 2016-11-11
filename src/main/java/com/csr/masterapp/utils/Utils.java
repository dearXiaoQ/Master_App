
/******************************************************************************
 Copyright Cambridge Silicon Radio Limited 2014 - 2015.
 ******************************************************************************/

package com.csr.masterapp.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

public class Utils {
	
	private static final int AD_DATA_HEADER_LENGTH = 2;
	private static final int AD_TYPE_COMPLETE_LIST_OF_16_BIT_UUIDS = 0x03;
	private static final byte UUID_1 = (byte)0xF1;
    private static final byte UUID_2 = (byte)0xFE;
    
    
	/**
     * Parse a scan record and check if the advert was from a CSRmesh bridge.
     * @param scanRecord The scan data returned from the LeScanCallback.
     * @return True if the scan data contains the UUID for CSRmesh bridge.
     */
    public static boolean isBridgeAdvert(byte [] scanRecord) {
    	int i = 0;
    	while (i < scanRecord.length) {
    		byte length = scanRecord[i];
    		if (length > AD_DATA_HEADER_LENGTH &&
    		    scanRecord[i+1] == AD_TYPE_COMPLETE_LIST_OF_16_BIT_UUIDS &&
    		    scanRecord[i+2] == UUID_1 &&
    		    scanRecord[i+3] == UUID_2) {
    	        return true;
    		}
    		else if (length == 0) {
    			return false;
    		}
    		else {
    			i += scanRecord[i] + 1;
    		}
    	}
    	return false;
    }
	
	public static String hexString(byte [] value) {
        if (value == null) return "null";        
        String out = "";        
        for (byte b : value) {
            out += String.format("%02x", b);
        }
        return out;
    }
	
 /**
 * Method to write text characters to file on SD card. 
 * @param filename
 * @param text
 * @return
 */
static public File writeToSDFile(String filename, String text) {

     File root = android.os.Environment.getExternalStorageDirectory(); 

     File dir = new File (root.getAbsolutePath());
     dir.mkdirs();
     File file = new File(dir, filename);

     try {
         FileOutputStream f = new FileOutputStream(file);
         PrintWriter pw = new PrintWriter(f);
         pw.println(text);
         pw.flush();
         pw.close();
         f.close();
     }
     catch (Exception e) {
         e.printStackTrace();
         return null;
     }
     
     return file;
 }
    /**
     * Convert celsius value to kelvin.
     * @param celsius Temperature in celsius.
     * @return Temperature in kelvin.
     */
    static public double convertCelsiusToKelvin(double celsius) {
        return (273.15 + celsius);
    }

    /**
     * Convert kelvin value to celsius.
     * @param kelvin Temperature in celsius.
     * @return Temperature in celsius.
     */
    static public double convertKelvinToCelsius(double kelvin) {
        return (kelvin - 273.15);
    }


    /**
     * Get the extension of a file.
     * @param file
     * @return extension.
     */
    static public String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf("."));

        } catch (Exception e) {
            return "";
        }

    }

    /**
     * 获取加密后的MAC地址
     * @param context
     * @return md5 mac address
     */
    static public String getMD5MacAddr(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return MD5Utils.encodeByMD5(info.getMacAddress());
    }

    /**
     * JSON解释
     *
     * @param str
     *            JSON字符串
     * @param name
     *            关键字
     * @return result
     *            关键字对应的值
     * */
    public static String ParseJSON(String str,String name){
        if(str.isEmpty()||name.isEmpty())
            return null;

        String result = null;
        try {
            JSONObject myJSobj= new JSONObject(str);
            result = myJSobj.has(name)?myJSobj.getString(name):null;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 用来获得手机扫描到的所有wifi的信息.
     *
     * @param c
     *            上下文
     * @return the current wifi scan result
     */
    static public List<ScanResult> getCurrentWifiScanResult(Context c) {
        WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        return wifiManager.getScanResults();
    }

    static public String getConnectWifiSsid(Context c) {
        WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        return wifiInfo.getSSID();
    }

}
