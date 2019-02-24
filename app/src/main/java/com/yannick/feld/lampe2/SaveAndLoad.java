package com.yannick.feld.lampe2;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.LinkedList;


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

    public static void SaveInt(Context context, String Key, int value){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Key, value);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static int getInt(Context context, String Key){
        return getInt(context, Key, 0);
    }

    public static int getInt(Context context, String Key, int defValue){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(Key,defValue);
    }

    public static void saveBitmap(Context context, int Key, Bitmap bitmap){

        try{
            FileOutputStream fout = context.openFileOutput(Integer.toString(Key) + ".png", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            fout.close();
        }catch(Exception e){
            e.printStackTrace();
            Log.e("ERROR", e.getMessage());
            Log.e("SAVING", "ERROR");
        }


    }
    public static Bitmap getBitmap(Context context, int key){
        try{
            File file = context.getFileStreamPath(Integer.toString(key) + ".png");
            Log.d("Path", file.getAbsolutePath());

            return BitmapFactory.decodeFile(file.getAbsolutePath());


        }catch(Exception e){
            e.printStackTrace();
            Log.e("LOADING", "ERROR");
            return null;
        }

    }


}
