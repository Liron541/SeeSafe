package com.cs407.seesafe

import android.app.NotificationChannel
import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput

class NotificationHelper private constructor() {

    // Declare a unique ID for each notification instance
    private var notificationId: Int = 0

    // Declare variables for sender name and message content
    private var sender: String? = null
    private var message: String? = null

    companion object {
        const val CHANNEL_ID = "channel_chat"
        const val TEXT_REPLY ="text_reply"

        @Volatile
        private var instance: NotificationHelper? = null

        fun getInstance(): NotificationHelper {
            return instance ?: synchronized(this) {
                instance ?: NotificationHelper().also { instance = it }
            }
        }
    }
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private val notificationItems: ArrayList<NotificationItem> = ArrayList()

    fun appendNotificationItem(sender: String, message: String) {
        val item = NotificationItem(
            sender,
            message,
            notificationItems.size
        )
        notificationItems.add(item)
    }

    fun showNotification(context: Context, id: Int) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val item: NotificationItem = if (id == -1) {
            notificationItems[notificationItems.size - 1]
        } else {
            notificationItems[id]
        }

        val remoteInput = RemoteInput.Builder(TEXT_REPLY)
            .setLabel("Reply")
            .build()

        val replyIntent = Intent(context, ReplyReceiver::class.java).apply {
            putExtra("id", item.getId())
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            item.getId(),
            replyIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_reply_icon,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()


        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setContentTitle(item.getSender())
            .setContentText(item.getMessage())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(action)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(item.getId(), builder.build())
    }

    fun setNotificationContent(sender: String, message: String) {
        this.sender = sender
        this.message = message
        this.notificationId++  // Increment to ensure each notification is unique
    }

    fun showNotification(context: Context) {
        // Ensure that the app has the required POST_NOTIFICATIONS permission before proceeding.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, exit the function without showing the notification
            return
        }

        // Set up a NotificationCompat.Builder instance to create and customize the notification.
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            // Set the small icon for the notification, which will appear in the status bar
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            // Set the title of the notification using the sender's name.
            .setContentTitle(sender)
            // Set the content text of the notification using the message content.
            .setContentText(message)
            // Set the notification priority; PRIORITY_DEFAULT keeps the notification non-intrusive.
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Get a NotificationManagerCompat instance to issue the notification.
        val notificationManager = NotificationManagerCompat.from(context)
        // Send out the notification with the unique notificationId
        notificationManager.notify(notificationId, builder.build())
    }
}