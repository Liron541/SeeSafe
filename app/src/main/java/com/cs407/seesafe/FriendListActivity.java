package com.cs407.seesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private ListView friendsListView;
    private TextView currentUserTextView;
    private Button backButton;

    private AppDatabase database;
    private UserDao userDao;

    private boolean isVolunteer;
    private String username; // Could represent volunteer username or blindUserId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        friendsListView = findViewById(R.id.friendsListView);
        currentUserTextView = findViewById(R.id.currentUserTextView);
        backButton = findViewById(R.id.backButton);

        database = AppDatabase.getInstance(this);
        userDao = database.userDao();

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        // Determine if this user is volunteer or blind user
        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        isVolunteer = sharedPreferences.getBoolean("IS_VOLUNTEER", false);

        currentUserTextView.setText("Current User：" + username);

        if (isVolunteer) {
            // Volunteer logic: load from Room database
            loadVolunteerFriends(username);
        } else {
            // Blind user logic: load from Firebase
            loadBlindUserFriends(username);
        }

        backButton.setOnClickListener(v -> finish());
    }

    private void loadVolunteerFriends(String username) {
        // Load from local DB as originally done
        new Thread(() -> {
            User user = userDao.getUserByUsername(username);
            List<String> friends = new ArrayList<>();
            if (user != null && user.getFriends() != null) {
                friends = parseFriends(user.getFriends());
            }

            List<String> finalFriends = friends;
            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, finalFriends);
                friendsListView.setAdapter(adapter);
            });
        }).start();
    }

    private void loadBlindUserFriends(String blindUserId) {
        // Blind user scenario: read friend list from Firebase: blind_users/{blindUserId}/friends
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("blind_users")
                .child(blindUserId).child("friends");

        dbRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String friendsString = task.getResult().getValue(String.class);
                List<String> friendsList = new ArrayList<>();
                if (friendsString != null && !friendsString.isEmpty()) {
                    String[] friendsArray = friendsString.split(",");
                    for (String friend : friendsArray) {
                        friendsList.add(friend.trim());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_list_item_1, friendsList);
                friendsListView.setAdapter(adapter);
            } else {
                Toast.makeText(FriendListActivity.this, "Failed to load blind user friends.", Toast.LENGTH_SHORT).show();
                Log.e("FriendListActivity", "Failed to retrieve friend list", task.getException());
            }
        });
    }

    private List<String> parseFriends(String friendsString) {
        String[] friendsArray = friendsString.split(",");
        List<String> friendsList = new ArrayList<>();
        for (String friend : friendsArray) {
            if (!friend.trim().isEmpty()) {
                friendsList.add(friend.trim());
            }
        }
        return friendsList;
    }

}
