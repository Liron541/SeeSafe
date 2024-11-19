package com.cs407.seesafe

class NotificationItem(
    private val sender: String?,
    private val message: String?,
    private val id: Int = -1
) {
    fun getSender(): String? {
        return sender
    }

    fun getMessage(): String? {
        return message
    }

    fun getId(): Int {
        return id
    }
}