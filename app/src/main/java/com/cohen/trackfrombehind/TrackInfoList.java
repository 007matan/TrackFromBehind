package com.cohen.trackfrombehind;

import java.util.ArrayList;

public class TrackInfoList {

    private String name = "";
    private ArrayList<TrackInfo> tracksInfo = new ArrayList<>();

    public TrackInfoList() {
    }

    public String getName() {
        return name;
    }

    public TrackInfoList setName(String name) {
        this.name = name;
        return this;
    }

    public ArrayList<TrackInfo> getTracksInfo() {
        return tracksInfo;
    }

    public TrackInfoList setTracksInfo(ArrayList<TrackInfo> tracksInfo) {
        this.tracksInfo = tracksInfo;
        return this;

    }
    public TrackInfoList addTrackInfo(TrackInfo trackInfo){
        tracksInfo.add(trackInfo);
        return this;
    }
}
