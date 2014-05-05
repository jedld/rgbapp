package com.rgb.matrix;

import android.content.Context;
import android.content.SharedPreferences;

import com.dayosoft.tiletron.app.SoundWrapper;
import com.facebook.RequestBatch;

import org.andengine.entity.sprite.Sprite;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.IFont;
import org.andengine.util.Constants;

import java.util.HashMap;

/**
 * Created by joseph on 5/1/14.
 */
public class Utils {

    static Utils instance;
    private final Context context;
    private final HashMap<String, Sprite> spriteAssets;
    private final HashMap<String, SoundWrapper> soundAssets;
    private final HashMap<String, Font> fontAssets;


    protected Utils(Context context, HashMap<String, Sprite> spriteAssets, HashMap<String,
            SoundWrapper> soundAssets, HashMap<String, Font> fontAssets) {
        this.context = context;
        this.spriteAssets = spriteAssets;
        this.soundAssets = soundAssets;
        this.fontAssets = fontAssets;
    }

    public static Utils getInstance(Context context, HashMap<String, Sprite> spriteAssets, HashMap<String,
            SoundWrapper> soundAssets, HashMap<String, Font> fontAssets) {
        if (instance == null) {
            instance = new Utils(context, spriteAssets, soundAssets, fontAssets);
        }
        return instance;
    }

    public static Utils getInstance() {
        return instance;
    }

    public Sprite getSprite(String name) {
        return spriteAssets.get(name);
    }

    public SoundWrapper getSound(String name) {
        return  soundAssets.get(name);
    }

    public IFont getFont(String name) {
        return fontAssets.get(name);
    }

    public static int getHighScore(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences("high_score", Context.MODE_PRIVATE);
        return sharedPrefs.getInt("high_score", 0);
    }

    public static boolean hasShownIntro(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("profile", Context.MODE_PRIVATE);
        return prefs.getBoolean("shown_intro", false);
//        return false;
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
