package com.example.centerm.downloadfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.michelle.downloadUtil.DownLoadService;
import com.michelle.downloadUtil.DownLoaderTask;
import com.michelle.downloadUtil.DownResultInterface;
import com.michelle.downloadUtil.InstalledReceiver;
import com.michelle.downloadUtil.UpdataApkUtil;
import com.michelle.downloadUtil.ZipExtraResultInterface;
import com.michelle.downloadUtil.ZipExtractorTask;

import java.io.File;


/**
 * author Created by michelle on 2018/11/27.
 * email: 1031983332@qq.com
 */

public class MainActivity extends Activity implements DownResultInterface, ZipExtraResultInterface, View.OnClickListener {

    public static final String ROOT_DIR = "/mnt/sdcard/mythroad";
    private final String TAG = "MainActivity";
    private Button text,text2,text3;
    private UpdataApkUtil updataApkUtil;
    private InstalledReceiver mInstalledReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Environment.getExternalStorageDirectory()=" + Environment.getExternalStorageDirectory());
        Log.d(TAG, "getCacheDir().getAbsolutePath()=" + getCacheDir().getAbsolutePath());
        text = (Button) findViewById(R.id.text);
        text3 = (Button) findViewById(R.id.auto_install);
        text2 = (Button) findViewById(R.id.root_install);

        sdIsExits();
        updataApkUtil = new UpdataApkUtil();

        /**
         * 注册安装程序广播(暂时发现在androidManifest.xml中注册，nexus5 Android7.1接收不到广播）
         */
        mInstalledReceiver = new InstalledReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        this.registerReceiver(mInstalledReceiver, filter);


        /**
         * 绑定下载服务
         */
//        Intent intent = new Intent(MainActivity.this, DownLoadService.class);
//        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        text.setOnClickListener(this);
        text2.setOnClickListener(this);
        text3.setOnClickListener(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(mServiceConnection!=null){
//            unbindService(mServiceConnection);
//        }
        if (mInstalledReceiver != null) {
            this.unregisterReceiver(mInstalledReceiver);
        }
    }


    /**
     * 判断SD卡是否存在,并且是否具有读写权限
     */
    private boolean sdIsExits() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            file();
            return true;
        } else {
            new AlertDialog.Builder(MainActivity.this).setTitle("提示")
                    .setMessage("没有内置存储").setIcon(R.drawable.ic_launcher)
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }

                    }).show();
        }

        return false;
    }

    /**
     * 判断文件是否存在
     */
    private void file() {
        File destDir = new File(ROOT_DIR);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    private void showDownLoadDialog() {
        new AlertDialog.Builder(this).setTitle("下载")
                .setMessage("确认下载")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onClick 1 = " + which);
                        dialog.dismiss();
                        doDownLoadWork(Constant.requestPath, Constant.sdDown);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        Log.d(TAG, "onClick 2 = " + which);
                    }
                })
                .show();
    }

    public void showUnzipDialog() {
        Constant.inPath = Constant.sdDown + new File(Constant.requestPath).getName();
        if (Constant.inPath.endsWith("zip")) {
            Log.e(TAG, "zip文件");
            new AlertDialog.Builder(this).setTitle("解压")
                    .setMessage("解压？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onClick 1 = " + which);
                            doZipExtractorWork(Constant.inPath, Constant.outPath);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            Log.d(TAG, "onClick 2 = " + which);
                        }
                    })
                    .show();

        } else if (Constant.inPath.endsWith("apk")) {
            Log.e(TAG, "apk文件");
            updataApkUtil.installApk(installAuto(), getApplicationContext());


        } else {
            return;
        }

    }

    /**
     * 自动安装
     */
    private Uri installAuto() {
        File apkFile = new File(Constant.inPath);
        Log.e(TAG, "apk目录： " + apkFile.getAbsolutePath());
        Uri contentUris;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentUris = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
            updataApkUtil.installApk(contentUris, getApplicationContext());

        } else {
            contentUris = Uri.fromFile(apkFile);

        }
        return contentUris;
    }

    public void doZipExtractorWork(String in, String out) {
        File file = new File(out);
        if (!file.exists()) {
            file.mkdirs();
        }
        ZipExtractorTask task = new ZipExtractorTask(in, out, this, true);
        task.setListener(this);
        task.execute();
    }


    String name = new File(Constant.requestPath).getName();
    ;

    private void doDownLoadWork(String download, String sdOutPath) {
        File file = new File(sdOutPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        DownLoaderTask task = new DownLoaderTask(download, sdOutPath, this);
        task.setListener(this);
        task.execute();
    }


    @Override
    public void downloadSuccess() {
        Log.e(TAG, "downloadSuccess");
        showUnzipDialog();
        //  text.setText("下载成功");
    }

    @Override
    public void downloadError() {
        Log.e(TAG, "downloadError");
        //  text.setText("下载失败");
    }

    @Override
    public void downloadStatues(int progress) {

    }

    @Override
    public void ZipExtraSuccess() {

        //   text.setText("解压成功");
        Log.e(TAG, "ZipExtraSuccess");
    }

    @Override
    public void ZipExtraError() {
        //  text.setText("解压失败");
        Log.e(TAG, "ZipExtraError()");
    }

    @Override
    public void ZipExtraStatus(int progress) {

    }

    private DownLoadService mDownLoadService;
    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownLoadService = ((DownLoadService.MyBinder) service).getServices();
            mDownLoadService.registerReceiver(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownLoadService.setAppUtil(updataApkUtil);

        }
    };


    /**
     * 自动安装和手动安装，差了一个 MyAccessibilityService
     * 手动安装的话，在AndroidMainfest.xml把MyAccessibilityService屏蔽调即可
     * 自动安装需要
     * MyAccessibilityService在清单文件里面注册，并且添加权限
     * @param view
     */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.text) {
            showDownLoadDialog();
        } else if (id == R.id.auto_install) {
            if (Constant.inPath.endsWith("apk")) {
                Log.e(TAG, "apk文件");
                updataApkUtil.installApk(installAuto(), getApplicationContext());
            }
        } else if (id == R.id.root_install) {
            updataApkUtil.installRoot(Constant.inPath, getApplicationContext(), installAuto());
        }

    }
}


