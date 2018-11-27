package com.michelle.downloadUtil;

/**
 * author Created by michelle on 2018/11/28.
 * email: 1031983332@qq.com
 * 解压结果接口
 */

public interface ZipExtraResultInterface {
    void ZipExtraSuccess();
    void ZipExtraError();
    void ZipExtraStatus(int progress);
}
