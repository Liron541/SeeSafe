package com.cs407.seesafe;

public class BlindUserLocation {
    private String id;
    private double latitude;
    private double longitude;
    private long timestamp;

    // Default constructor required for calls to DataSnapshot.getValue(BlindUserLocation.class)
    public BlindUserLocation() {}

    public BlindUserLocation(String id, double latitude, double longitude, long timestamp) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
