package com.cs407.seesafe
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.RemoteInput

class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput: Bundle? = RemoteInput.getResultsFromIntent(intent)
        val id = intent.getIntExtra("id", -1)

        if (remoteInput != null) {
            val charSequence: CharSequence? =
                remoteInput.getCharSequence(NotificationHelper.TEXT_REPLY)
            if (charSequence == null) return
            Toast.makeText(
                context,
                context.getString(R.string.replied, charSequence.toString(), id),
                Toast.LENGTH_LONG
            ).show()
            NotificationHelper.getInstance().showNotification(context, id)
        }
    }
}