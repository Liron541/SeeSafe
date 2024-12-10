package com.cs407.seesafe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private EditText fullNameEditText, displayNameEditText, titleEditText, namePronunciationEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize EditText fields
        fullNameEditText = findViewById(R.id.fullNameEditText);
        displayNameEditText = findViewById(R.id.displayNameEditText);
        titleEditText = findViewById(R.id.titleEditText);
        namePronunciationEditText = findViewById(R.id.namePronunciationEditText);

        // Initialize Save Button
        saveButton = findViewById(R.id.saveButton);

        // Load data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        fullNameEditText.setText(sharedPreferences.getString("fullName", ""));
        displayNameEditText.setText(sharedPreferences.getString("displayName", ""));
        titleEditText.setText(sharedPreferences.getString("title", ""));
        namePronunciationEditText.setText(sharedPreferences.getString("namePronunciation", ""));

        // Save data and navigate back when Save button is clicked
        saveButton.setOnClickListener(v -> {
            // Save the user inputs to SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("fullName", fullNameEditText.getText().toString());
            editor.putString("displayName", displayNameEditText.getText().toString());
            editor.putString("title", titleEditText.getText().toString());
            editor.putString("namePronunciation", namePronunciationEditText.getText().toString());
            editor.apply();

            // Navigate back to the previous activity
            finish();
        });
    }
}
