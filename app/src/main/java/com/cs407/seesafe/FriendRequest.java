package com.cs407.seesafe;

public class FriendRequest {

    private String volunteerId;
    private long timestamp;


    public FriendRequest() {

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

