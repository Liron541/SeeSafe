package com.cs407.seesafe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String requestKey = intent.getStringExtra("requestKey");
        String blindUserId = intent.getStringExtra("blindUserId");

        if ("ACCEPT_FRIEND_REQUEST".equals(action)) {
            addBlindUserToVolunteerFriends(context, blindUserId);
            Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
        } else if ("DECLINE_FRIEND_REQUEST".equals(action)) {
            // Optional: remove request from DB if you want
            Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
        }
    }

    private void addBlindUserToVolunteerFriends(Context context, String blindUserId) {
        // Get volunteer username from SharedPreferences or passed logic
        String volunteerUsername = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("username", null);
        if (volunteerUsername == null) return;

        AppDatabase db = AppDatabase.getInstance(context);
        UserDao userDao = db.userDao();

        new Thread(() -> {
            User volunteerUser = userDao.getUserByUsername(volunteerUsername);
            if (volunteerUser != null) {
                String currentFriends = volunteerUser.getFriends();
                if (currentFriends == null || currentFriends.isEmpty()) {
                    currentFriends = blindUserId;
                } else {
                    currentFriends = currentFriends + "," + blindUserId;
                }
                volunteerUser.setFriends(currentFriends);
                userDao.insert(volunteerUser); // Insert again to update
            }
        }).start();
    }
}
