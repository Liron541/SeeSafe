package com.cs407.seesafe;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class BlindFriendsActivity extends AppCompatActivity {

    // Assume blindUserId is known or passed via Intent
    private static final String BLIND_USER_ID = "blindUser123";

    private MaterialButton btnFriend1, btnFriend2, btnFriend3, btnFriend4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blindfriends); // The layout with friend buttons

        // Initialize buttons
        btnFriend1 = findViewById(R.id.btn_friend1);
        btnFriend2 = findViewById(R.id.btn_friend2);
        btnFriend3 = findViewById(R.id.btn_friend3);
        btnFriend4 = findViewById(R.id.btn_friend4);

        loadBlindUserFriends();
    }

    private void loadBlindUserFriends() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users")
                .child("blindUser123").child("friends");


        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String friendsString = snapshot.getValue(String.class);
                Log.d("Debug:FriendListRead", "Retrieved friends: " + friendsString);

                List<String> friendsList = new ArrayList<>();
                if (friendsString != null && !friendsString.isEmpty()) {
                    String[] friendsArray = friendsString.split(",");
                    for (String friend : friendsArray) {
                        friendsList.add(friend.trim());
                    }
                }
                updateFriendButtons(friendsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Debug:FriendListError", "Failed to read friend list", error.toException());
            }
        });
    }


    private void updateFriendButtons(List<String> friendsList) {
        Log.d("Debug:UpdateButtons", "Friends list size: " + friendsList.size());

        if (friendsList.size() > 0) {
            btnFriend1.setText(friendsList.get(0));
            Log.d("Debug:ButtonUpdate", "Button 1 updated with: " + friendsList.get(0));
        } else {
            btnFriend1.setText("Friend #1");
        }

        if (friendsList.size() > 1) {
            btnFriend2.setText(friendsList.get(1));
            Log.d("Debug:ButtonUpdate", "Button 2 updated with: " + friendsList.get(1));
        } else {
            btnFriend2.setText("Friend #2");
        }

        if (friendsList.size() > 2) {
            btnFriend3.setText(friendsList.get(2));
            Log.d("Debug:ButtonUpdate", "Button 3 updated with: " + friendsList.get(2));
        } else {
            btnFriend3.setText("Friend #3");
        }

        if (friendsList.size() > 3) {
            btnFriend4.setText(friendsList.get(3));
            Log.d("Debug:ButtonUpdate", "Button 4 updated with: " + friendsList.get(3));
        } else {
            btnFriend4.setText("Friend #4");
        }
    }
}
