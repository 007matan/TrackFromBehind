package com.cohen.trackfrombehind;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DeviceBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("pttt", "DeviceBootReceiver");

        //! check if Preferences not null?
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            MyReminder.startReminder(context);
        }
    }
}
