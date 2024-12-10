package com.cs407.seesafe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Locale;
import java.io.IOException;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import org.json.JSONException;
import org.json.JSONObject;

public class VideoChatActivity extends AppCompatActivity {

    private final String channelName = "SeeSafe1"; // 固定频道名
    private final String token = "007eJxTYFixNvaW0qO8Xwu2eTSY66+sjfnB9NBma1S1Y3vszPVGivwKDCkWyaapxpaJphapaSYGluZJpuaWlqkmBgaJSWapZkkpbzQC0xsCGRk+PuNlZWSAQBCfgyE4NTU4MS3VkIEBAO2jIOY=";

    private RtcEngine mRtcEngine;
    private TextToSpeech tts;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                setupRemoteVideo(uid); // 调用远端视频设置方法
                tts.speak("A user has joined the call.", TextToSpeech.QUEUE_FLUSH, null, null);
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                Toast.makeText(VideoChatActivity.this, "user offline: " + uid, Toast.LENGTH_SHORT).show();
                tts.speak("A user has left the call.", TextToSpeech.QUEUE_FLUSH, null, null);
                removeRemoteVideo();
            });
        }
    };

    private static final int PERMISSION_REQ_ID = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);
        String blindUserId = "blindUser123";

        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String volunteerUsername = sharedPreferences.getString("username", null);
        Log.d("VideoChatActivity", "Retrieved volunteerUsername from SharedPreferences: " + volunteerUsername);

        Button addFriendButton = findViewById(R.id.btn_add_friend);
        addFriendButton.setOnClickListener(v -> {
            if (volunteerUsername == null || volunteerUsername.isEmpty()) {
                Toast.makeText(VideoChatActivity.this, "Cannot send friend request because the username is unknown.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Use the correct username when sending the friend request
            sendFriendRequest("blindUser123", volunteerUsername);
        });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.speak("Welcome to the video call.", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // 使用固定的频道名和 Token
        if (checkPermissions()) {
            initializeAndJoinChannel();
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
        }

        // 检查权限
        if (checkPermissions()) {
            initializeAndJoinChannel();
        } else {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSION_REQ_ID);
        }

        // 处理结束通话按钮点击事件
        Button endCallButton = findViewById(R.id.btn_end_call);
        endCallButton.setOnClickListener(v -> {
            tts.speak("Ending the call. Thank you for your help! Goodbye.", TextToSpeech.QUEUE_FLUSH, null, null);
            if (mRtcEngine != null) {
                mRtcEngine.leaveChannel(); // 离开频道
            }
            finish(); // 结束当前活动
        });
    }

    private void initializeAndJoinChannel() {
        try {
            // 初始化 RtcEngine
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = "d8c5e39a58ef4097b5799e400ab6e6bd";
            config.mEventHandler = mRtcEventHandler;

            mRtcEngine = RtcEngine.create(config);

            // 启用视频模块
            mRtcEngine.enableVideo();

            // 设置本地视频视图
            setupLocalVideo();

            // 加入频道
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;

            mRtcEngine.joinChannel(token, channelName, 0, options);
            tts.speak("You have joined the video call.", TextToSpeech.QUEUE_FLUSH, null, null);
        } catch (Exception e) {
            throw new RuntimeException("Agora SDK initialization failed, check the parameters", e);
        }
    }

    private void setupLocalVideo() {
        FrameLayout container = findViewById(R.id.local_video_view_container);
        SurfaceView surfaceView = new SurfaceView(this);
        container.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
        mRtcEngine.startPreview();
    }

    private void setupRemoteVideo(int uid) {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.setZOrderMediaOverlay(true);
        container.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
    }

    private void removeRemoteVideo() {
        FrameLayout container = findViewById(R.id.remote_video_view_container);
        container.removeAllViews();
    }

    private String[] getRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            if (checkPermissions()) {
                initializeAndJoinChannel();
            } else {
                Toast.makeText(this, "Permission needs to be granted to continue", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            mRtcEngine.stopPreview();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    private void sendFriendRequest(String blindUserId, String volunteerId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        String requestKey = database.child("friend_requests").push().getKey();
        if (requestKey == null) return;

        FriendRequest friendRequest = new FriendRequest(blindUserId, volunteerId, System.currentTimeMillis());
        database.child("friend_requests").child(requestKey).setValue(friendRequest)
                .addOnSuccessListener(aVoid -> {
                    Log.d("VideoChatActivity", "Friend request stored, sending notification...");
                    sendFriendRequestNotificationToVolunteer(volunteerId, blindUserId, requestKey);
                    Toast.makeText(VideoChatActivity.this, "Friend request sent!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(VideoChatActivity.this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendFriendRequestNotificationToVolunteer(String volunteerId, String blindUserId, String requestKey) {
        try {
            JSONObject message = new JSONObject();
            JSONObject data = new JSONObject();

            data.put("request_type", "friend_request");
            data.put("blindUserId", blindUserId);
            data.put("requestKey", requestKey);

            // Volunteers should subscribe to topic "volunteer_<volunteerId>"
            message.put("data", data);
            message.put("topic", "volunteer_" + volunteerId);

            sendNotification(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(JSONObject message) {
        new Thread(() -> {
            try {
                String accessToken = getAccessToken();
                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(message.toString(), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/seesafe-2a331/messages:send")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json; UTF-8")
                        .post(body)
                        .build();

                okhttp3.Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    System.err.println("Notification sending failed: " + response.code() + " - " + response.message());
                }
                response.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String getAccessToken() throws IOException {
        InputStream serviceAccountStream = getResources().openRawResource(R.raw.service_account_key);
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(Arrays.asList("https://www.googleapis.com/auth/firebase.messaging"));
        googleCredentials.refresh();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}