package com.cohen.trackfrombehind;

import com.google.android.gms.maps.model.PolylineOptions;

public class TrackInfo {
    //private PolylineOptions polylineOptions;
    private double distance;
    private double calories;
    private String time;

    private TrackList trackList;

    public TrackInfo() {
    }

    //public PolylineOptions getPolylineOptions() {
    //    return polylineOptions;
    //}

    //public TrackInfo setPolylineOptions(PolylineOptions polylineOptions) {
    //    this.polylineOptions = polylineOptions;
    //    return this;
    //}

    public double getDistance() {
        return distance;
    }

    public TrackInfo setDistance(double distance) {
        this.distance = distance;
        return this;
    }

    public double getCalories() {
        return calories;
    }

    public TrackInfo setCalories(double calories) {
        this.calories = calories;
        return this;
    }

    public String getTime() {
        return time;
    }

    public TrackInfo setTime(String time) {
        this.time = time;
        return this;
    }

    public TrackList getTrackList() {
        return trackList;
    }

    public TrackInfo setTrackList(TrackList trackList) {
        this.trackList = trackList;
        return this;
    }
}
