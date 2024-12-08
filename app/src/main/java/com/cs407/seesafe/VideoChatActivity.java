package com.cs407.seesafe;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;
import java.util.UUID;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class VideoChatActivity extends AppCompatActivity {

    private static final String TAG = "VideoChatActivity";
    private static final int PERMISSION_REQUEST_CODE = 1002;

    private final String channelName = "SeeSafe1"; // 固定频道名
    private final String token = "007eJxTYFixNvaW0qO8Xwu2eTSY66+sjfnB9NBma1S1Y3vszPVGivwKDCkWyaapxpaJphapaSYGluZJpuaWlqkmBgaJSWapZkkpbzQC0xsCGRk+PuNlZWSAQBCfgyE4NTU4MS3VkIEBAO2jIOY=";

    private RtcEngine mRtcEngine;
    private TextToSpeech tts;

    private DatabaseReference databaseReference;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_BLIND_USER_ID = "blind_user_id";
    private String currentBlindUserId = null;

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

        databaseReference = FirebaseDatabase.getInstance().getReference("blind_users_locations");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.speak("Welcome to the video call.", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        // Initialize "Send Location" button
        Button sendLocationButton = findViewById(R.id.btn_send_location);
        sendLocationButton.setOnClickListener(v -> {
            sendLocation();
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

    private String getOrCreateBlindUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentBlindUserId = sharedPreferences.getString(KEY_BLIND_USER_ID, null);
        if (currentBlindUserId == null) {
            currentBlindUserId = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_BLIND_USER_ID, currentBlindUserId).apply();
            Log.d(TAG, "Generated new blindUserId: " + currentBlindUserId);
        } else {
            Log.d(TAG, "Retrieved existing blindUserId: " + currentBlindUserId);
        }
        return currentBlindUserId;
    }

    private void sendLocation() {
        String username = getCurrentUsername(); // Retrieve the current user's username

        if (username != null && !username.isEmpty()) {
            // It's a blind user; proceed to send location
            // Initialize LocationManager
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            // Define LocationListener
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    // Once location is received, upload to Firebase
                    uploadLocationToFirebase(location);
                    // Remove updates to stop listening
                    locationManager.removeUpdates(this);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    // Deprecated in API 29
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                    // Optional: Handle provider enabled
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Toast.makeText(VideoChatActivity.this, "Please enable GPS to send location.", Toast.LENGTH_SHORT).show();
                }
            };

            // Check for location permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request permissions if not granted
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_CODE);
                return;
            }

            // Request single location update
            try {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, Looper.getMainLooper());
                Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
            } catch (SecurityException e) {
                Log.e(TAG, "Location permission not granted.", e);
                Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // It's a volunteer; do not send location to Firebase
            Toast.makeText(this, "Volunteers do not send location.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Volunteers cannot send location.");
            tts.speak("Volunteers cannot send location.", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private String getCurrentUsername() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("current_username", null);
    }

    /**
     * Uploads the blind user's location to Firebase.
     *
     * @param location The current location of the blind user.
     */
    private void uploadLocationToFirebase(Location location) {
        String username = getCurrentUsername(); // Retrieve the current user's username

        if (username != null && !username.isEmpty()) {
            // It's a blind user; proceed to send location

            // Retrieve or generate blindUserId
            String blindUserId = getOrCreateBlindUserId();

            // If a previous blindUserId exists, delete its entry
            if (blindUserId != null) {
                databaseReference.child(blindUserId).removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Previous location entry deleted for blindUserId: " + blindUserId);
                            // Now, upload the new location
                            uploadNewLocation(blindUserId, location);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Failed to delete previous location for blindUserId: " + blindUserId, e);
                            Toast.makeText(VideoChatActivity.this, "Failed to update location.", Toast.LENGTH_SHORT).show();
                            tts.speak("Failed to update location.", TextToSpeech.QUEUE_FLUSH, null, null);
                        });
            }
        } else {
            // It's a volunteer; do not send location to Firebase
            Toast.makeText(this, "Volunteers do not send location.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Volunteers cannot send location.");
            tts.speak("Volunteers cannot send location.", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    /**
     * Uploads the new location to Firebase under the specified blindUserId.
     *
     * @param blindUserId The unique ID for the blind user.
     * @param location    The current location of the blind user.
     */
    private void uploadNewLocation(String blindUserId, Location location) {
        // Create a BlindUserLocation object
        BlindUserLocation blindUserLocation = new BlindUserLocation(blindUserId, location.getLatitude(), location.getLongitude(), System.currentTimeMillis());

        // Upload to Firebase under "blind_users_locations/{blindUserId}"
        databaseReference.child(blindUserId).setValue(blindUserLocation)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(VideoChatActivity.this, "Location sent successfully!", Toast.LENGTH_SHORT).show();
                    tts.speak("Location sent successfully.", TextToSpeech.QUEUE_FLUSH, null, null);
                    // Set up onDisconnect to remove the entry automatically when the user disconnects
                    databaseReference.child(blindUserId).onDisconnect().removeValue();
                    Log.d(TAG, "New location uploaded for blindUserId: " + blindUserId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(VideoChatActivity.this, "Failed to send location.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to upload location for blindUserId: " + blindUserId, e);
                    tts.speak("Failed to send location.", TextToSpeech.QUEUE_FLUSH, null, null);
                });
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
}
