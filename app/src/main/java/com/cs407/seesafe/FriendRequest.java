package com.cs407.seesafe;

public class FriendRequest {
    private String blindUserId;
    private String volunteerId;
    private long timestamp;

    public FriendRequest() {
        // Default constructor required for calls to DataSnapshot.getValue(FriendRequest.class)
    }

    public FriendRequest(String blindUserId, String volunteerId, long timestamp) {
        this.blindUserId = blindUserId;
        this.volunteerId = volunteerId;
        this.timestamp = timestamp;
    }

    public String getBlindUserId() {
        return blindUserId;
    }

    public void setBlindUserId(String blindUserId) {
        this.blindUserId = blindUserId;
    }

    public String getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(String volunteerId) {
        this.volunteerId = volunteerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

