package com.cs407.seesafe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.ArrayList;
import java.util.List;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String requestKey = intent.getStringExtra("requestKey");
        String blindUserId = intent.getStringExtra("blindUserId");

        if ("ACCEPT_FRIEND_REQUEST".equals(action)) {
            addBlindUserToVolunteerFriends(context, blindUserId);
            Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();

            // Retrieve volunteer username
            String volunteerUsername = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .getString("username", null);
            Log.d("Debug:VolunteerUsername", "Volunteer username: " + volunteerUsername);

            if (volunteerUsername != null && !volunteerUsername.isEmpty()) {
                // Also add the volunteer's username to the blind user's friend list
                addVolunteerToBlindUserFriends(context, blindUserId, volunteerUsername);
            } else {
                // If for some reason volunteerUsername is not found
                Toast.makeText(context, "No volunteer username found to add to blind user's friend list", Toast.LENGTH_SHORT).show();
            }
        } else if ("DECLINE_FRIEND_REQUEST".equals(action)) {
            // Optional: handle decline if needed
            Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
        }
    }

    private void addBlindUserToVolunteerFriends(Context context, String blindUserId) {
        // Existing logic adding the blind user to volunteer's friend list
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

    private void addVolunteerToBlindUserFriends(Context context, String blindUserId, String volunteerUsername) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("blind_users").child(blindUserId).child("friends");

        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> currentFriends = task.getResult().getValue(new GenericTypeIndicator<List<String>>() {});

                if (currentFriends == null) {
                    currentFriends = new ArrayList<>();
                }

                Log.d("Debug:FirebaseRead", "Current friends: " + currentFriends);

                // Use a final reference to avoid lambda modification issues
                final List<String> finalFriends = new ArrayList<>(currentFriends);

                if (!finalFriends.contains(volunteerUsername)) {
                    finalFriends.add(volunteerUsername);

                    dbRef.setValue(finalFriends)
                            .addOnSuccessListener(aVoid -> Log.d("Debug:FirebaseWrite", "Updated friends list: " + finalFriends))
                            .addOnFailureListener(e -> Log.e("Debug:FirebaseWriteError", "Failed to update friends.", e));
                } else {
                    Log.d("Debug:NoUpdateNeeded", "Volunteer already in friends list.");
                }
            } else {
                Log.e("Debug:FirebaseReadError", "Failed to retrieve friends list.", task.getException());
            }
        });
    }


}
