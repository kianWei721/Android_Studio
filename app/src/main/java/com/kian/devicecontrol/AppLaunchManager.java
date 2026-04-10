package com.kian.devicecontrol;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * AppLaunchManager — 负责通过包名启动第三方 App。
 * <p>
 * 使用 {@link PackageManager#getLaunchIntentForPackage(String)} 获取启动 Intent，
 * 如果目标应用未安装则给出提示。
 */
public final class AppLaunchManager {

    private static final String TAG = "AppLaunchManager";

    private final Context context;

    public AppLaunchManager(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 打开指定包名的 App。
     *
     * @param packageName 目标 App 的包名
     * @return true 表示成功发起了启动 Intent；false 表示目标 App 未安装或无法启动
     */
    public boolean launchApp(@NonNull String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(packageName);

        if (launchIntent == null) {
            String msg = "未找到应用: " + packageName;
            Log.w(TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            return false;
        }

        // 确保在新任务栈中启动
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        try {
            context.startActivity(launchIntent);
            Log.i(TAG, "成功启动应用: " + packageName);
            return true;
        } catch (Exception e) {
            String msg = "启动应用失败: " + packageName;
            Log.e(TAG, msg, e);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
