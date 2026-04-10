package com.kian.devicecontrol;

import android.app.Activity;
import android.app.KeyguardManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;

/**
 * BridgeUnlockActivity — 桥接解锁 Activity。
 * <p>
 * 职责：
 * 1. 在锁屏上方显示（通过 Manifest 属性 showOnLockScreen + turnScreenOn）
 * 2. 调用 {@link KeyguardManager#requestDismissKeyguard} 请求系统解锁
 * 3. 如果设备设置了安全锁（PIN / 密码 / 图案），系统会弹出验证界面，
 *    等待用户手动输入，解锁后打开目标 App
 * 4. 如果设备仅为滑动锁屏，系统会直接 dismiss keyguard
 * <p>
 * 重要边界：不实现自动输入密码，仅依赖系统 API 请求解锁。
 */
public class BridgeUnlockActivity extends Activity {

    private static final String TAG = "BridgeUnlockActivity";

    /** Intent extra：目标 App 包名 */
    public static final String EXTRA_TARGET_PACKAGE = "extra_target_package";

    private String targetPackage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 确保可以在锁屏上方显示并点亮屏幕（兼容 API 27+ 的新方式）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        // 获取目标包名
        targetPackage = getIntent().getStringExtra(EXTRA_TARGET_PACKAGE);
        Log.i(TAG, "onCreate -> targetPackage=" + targetPackage);

        // 请求 dismiss keyguard
        requestDismissKeyguard();
    }

    /**
     * 调用系统 KeyguardManager.requestDismissKeyguard()。
     * <p>
     * API 26+（minSdk 26）可安全调用。
     * 系统会根据当前锁屏类型决定行为：
     * - 滑动锁屏 → 直接解锁
     * - 安全锁屏（PIN / 密码 / 图案 / 生物识别）→ 弹出系统验证界面
     */
    private void requestDismissKeyguard() {
        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        if (km == null) {
            Log.e(TAG, "KeyguardManager 不可用，直接关闭");
            finish();
            return;
        }

        km.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
            @Override
            public void onDismissSucceeded() {
                Log.i(TAG, "Keyguard dismiss 成功");
                launchTargetApp();
                finish();
            }

            @Override
            public void onDismissCancelled() {
                // 用户取消了解锁（按返回键等）
                Log.w(TAG, "Keyguard dismiss 被取消");
                finish();
            }

            @Override
            public void onDismissError() {
                Log.e(TAG, "Keyguard dismiss 发生错误");
                finish();
            }
        });
    }

    /**
     * 解锁成功后，打开目标 App。
     */
    private void launchTargetApp() {
        if (targetPackage == null || targetPackage.isEmpty()) {
            Log.w(TAG, "目标包名为空，跳过启动");
            return;
        }

        AppLaunchManager launcher = new AppLaunchManager(this);
        launcher.launchApp(targetPackage);
    }
}
