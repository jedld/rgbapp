package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by joseph on 5/1/14.
 */
public class Utils {


    public static boolean hasShownIntro(Context context) {
//        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
//        return prefs.getBoolean("shown_intro", false);
        return false;
    }

    public static void introShown(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("shown_intro", true).commit();
    }

    public static boolean getMusicState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        return prefs.getBoolean("music", true);

    }

    public static boolean getSoundState(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        return prefs.getBoolean("sound", true);
    }

    public static void saveMusicState(Context context, boolean state) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("music", state).commit();
    }

    public static void saveSoundState(Context context, boolean state) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("sound", state).commit();
    }
}
