package com.cs407.seesafe

import android.content.Context

class MessagingManager(private val context: Context, private val notificationHelper: NotificationHelper) {

    suspend fun sendMessage(sender: User, receiver: User, messageContent: String, messageDao: MessageDao) {
        // Save the message to the database
        val message = Message(
            senderId = sender.id,
            receiverId = receiver.id,
            content = messageContent
        )
        messageDao.insertMessage(message)

        // Trigger a notification for the receiver
        notificationHelper.appendNotificationItem(sender.username ?: "Unknown", messageContent)
        notificationHelper.showNotification(context, -1) // Use -1 to show the latest notification
    }
}
