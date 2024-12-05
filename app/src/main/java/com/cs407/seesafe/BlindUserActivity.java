package com.cs407.seesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BlindUserActivity extends AppCompatActivity {

    private static final String FIXED_CHANNEL_NAME = "SeeSafe1"; // 固定频道名
    private static final String FIXED_TOKEN = "007eJxTYFixNvaW0qO8Xwu2eTSY66+sjfnB9NBma1S1Y3vszPVGivwKDCkWyaapxpaJphapaSYGluZJpuaWlqkmBgaJSWapZkkpbzQC0xsCGRk+PuNlZWSAQBCfgyE4NTU4MS3VkIEBAO2jIOY="; // 固定 Token
    private static final int PERMISSION_REQUEST_CODE = 1;

    private DatabaseReference databaseReference;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blind);

        // 初始化 Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US); // Set language to US English
            }
        });

        // 请求通知权限
        requestNotificationPermission();

        // 设置“发送帮助请求”按钮的点击事件
        Button sendRequestButton = findViewById(R.id.btn_send_request);
        sendRequestButton.setOnClickListener(v -> {
            tts.speak("Sending help request. Please wait.", TextToSpeech.QUEUE_FLUSH, null, null);
            sendHelpRequest();
        });

        // Set up "Volunteer Friends" button
        Button volunteerFriendsButton = findViewById(R.id.btn_volunteer_friends);
        volunteerFriendsButton.setOnClickListener(v -> {
            tts.speak("Opening Volunteer Friends list.", TextToSpeech.QUEUE_FLUSH, null, null);
            openVolunteerFriendsList();
        });
    }

    private void openVolunteerFriendsList() {
        Log.d("BlindUserActivity", "Opening Volunteer Friends List...");
        Intent intent = new Intent(BlindUserActivity.this, BlindFriendsActivity.class);
        startActivity(intent);
    }

    private void sendHelpRequest() {
        Log.d("BlindUserActivity", "发送帮助请求...");

        // 创建帮助请求对象
        HelpRequest helpRequest = new HelpRequest();
        helpRequest.setUserId("blindUserId");
        helpRequest.setTimestamp(System.currentTimeMillis());
        helpRequest.setChannelName(FIXED_CHANNEL_NAME);

        // 将帮助请求发送到 Firebase 数据库
        databaseReference.child("help_requests").setValue(helpRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("BlindUserActivity", "帮助请求成功发送");
                        tts.speak("Help request sent successfully. Starting video call.", TextToSpeech.QUEUE_FLUSH, null, null);
                        sendNotificationToVolunteers();
                        startVideoChatActivity();
                    } else {
                        Log.e("BlindUserActivity", "帮助请求发送失败", task.getException());
                        tts.speak("Failed to send help request. Please try again.", TextToSpeech.QUEUE_FLUSH, null, null);
                        Toast.makeText(this, "Can not send help request!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startVideoChatActivity() {
        Intent intent = new Intent(this, VideoChatActivity.class);
        intent.putExtra("CHANNEL_NAME", FIXED_CHANNEL_NAME);
        intent.putExtra("TOKEN", FIXED_TOKEN);
        startActivity(intent);
    }

    private String getAccessToken() throws IOException {
        InputStream serviceAccountStream = getResources().openRawResource(R.raw.service_account_key);
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    private void sendNotification(JSONObject notification) {
        new Thread(() -> {
            try {
                String accessToken = getAccessToken();
                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(notification.toString(), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/seesafe-2a331/messages:send")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json; UTF-8")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    Log.i("FCM", "通知发送成功");
                } else {
                    Log.e("FCM", "通知发送失败，响应代码：" + response.code() + "，响应信息：" + response.body().string());
                }
            } catch (Exception e) {
                Log.e("BlindUserActivity", "通知发送失败", e);
            }
        }).start();
    }

    private void sendNotificationToVolunteers() {
        try {
            JSONObject message = new JSONObject();
            JSONObject data = new JSONObject();

            data.put("channelName", FIXED_CHANNEL_NAME);
            data.put("token", FIXED_TOKEN);

            message.put("data", data);
            message.put("topic", "help_requests");

            sendNotification(new JSONObject().put("message", message));
        } catch (JSONException e) {
            Log.e("BlindUserActivity", "构造通知消息失败", e);
        }
    }

    private void requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission have been granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Requires notification permission", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
