package com.cs407.seesafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnVolunteerLogin = findViewById(R.id.btn_volunteer_login);
        Button btnRequestHelp = findViewById(R.id.btn_request_help);

        // 志愿者登录
        btnVolunteerLogin.setOnClickListener(v -> {
            // 设置为志愿者设备
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("IS_VOLUNTEER", true)
                    .apply();

            // 跳转到志愿者界面
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // 盲人登录
        btnRequestHelp.setOnClickListener(v -> {
            // 设置为盲人设备
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("IS_VOLUNTEER", false)
                    .apply();

            // 跳转到盲人界面
            Intent intent = new Intent(MainActivity.this, BlindUserActivity.class);
            startActivity(intent);
        });
    }
}

