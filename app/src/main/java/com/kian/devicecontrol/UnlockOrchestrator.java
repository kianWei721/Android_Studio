package com.kian.devicecontrol;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * UnlockOrchestrator — 解锁与 App 启动流程编排器。
 * <p>
 * 将各职责单一的组件串联成完整的流程：
 * <pre>
 *   收到目标包名
 *       │
 *       ▼
 *   LockStateChecker: 检查锁屏状态
 *       │
 *       ├─ 未锁屏 → AppLaunchManager: 直接启动目标 App
 *       │
 *       └─ 已锁屏
 *           │
 *           ▼
 *       DeviceWakeHelper: 唤醒屏幕
 *           │
 *           ▼
 *       启动 BridgeUnlockActivity: 请求 dismiss keyguard
 *           │
 *           ▼
 *       解锁成功后由 BridgeUnlockActivity 回调 AppLaunchManager 打开目标 App
 * </pre>
 * <p>
 * 对外只暴露一个入口方法 {@link #unlockAndLaunch(String)}，
 * 调用者无需关心内部编排细节。
 */
public final class UnlockOrchestrator {

    private static final String TAG = "UnlockOrchestrator";

    private final Context context;
    private final LockStateChecker lockStateChecker;
    private final DeviceWakeHelper deviceWakeHelper;
    private final AppLaunchManager appLaunchManager;

    /**
     * 构造方法：自动创建所有依赖组件。
     *
     * @param context 应用上下文（会自动取 applicationContext）
     */
    public UnlockOrchestrator(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.lockStateChecker = new LockStateChecker(context);
        this.deviceWakeHelper = new DeviceWakeHelper(context);
        this.appLaunchManager = new AppLaunchManager(context);
    }

    /**
     * 构造方法：支持注入依赖，方便测试。
     *
     * @param context          应用上下文
     * @param lockStateChecker 锁屏状态检测器
     * @param deviceWakeHelper 亮屏工具
     * @param appLaunchManager App 启动管理器
     */
    public UnlockOrchestrator(@NonNull Context context,
                              @NonNull LockStateChecker lockStateChecker,
                              @NonNull DeviceWakeHelper deviceWakeHelper,
                              @NonNull AppLaunchManager appLaunchManager) {
        this.context = context.getApplicationContext();
        this.lockStateChecker = lockStateChecker;
        this.deviceWakeHelper = deviceWakeHelper;
        this.appLaunchManager = appLaunchManager;
    }

    /**
     * 执行「解锁并启动 App」完整流程。
     * <p>
     * 流程说明：
     * <ol>
     *   <li>通过 {@link LockStateChecker} 检查设备是否处于锁屏状态</li>
     *   <li>如果未锁屏，直接通过 {@link AppLaunchManager} 启动目标 App</li>
     *   <li>如果已锁屏：
     *     <ol>
     *       <li>通过 {@link DeviceWakeHelper} 唤醒屏幕</li>
     *       <li>启动 {@link BridgeUnlockActivity}，在锁屏上方请求 dismiss keyguard</li>
     *       <li>解锁成功后由 {@link BridgeUnlockActivity} 回调打开目标 App</li>
     *     </ol>
     *   </li>
     * </ol>
     *
     * @param targetPackage 目标 App 的包名（如 "com.tencent.mm"）
     */
    public void unlockAndLaunch(@NonNull String targetPackage) {
        Log.i(TAG, "unlockAndLaunch -> targetPackage=" + targetPackage);

        // Step 1：检查锁屏状态
        if (!lockStateChecker.isKeyguardLocked()) {
            // 设备未锁屏，直接启动目标 App
            Log.i(TAG, "设备未锁屏，直接启动目标 App");
            appLaunchManager.launchApp(targetPackage);
            return;
        }

        // Step 2：设备已锁屏，先唤醒屏幕
        Log.i(TAG, "设备已锁屏，开始唤醒屏幕");
        boolean wakeResult = deviceWakeHelper.wakeUpScreen();
        if (!wakeResult) {
            Log.w(TAG, "屏幕唤醒失败，仍尝试启动 BridgeUnlockActivity");
        }

        // Step 3：启动 BridgeUnlockActivity，在锁屏上方请求 dismiss keyguard
        launchBridgeUnlockActivity(targetPackage);
    }

    /**
     * 启动 BridgeUnlockActivity。
     * <p>
     * BridgeUnlockActivity 会在锁屏上方显示，调用系统
     * {@code KeyguardManager.requestDismissKeyguard()} 请求解锁。
     * 解锁成功后由 BridgeUnlockActivity 内部通过 {@link AppLaunchManager} 打开目标 App。
     *
     * @param targetPackage 目标 App 的包名
     */
    private void launchBridgeUnlockActivity(@NonNull String targetPackage) {
        Log.i(TAG, "启动 BridgeUnlockActivity -> targetPackage=" + targetPackage);

        Intent intent = new Intent(context, BridgeUnlockActivity.class);
        intent.putExtra(BridgeUnlockActivity.EXTRA_TARGET_PACKAGE, targetPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 获取 LockStateChecker 实例，供外部查询锁屏状态。
     */
    @NonNull
    public LockStateChecker getLockStateChecker() {
        return lockStateChecker;
    }

    /**
     * 获取 DeviceWakeHelper 实例，供外部执行亮屏操作。
     */
    @NonNull
    public DeviceWakeHelper getDeviceWakeHelper() {
        return deviceWakeHelper;
    }

    /**
     * 获取 AppLaunchManager 实例，供外部直接启动 App。
     */
    @NonNull
    public AppLaunchManager getAppLaunchManager() {
        return appLaunchManager;
    }
}
