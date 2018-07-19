package com.kai.mobilesafe.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.kai.mobilesafe.R;
import com.kai.mobilesafe.utils.StreamUtil;
import com.kai.mobilesafe.utils.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    // 更新新版本的状态码
    protected static final int UPDATE_VERSION = 100;

    // 进入主界面的状态码
    protected static final int ENTER_HOME = 101;

    // url地址出错状态码
    protected static final int URL_ERROR = 102;

    // io异常状态码
    protected static final int IO_ERROR = 103;

    // json解析出错状态码
    protected static final int JSON_ERROR = 104;

    protected TextView tv_version_name;

    private int mLocalVersionCode;

    private String mVersionDes;

    private String mDownloadUrl;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_VERSION:
                    // 弹出对话框,提示用户更新
                    showUpdateDialog();
                    break;
                case ENTER_HOME:
                    // 进入主界面,activity的跳转过程
                    enterHome();
                    break;
                case URL_ERROR:
                    ToastUtil.show(getApplicationContext(), "url异常");
                    enterHome();
                    break;
                case IO_ERROR:
                    ToastUtil.show(getApplicationContext(), "读取异常");
                    enterHome();
                    break;
                case JSON_ERROR:
                    ToastUtil.show(getApplicationContext(), "json解析异常");
                    enterHome();
                    break;
                default:
            }
        }
    };


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
     * 弹出对话框,提示用户更新
     */
    private void showUpdateDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher_background);
        builder.setTitle("版本更新");
        //设置描述内容
        builder.setMessage(mVersionDes);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 下载apk,apk链接,downloadUrl
                downloadApk();
            }
        });

        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消对话框,进入主界面
                enterHome();
            }
        });
        builder.show();
    }


    private void downloadApk() {
        //apk下载链接地址,放置apk的所在路径

        // 1.判断sk卡是否可用,是否挂载上
        if (true) {
            //2.获取sd路径
            ToastUtil.show(getApplicationContext(),"开始下载");
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + "mobilesafe.apk";
            //3.发送请求,获取apk,并且放置到指定路径
            HttpUtils httpUtils = new HttpUtils();
            //4.发送请求,传递参数(下载地址,下载apk所在的位置)
            httpUtils.download(mDownloadUrl, path, new RequestCallBack<File>() {
                @Override
                public void onSuccess(ResponseInfo<File> responseInfo) {
                    // 下载成功
                    Log.i(TAG, "下载成功");
                    File file = responseInfo.result;
                }

                @Override
                public void onFailure(HttpException e, String s) {
                    // 下载失败
                    Log.i(TAG, "下载失败");
                }

                //刚刚开始下载
                @Override
                public void onStart() {
                    Log.i(TAG, "刚刚开始下载");
                    super.onStart();
                }

                //下载过程中的方法(下载apk总大小,当前的下载位置,是否正在下载)
                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    Log.i(TAG, "下载中....");
                    Log.i(TAG, "total = " + total);
                    Log.i(TAG, "current = " + current);
                    super.onLoading(total, current, isUploading);
                }
            });
        }
        ToastUtil.show(getApplicationContext(),"没开始下载");
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

    /**
     * 检查版本更新
     */
    private void checkVersion() {

        new Thread() {
            @Override
            public void run() {
                //发送请求获取数据，参数则为请求json的链接地址
                // 10.0.2.2仅限于模拟器访问电脑的tomcat
                Message msg = Message.obtain();
                long startTime = System.currentTimeMillis();
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
                    if (connection.getResponseCode() == 200) {

                        //5.以流的形式,将数据获取下来
                        InputStream is = connection.getInputStream();
                        //6.将流转换成字符串(工具类封装)
                        String json = StreamUtil.streamToString(is);
                        Log.i(TAG, json);
                        //7.json解析
                        JSONObject jsonObject = new JSONObject(json);

                        //debug调试,解决问题
                        String versionName = jsonObject.getString("versionName");
                        mVersionDes = jsonObject.getString("versionDes");
                        String versionCode = jsonObject.getString("versionCode");
                        mDownloadUrl = jsonObject.getString("downloadUrl");

                        //日志打印
                        Log.i(TAG, versionName);
                        Log.i(TAG, mVersionDes);
                        Log.i(TAG, versionCode);
                        Log.i(TAG, mDownloadUrl);

                        //8.比对版本号(服务器版本号>本地版本号,提示用户更新)
                        if (mLocalVersionCode < Integer.parseInt(versionCode)) {
                            // 提示用户更新,弹出对话框(UI),消息机制
                            msg.what = UPDATE_VERSION;
                        } else {
                            // 进入主界面
                            msg.what = ENTER_HOME;
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    msg.what = URL_ERROR;
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.what = IO_ERROR;
                } catch (JSONException e) {
                    msg.what = JSON_ERROR;
                } finally {
                    //指定睡眠时间,请求网络的时长超过4秒则不做处理
                    //请求网络的时长小于4秒,强制让其睡眠满4秒
                    long endTime = System.currentTimeMillis();
                    if (endTime - startTime < 4000) {
                        try {
                            Thread.sleep(4000 - (endTime - startTime));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.sendMessage(msg);
                }
            }

            ;
        }.start();
    }

    /**
     * 获取版本名称：清单文件中
     *
     * @return 应用版本名称 返回null代表异常
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
     *
     * @return 非0则表示获取成功
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

    /**
     * 进入主界面
     */
    public void enterHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        // 在开启一个界面后,关闭导航界面
        finish();
    }
}
