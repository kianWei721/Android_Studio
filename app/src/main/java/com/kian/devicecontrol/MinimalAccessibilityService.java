package com.kian.devicecontrol;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * MinimalAccessibilityService — 最小无障碍服务骨架。
 * <p>
 * 当前仅保留服务结构和日志输出，不实现复杂的自动点击或 UI 操控逻辑。
 * <p>
 * 激活方式：用户需在「设置 → 无障碍 → DeviceControlAgent」中手动开启。
 * <p>
 * 后续可在此基础上扩展：
 * - 监听指定 App 的窗口变化
 * - 自动执行 UI 操作（如点击按钮）
 */
public class MinimalAccessibilityService extends AccessibilityService {

    private static final String TAG = "MinAccessibilitySvc";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 骨架实现：仅记录事件类型和来源包名
        if (event == null) {
            return;
        }

        CharSequence packageName = event.getPackageName();
        int eventType = event.getEventType();

        Log.d(TAG, "onAccessibilityEvent -> type=" + eventType
                + ", package=" + (packageName != null ? packageName : "null"));
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "onInterrupt -> 无障碍服务被中断");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "onServiceConnected -> 无障碍服务已连接");

        // 通过代码配置服务信息（也可仅通过 XML 配置）
        AccessibilityServiceInfo info = getServiceInfo();
        if (info != null) {
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
            info.notificationTimeout = 200;
            setServiceInfo(info);
            Log.i(TAG, "AccessibilityServiceInfo 已更新");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy -> 无障碍服务已销毁");
    }
}
