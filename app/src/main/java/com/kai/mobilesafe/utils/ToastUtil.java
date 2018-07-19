package com.kai.mobilesafe.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    /**
     * 打印Toast
     * @param context   上下文环境
     * @param msg       打印文本内容
     */
    public static void show(Context context, String msg) {
        Toast.makeText(context, msg, 0).show();
    }
}
