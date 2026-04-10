package com.kian.devicecontrol;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * ControlAccessibilityService — 设备控制用无障碍服务骨架。
 * <p>
 * 当前仅做日志输出，不实现自动点击密码键盘等复杂逻辑。
 * <p>
 * 后续可在 {@link #onAccessibilityEvent} 或独立方法中
 * 调用 {@link #performGlobalAction(int)} 实现返回、Home 等全局操作。
 */
public class ControlAccessibilityService extends AccessibilityService {

    private static final String TAG = "CtrlAccessibilitySvc";

    // ─── 生命周期 ────────────────────────────────────────────

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i(TAG, "onServiceConnected -> 控制无障碍服务已连接");

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
        Log.i(TAG, "onDestroy -> 控制无障碍服务已销毁");
    }

    // ─── 事件回调 ────────────────────────────────────────────

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) {
            return;
        }

        CharSequence packageName = event.getPackageName();
        int eventType = event.getEventType();

        Log.d(TAG, "onAccessibilityEvent -> type=" + eventType
                + ", package=" + (packageName != null ? packageName : "null"));

        // TODO: 根据 packageName / eventType 分发到具体处理逻辑
    }

    @Override
    public void onInterrupt() {
        Log.w(TAG, "onInterrupt -> 控制无障碍服务被中断");
    }

    // ─── performGlobalAction 扩展位置 ────────────────────────

    /**
     * 预留：执行全局操作（如返回、Home、最近任务等）。
     * <p>
     * 调用示例：
     * <pre>
     *   executeGlobalAction(GLOBAL_ACTION_BACK);
     *   executeGlobalAction(GLOBAL_ACTION_HOME);
     *   executeGlobalAction(GLOBAL_ACTION_RECENTS);
     * </pre>
     *
     * @param action 全局操作常量，见 {@link AccessibilityService} GLOBAL_ACTION_* 常量
     * @return 操作是否成功
     */
    public boolean executeGlobalAction(int action) {
        Log.i(TAG, "executeGlobalAction -> action=" + action);
        return performGlobalAction(action);
    }
}
