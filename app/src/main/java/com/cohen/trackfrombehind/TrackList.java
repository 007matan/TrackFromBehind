package com.cohen.trackfrombehind;

import java.util.ArrayList;

public class TrackList {

    private String name = "";
    private ArrayList<Loc> tracks = new ArrayList<>();

    public TrackList(){}

    public String getName() {
        return name;
    }

    public TrackList setName(String name) {
        this.name = name;
        return this;
    }

    public ArrayList<Loc> getTracks() {
        return tracks;
    }

    public TrackList setTracks(ArrayList<Loc> tracks) {
        this.tracks = tracks;
        return this;
    }

    public TrackList addLoc(Loc loc){
        tracks.add(loc);
        return this;
    }
}
