package com.cohen.trackfrombehind;

import android.content.Context;
import android.content.SharedPreferences;

public class MySP {

    private static final String SERVICE_FILE = "SERVICE_FILE"; //why i need this? consider i got that in the MainActivity public static final String SP_KEY_SERVICE = "SP_KEY_SERVICE"; ?

    private SharedPreferences preferences;

    public MySP(Context context) {
        preferences = context.getSharedPreferences(SERVICE_FILE, Context.MODE_PRIVATE);
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key, int def) {
        return preferences.getInt(key, def);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key, String def) {
        return preferences.getString(key, def);
    }
}
