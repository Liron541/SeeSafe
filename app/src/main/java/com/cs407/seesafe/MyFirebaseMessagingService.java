package com.cs407.seesafe;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String fixedChannelName = "SeeSafe1"; // 固定频道名
    private final String fixedToken = "007eJxTYFixNvaW0qO8Xwu2eTSY66+sjfnB9NBma1S1Y3vszPVGivwKDCkWyaapxpaJphapaSYGluZJpuaWlqkmBgaJSWapZkkpbzQC0xsCGRk+PuNlZWSAQBCfgyE4NTU4MS3VkIEBAO2jIOY=";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("MyFirebaseMessagingService", "收到消息： " + remoteMessage.getData());

        // 检查设备角色
        boolean isVolunteerDevice = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("IS_VOLUNTEER", true);

        if (!isVolunteerDevice) {
            // 如果设备是盲人端，忽略消息
            Log.d("MyFirebaseMessagingService", "此设备是盲人端，忽略通知");
            return;
        }

        // 从数据中检查请求类型
        String requestType = remoteMessage.getData().get("request_type");

        if ("friend_request".equals(requestType)) {
            // 如果是好友请求
            String blindUserId = remoteMessage.getData().get("blindUserId");
            String requestKey = remoteMessage.getData().get("requestKey");
            if (blindUserId != null && requestKey != null) {
                showFriendRequestNotification(blindUserId, requestKey);
            } else {
                Log.w("MyFirebaseMessagingService", "Friend request missing blindUserId or requestKey");
            }
        } else {
            // 处理帮助请求或默认消息
            // 设置默认通知内容
            String title = "Help!";
            String messageBody = "Blind users need help";

            // 如果消息包含通知字段，覆盖默认内容
            if (remoteMessage.getNotification() != null) {
                title = remoteMessage.getNotification().getTitle() != null
                        ? remoteMessage.getNotification().getTitle()
                        : title;
                messageBody = remoteMessage.getNotification().getBody() != null
                        ? remoteMessage.getNotification().getBody()
                        : messageBody;
            }

            // 创建点击通知时启动的 Intent
            Intent intent = new Intent(this, VolunteerActivity.class);
            intent.putExtra("CHANNEL_NAME", fixedChannelName);
            intent.putExtra("TOKEN", fixedToken);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // 调用发送通知方法
            sendNotification(title, messageBody, intent);

        }
        Log.d("MyFirebaseMessagingService", "Message data: " + remoteMessage.getData());
    }

    private void showFriendRequestNotification(String blindUserId, String requestKey) {
        // Create intents for accept and decline actions
        Intent acceptIntent = new Intent(this, NotificationActionReceiver.class);
        acceptIntent.setAction("ACCEPT_FRIEND_REQUEST");
        acceptIntent.putExtra("requestKey", requestKey);
        acceptIntent.putExtra("blindUserId", blindUserId);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent declineIntent = new Intent(this, NotificationActionReceiver.class);
        declineIntent.setAction("DECLINE_FRIEND_REQUEST");
        declineIntent.putExtra("requestKey", requestKey);
        declineIntent.putExtra("blindUserId", blindUserId);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                this, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set up channel for friend request notifications
        String channelId = "friend_request_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure this icon exists
                .setContentTitle("Friend Request")
                .setContentText("A blind user wants to add you as a friend.")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .addAction(R.drawable.ic_accept, "Accept", acceptPendingIntent)
                .addAction(R.drawable.ic_decline, "Decline", declinePendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel if on Oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Friend Request Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        // Check notification permission if on Android 13+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            try {
                // Use a unique notification ID (System.currentTimeMillis())
                notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                Log.d("MyFirebaseMessagingService", "Friend request notification displayed.");
            } catch (SecurityException e) {
                Log.w("MyFirebaseMessagingService", "SecurityException: Notification permission not granted");
            }
        } else {
            Log.w("MyFirebaseMessagingService", "Notification permission not granted, cannot show notification.");
        }
    }

    private void sendNotification(String title, String messageBody, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "help_request_channel"; // 固定通知通道 ID

        //accept message & decline message
        Intent acceptIntent = new Intent("accept_action");
        acceptIntent.setClass(this, NotificationActionReceiver.class);
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Intent for Decline action
        Intent declineIntent = new Intent("decline_action");
        declineIntent.setClass(this, NotificationActionReceiver.class);
        PendingIntent declinePendingIntent = PendingIntent.getBroadcast(
                this, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // 确保有对应的图标资源
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Help Request Notification",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(0, notificationBuilder.build());
        } else {
            Log.w("MyFirebaseMessagingService", "Notification permission not granted, cannot show notification.");
        }


    }
}
