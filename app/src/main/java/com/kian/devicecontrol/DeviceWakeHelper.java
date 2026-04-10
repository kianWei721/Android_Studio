package com.kian.devicecontrol;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * DeviceWakeHelper — 设备亮屏工具类。
 * <p>
 * 职责：通过 {@link PowerManager.WakeLock} 唤醒屏幕，确保屏幕在一段时间内保持点亮状态，
 * 以便后续操作（如在锁屏上方启动 Activity）可以正常执行。
 * <p>
 * 注意：使用 {@code SCREEN_BRIGHT_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP} 组合，
 * 该方式在非 root、非 Device Owner 场景下仍然可用，适用于大多数普通应用。
 * <p>
 * 需要权限：{@code android.permission.WAKE_LOCK}
 */
public final class DeviceWakeHelper {

    private static final String TAG = "DeviceWakeHelper";

    /** 默认 WakeLock 持有时长（毫秒）：3 秒 */
    private static final long DEFAULT_WAKE_DURATION_MS = 3000L;

    private final Context context;

    /** 当前持有的 WakeLock，避免重复获取造成资源泄漏 */
    private PowerManager.WakeLock currentWakeLock;

    public DeviceWakeHelper(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 唤醒屏幕，使用默认持有时长（3 秒）。
     * <p>
     * WakeLock 会在超时后自动释放，无需手动管理。
     *
     * @return true 表示成功获取 WakeLock 并唤醒屏幕；false 表示 PowerManager 不可用
     */
    public boolean wakeUpScreen() {
        return wakeUpScreen(DEFAULT_WAKE_DURATION_MS);
    }

    /**
     * 唤醒屏幕，指定 WakeLock 持有时长。
     *
     * @param durationMs WakeLock 持有时长（毫秒），超时后自动释放
     * @return true 表示成功获取 WakeLock 并唤醒屏幕；false 表示 PowerManager 不可用
     */
    public boolean wakeUpScreen(long durationMs) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            Log.w(TAG, "PowerManager 不可用，无法唤醒屏幕");
            return false;
        }

        if (pm.isInteractive()) {
            Log.d(TAG, "屏幕已处于亮屏状态，跳过唤醒");
            return true;
        }

        // 释放之前持有的 WakeLock，避免重复获取造成资源泄漏
        releaseCurrentWakeLock();

        // SCREEN_BRIGHT_WAKE_LOCK 已标记弃用，但在非 Device Owner 场景下
        // 仍是最简单可行的唤醒方式。
        @SuppressWarnings("deprecation")
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE,
                TAG + ":WakeUp"
        );

        wakeLock.acquire(durationMs);
        currentWakeLock = wakeLock;
        Log.i(TAG, "屏幕唤醒 WakeLock 已获取（" + durationMs + " ms 超时自动释放）");
        return true;
    }

    /**
     * 释放当前持有的 WakeLock（如果存在且仍处于持有状态）。
     */
    private void releaseCurrentWakeLock() {
        if (currentWakeLock != null && currentWakeLock.isHeld()) {
            currentWakeLock.release();
            Log.d(TAG, "已释放之前的 WakeLock");
        }
        currentWakeLock = null;
    }

    /**
     * 检查屏幕当前是否处于亮屏（交互）状态。
     *
     * @return true 表示屏幕已亮
     */
    public boolean isScreenOn() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            Log.w(TAG, "PowerManager 不可用");
            return false;
        }
        return pm.isInteractive();
    }
}
