package com.cs407.seesafe;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivities extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // Set the title for the activity
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Help - FAQs");
        }
    }
}
