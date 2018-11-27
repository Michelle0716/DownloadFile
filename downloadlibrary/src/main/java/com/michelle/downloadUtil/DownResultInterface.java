package com.michelle.downloadUtil;

/**
 * author Created by michelle on 2018/11/27.
 * email: 1031983332@qq.com
 * 下载成功或失败回调
 */

public interface DownResultInterface {
    void downloadSuccess();
    void downloadError();
    void downloadStatues(int progress);
}
