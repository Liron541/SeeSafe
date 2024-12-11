package com.cs407.seesafe;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.List;

public class VolunteerHomePage extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private Switch switchControl;
    private Switch switchAvailable;
    private Button friendsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helper);

        // Toolbar setup
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + username + "!");
        }

        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String username1 = sharedPreferences.getString("username", null);
        if (username1 != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("volunteer_" + username1)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("VolunteerHomePage", "Subscribed to volunteer_" + username1 + " topic for friend requests");
                        } else {
                            Log.e("VolunteerHomePage", "Failed to subscribe to volunteer_" + username1);
                        }
                    });
        } else {
            Log.w("VolunteerHomePage", "No username found in SharedPreferences; cannot subscribe to volunteer topic.");
        }

        // Buttons setup
        ImageButton mapButton = findViewById(R.id.mapButton);

        mapButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(VolunteerHomePage.this, MapActivity.class);
            startActivity(mapIntent);
        });


        // Handle notifications and permissions
        handleIntent(getIntent());
        requestNotificationPermission();
        setupSwitchAvailable();
    }

    private void handleIntent(Intent intent) {
        String channelName = intent.getStringExtra("CHANNEL_NAME");
        String token = intent.getStringExtra("TOKEN");

        if (channelName != null && token != null) {
            Log.d("VolunteerActivity", "从通知中接收到参数: " + channelName + ", Token: " + token);
            startVideoChat(channelName, token);
            finish();
        }
    }

    private void setupSwitchAvailable() {
        switchAvailable = findViewById(R.id.switch_available);
        switchAvailable.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
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

    // Toolbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_manu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
