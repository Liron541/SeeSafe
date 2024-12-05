package com.cs407.seesafe;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
public class ProfileActivity extends AppCompatActivity{

    private EditText fullNameEditText, displayNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize EditText fields
        fullNameEditText = findViewById(R.id.fullNameEditText);
        displayNameEditText = findViewById(R.id.displayNameEditText);

        // Get username from intent
        String username = getIntent().getStringExtra("username");

//        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
//        String username = sharedPreferences.getString("username", null);


        // Set username in Full Name and Display Name
        if (username != null) {
            fullNameEditText.setText(username);
            displayNameEditText.setText(username);
        }
    }
}
