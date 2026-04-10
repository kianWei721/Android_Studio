package com.kian.devicecontrol;

import android.app.KeyguardManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * LockStateChecker — 设备锁屏状态检测工具类。
 * <p>
 * 职责：
 * 1. 判断 Keyguard（锁屏界面）是否正在显示 → {@link #isKeyguardLocked()}
 * 2. 判断设备是否处于安全锁定状态（PIN / 图案 / 密码等） → {@link #isDeviceLocked()}
 */
public final class LockStateChecker {

    private static final String TAG = "LockStateChecker";

    private final KeyguardManager keyguardManager;

    public LockStateChecker(@NonNull Context context) {
        this.keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    /**
     * 检查 Keyguard 是否正在显示（即锁屏界面是否可见）。
     *
     * @return true 表示设备正处于锁屏状态
     */
    public boolean isKeyguardLocked() {
        boolean locked = keyguardManager.isKeyguardLocked();
        Log.d(TAG, "isKeyguardLocked: " + locked);
        return locked;
    }

    /**
     * 检查设备是否处于安全锁定状态（需要 PIN / 图案 / 密码 / 生物识别）。
     * <p>
     * 注意：此方法仅在 API 22+ 可用；minSdk 26 下可安全调用。
     *
     * @return true 表示设备需要用户手动解锁
     */
    public boolean isDeviceLocked() {
        boolean locked = keyguardManager.isDeviceLocked();
        Log.d(TAG, "isDeviceLocked: " + locked);
        return locked;
    }

    /**
     * 判断设备是否设置了安全锁（PIN / 密码 / 图案等）。
     *
     * @return true 表示设备设置了安全锁
     */
    public boolean isDeviceSecure() {
        boolean secure = keyguardManager.isDeviceSecure();
        Log.d(TAG, "isDeviceSecure: " + secure);
        return secure;
    }
}
