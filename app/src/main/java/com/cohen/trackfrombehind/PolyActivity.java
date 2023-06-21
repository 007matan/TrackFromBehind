package com.cohen.trackfrombehind;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;


/**
 * An activity that displays a Google map with polylines to represent paths or routes,
 * and polygons to represent areas.
 */
public class PolyActivity extends AppCompatActivity
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

    public static final String SP_KEY_SERVICE = "SP_KEY_SERVICE";
    public static final String SP_KEY_START_TIME = "SP_KEY_START_TIME";


    private Vector<Loc> lastLoc = new Vector<>();


    private long startTime;

    GoogleMap googleMap = null;
    //Polyline polyline = null;
    //ArrayList<LatLng> latLngVector = new ArrayList<LatLng>();
    //private LatLng[] latlng = new LatLng[10];

    private PolylineOptions polylineOptions = new PolylineOptions();
    //List<LatLng> originalLatLngList = new ArrayList<>();




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);

        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViews();
        initViews();

        MyReminder.startReminder(this);
    }


    private void initViews() {
        start.setOnClickListener(v -> startService(v));
        stop.setOnClickListener(v -> stopService(v));
    }


    private void findViews() {
        id_map_speed = findViewById(R.id.id_map_speed);
        id_map_dis = findViewById(R.id.id_map_dis);
        id_map_cal = findViewById(R.id.id_map_cal);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        //chronometer = findViewById(R.id.chronometer);

        id_poly_min = findViewById(R.id.id_poly_min);
        id_poly_sec = findViewById(R.id.id_poly_sec);
    }

    private BroadcastReceiver myRadio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(LocationService.BROADCAST_NEW_LOCATION_EXTRA_KEY);
            Loc loc = new Gson().fromJson(json, Loc.class);


            if(googleMap != null) {
                LatLng latLng = new LatLng(loc.getLat(), loc.getLon());
                polylineOptions.add(latLng);
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



                //String jsonTrackList = MySPV3.getInstance().getString(LocationService.SP_KEY_LOCATION, "NuN");
                String jsonBikerInfo = MySPV3.getInstance().getString(RegisterActivity.SP_KEY_TRAINER, "NuN");

                //if (jsonTrackList != "NuN" && jsonTrackList != "") {


                    //TrackList trackList = new Gson().fromJson(jsonTrackList, TrackList.class);

                    Trainer trainer = new Gson().fromJson(jsonBikerInfo, Trainer.class);
                    int size_LocList = trackList.getTracks().size();
                    double calc_dis = 0;
                    double calc_cal = 0;
                    if (size_LocList > 3) {
                        // if running - from SP
                        //String jsonDistance =  MySPV3.getInstance().getString(SP_KEY_DISTANCE, "NaN");
                        //Double distance = Double.parseDouble(jsonDistance);
                        //check

                        //
                        //distance += haversineDistance(trackList.getTracks().get(idx).getLat(), trackList.getTracks().get(idx).getLon(), trackList.getTracks().get(idx + 1).getLat(), trackList.getTracks().get(idx + 1).getLon());
                        for (int idx = 3; idx < size_LocList - 3; idx+=3) {
                            calc_dis += haversineDistance(trackList.getTracks().get(idx).getLat(), trackList.getTracks().get(idx).getLon(), trackList.getTracks().get(idx -3).getLat(), trackList.getTracks().get(idx -3).getLon());
                            calc_cal += calculateCaloriesBurned(trackList.getTracks().get(idx).getLat(), trackList.getTracks().get(idx).getLon(), trackList.getTracks().get(idx -3).getLat(), trackList.getTracks().get(idx -3).getLon(),
                                    ((trackList.getTracks().get(idx).getSpeed() + trackList.getTracks().get(idx-3).getSpeed())/2),
                                    trainer.getHeight(), trainer.getWeight(), trainer.getTrainAWeek(), Calendar.getInstance().get(Calendar.YEAR) - trainer.getBirthYear());
                        }
                        id_map_dis.setText(new DecimalFormat("##.##").format(calc_dis));
                        id_map_cal.setText(new DecimalFormat("##.#").format(calc_cal));
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
                    id_poly_min.setText(String.valueOf(minuets * -1));
                    id_poly_sec.setText(String.valueOf(sec * -1));
               // }
            }

        }

    };

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * In this tutorial, we add polylines and polygons to represent routes and areas on the map.
     */
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
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);



    }
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(LocationService.BROADCAST_NEW_LOCATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(myRadio, intentFilter);
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
        if(isActive != "NuN" && isActive != "" && isActive != "OFF"){
            start.setVisibility(View.VISIBLE);
            stop.setVisibility(View.INVISIBLE);
        }
        else{
            stop.setVisibility(View.VISIBLE);
            start.setVisibility(View.INVISIBLE);
        }
    }

    private void startService(View view) {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.START_FOREGROUND_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            startService(intent);
        }

        //MySP mySP = new MySP(this);
        //mySP.putString(SP_KEY_SERVICE, "ACTIVE");

        MySPV3.getInstance().putString(SP_KEY_SERVICE, "ACTIVE");
        //startChronometer(view);

        startTime = System.currentTimeMillis();
        MySPV3.getInstance().putString(SP_KEY_START_TIME, String.valueOf(System.currentTimeMillis()));

        start.setVisibility(View.INVISIBLE);
        stop.setVisibility(View.VISIBLE);

    }


    private void stopService(View view) {
        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.STOP_FOREGROUND_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
            // or
            //ContextCompat.startForegroundService(this, startIntent);
        } else {
            startService(intent);
        }
        //MySP mySP = new MySP(this);
        //mySP.putString(SP_KEY_SERVICE, "OFF");

        MySPV3.getInstance().putString(SP_KEY_SERVICE, "OFF");
        MySPV3.getInstance().putString(LocationService.SP_KEY_LOCATION, "");
        MySPV3.getInstance().putString(SP_KEY_START_TIME, "");

        //stopChronometer(view);

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
    public static double calculateDistanceV2(double lat1, double lon1, double lat2, double lon2) {
        // Convert decimal degrees to radians
        double latRad1 = Math.toRadians(lat1);
        double lonRad1 = Math.toRadians(lon1);
        double latRad2 = Math.toRadians(lat2);
        double lonRad2 = Math.toRadians(lon2);

        // Haversine formula
        double dlon = lonRad2 - lonRad1;
        double dlat = latRad2 - latRad1;
        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(latRad1) * Math.cos(latRad2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double radius = 6371; // Radius of the Earth in kilometers. Change this value for miles or other units.
        double distance = radius * c;

        return distance;
    }
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert decimal degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Vincenty's formulae
        double deltaLon = lon2Rad - lon1Rad;
        double U1 = Math.atan((1 - FLATTENING) * Math.tan(lat1Rad));
        double U2 = Math.atan((1 - FLATTENING) * Math.tan(lat2Rad));
        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double lambdaLon = deltaLon;
        double lambdaPrime = 2 * Math.PI;
        int iterLimit = 100; // Iteration limit for convergence

        double sinLambdaLon=0;
        double cosLambdaLon=0;
        double sinSigma=0;
        double cosSigma=0;
        double sigma=0;
        double sinAlpha=0;
        double cosSqAlpha = 0;
        double cos2SigmaM=0;
        double C=0;

        while (Math.abs(lambdaLon - lambdaPrime) > 1e-12 && iterLimit > 0) {
            sinLambdaLon = Math.sin(lambdaLon);
            cosLambdaLon = Math.cos(lambdaLon);
            sinSigma = Math.sqrt((cosU2 * sinLambdaLon) * (cosU2 * sinLambdaLon) +
             (cosU1 * sinU2 - sinU1 * cosU2 * cosLambdaLon) *
                     (cosU1 * sinU2 - sinU1 * cosU2 * cosLambdaLon));
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambdaLon;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = (cosU1 * cosU2 * sinLambdaLon) / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - (2 * sinU1 * sinU2) / cosSqAlpha;
            C = FLATTENING / 16 * cosSqAlpha * (4 + FLATTENING * (4 - 3 * cosSqAlpha));
            lambdaPrime = lambdaLon;
            lambdaLon = deltaLon + (1 - C) * FLATTENING * sinAlpha *
                    (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma *
                            (-1 + 2 * cos2SigmaM * cos2SigmaM)));

            iterLimit--;
        }

        if (iterLimit == 0) {
            throw new RuntimeException("Vincenty's formulae did not converge");
        }

        double uSq = cosSqAlpha * ((EQUATORIAL_RADIUS * EQUATORIAL_RADIUS -
                (EQUATORIAL_RADIUS * FLATTENING) *
                        (EQUATORIAL_RADIUS * FLATTENING)) /
                (EQUATORIAL_RADIUS * FLATTENING)) *
                ((EQUATORIAL_RADIUS * EQUATORIAL_RADIUS -
                        (EQUATORIAL_RADIUS * FLATTENING) *
                                (EQUATORIAL_RADIUS * FLATTENING)) /
                        (EQUATORIAL_RADIUS * FLATTENING));
        double A = 1 + (uSq / 16384) *
                (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = (uSq / 1024) *
                (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B *
                Math.sin(sigma) *
                (cos2SigmaM + (B / 4) *
                        (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                                (B / 6) * cos2SigmaM *
                                        (-3 + 4 * sinSigma * sinSigma) *
                                        (-3 + 4 * cos2SigmaM * cos2SigmaM)));
        double distance = EQUATORIAL_RADIUS * A * (sigma - deltaSigma);

        double tolerance = 0.05; // Set the tolerance value

        if (Math.abs(distance) < tolerance) {
            return Math.abs(distance) < 0.05 ? 0.0 : distance;
        }

        return distance;// distance/1609.34; mile
    }
    public void startChronometer(View v){
        //if(!running){
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        //running = true;
        //}
    }

    public void stopChronometer(View v){
        //if(running){
        chronometer.stop(); //only stop the text
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
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow), 10));
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