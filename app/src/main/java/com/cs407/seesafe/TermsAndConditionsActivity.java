package com.cs407.seesafe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private Button proceedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        proceedButton = findViewById(R.id.proceedButton);

        // Handle Proceed button click
        proceedButton.setOnClickListener(v -> {
            // Navigate to the Blind User Activity
            Intent intent = new Intent(TermsAndConditionsActivity.this, BlindUserActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
