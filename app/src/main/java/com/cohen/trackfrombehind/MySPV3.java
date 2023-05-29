package com.cohen.trackfrombehind;

import android.content.Context;
import android.content.SharedPreferences;

public class MySPV3 {

    private static final String DB_FILE = "DB_FILE";


    private static MySPV3 mySPV3 = null;
    private SharedPreferences preferences;

    public static MySPV3 getInstance() {
        return mySPV3;
    }

    public static void init(Context context) {
        if (mySPV3 == null) {
            mySPV3 = new MySPV3(context);
        }
    }

    private MySPV3(Context context) {

        preferences = context.getSharedPreferences(DB_FILE, Context.MODE_PRIVATE);
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
