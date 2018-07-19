package com.kai.mobilesafe.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kai.mobilesafe.R;
import com.kai.mobilesafe.utils.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    private TextView tv_version_name;

    private int mLocalVersionCode;

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // 初始化UI
        initUI();
        // 初始化数据
        initData();
    }

    /**
     * 初始化UI方法
     */
    private void initUI() {
        tv_version_name = (TextView) findViewById(R.id.tv_version_name);
    }

    /**
     * 获取数据方法
     */
    private void initData() {
        //1.应用版本名称
        tv_version_name.setText("版本名称:" + getVersionName());

        //2.检测（本地版本号和服务器版本号比对）是否有更新，如果有跟新，提示用户下载（number）
        mLocalVersionCode = getVersionCode();
        //3.获取服务器版本号（客户端发请求，服务端给响应，（json))
        //http://www.xxx.com.update.json?key=value  返回200请求成功，流的方式读取下来
        //json中内容包括：
        /* 更新版本的版本名称
         * 新版本的描述信息
         * 服务器版本号
         * 新版本apk下载地址
         */
        checkVersion();
    }

    private void checkVersion() {

        new Thread(){
            @Override
            public void run() {
                //发送请求获取数据，参数则为请求json的链接地址
                // 10.0.2.2仅限于模拟器访问电脑的tomcat
                try {
                    // 1.封装url地址 http://10.0.2.2:8080/update11.json
                    URL url = new URL("http://10.0.2.2:8080/update11.json");
                    // 2. 开启一个链接
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //3.设置常见请求参数(请求头)

                    // 请求超时
                    connection.setConnectTimeout(2000);
                    // 读取超时
                    connection.setReadTimeout(2000);
                    // 默认就是get请求方式
//                    connection.setRequestMethod("GET");

                    // 4.获取请求成功响应码
                    if (connection.getResponseCode()  == 200) {

                        //5.以流的形式,将数据获取下来
                        InputStream is = connection.getInputStream();
                        //6.将流转换成字符串(工具类封装)
                        String json = StreamUtil.streamToString(is);

                        Log.i(TAG, json);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    /**
     * 获取版本名称：清单文件中
     * @return  应用版本名称 返回null代表异常
     */
    public String getVersionName() {
        //1.包管理者对象packageManager
        PackageManager pm = getPackageManager();
        //2.从包的管理者对象中，获取指定包名的基本信息（版本名称，版本号），传0代表获取基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            //3.获取版本名称
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 返回版本号
     * @return  非0则表示获取成功
     */
    public int getVersionCode() {
        //1.包管理者对象packageManager
        PackageManager pm = getPackageManager();
        //2.从包的管理者对象中，获取指定包名的基本信息（版本名称，版本号），传0代表获取基本信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
            //3.获取版本名称
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
