package com.cohen.trackfrombehind;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;


/**
 * An activity that displays a Google map with polylines to represent paths or routes,
 * and polygons to represent areas.
 */
public class CyclingActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnPolygonClickListener {

    private static final double EQUATORIAL_RADIUS = 6378137; // Equatorial radius in meters
    private static final double FLATTENING = 1 / 298.257223563; // Flattening

    private static double sum_Distance = 0;

    public static final String SP_KEY_DISTANCE = "SP_KEY_DISTANCE";
    public static final String SP_KEY_LOCATION = "SP_KEY_LOCATION";

    private TextView id_map_speed;
    private TextView id_map_dis;
    private TextView id_map_cal;
    private ExtendedFloatingActionButton start;
    private ExtendedFloatingActionButton stop;

    private Chronometer chronometer;

    private TextView id_poly_min;
    private TextView id_poly_sec;
    private boolean running;

    private AppCompatImageButton id_IMG_BTN_torecords;

    public static final String SP_KEY_SERVICE = "SP_KEY_SERVICE";
    public static final String SP_KEY_START_TIME = "SP_KEY_START_TIME";
    public static final String SP_KEY_TRACK_INFO = "SP_KEY_TRACK_INFO";
    public static final String SP_KEY_SUM_CALORIES = "SP_KEY_SUM_CALORIES";
    public static final String SP_KEY_SUM_DISTANCE = "SP_KEY_SUM_DISTANCE";

    GoogleMap googleMap = null;

    private PolylineOptions polylineOptions = new PolylineOptions();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_cycling);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViews();
        initViews();

        MyReminder.startReminder(this);
    }


    private void initViews() {
        start.setOnClickListener(v -> startTrack(v));
        stop.setOnClickListener(v -> stopTrack(v));

        id_IMG_BTN_torecords.setOnClickListener(v -> toTracksRecord(v));
    }

    private void toTracksRecord(View v) {
        // you can ask if thers ant record first


        startActivity(new Intent(this, TrackRecordActivity.class));
    }


    private void findViews() {
        id_map_speed = findViewById(R.id.id_map_speed);
        id_map_dis = findViewById(R.id.id_map_dis);
        id_map_cal = findViewById(R.id.id_map_cal);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        chronometer = findViewById(R.id.chronometer);

        id_IMG_BTN_torecords = findViewById(R.id.id_IMG_BTN_torecords);

        //id_poly_min = findViewById(R.id.id_poly_min);
        //id_poly_sec = findViewById(R.id.id_poly_sec);
    }

    private BroadcastReceiver myRadio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(LocationService.BROADCAST_NEW_LOCATION_EXTRA_KEY);
            Loc loc = new Gson().fromJson(json, Loc.class);


            if(googleMap != null) {
                LatLng latLng = new LatLng(loc.getLat(), loc.getLon());
                googleMap.clear();
                polylineOptions.add(latLng);

                polylineOptions.endCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.img_arrow), 10));


                googleMap.addPolyline(polylineOptions);
                id_map_speed.setText(new DecimalFormat("##.##").format(loc.getSpeed()));



                TrackList trackList = new TrackList();
                String track_list = MySPV3.getInstance().getString(SP_KEY_LOCATION, "NuN");
                if(track_list != "NuN" && track_list != ""){
                    trackList = new Gson().fromJson(track_list, TrackList.class);
                    trackList.addLoc(loc);
                }
                else {
                    trackList.addLoc(loc);
                }
                String jsonTrackList = new Gson().toJson(trackList);
                MySPV3.getInstance().putString(SP_KEY_LOCATION, jsonTrackList);

                String jsonBikerInfo = MySPV3.getInstance().getString(RegisterActivity.SP_KEY_TRAINER, "NuN");

                Trainer trainer = new Gson().fromJson(jsonBikerInfo, Trainer.class);
                int size_LocList = trackList.getTracks().size();
                double calc_dis = 0;
                double calc_cal = 0;
                if (size_LocList > 3) {
                    for (int idx = 3; idx < size_LocList - 3; idx+=3) {
                        calc_dis += haversineDistance(trackList.getTracks().get(idx).getLat(), trackList.getTracks().get(idx).getLon(), trackList.getTracks().get(idx -3).getLat(), trackList.getTracks().get(idx -3).getLon());
                        calc_cal += calculateCaloriesBurned(trackList.getTracks().get(idx).getLat(), trackList.getTracks().get(idx).getLon(), trackList.getTracks().get(idx -3).getLat(), trackList.getTracks().get(idx -3).getLon(),
                                ((trackList.getTracks().get(idx).getSpeed() + trackList.getTracks().get(idx-3).getSpeed())/2),
                                trainer.getHeight(), trainer.getWeight(), trainer.getTrainAWeek(), Calendar.getInstance().get(Calendar.YEAR) - trainer.getBirthYear());
                    }
                    String distance = new DecimalFormat("##.##").format(calc_dis);
                    String calories = new DecimalFormat("##.##").format(calc_cal);

                    String json_sum_distance = new Gson().toJson(distance);
                    MySPV3.getInstance().putString("SP_KEY_SUM_DISTANCE", distance);
                    String json_sum_calories = new Gson().toJson(calories);
                    MySPV3.getInstance().putString("SP_KEY_SUM_CALORIES", calories);
                    id_map_dis.setText(distance);
                    id_map_cal.setText(calories);
                }
                if(size_LocList % 4 == 0 || size_LocList == 1){
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLat(), loc.getLon()), 17));
                }


                long currTime  = System.currentTimeMillis();
                String jsonStartTime = MySPV3.getInstance().getString(SP_KEY_START_TIME, "NuN");
                long start_time = Long.valueOf(jsonStartTime);
                int timSec = Long.valueOf(/*startTime*/ start_time - currTime).intValue() / 1000;
                int minuets = timSec / 60;
                int sec = timSec - (minuets * 60);
                //id_poly_min.setText(String.valueOf(minuets * -1));
                //id_poly_sec.setText(String.valueOf(sec * -1));
            }

        }

    };

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

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
                //polylineOptions.endCap(
                //        new CustomCap(
                 //               BitmapDescriptorFactory.fromResource(R.drawable.img_arrow), 10));
                googleMap.addPolyline(polylineOptions);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(trackList.getTracks().get(i-1).getLat(), trackList.getTracks().get(i-1).getLon()), 17));

                //calculate distance and calories from SP
                int size_LocList = trackList.getTracks().size();
                double calc_dis = 0;
                double calc_cal = 0;
                if (size_LocList > 3) {
                    for (int idx = 3; idx < size_LocList - 3; idx+=3) {
                        calc_dis += haversineDistance(trackList.getTracks().get(idx).getLat(), trackList.getTracks().get(idx).getLon(), trackList.getTracks().get(idx -3).getLat(), trackList.getTracks().get(idx -3).getLon());
                        calc_cal += calculateCaloriesBurned(trackList.getTracks().get(idx).getLat(), trackList.getTracks().get(idx).getLon(), trackList.getTracks().get(idx -3).getLat(), trackList.getTracks().get(idx -3).getLon(),
                                ((trackList.getTracks().get(idx).getSpeed() + trackList.getTracks().get(idx-3).getSpeed())/2),
                                trainer.getHeight(), trainer.getWeight(), trainer.getTrainAWeek(), Calendar.getInstance().get(Calendar.YEAR) - trainer.getBirthYear());
                    }
                    id_map_dis.setText(new DecimalFormat("##.##").format(calc_dis));
                    id_map_cal.setText(new DecimalFormat("##.#").format(calc_cal));
                }

            }
        }

        // Set listeners for click events.
        //googleMap.setOnPolylineClickListener(this);
        //googleMap.setOnPolygonClickListener(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(myRadio, intentFilter);


        String isActive = MySPV3.getInstance().getString(SP_KEY_SERVICE, "NuN");
        if(/*isActive != "NuN" && isActive != ""  &&*/ isActive == "ACTIVE"){
            start.setVisibility(View.INVISIBLE);
            stop.setVisibility(View.VISIBLE);
            reEnterChronometer();
        }
        else{
            stop.setVisibility(View.INVISIBLE);
            start.setVisibility(View.VISIBLE);

        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myRadio);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String isActive = MySPV3.getInstance().getString(SP_KEY_SERVICE, "NuN");
        if(/*isActive != "NuN" && isActive != ""  &&*/ isActive == "ACTIVE"){
            start.setVisibility(View.INVISIBLE);
            stop.setVisibility(View.VISIBLE);
            reEnterChronometer();
        }
        else{
            stop.setVisibility(View.INVISIBLE);
            start.setVisibility(View.VISIBLE);

        }

    }


    private void startTrack(View view) {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.START_FOREGROUND_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            startService(intent);
        }

        MySPV3.getInstance().putString(SP_KEY_SERVICE, "ACTIVE");
        startChronometer(view);

        MySPV3.getInstance().putString(SP_KEY_START_TIME, String.valueOf(System.currentTimeMillis()));

        start.setVisibility(View.INVISIBLE);
        stop.setVisibility(View.VISIBLE);

    }


    private void stopTrack (View view) {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.STOP_FOREGROUND_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            startService(intent);
        }

        TrackList trackList = new TrackList();
        String track_list = MySPV3.getInstance().getString(SP_KEY_LOCATION, "NuN");
        trackList = new Gson().fromJson(track_list, TrackList.class);

        TrackInfo trackInfo = new TrackInfo()
                //.setPolylineOptions(polylineOptions)
                .setCalories(Double.valueOf(id_map_cal.getText().toString()))
                .setDistance(Double.valueOf(id_map_dis.getText().toString()))
                .setTime(chronometer.getText().toString())
                .setTrackList(trackList);

        TrackInfoList trackInfoList = new TrackInfoList();
        String track_info_list = MySPV3.getInstance().getString(SP_KEY_TRACK_INFO, "NuN");
        if(track_info_list != "NuN" && track_info_list != ""){
            trackInfoList = new Gson().fromJson(track_info_list, TrackInfoList.class);
            trackInfoList.addTrackInfo(trackInfo);
        }
        else {
            trackInfoList.addTrackInfo(trackInfo);
        }


        String jsonTrackListInfo = new Gson().toJson(trackInfoList);
        MySPV3.getInstance().putString(SP_KEY_TRACK_INFO, jsonTrackListInfo);

        MySPV3.getInstance().putString(SP_KEY_SERVICE, "OFF");
        MySPV3.getInstance().putString(LocationService.SP_KEY_LOCATION, "");
        MySPV3.getInstance().putString(SP_KEY_START_TIME, "");
        MySPV3.getInstance().putString(SP_KEY_SUM_DISTANCE, "0.0");
        MySPV3.getInstance().putString(SP_KEY_SUM_CALORIES, "0.0");

        stopChronometer(view);




        stop.setVisibility(View.INVISIBLE);
        start.setVisibility(View.VISIBLE);
    }





    public double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        // Radius of the Earth (in kilometers)
        double radius = 6371.0;

        // Calculate the differences between the latitudes and longitudes
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;

        // Haversine formula
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance
        double distance = radius * c;

        // Return the distance rounded to 2 decimal places
        return Math.round(distance * 100.0) / 100.0;
    }

    public double calculateCaloriesBurned(double lat1, double lon1, double lat2, double lon2,
                                                 double cyclingSpeed, double height, double weight,
                                                 int trainingPerWeek, int age) {
        // Constants for the calculation
        final double MET = 8.0; // Metabolic Equivalent of Task for cycling
        final double KILOJOULES_TO_CALORIES = 0.239; // Conversion factor

        // Calculate distance between two locations using Haversine formula
        double distance = haversineDistance(lat1, lon1, lat2, lon2);

        // Calculate the time spent cycling based on distance and speed
        double timeInHours = distance / cyclingSpeed;

        // Calculate the average metabolic rate in kilocalories per hour
        double metabolicRate = calculateMetabolicRate(weight, height, age);

        // Adjust metabolic rate based on training frequency
        metabolicRate *= getTrainingFactor(trainingPerWeek);

        // Calculate calories burned
        double caloriesBurned = metabolicRate * timeInHours;

        // Convert calories burned from kilocalories to calories
        caloriesBurned *= KILOJOULES_TO_CALORIES;

        return caloriesBurned;
    }
    private double getTrainingFactor(int trainingPerWeek) {
        // Adjusts metabolic rate based on training frequency

        if (trainingPerWeek <= 1) {
            return 1.0; // No adjustment
        } else if (trainingPerWeek <= 3) {
            return 1.2; // Light training
        } else if (trainingPerWeek <= 5) {
            return 1.4; // Moderate training
        } else {
            return 1.6; // Intense training
        }
    }
    private static double calculateMetabolicRate(double weight, double height, int age) {
        // Calculates the average metabolic rate based on weight, height, and age
        // Adjustments can be made based on research or desired criteria

        double metabolicRate = 0.0;

        if (age <= 0) {
            throw new IllegalArgumentException("Invalid age value. Age must be greater than 0.");
        }

        // Calculate metabolic rate based on age, weight, and height
        if (age <= 17) {
            metabolicRate = (weight * 5) + (height * 6.25) - (age * 5) + 5;
        } else if (age <= 29) {
            metabolicRate = (weight * 4) + (height * 6.25) - (age * 5) + 5;
        } else if (age <= 49) {
            metabolicRate = (weight * 3) + (height * 6.25) - (age * 5) + 5;
        } else if (age <= 69) {
            metabolicRate = (weight * 2) + (height * 6.25) - (age * 5) + 5;
        } else {
            metabolicRate = (weight * 1) + (height * 6.25) - (age * 5) + 5;
        }

        return metabolicRate;
    }

    public void startChronometer(View v){
        //if(!running){
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        //running = true;
        //}
    }
    public void reEnterChronometer(){
        long currTime  = System.currentTimeMillis();
        String jsonStartTime = MySPV3.getInstance().getString(SP_KEY_START_TIME, "NuN");
        long start_time = Long.valueOf(jsonStartTime);
        int timmillis = Long.valueOf(/*startTime*/ start_time - currTime).intValue();
        chronometer.setBase(SystemClock.elapsedRealtime()+ timmillis);
        chronometer.start();
    }

    public void stopChronometer(View v){
        //if(running){
        chronometer.stop(); //only stop the text
        //chronometer.getText().toString();
        //running = false;
        //}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            if (getIntent().getAction().equals(LocationService.MAIN_ACTION)) {
                // came from notification
            }
        }
    }

    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */
    private void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "A":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setEndCap(
                        new CustomCap(
                                BitmapDescriptorFactory.fromResource(R.drawable.img_arrow), 10));
                break;
            case "B":
                // Use a round cap at the start of the line.
                polyline.setEndCap(new RoundCap());
                break;
        }

        polyline.setStartCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(COLOR_BLACK_ARGB);
        polyline.setJointType(JointType.ROUND);
    }

    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    /**
     * Listens for clicks on a polyline.
     * @param polyline The polyline object that the user has clicked.
     */
    @Override
    public void onPolylineClick(Polyline polyline) {
        // Flip from solid stroke to dotted stroke pattern.
        if ((polyline.getPattern() == null) || (!polyline.getPattern().contains(DOT))) {
            polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        } else {
            // The default pattern is a solid stroke.
            polyline.setPattern(null);
        }

        Toast.makeText(this, "Route type " + polyline.getTag().toString(),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Listens for clicks on a polygon.
     * @param polygon The polygon object that the user has clicked.
     */
    @Override
    public void onPolygonClick(Polygon polygon) {
        // Flip the values of the red, green, and blue components of the polygon's color.
        int color = polygon.getStrokeColor() ^ 0x00ffffff;
        polygon.setStrokeColor(color);
        color = polygon.getFillColor() ^ 0x00ffffff;
        polygon.setFillColor(color);

        Toast.makeText(this, "Area type " + polygon.getTag().toString(), Toast.LENGTH_SHORT).show();
    }

    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_DARK_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_LIGHT_GREEN_ARGB = 0xff81C784;
    private static final int COLOR_DARK_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_LIGHT_ORANGE_ARGB = 0xffF9A825;

    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);

    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);

    /**
     * Styles the polygon, based on type.
     * @param polygon The polygon object that needs styling.
     */
    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;
        int fillColor = COLOR_WHITE_ARGB;

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "alpha":
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA;
                strokeColor = COLOR_DARK_GREEN_ARGB;
                fillColor = COLOR_LIGHT_GREEN_ARGB;
                break;
            case "beta":
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_BETA;
                strokeColor = COLOR_DARK_ORANGE_ARGB;
                fillColor = COLOR_LIGHT_ORANGE_ARGB;
                break;
        }

        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);
        polygon.setFillColor(fillColor);
    }
}

/*
 <LinearLayout
        android:id="@+id/id_poly_timer"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:layout_centerHorizontal="true"
        android:layout_below="@id/id_map_LL">
        <TextView
            android:id="@+id/id_poly_sec"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:text="00"
            android:textSize="20sp">

        </TextView>
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:textSize="20sp"
            android:text=":"/>


        <TextView
            android:id="@+id/id_poly_min"
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:text="00"
            android:textSize="20sp">

        </TextView>

    </LinearLayout>
 */