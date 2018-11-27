package com.michelle.downloadUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


/**
 * author Created by michelle on 2018/11/27.
 * email: 1031983332@qq.com
 */


public class DownLoaderTask extends AsyncTask<Void, Integer, Long> {
    private final String TAG = "DownLoaderTask";
    private URL mUrl;
    private File mFile;
    private ProgressDialog mDialog;
    private int mProgress = 0;
    private ProgressReportingOutputStream mOutputStream;
    private Context mContext;
    public DownLoaderTask(String url, String out, Context context){
        super();
        if(context!=null){
            mDialog = new ProgressDialog(context);
            mContext = context;
        }
        else{
            mDialog = null;
        }

        try {
            mUrl = new URL(url);
            String fileName = new File(mUrl.getFile()).getName();
            mFile = new File(out, fileName);
            Log.d(TAG, "out="+out+", name="+fileName+",mUrl.getFile()="+mUrl.getFile());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    private DownResultInterface downloadTask;
    public void setListener(DownResultInterface listener){
        downloadTask=listener;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        //super.onPreExecute();
        if(mDialog!=null){
            mDialog.setTitle("Downloading...");
            mDialog.setMessage(mFile.getName());
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    cancel(true);
                }
            });
            mDialog.show();
        }
    }

    @Override
    protected Long doInBackground(Void... params) {
        // TODO Auto-generated method stub

        return download();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // TODO Auto-generated method stub
        //super.onProgressUpdate(values);
        if(mDialog==null)
            return;
        if(values.length>1){
            int contentLength = values[1];
            if(contentLength==-1){
                mDialog.setIndeterminate(true);
            }
            else{
                mDialog.setMax(100);
            }
        }
        else{
            mDialog.setProgress(values[0].intValue());
        }
    }

    @Override
    protected void onPostExecute(Long result) {
        // TODO Auto-generated method stub
        //super.onPostExecute(result);
        Log.e("download","下载onPostExecute");
        if(mDialog!=null&&mDialog.isShowing()){
            mDialog.dismiss();
        }
        if(isCancelled())
            return;
        if(downloadTask!=null){
            downloadTask.downloadSuccess();
        }

    }

    private long download(){
        URLConnection connection = null;
        int bytesCopied = 0;
        try {
            connection = mUrl.openConnection();
            int length = connection.getContentLength();
            totalSize=length;
            Log.e("download","下载总量： "+length);
            if(mFile.exists()&&length == mFile.length()){
                Log.d(TAG, "file "+mFile.getName()+" already exits!!");
                return 0l;
            }
            mOutputStream = new ProgressReportingOutputStream(mFile);
            publishProgress(0,length);
            bytesCopied =copy(connection.getInputStream(),mOutputStream);
            if(bytesCopied!=length&&length!=-1){
                Log.e(TAG, "Download incomplete bytesCopied="+bytesCopied+", length"+length);
            }else {
                Log.e(TAG, "Download complete bytesCopied="+bytesCopied+", length"+length);
                totalSize=bytesCopied;
            }
            mOutputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytesCopied;
    }
    private int copy(InputStream input, OutputStream output){
        byte[] buffer = new byte[1024*8];
        BufferedInputStream in = new BufferedInputStream(input, 1024*8);
        BufferedOutputStream out  = new BufferedOutputStream(output, 1024*8);
        int count =0,n=0;
        try {
            while((n=in.read(buffer, 0, 1024*8))!=-1){
                out.write(buffer, 0, n);
                count+=n;
            }
            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return count;
    }
    int totalSize ;

    /**
     *
     */
    private final class ProgressReportingOutputStream extends FileOutputStream {

        public ProgressReportingOutputStream(File file)
                throws FileNotFoundException {
            super(file);
            long size=file.getTotalSpace();
            Log.e("download下载","文件大小： "+size);

        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount)
                throws IOException {

            super.write(buffer, byteOffset, byteCount);

            mProgress += byteCount;
            float pressent = (float) mProgress / totalSize * 100;//i 和 mNumber都是int型数
            Log.e(TAG,"解压进度"+pressent);
            int num=(int)(pressent);

            publishProgress(num);

        }

    }
}
