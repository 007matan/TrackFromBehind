package com.cohen.trackfrombehind;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

public class PermissionActivity extends AppCompatActivity {

    private enum STATE {
        NA,
        NO_REGULAR_PERMISSION,
        NO_BACKGROUND_PERMISSION,
        LOCATION_DISABLE,
        LOCATION_SETTINGS_PROCCESS,
        LOCATION_SETTINGS_OK,
        LOCATION_ENABLE
    }


    private Button id_per_continue_BTN;

    //private STATE state = PermissionActivity.STATE.NA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        //
        id_per_continue_BTN = findViewById(R.id.id_per_cont_BTN);
        //findViews();
        initViews();

        updateActivity();
    }

    private void updateActivity() {
        boolean isLocationServiceOn = isLocationEnabled(this);
        int result_fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result_coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result_fine == PackageManager.PERMISSION_GRANTED && result_coarse == PackageManager.PERMISSION_GRANTED) {
            //id_main_camera.setBackgroundColor(Color.parseColor("#7CFC00"));
            //Toast.makeText(PermissionActivity.this, "have Permission for background location", Toast.LENGTH_SHORT).show();
            if(isLocationServiceOn){
                //Toast.makeText(SecondActivity.this, "location Service On", Toast.LENGTH_SHORT).show();
                //newActivity
                startActivity(new Intent(this, PolyActivity.class));
                finish();
            }
            else
                Toast.makeText(PermissionActivity.this, "location Service Off, Please activate before continue", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateActivity2() {
        boolean isLocationServiceOn = isLocationEnabled(this);
        int result_fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int result_coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result_fine == PackageManager.PERMISSION_GRANTED && result_coarse == PackageManager.PERMISSION_GRANTED) {
            //id_main_camera.setBackgroundColor(Color.parseColor("#7CFC00"));
            //Toast.makeText(PermissionActivity.this, "have Permission for background location", Toast.LENGTH_SHORT).show();
            if(isLocationServiceOn){
                //Toast.makeText(SecondActivity.this, "location Service On", Toast.LENGTH_SHORT).show();
                //newActivity
                startActivity(new Intent(this, PolyActivity.class));
                finish();
            }
            else
                Toast.makeText(PermissionActivity.this, "location Service Off", Toast.LENGTH_SHORT).show();
        } else {
            //id_main_camera.setBackgroundColor(Color.parseColor("#710C04"));
            //Toast.makeText(PermissionActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
                Toast.makeText(PermissionActivity.this, "Be aware, the next time will be the last time you have this option from here", Toast.LENGTH_LONG).show();//should be right before the *second* time, and right after the *first* time - "it wiil be the last time you can do it from here
            }
            else
            Toast.makeText(PermissionActivity.this, "Please active from app setting location permission", Toast.LENGTH_LONG).show();
        }
    }

    public static Boolean isLocationEnabled(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This is a new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            // This was deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private void findViews() {
        id_per_continue_BTN.findViewById(R.id.id_per_cont_BTN);
    }

    private void initViews() {
        id_per_continue_BTN.setOnClickListener(v -> permissionAsk());
    }

    private void permissionAsk() {
        updateActivity(); //updateActivity because - maybe the user just needed to turn on location
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.

                } else {
                    permissionDenied();
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
                updateActivity2();
            });

    private void permissionDenied() {
        Toast.makeText(PermissionActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
    }
}