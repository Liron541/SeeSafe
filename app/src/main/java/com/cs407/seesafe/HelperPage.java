package com.cs407.seesafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class HelperPage extends AppCompatActivity {

    private SwitchCompat switchControl;
    private Button friendsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helper);

        switchControl = findViewById(R.id.switchControl);
        friendsButton = findViewById(R.id.friendsButton);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        switchControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(HelperPage.this, "Switch On", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HelperPage.this, "Switch Off", Toast.LENGTH_SHORT).show();
            }
        });

        friendsButton.setOnClickListener(v -> {
            Intent friendIntent = new Intent(HelperPage.this, FriendListActivity.class);
            friendIntent.putExtra("username", username);
            startActivity(friendIntent);
        });
    }
}


