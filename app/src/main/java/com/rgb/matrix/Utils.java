package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.Constants;

import java.util.HashMap;

/**
 * Created by joseph on 5/1/14.
 */
public class Utils {

    static Utils instance;
    private final Context context;
    private final HashMap<String, Sprite> spriteAssets;

    protected Utils(Context context, HashMap<String, Sprite> spriteAssets) {
        this.context = context;
        this.spriteAssets = spriteAssets;
    }

    public static Utils getInstance(Context context, HashMap<String, Sprite> spriteAssets) {
        if (instance == null) {
            instance = new Utils(context, spriteAssets);
        }
        return instance;
    }

    public static Utils getInstance() {
        return instance;
    }

    public Sprite getSprite(String name) {
        return spriteAssets.get(name);
    }

    public static int getHighScore(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("high_score", Context.MODE_PRIVATE);
        return sharedPrefs.getInt("high_score", 0);
    }

    public static boolean hasShownIntro(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        return prefs.getBoolean("shown_intro", false);
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

    public static boolean withinTouchBounds(RectangleButton button, TouchEvent pSceneTouchEvent) {
        float[] shareButtonCoords = button.getParent().convertLocalToSceneCoordinates(button.getX(), button.getY());
        if (shareButtonCoords[Constants.VERTEX_INDEX_X] < pSceneTouchEvent.getX() &&
                shareButtonCoords[Constants.VERTEX_INDEX_Y] < pSceneTouchEvent.getY() &&
                pSceneTouchEvent.getX() < shareButtonCoords[Constants.VERTEX_INDEX_X] + button.getWidth() &&
                pSceneTouchEvent.getY() < shareButtonCoords[Constants.VERTEX_INDEX_Y] + button.getHeight()
                ) {
            return true;
        }
        return false;
    }
}
