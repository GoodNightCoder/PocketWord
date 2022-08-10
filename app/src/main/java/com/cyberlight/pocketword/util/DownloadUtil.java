package com.cyberlight.pocketword.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadUtil {
    public static boolean download(Context context, String urlStr, String filename) {
        // 创建URL
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        // 创建HttpURLConnection
        HttpURLConnection urlConnection;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // 设置连接超时
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);
        // 连接
        try {
            urlConnection.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // 获取输入流并写入到文件
        try (InputStream inputStream = urlConnection.getInputStream();
             FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            int readByte;
            while ((readByte = inputStream.read()) != -1) {
                fos.write(readByte);
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
