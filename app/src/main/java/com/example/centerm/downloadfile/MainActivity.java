package com.example.centerm.downloadfile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import com.michelle.downloadUtil.DownLoaderTask;
import com.michelle.downloadUtil.DownResultInterface;
import com.michelle.downloadUtil.ZipExtraResultInterface;
import com.michelle.downloadUtil.ZipExtractorTask;

import java.io.File;


/**
 * author Created by michelle on 2018/11/27.
 * email: 1031983332@qq.com
 */


/**
 * author Created by michelle on 2018/11/27.
 * email: 1031983332@qq.com
 */

public class MainActivity extends Activity  implements DownResultInterface,ZipExtraResultInterface {

    public static final String ROOT_DIR = "/mnt/sdcard/mythroad";
    private final String TAG="MainActivity";
    private TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Environment.getExternalStorageDirectory()="+ Environment.getExternalStorageDirectory());
        Log.d(TAG, "getCacheDir().getAbsolutePath()="+getCacheDir().getAbsolutePath());
        text=(TextView)findViewById(R.id.text);
        showDownLoadDialog();
        sdIsExits();

    }

    /**
     *  判断SD卡是否存在,并且是否具有读写权限
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

    private void showDownLoadDialog(){
        new AlertDialog.Builder(this).setTitle("下载")
                .setMessage("确认下载")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onClick 1 = "+which);
                        dialog.dismiss();
                        doDownLoadWork(requestPath,sdDown);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        dialog.dismiss();
                        Log.d(TAG, "onClick 2 = "+which);
                    }
                })
                .show();
    }

    public void showUnzipDialog(){
        inPath=sdDown+new File(requestPath).getName();
        if(!inPath.endsWith("zip")){
            Log.e(TAG,"非zip文件");
            return;
        }
        new AlertDialog.Builder(this).setTitle("解压")
                .setMessage("解压？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onClick 1 = "+which);
                        doZipExtractorWork(inPath,outPath);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onClick 2 = "+which);
                    }
                })
                .show();
    }

    String path=Environment.getExternalStorageDirectory().getAbsolutePath();
    String inPath=path+"/mythroad/maopao.zip";
    String outPath=path+"/mytsss/";


    public void doZipExtractorWork(String in ,String out){
        File file =new File(out);
        if(!file.exists()){
            file.mkdirs();
        }
        ZipExtractorTask task = new ZipExtractorTask(in,out, this, true);
        task.setListener(this);
        task.execute();
    }

    // String requestPath="http://7xjww9.com1.z0.glb.clouddn.com/Hopetoun_falls.jpg";

    String requestPath="https://github.com/Michelle0716/UtilTool/archive/V1.1.zip";
    String sdDown=path+"/mythroads/";
    String name=new File(requestPath).getName();;

    private void doDownLoadWork(String download,String sdOutPath){
        File file =new File(sdOutPath);
        if(!file.exists()){
            file.mkdirs();
        }
        DownLoaderTask task = new DownLoaderTask(download, sdOutPath, this);
        task.setListener(this);
        task.execute();
    }


    private ProgressDialog mDialog;
    private ProgressDialog getDialog(Context context,String title,int style){
        mDialog=new ProgressDialog(context);
        mDialog.setTitle(title);
        mDialog.setProgressStyle(style);
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO Auto-generated method stub
                dialog.cancel();
            }
        });

        return mDialog;
    }


    @Override
    public void downloadSuccess() {
        Log.e(TAG, "downloadSuccess");
        showUnzipDialog();
        text.setText("下载成功");
    }

    @Override
    public void downloadError() {
        Log.e(TAG,"downloadError");
        text.setText("下载失败");
    }

    @Override
    public void downloadStatues(int progress) {

    }

    @Override
    public void ZipExtraSuccess() {
        text.setText("解压成功");
        Log.e(TAG,"ZipExtraSuccess");
    }

    @Override
    public void ZipExtraError() {
        text.setText("解压失败");
        Log.e(TAG,"ZipExtraError()");
    }

    @Override
    public void ZipExtraStatus(int progress) {

    }
}
