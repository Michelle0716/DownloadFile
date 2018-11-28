# DownloadFile
下载和解压文件

# 异常
 1、 java.lang.IllegalArgumentException: Failed to find configured root that contains /storage/emulated
 
 2、FileProvider.getUriForFile 空指针
 
 
 3、升级到Android8.0后，一直提示”安装解析包错误”
 
# 异常原因
随着Android 7.0的到来引入“私有目录被限制访问”，“StrictMode API 政策”，为了进一步提高私有文件的安全性，Android不再由开发者放宽私有文件的访问权限，之前我们一直使用”file:///”绝对路径来传递文件地址的方式，在接收方访问时很容易触发SecurityException的异常。


FileProvider是ContentProvider的一个特殊子类，本质上还是基于ContentProvider的实现，FileProvider会把”file:///”的路径转换为特定的”content://”形式的content uri，接收方通过这个uri再使用ContentResolver去媒体库查询解析。


# 适配7.0
## 使用FileProvider四部曲
####   第一步，指定一个FileProvider。
在AndroidManifest.xml中声明一个条目

authorities的属性为build.grade的applicationId+自定义命名名称；

android:exported="false"因为安全限制，一定要设置为false。

```
<application
        ...>
       <provider
                android:name="android.support.v4.content.FileProvider"
                android:authorities="${applicationId}.fileProvider"
                android:exported="false"
                android:grantUriPermissions="true">
                <meta-data
                    android:name="android.support.FILE_PROVIDER_PATHS"
                    android:resource="@xml/file_paths" />
            </provider>
        ...
    </application>

```

## 第二步，指定想分享的目录
在res目录下新建一个xml目录，在xml目录下面新建一个xml文件

命名要和androidMainfest里面的android:resource="@xml/file_paths"一致：file_paths



```

<?xml version="1.0" encoding="utf-8"?>
<resources>
    <paths>
        <external-path
            name="files_root"
            path="mythroads/" />

    </paths>


</resources>


```

### 关于paths设置


```

    <!--代表外部存储区域的根目录下的文件 Environment.getExternalStorageDirectory()/DCIM/camerademo目录-->
    <external-path name="hm_DCIM" path="DCIM/camerademo" />
  
  
    <!--代表app 私有的存储区域 Context.getFilesDir()目录下的images目录 /data/user/0/com.hm.camerademo/files/images-->
    <files-path name="hm_private_files" path="images" />
    
    
    <!--代表app 私有的存储区域 Context.getCacheDir()目录下的images目录 /data/user/0/com.hm.camerademo/cache/images-->
    <cache-path name="hm_private_cache" path="images" />
    
    
    
    <!--代表app 外部存储区域根目录下的文件 Context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)目录下的Pictures目录-->
    <!--/storage/emulated/0/Android/data/com.hm.camerademo/files/Pictures-->
    <external-files-path name="hm_external_files" path="Pictures" />
    
    
    
    
    <!--代表app 外部存储区域根目录下的文件 Context.getExternalCacheDir目录下的images目录-->
    <!--/storage/emulated/0/Android/data/com.hm.camerademo/cache/images-->
    <external-cache-path name="hm_external_cache" path="" />


```

因为我的apk是存放在

/storage/emulated/0/mythroads/test.apk

所以采用external-path

如果目录写错，就会出现如异常一错误。


## 三、生成可用路径

```
if(Constant.inPath.endsWith("apk")){
            File apkFile = new File(Constant.inPath);
            Log.e(TAG,"apk目录： "+apkFile.getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUris = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID+ ".fileProvider", apkFile);
                installApk(contentUris,getApplicationContext());

            }else {
                Uri uri = Uri.fromFile(apkFile);
                installApk(uri,getApplicationContext());

            }

        }
```

### 注意
FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID+ ".fileProvider", apkFile);

第二个参数，最好是和androidMainfest.xml里面的android:authorities一致

## 四、安装

```
public void installApk(Uri contentUri,Context context ) {
        Log.i(TAG, "开始执行安装: " + contentUri);


       Intent intent = new Intent(Intent.ACTION_VIEW);
       intent.addCategory(Intent.CATEGORY_DEFAULT);
       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
           intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//增加读写权限

       }else {
           intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
       }
       if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
           Log.e(TAG,"启动");
           context.startActivity(intent);
       }
        //  notificationManager.cancel(1);//取消通知
    }
    
    
```

### 备注
7.0以下路径：
file:///storage/emulated/0/Android/data/com.hm.camerademo/files/Pictures/20170225_140305187933259.jpg 

7.0以上转化后：

xxx指的是androidMainfest.xml里面的android:authorities
yy指的是xml的file_paths的路径下的转换名字name=""

content://xxxx/yy/LLS-v4.0-595-20160908-143200.apk


# 适配8.0
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES"/>


## 适配转换原则
file://到content://的转换规则：

1.替换前缀：把file://替换成content://${android:authorities}。

2.匹配和替换

2.1.遍历的子节点，找到最大能匹配上文件路径前缀的那个子节点。

2.2.用path的值替换掉文件路径里所匹配的内容。

3.文件路径剩余的部分保持不变。


##  集成



```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```




```
dependencies {
	        implementation 'com.github.Michelle0716:DownloadFile:Tag'
	}
```



监听下载和压缩

implements DownResultInterface, ZipExtraResultInterface 


下载



```
DownLoaderTask task = new DownLoaderTask(download, sdOutPath, this);
        task.setListener(this);
        task.execute();
```

解压


```
ZipExtractorTask task = new ZipExtractorTask(in, out, this, true);
        task.setListener(this);
        task.execute();
        
```

安装


```
if (Constant.inPath.endsWith("apk")) {
            Log.e(TAG, "apk文件");
            File apkFile = new File(Constant.inPath);
            Log.e(TAG, "apk目录： " + apkFile.getAbsolutePath());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri contentUris = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileProvider", apkFile);
                updataApkUtil.installApk(contentUris, getApplicationContext());

            } else {
                Uri uri = Uri.fromFile(apkFile);
                updataApkUtil.installApk(uri, getApplicationContext());

            }

        } 
        
        
```

  
  
  
