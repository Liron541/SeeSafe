package com.cs407.seesafe;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendFriendRequestActivity extends AppCompatActivity {

    private Button sendRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_friend_request);

        // Initialize the button
        sendRequestButton = findViewById(R.id.button);

        // Set up the click listener
        sendRequestButton.setOnClickListener(v -> sendFriendRequest("volunteerId123"));
    }

    private void sendFriendRequest(String volunteerId) {
        // Create the friend request payload
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("senderId", "blindUserId123"); // Replace with actual blind user ID
            requestData.put("receiverId", volunteerId);    // Volunteer ID passed as parameter
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating request data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use OkHttp to send the request
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(requestData.toString(), MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url("https://your-server-url.com/sendFriendRequest") // Replace with your backend endpoint
                .post(body)
                .build();

        // Make the request in a separate thread
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error sending request", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}

