package com.csr.masterapp.utils;

import android.util.Log;

import java.security.MessageDigest;


public class MD5Utils {

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8",
            "9", "a", "b", "c", "d", "e", "f"};


    public static String generateMD5(String inputStr) {
        Log.d("MD5Cipher", encodeByMD5(inputStr));
        return encodeByMD5(inputStr).toUpperCase();
    }

    /**
     * 对字符串进行MD5加密*/
    public static String encodeByMD5(String originStr) {
        if (originStr != null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] results = md.digest(originStr.getBytes());
                String resultStr = byteArrayToHexString(results);
                //return resultStr.substring(8, 24);
                return resultStr;
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }



    /**
     * 转换字节数组为十六进制字符串
     * @param bytes 字节数组
     * @return    十六进制字符
     */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            resultSb.append(byteToHexString(bytes[i]));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

}
