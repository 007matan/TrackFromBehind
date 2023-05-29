package com.cohen.trackfrombehind;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity {


    private ExtendedFloatingActionButton start;
    private ExtendedFloatingActionButton stop;
    private TextView info;

    public static final String SP_KEY_SERVICE = "SP_KEY_SERVICE";

    private BroadcastReceiver myRadio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra(LocationService.BROADCAST_NEW_LOCATION_EXTRA_KEY);
            Loc loc = new Gson().fromJson(json, Loc.class);
            info.setText(loc.getLat() + "\n" + loc.getLon() + "\n" + loc.getSpeed());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        info = findViewById(R.id.info);
        start.setOnClickListener(v -> startService());
        stop.setOnClickListener(v -> stopService());

        MyReminder.startReminder(this);
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            if (getIntent().getAction().equals(LocationService.MAIN_ACTION)) {
                // came from notification
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startService() {
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

    }

    private void stopService() {
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

    }
}