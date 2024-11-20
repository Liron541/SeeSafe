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

        // ------------------------Drop Down------------------------------v
        expandableListView = findViewById(R.id.expandableListView);
        prepareData();

        adapter = new ExpandableListAdapter(this, groupList, childList);
        expandableListView.setAdapter(adapter);

        // Set a listener for child item clicks
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedItem = childList.get(groupList.get(groupPosition)).get(childPosition);
            Toast.makeText(VolunteerHomePage.this, "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            return true;
        });
        // ------------------------Drop Down------------------------------^

        // ------------------------Tool Bar------------------------------v
        //toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        // Set the Toolbar title with the username
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Welcome, " + username + "!");
        }

        // ------------------------Tool Bar------------------------------^

        switchControl = findViewById(R.id.switchControl);
        friendsButton = findViewById(R.id.friendsButton);

        switchControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(VolunteerHomePage.this, "Switch On", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(VolunteerHomePage.this, "Switch Off", Toast.LENGTH_SHORT).show();
            }
        });

        friendsButton.setOnClickListener(v -> {
            Intent friendIntent = new Intent(VolunteerHomePage.this, FriendListActivity.class);
            friendIntent.putExtra("username", username);
            startActivity(friendIntent);
        });
    }

    // ------------------------Drop Down Default------------------------------v
    private void prepareData() {
        groupList = new ArrayList<>();
        childList = new HashMap<>();

        // Add groups
        groupList.add("Volunteers");
        groupList.add("Blind Friends");

        // Add children to each group
        List<String> volunteers = new ArrayList<>();
        volunteers.add("Volunteer 1");
        volunteers.add("Volunteer 2");
        volunteers.add("Volunteer 3");

        List<String> blindFriends = new ArrayList<>();
        blindFriends.add("Blind Friend 1");
        blindFriends.add("Blind Friend 2");

        childList.put(groupList.get(0), volunteers); // Add volunteers to group 1
        childList.put(groupList.get(1), blindFriends); // Add blind friends to group 2
    }
    // ------------------------Drop Down Default------------------------------^

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


