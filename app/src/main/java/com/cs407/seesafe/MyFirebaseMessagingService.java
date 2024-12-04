package com.cs407.seesafe;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String fixedChannelName = "SeeSafe123"; // 固定频道名
    private final String fixedToken = "007eJxTYIiae+u9SLFty0E/90BRP85zOiKW7jEuTxwNpLf1PsnXb1FgSLFINk01tkw0tUhNMzGwNE8yNbe0TDUxMEhMMks1S0pZcMM/vSGQkWG2/C0WRgYIBPG5GIJTU4MT01INjYwZGABImh7y";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("MyFirebaseMessagingService", "收到消息： " + remoteMessage.getData());

        // 检查设备角色
        boolean isVolunteerDevice = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("IS_VOLUNTEER", false);

        if (!isVolunteerDevice) {
            // 如果设备是盲人端，忽略消息
            Log.d("MyFirebaseMessagingService", "此设备是盲人端，忽略通知");
            return;
        }

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

    private void sendNotification(String title, String messageBody, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String channelId = "help_request_channel"; // 固定通知通道 ID
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

        notificationManager.notify(0, notificationBuilder.build());
    }
}
