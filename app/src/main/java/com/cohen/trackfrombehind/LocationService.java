package com.cohen.trackfrombehind;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {

    public static final String BROADCAST_NEW_LOCATION = "BROADCAST_NEW_LOCATION";
    public static final String BROADCAST_NEW_LOCATION_EXTRA_KEY = "BROADCAST_NEW_LOCATION_EXTRA_KEY";


    public static final String START_FOREGROUND_SERVICE = "START_FOREGROUND_SERVICE";
    public static final String STOP_FOREGROUND_SERVICE = "STOP_FOREGROUND_SERVICE";


    public static int NOTIFICATION_ID = 154;
    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID = "com.cohen.class23a_ands_4.CHANNEL_ID_FOREGROUND";
    public static String MAIN_ACTION = "com.guy.class23a_ands_4.locationservice.action.main";
    private NotificationCompat.Builder notificationBuilder;
    private boolean isServiceRunningRightNow = false;

    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    private FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            stopForeground(true);
            return START_NOT_STICKY;
        }


        Log.d("pttt", "onStartCommand A");
        if (intent.getAction().equals(START_FOREGROUND_SERVICE)) {
            if (isServiceRunningRightNow) {
                return START_STICKY;
            }
            Log.d("pttt", "onStartCommand B");


            isServiceRunningRightNow = true;
            notifyToUserForForegroundService();
            startRecording();

        } else if (intent.getAction().equals(STOP_FOREGROUND_SERVICE)) {
            stopRecording();
            stopForeground(true);
            stopSelf();

            isServiceRunningRightNow = false;
            return START_NOT_STICKY;
        }

        return START_STICKY;
    }



//    int counter = 0;
//    private MCT5.CycleTicker cycleTicker = new MCT5.CycleTicker() {
//        @Override
//        public void secondly(int repeatsRemaining) {
//            Log.d("pttt", Thread.currentThread().getName() + " - Hi " + System.currentTimeMillis());
//            counter += 100;
//
//            String content = new SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.US).format(System.currentTimeMillis());
//            updateNotification(counter + "m \n" + content);
//        }
//
//        @Override
//        public void done() {}
//    };

    private void startRecording() {
        // Keep CPU working
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PassiveApp:tag");
        wakeLock.acquire();


        //MCT5.get().cycle(cycleTicker, MCT5.CONTINUOUSLY_REPEATS, 5000);

         //Run GPS
        fusedLocationProviderClient = getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setSmallestDisplacement(0.5f);
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(500);
            //locationRequest.setMaxWaitTime(TimeUnit.MINUTES.toMillis(1));
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
        //runGPS();

    }

//    @SuppressLint("MissingPermission")
//    private void runGPS(){
//
//        boolean isLocationServiceOn = isLocationEnabled(this);
//        if(isLocationServiceOn)
//            Toast.makeText(LocationService.this, "location Services On", Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(LocationService.this, "location Services Off", Toast.LENGTH_SHORT).show();
//
//        fusedLocationProviderClient = getFusedLocationProviderClient(this);
//        if (permissionCheck(android.Manifest.permission.ACCESS_FINE_LOCATION) == 1 && permissionCheck(Manifest.permission.ACCESS_COARSE_LOCATION) == 1) {
//            LocationRequest locationRequest = LocationRequest.create();
//            locationRequest.setSmallestDisplacement(0.5f);
//            locationRequest.setInterval(1000);
//            locationRequest.setFastestInterval(500);
//            //locationRequest.setMaxWaitTime(TimeUnit.MINUTES.toMillis(1));
//            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
//        }
//    }
//    private int permissionCheck(String prm) {
//        //checking permission
//        boolean granted = ContextCompat.checkSelfPermission(this, prm) == PackageManager.PERMISSION_GRANTED;
//        if(granted){
//            return 1;
//        } else {
////            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, prm)){
////                Toast.makeText(getApplicationContext(), "Be aware, its the last time you have this option from here", Toast.LENGTH_LONG).show();
////                return 2;
////            }
//            return  3;
//        }
//    }
//    public static Boolean isLocationEnabled(Context context){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            // This is a new method provided in API 28
//            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//            return lm.isLocationEnabled();
//        } else {
//            // This was deprecated in API 28
//            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
//                    Settings.Secure.LOCATION_MODE_OFF);
//            return (mode != Settings.Secure.LOCATION_MODE_OFF);
//        }
//    }


    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult.getLastLocation() != null) {
                Log.d("pttt", ":getLastLocation");

                Intent intent = new Intent(BROADCAST_NEW_LOCATION);
                Loc loc = new Loc()
                        .setLat(locationResult.getLastLocation().getLatitude())
                        .setLon(locationResult.getLastLocation().getLongitude())
                        .setSpeed(locationResult.getLastLocation().getSpeed() / 3.6);

                String json = new Gson().toJson(loc);
                intent.putExtra(BROADCAST_NEW_LOCATION_EXTRA_KEY, json);
                LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(intent);
            } else {
                Log.d("pttt", "Location information isn't available.");
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    private void stopRecording() {
        // Release CPU Holding
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }

        //MCT5.get().remove(cycleTicker);

        // Stop GPS
        if (fusedLocationProviderClient != null) {
            Task<Void> task = fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            task.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("pttt", "stop Location Callback removed.");
                        stopSelf();
                    } else {
                        Log.d("pttt", "stop Failed to remove Location Callback.");
                    }
                }
            });
        }
    }

    private void updateNotification(String content) {
        notificationBuilder.setContentText(content);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }







    // // // // // // // // // // // // // // // // Notification  // // // // // // // // // // // // // // //

    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = getNotificationBuilder(this,
                CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        notificationBuilder
                .setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_cycling)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round))
                .setContentTitle("App in progress")
                .setContentText("Content")
        ;

        Notification notification = notificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);

        if (NOTIFICATION_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = NOTIFICATION_ID;
    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Cycling map channel";
        String description = notifications_channel_description;
        final NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    public static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runs = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
