package com.cs407.seesafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private ListView friendsListView;
    private TextView currentUserTextView;
    private Button backButton;

    private AppDatabase database;
    private UserDao userDao;

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
        String username = intent.getStringExtra("username");

        currentUserTextView.setText("Current User：" + username);

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

        backButton.setOnClickListener(v -> finish());
    }

    private List<String> parseFriends(String friendsString) {
        String[] friendsArray = friendsString.split(",");
        List<String> friendsList = new ArrayList<>();
        for (String friend : friendsArray) {
            friendsList.add(friend.trim());
        }
        return friendsList;
    }
}