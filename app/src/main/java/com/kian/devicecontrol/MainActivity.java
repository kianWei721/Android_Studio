package com.kian.devicecontrol;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.kian.devicecontrol.databinding.ActivityMainBinding;

/**
 * MainActivity — 主控制界面。
 * <p>
 * 提供两个按钮，模拟本地控制命令：
 * 1. 打开微信（com.tencent.mm）
 * 2. 打开系统设置（com.android.settings）
 * <p>
 * 按钮点击后通过 {@link UnlockOrchestrator} 执行「检查锁屏 → 唤醒 → 解锁 → 启动 App」完整流程。
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /** 微信包名 */
    private static final String PACKAGE_WECHAT = "com.tencent.mm";
    /** 系统设置包名 */
    private static final String PACKAGE_SETTINGS = "com.android.settings";

    private ActivityMainBinding binding;
    private UnlockOrchestrator unlockOrchestrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        unlockOrchestrator = new UnlockOrchestrator(this);

        setupButtons();

        Log.i(TAG, "MainActivity 已启动");
    }

    /**
     * 设置按钮点击事件。
     */
    private void setupButtons() {
        // 按钮 1：打开微信
        binding.btnLaunchWechat.setOnClickListener(v -> {
            Log.i(TAG, "用户点击：打开微信");
            Toast.makeText(this, "正在打开微信…", Toast.LENGTH_SHORT).show();
            unlockOrchestrator.unlockAndLaunch(PACKAGE_WECHAT);
        });

        // 按钮 2：打开系统设置
        binding.btnLaunchSettings.setOnClickListener(v -> {
            Log.i(TAG, "用户点击：打开系统设置");
            Toast.makeText(this, "正在打开系统设置…", Toast.LENGTH_SHORT).show();
            unlockOrchestrator.unlockAndLaunch(PACKAGE_SETTINGS);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
