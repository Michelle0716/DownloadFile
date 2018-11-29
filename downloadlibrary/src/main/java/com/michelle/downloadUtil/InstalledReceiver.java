package com.michelle.downloadUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * author Created by michelle on 2018/11/27.
 * email: 1031983332@qq.com
 * 下载成功或失败回调监听手机系统，app安装和卸载
 */
public class InstalledReceiver extends BroadcastReceiver {

    private static final String TAG = InstalledReceiver.class.getSimpleName() + ">>>>>";

    @Override
    public void onReceive(Context context, Intent intent) {
        //接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            Log.e(TAG, "安装了:" + packageName);
            Intent intentDownload = new Intent(context, DownLoadService.class);
            context.startService(intentDownload);
        }
        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            Log.e(TAG, "卸载了:" + packageName);
        }
    }
}
