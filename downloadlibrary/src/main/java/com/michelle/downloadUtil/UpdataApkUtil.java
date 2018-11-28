package com.michelle.downloadUtil;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * author Created by michelle on 2018/11/28.
 * email: 1031983332@qq.com
 * 下载更新apk
 * 7.0 及以上版本的手机用户在使用到应用部分功能时可能出现 App 崩溃闪退。其中，大部分原因都是由项目中使用到 file:// 类型的 URI 所引发的
 * 从 7.0 开始，Android SDK 中的 StrictMode 策略禁止开发人员在应用外部公开 file:// URI。
 * 具体表现为，当我们在应用中使用包含 file:// URI 的 Intent 离开自己的应用时，程序会发生故障。
 */

public class UpdataApkUtil {

    private String TAG = "UpdataApkUtil.class";

    public void UpdataApkUtil() {

    }


    /**
     * 检测 APP是否已安装。
     *
     * @param context     当前上下文
     * @param packageName 包名
     * @return
     */
    public static boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<String>();

        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        // 判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName);
    }


    /**
     * 在当前app,启动第三方app
     *
     * @param activity
     * @param packageName
     */
    public void openApp(Activity activity, String packageName) {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);  //com.xx.xx是我们获取到的包名
        activity.startActivity(intent);

    }


    /**
     * @param
     * @param context
     */
    public void installApk(Uri contentUri, Context context) {
        Log.i(TAG, "开始执行安装: " + contentUri);


        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//增加读写权限

        } else {
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        }
        if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            Log.e(TAG, "启动");
            context.startActivity(intent);
        }
        //  notificationManager.cancel(1);//取消通知
    }


    public Uri getPathUri(Context context, String filePath) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String packageName = context.getPackageName();
            Log.e(TAG, "包名" + packageName);
            packageName = "com.example.centerm.downloadfile";//applicationId
            packageName = BuildConfig.APPLICATION_ID;
            uri = FileProvider.getUriForFile(context, packageName + ".fileProvider", new File(filePath));
        } else {
            uri = Uri.fromFile(new File(filePath));
        }
        return uri;
    }


    /**
     * @param activity
     * @param url      包映射，如："package:com.demo.CanavaCancel"
     */
    public void deleteApkFile(Activity activity, String url) {
        Uri packageURI = Uri.parse(url);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        activity.startActivity(uninstallIntent);

    }

    /**
     * 获取app版本号
     *
     * @param context
     * @return
     */
    public int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        int versionCode = 0;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    /**
     * 更新下载apk
     *
     * @param context 上下文对象.MyApp.getContext()
     * @param title   程序的名字
     * @param url     下载的url地址
     */

    public static long downLoadApk(Context context, String title, String url) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "ausee.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置 Notification 信息
        request.setTitle(title);
        request.setDescription("下载完成后请点击打开");
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();
        request.setMimeType("application/vnd.android.package-archive");

        // 实例化DownloadManager 对象
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        final long refrence = downloadManager.enqueue(request);

        return refrence;
    }


}
