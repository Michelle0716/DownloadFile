package com.michelle.downloadUtil;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * author Created by michelle on 2018/11/27.
 * email: 1031983332@qq.com
 * 实现无root自动安装，即，我们不需要点击安装按钮，就可以进行安装
 */

public class MyAccessibilityService extends AccessibilityService {

    private static final String TAG = MyAccessibilityService.class.getSimpleName() + ">>>>>";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e(TAG,"-------------------------------------------------------------");
        Log.e(TAG, "onAccessibilityEvent：  "+event.toString());

        int eventType = event.getEventType();//事件类型
        Log.e(TAG,"packageName:" + event.getPackageName() + "");//响应事件的包名，也就是哪个应用才响应了这个事件
        Log.e(TAG,"source:" + event.getSource() + "");//事件源信息
        Log.e(TAG,"source class:" + event.getClassName() + "");//事件源的类名，比如android.widget.TextView
        Log.e(TAG,"event type(int):" + eventType + "");



        /**
         * 在某些rom上，event.getSource()获取到的某些值为null；
         */
        // AccessibilityNodeInfo nodeInfo = event.getSource()：
        AccessibilityNodeInfo nodeInfo = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            nodeInfo = getRootInActiveWindow();
        }
        if (nodeInfo != null) {
            if (!checkIfUnInstall(nodeInfo)) {
                iterateNodes(nodeInfo);
            }
        }
    }

    /**
     * 检查是否是卸载操作（如果安装界面出现“卸载”，那就无计咯）
     *自动先卸载，再安装
     * @param nodeInfo
     * @return
     */
    private Boolean checkIfUnInstall(AccessibilityNodeInfo nodeInfo) {
        Log.e(TAG, "checkIfUnInstall");
        List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByText("卸载");
        if (infos != null && !infos.isEmpty()) {
            return true;
        }
        return false;
    }


    /**
     *重复节点，查询是否具有clickTexts里面的内容
     *  //通过文字找到当前的节点
     * @param nodeInfo
     */
    private void iterateNodes(AccessibilityNodeInfo nodeInfo) {
        String[] clickTexts = {"安装", "继续", "继续安装", "替换", "下一步", "仅允许一次", "完成", "确定"};
        for (String clickText : clickTexts) {
            List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByText(clickText);
            for (AccessibilityNodeInfo info : infos) {
                if (info.isClickable() && info.isEnabled()) {
                    //可点击，可执行，即执行点击操作
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        Log.e(TAG, "无障碍服务已开启");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "无障碍服务已关闭");
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {
        Log.e(TAG, "onInterrupt");
    }
}
