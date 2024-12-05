package com.cs407.seesafe;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VolunteerHomePage extends AppCompatActivity {

    private SwitchCompat switchControl;
    private Button friendsButton;

    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> groupList;
    private HashMap<String, List<String>> childList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.helper);

        // Toolbar setup
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + username + "!");
        }

        // Set up buttons
        Button volunteersButton = findViewById(R.id.volunteersButton);
        Button blindFriendsButton = findViewById(R.id.blindFriendsButton);
        Button mapButton = findViewById(R.id.mapButton);

        volunteersButton.setOnClickListener(v -> {
            Intent volunteerIntent = new Intent(VolunteerHomePage.this, FriendListActivity.class);
            volunteerIntent.putExtra("group", "Volunteers");
            startActivity(volunteerIntent);
        });

        blindFriendsButton.setOnClickListener(v -> {
            Intent blindFriendsIntent = new Intent(VolunteerHomePage.this, FriendListActivity.class);
            blindFriendsIntent.putExtra("group", "Blind Friends");
            startActivity(blindFriendsIntent);
        });

        mapButton.setOnClickListener(v -> {
            Intent mapIntent = new Intent(VolunteerHomePage.this, MapActivity.class);
            startActivity(mapIntent);
        });

        // Switch logic
        switchControl = findViewById(R.id.switchControl);
        switchControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(VolunteerHomePage.this, "Switch On", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(VolunteerHomePage.this, "Switch Off", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_manu, menu);
        return true;
    }

    // Handle menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_help) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


