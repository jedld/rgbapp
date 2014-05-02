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
}
