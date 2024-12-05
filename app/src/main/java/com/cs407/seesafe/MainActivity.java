package com.cs407.seesafe;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US); // Set TTS language to US English
            }
        });

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

            tts.speak("You clicked Request Help. Redirecting to the terms and conditions page.", TextToSpeech.QUEUE_FLUSH, null, null);
            // 设置为盲人设备
            getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("IS_VOLUNTEER", false)
                    .apply();

            // 跳转到盲人界面
            Intent intent = new Intent(MainActivity.this, TermsAndConditionsActivity.class);
            startActivity(intent);
        });
    }
    @Override
    protected void onDestroy() {
        // Shutdown TTS when activity is destroyed to release resources
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

