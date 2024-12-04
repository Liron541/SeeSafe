package com.cs407.seesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.messaging.FirebaseMessaging;

public class VolunteerActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private Switch switchAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer);

        // 检查是否通过通知传递了参数
        handleIntent(getIntent());

        // 请求通知权限
        requestNotificationPermission();

        // 初始化 UI
        setupUI();
    }

    private void handleIntent(Intent intent) {
        String channelName = intent.getStringExtra("CHANNEL_NAME");
        String token = intent.getStringExtra("TOKEN");

        if (channelName != null && token != null) {
            // 如果接收到通知参数，直接跳转到视频聊天界面
            Log.d("VolunteerActivity", "从通知中接收到参数: " + channelName + ", Token: " + token);
            startVideoChat(channelName, token);
            finish(); // 结束当前活动，避免返回到登录界面
        }
    }

    private void setupUI() {
        switchAvailable = findViewById(R.id.switch_available);
        switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 志愿者上线，订阅主题
                FirebaseMessaging.getInstance().subscribeToTopic("help_requests")
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Online, waiting for help request", Toast.LENGTH_SHORT).show();
                                Log.d("VolunteerActivity", "已成功订阅主题 help_requests");
                            } else {
                                Toast.makeText(this, "fail to go online", Toast.LENGTH_SHORT).show();
                                Log.e("VolunteerActivity", "订阅主题失败", task.getException());
                            }
                        });
            } else {
                // 志愿者下线，取消订阅
                FirebaseMessaging.getInstance().unsubscribeFromTopic("help_requests")
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Offline now", Toast.LENGTH_SHORT).show();
                                Log.d("VolunteerActivity", "已成功取消订阅主题 help_requests");
                            } else {
                                Toast.makeText(this, "fail to go offline", Toast.LENGTH_SHORT).show();
                                Log.e("VolunteerActivity", "取消订阅失败", task.getException());
                            }
                        });
            }
        });
    }

    private void startVideoChat(String channelName, String token) {
        Intent intent = new Intent(this, VideoChatActivity.class);
        intent.putExtra("CHANNEL_NAME", channelName);
        intent.putExtra("TOKEN", token);
        startActivity(intent);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE);
        }
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Requires notification permission", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
