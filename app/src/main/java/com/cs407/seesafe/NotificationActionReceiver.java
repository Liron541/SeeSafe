package com.cs407.seesafe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("accept_friend_request".equals(action)) {
            handleFriendRequestResponse(context, true);
        } else if ("decline_friend_request".equals(action)) {
            handleFriendRequestResponse(context, false);
        }
    }

    private void handleFriendRequestResponse(Context context, boolean accepted) {
        // Notify the server of the response
        JSONObject response = new JSONObject();
        try {
            response.put("response", accepted ? "accepted" : "declined");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(response.toString(), MediaType.get("application/json; charset=utf-8"));
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://your-server-url.com/respondFriendRequest")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, "Failed to respond to request", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful() && accepted) {
                    Toast.makeText(context, "Friend request accepted", Toast.LENGTH_SHORT).show();
                } else if (!accepted) {
                    Toast.makeText(context, "Friend request declined", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

