package com.yannick.feld.lampe2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;



public final class SaveAndLoad {



    public static void SaveBoolean(Context context, String Key, Boolean toSave){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Key, toSave);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static boolean getBoolean(Context context, String Key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Key,true);

    }


}
