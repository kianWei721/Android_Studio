package com.kian.devicecontrol;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * DeviceController — 设备控制编排器。
 * <p>
 * 将锁屏状态检测、屏幕唤醒、解锁桥接、App 启动等步骤串联在一起，
 * 对外提供统一的 {@link #wakeAndLaunch(String)} 入口。
 * <p>
 * 流程：
 * 1. 检查是否锁屏 → 如果未锁屏，直接打开 App
 * 2. 唤醒屏幕（通过 PowerManager.WakeLock）
 * 3. 启动 {@link BridgeUnlockActivity}，在锁屏上方请求解锁
 * 4. 解锁成功后由 BridgeUnlockActivity 回调打开目标 App
 */
public final class DeviceController {

    private static final String TAG = "DeviceController";

    private final Context context;
    private final LockStateChecker lockStateChecker;
    private final AppLaunchManager appLaunchManager;

    public DeviceController(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.lockStateChecker = new LockStateChecker(context);
        this.appLaunchManager = new AppLaunchManager(context);
    }

    /**
     * 唤醒屏幕并启动目标 App。
     * <p>
     * 如果设备未锁屏，直接启动；
     * 如果设备锁屏，先唤醒屏幕，然后通过 BridgeUnlockActivity 请求 dismiss keyguard。
     *
     * @param targetPackage 目标 App 的包名
     */
    public void wakeAndLaunch(@NonNull String targetPackage) {
        Log.i(TAG, "wakeAndLaunch -> targetPackage=" + targetPackage);

        if (!lockStateChecker.isKeyguardLocked()) {
            // 设备未锁屏，直接启动目标 App
            Log.i(TAG, "设备未锁屏，直接启动目标 App");
            appLaunchManager.launchApp(targetPackage);
            return;
        }

        // 设备锁屏 → 先唤醒屏幕
        wakeUpScreen();

        // 启动 BridgeUnlockActivity，在锁屏上方请求 dismiss keyguard
        Log.i(TAG, "设备锁屏，启动 BridgeUnlockActivity");
        Intent intent = new Intent(context, BridgeUnlockActivity.class);
        intent.putExtra(BridgeUnlockActivity.EXTRA_TARGET_PACKAGE, targetPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 通过 PowerManager.WakeLock 唤醒屏幕（短暂持有，确保屏幕点亮）。
     */
    private void wakeUpScreen() {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (pm == null) {
            Log.w(TAG, "PowerManager 不可用");
            return;
        }

        // 使用 SCREEN_BRIGHT_WAKE_LOCK + ACQUIRE_CAUSES_WAKEUP 唤醒并点亮屏幕。
        // FULL_WAKE_LOCK 已弃用（API 17+），SCREEN_BRIGHT_WAKE_LOCK 同样弃用但更轻量，
        // 在非 root、非 device owner 场景下仍是可用的简易方式。
        @SuppressWarnings("deprecation")
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE,
                TAG + ":WakeUp"
        );

        // 持有 3 秒后自动释放，确保屏幕点亮足够长以让 BridgeUnlockActivity 启动
        wakeLock.acquire(3000L);
        Log.i(TAG, "屏幕唤醒 WakeLock 已获取（3 秒超时自动释放）");
    }

    /** 暴露 AppLaunchManager 以便 BridgeUnlockActivity 使用。 */
    @NonNull
    public AppLaunchManager getAppLaunchManager() {
        return appLaunchManager;
    }

    /** 暴露 LockStateChecker 以便外部查询锁屏状态。 */
    @NonNull
    public LockStateChecker getLockStateChecker() {
        return lockStateChecker;
    }
}
