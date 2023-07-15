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
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PermissionActivity extends AppCompatActivity {


    private enum PERMISSION_STATE {
        PERMISSION_GRANTED,
        PERMISSION_DENIED_ONCE,
        PERMISSION_DENIDE
    }


    private Button id_per_continue_BTN;
    private Button id_per_continueSec_BTN;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);


        id_per_continue_BTN = findViewById(R.id.id_per_cont_BTN);
        id_per_continueSec_BTN = findViewById(R.id.id_per_contSec_BTN);
        initViews();

        permissionsCheckInit();
    }

    private void permissionsCheckInit(){
        boolean isLocationServiceOn = isLocationEnabled(this);
        PERMISSION_STATE result_fine = permissionCheckYesNO(Manifest.permission.ACCESS_FINE_LOCATION);
        PERMISSION_STATE result_coarse = permissionCheckYesNO(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result_fine == PERMISSION_STATE.PERMISSION_GRANTED && result_coarse == PERMISSION_STATE.PERMISSION_GRANTED) {
            if(isLocationServiceOn){
                startActivity(new Intent(this, CyclingActivity.class));
                finish();
            }
            else
                Toast.makeText(PermissionActivity.this, "location Service Off, Please activate before continue", Toast.LENGTH_SHORT).show();
        }
    }

    private PERMISSION_STATE permissionCheck(String prm){
        boolean granted = ContextCompat.checkSelfPermission(this ,prm) == PackageManager.PERMISSION_GRANTED;
        if(granted){
            return PERMISSION_STATE.PERMISSION_GRANTED;
        } else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, prm)){
                Toast.makeText(PermissionActivity.this, "Be aware, it will be the last time you can give permission from here", Toast.LENGTH_SHORT).show();
                return PERMISSION_STATE.PERMISSION_DENIED_ONCE;
            }
            return PERMISSION_STATE.PERMISSION_DENIDE;
        }
    }

    private PERMISSION_STATE permissionCheckYesNO(String prm){
        boolean granted = ContextCompat.checkSelfPermission(this ,prm) == PackageManager.PERMISSION_GRANTED;
        if(granted){
            return PERMISSION_STATE.PERMISSION_GRANTED;
        }
        return PERMISSION_STATE.PERMISSION_DENIDE;

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


    private void initViews() {
        id_per_continue_BTN.setOnClickListener(v -> requestCoarse());
        id_per_continueSec_BTN.setOnClickListener(v -> requestFine());
    }


    private void requestCoarse(){
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        PERMISSION_STATE currState = permissionCheck(Manifest.permission.ACCESS_COARSE_LOCATION);
        switch (currState){
            case PERMISSION_GRANTED:
                id_per_continue_BTN.setVisibility(View.INVISIBLE);
                id_per_continueSec_BTN.setVisibility(View.VISIBLE);
                Toast.makeText(PermissionActivity.this, "Permission Granted - coarse location", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestFine(){
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        PERMISSION_STATE currState = permissionCheck(Manifest.permission.ACCESS_FINE_LOCATION);
        switch (currState){
            case PERMISSION_GRANTED:
                Toast.makeText(PermissionActivity.this, "Permission Granted - fine location", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, CyclingActivity.class));
                finish();
                }

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

            });
    private void permissionDenied() {
        Toast.makeText(PermissionActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
    }

}