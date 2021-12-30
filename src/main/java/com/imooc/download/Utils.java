package com.imooc.download;

import android.text.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    /**
     * md5 加密
     * @param url
     * @return
     */
    public static String md5Url(String url)
    {
        if (TextUtils.isEmpty(url)){
            return url;
        }
        StringBuffer sb = new StringBuffer();
        try {
            MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(url.getBytes());
            byte[] bytes = digest.digest();
            for (byte b : bytes) {
                // 转成16进制
                String hexStr = Integer.toHexString(b & 0xff);
                sb.append(hexStr.length() == 1 ? "0"+hexStr : hexStr);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 关闭流
     * @param closeable
     */
    public static void close(Closeable closeable){
       if (closeable!=null){
           try {
               closeable.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }
}


