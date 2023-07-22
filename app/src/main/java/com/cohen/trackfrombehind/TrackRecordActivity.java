package com.cohen.trackfrombehind;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

public class TrackRecordActivity extends AppCompatActivity implements MyTrackInfoViewHolder.OnTrackInfoListener,
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private TrackInfoList trackInfoList;

    GoogleMap googleMap = null;

    private PolylineOptions polylineOptions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_record);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.id_record_map);
        mapFragment.getMapAsync(this);

        RecyclerView recyclerView = findViewById(R.id.id_RE_VW_tracks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        trackInfoList = new TrackInfoList();
        String track_info_list = MySPV3.getInstance().getString(CyclingActivity.SP_KEY_TRACK_INFO, "NuN");
        if(track_info_list != "NuN" && track_info_list != ""){
            trackInfoList = new Gson().fromJson(track_info_list, TrackInfoList.class);
            recyclerView.setAdapter(new TrackInfoAdapter(getApplicationContext(), trackInfoList, this));
        }

    }

    @Override
    public void inTrackInfoClick(int position) {
        //change map according to position-

        //polylineOptions = trackInfoList.getTracksInfo().get(position).getPolylineOptions();
        TrackList trackList = new TrackList();
        trackList = trackInfoList.getTracksInfo().get(position).getTrackList();
        int i;
        polylineOptions = new PolylineOptions();
        for(i = 0; i < trackList.getTracks().size(); i++){
            polylineOptions.add(new LatLng(trackList.getTracks().get(i).getLat(), trackList.getTracks().get(i).getLon()));
        }
        if(googleMap != null){
            googleMap.addPolyline(polylineOptions);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(trackList.getTracks().get(i-1).getLat(), trackList.getTracks().get(i-1).getLon()), 17));

        }


        //you cant use the exut on SP DP_KEY_LOCATION cause the only last track saved there
        // you need to save track list instead polyline
        /*
        String jsonTrackList = MySPV3.getInstance().getString(LocationService.SP_KEY_LOCATION, "NuN");
        String jsonBikerInfo = MySPV3.getInstance().getString(RegisterActivity.SP_KEY_TRAINER, "NuN");

        //check if theres track in the SP
        if (jsonTrackList != "NuN" && jsonTrackList != ""){
            TrackList trackList = new Gson().fromJson(jsonTrackList, TrackList.class);
            Trainer trainer = new Gson().fromJson(jsonBikerInfo, Trainer.class);
            if(googleMap != null) {
                int i;
                // make the track by the loc values from SP
                for(i = 0; i < trackList.getTracks().size(); i++){
                    polylineOptions.add(new LatLng(trackList.getTracks().get(i).getLat(), trackList.getTracks().get(i).getLon()));
                }
                googleMap.addPolyline(polylineOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(trackList.getTracks().get(i-1).getLat(), trackList.getTracks().get(i-1).getLon()), 17));

         */
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
    }
}